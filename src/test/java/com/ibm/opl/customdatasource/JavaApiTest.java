package com.ibm.opl.customdatasource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.*;
import org.junit.Test;

import ilog.concert.IloException;
import ilog.opl.IloOplException;

public class JavaApiTest {
  public static String OIL_DAT="models/oil.dat";
  public static String OIL_MOD="models/oil.mod";
  public static String CONFIG_RESOURCE = "models/oil_sqlite.xml";
  public static String CONFIG_RESOURCE_NAME_MAPPING_OK = "models/oil_sqlite_name_mapping_ok_select.xml";
  public static String CONFIG_RESOURCE_NAME_MAPPING_WRONG = "models/oil_sqlite_name_mapping_wrong_select.xml";
  public static String CONFIG_RESOURCE_NO_NAME_MAPPING = "models/oil_sqlite_no_name_mapping.xml";
  
  public static String CREATE_OIL_MOD="models/oil_create_db.mod";
  public static String CREATE_CONFIG_RESOURCE = "models/oil_create_db.xml";


  /**
   * Tests the JdbcCustomReader/Writer by API.
   */
  @Test
  public final void testApiCall() {
    File tempdb = null;
    try {
      // create temp db
      tempdb = File.createTempFile("testApiCall", ".db");
      String connectionString = "jdbc:sqlite:" + tempdb.getAbsolutePath();
      Connection conn = null;
      try {
        // creates the db
        conn = DriverManager.getConnection(connectionString);
      } finally {
        if (conn != null)
          conn.close();
      }
      
      // use a .mod to create tmp database
      // The tmp database is created with wrong column names
      // (name, demand, price) are replaced by (nom, demande, prix)
      String createModFilename = new File(getClass().getResource(CREATE_OIL_MOD).getFile()).getAbsolutePath();
      String createJdbcConfigurationFile = new File(getClass().getResource(CREATE_CONFIG_RESOURCE).getFile()).getAbsolutePath();
      TestUtils.runMod(createModFilename, null, createJdbcConfigurationFile, connectionString);
      
      // now solve the oil model => we want an error here
      // in that particular test,that should be a runtime exception
      try {
        String modFilename = new File(getClass().getResource(OIL_MOD).getFile()).getAbsolutePath();
        String[] datFilenames = {new File(getClass().getResource(OIL_DAT).getFile()).getAbsolutePath()};
        String jdbcConfigurationFile = new File(getClass().getResource(CONFIG_RESOURCE_NAME_MAPPING_WRONG).getFile()).getAbsolutePath();
        TestUtils.runMod(modFilename, datFilenames, jdbcConfigurationFile, connectionString);
        fail("We should have encountered a SQLException: no such column");
      } catch (RuntimeException re) {
        Throwable cause = re.getCause();
        assertTrue(cause instanceof SQLException);
        assertTrue(cause.getMessage().contains("no such column"));
      }
      
      // resolve, using the right name mapping
      {
        String modFilename = new File(getClass().getResource(OIL_MOD).getFile()).getAbsolutePath();
        String[] datFilenames = {new File(getClass().getResource(OIL_DAT).getFile()).getAbsolutePath()};
        String jdbcConfigurationFile = new File(getClass().getResource(CONFIG_RESOURCE_NAME_MAPPING_OK).getFile()).getAbsolutePath();
        TestUtils.runMod(modFilename, datFilenames, jdbcConfigurationFile, connectionString);
      }
      // resolve, without name mapping
      {
        String modFilename = new File(getClass().getResource(OIL_MOD).getFile()).getAbsolutePath();
        String[] datFilenames = {new File(getClass().getResource(OIL_DAT).getFile()).getAbsolutePath()};
        String jdbcConfigurationFile = new File(getClass().getResource(CONFIG_RESOURCE_NO_NAME_MAPPING).getFile()).getAbsolutePath();
        TestUtils.runMod(modFilename, datFilenames, jdbcConfigurationFile, connectionString);
      }
    } catch (IloOplException ex) {
      ex.printStackTrace();
      fail("### OPL exception: " + ex.getMessage());
    } catch (IloException ex) {
      ex.printStackTrace();
      fail("### CONCERT exception: " + ex.getMessage());
    } catch (Exception ex) {
      ex.printStackTrace();
      fail("### UNEXPECTED UNKNOWN ERROR ...");
    } finally {
      if (tempdb != null) {
        tempdb.delete();
      }
    }
  }
}
