package com.ibm.opl.customdatasource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Test;

import com.ibm.opl.customdatasource.sql.NamedParametersPreparedStatement;

public class NamedPreparedStatementTest {
  /**
   * Test which main purpose is to test/debug the parseQuery() function.
   */
  @Test
  public void testParse() {
    String q = "SELECT * FROM A_TABLE WHERE VALUE0 = 'everything here is ignored :zzz and let''s escape' AND VALUE1 = :x and VALUE2 = :y AND :x != :zzz";
    // expected prepared statement:
    String xpq = "SELECT * FROM A_TABLE WHERE VALUE0 = 'everything here is ignored :zzz and let''s escape' AND VALUE1 = ? and VALUE2 = ? AND ? != ?";
    Map<String, int[]> index= new HashMap<String, int[]>();
    String pq = NamedParametersPreparedStatement.parseQuery(q, index);

    assertEquals("Not the expeced parsed query", pq, xpq);
    assertTrue("zzz name not found", index.get("zzz") != null);
    assertTrue("wrong index for zzz", index.get("zzz")[0] == 4);
    assertTrue("y name not found", index.get("y") != null);
    assertTrue("wrong index for y", index.get("y")[0] == 2);
    assertTrue("x name not found", index.get("x") != null);
    assertTrue("wrong indice for x", index.get("x")[0] == 1 && index.get("x")[1] == 3);
  }
  
  @Test
  public void testParseLimits() {
    Map<String, int[]> index= new HashMap<String, int[]>();
    String pq = null;
    pq = NamedParametersPreparedStatement.parseQuery(":", index);
    assertEquals(":", pq);
    pq = NamedParametersPreparedStatement.parseQuery(":a:1:", index);
    assertEquals("?:1:", pq);
    pq = NamedParametersPreparedStatement.parseQuery(null, index);
    assertEquals(null, pq);
  }
  
  @Test
  public void testExecute() {
    // test also case sensitivity
    Connection conn = null;
    Statement stmt = null;
    try {
      conn = DriverManager.getConnection("jdbc:sqlite::memory:");
      // create table
      DatabaseMetaData meta = conn.getMetaData();
      ResultSet rs = null;
      stmt = conn.createStatement();
      boolean table_exists = false;
      try {
        rs = meta.getTables(null, null, "ATABLE", null);
        table_exists = rs.next();
      } finally {
        if (rs != null)
          rs.close();
      }
      if (table_exists) {
        stmt.execute("DROP TABLE ATABLE");
      }
      stmt.execute("CREATE TABLE ATABLE (a String, b integer, c long, d double)");
      
      // create named prepared statement
      String q = "INSERT INTO ATABLE VALUES (:string, :integer, :long, :DOUBle)";
      NamedParametersPreparedStatement ps = null;
      try {
        ps = new NamedParametersPreparedStatement(conn, q);
        assertTrue(ps.hasNamedParameters());
        
        ps.setString("string", "ok");
        // out of order
        ps.setLong("long", Long.MAX_VALUE);
        ps.setInt("integer", 1234);
        ps.setDouble("double", 10.0);
        ps.execute();
      } finally {
        if (ps != null)   ps.close();   ps=null;
      }
      // query the result
      q = "SELECT * FROM ATABLE WHERE a == :value";
      ps = null;
      rs = null;
      try {
        ps = new NamedParametersPreparedStatement(conn, q);
        ps.setObject("value", "ok");
        rs = ps.executeQuery();
        assertTrue("Should have found 1 row", rs.next());
        assertEquals(Long.MAX_VALUE, rs.getLong(3));
        assertFalse("There are too many rows", rs.next());
      } finally {
        if (rs != null)   rs.close();   rs=null;
        if (ps != null)   ps.close();   ps=null;
      }
      // if statement is not a named prepared statement, we must know it
      q = "SELECT * FROM ATABLE WHERE a == ?";
      ps = null;
      try {
        ps = new NamedParametersPreparedStatement(conn, q);
        assertFalse(ps.hasNamedParameters());
      } finally {
        if (ps != null)   ps.close();   ps=null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      fail(e.getMessage());
    } finally {
      try {
        if (stmt != null)
          stmt.close();
        if (conn != null)
            conn.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
