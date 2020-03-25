package com.ibm.opl.customdatasource;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;

import ilog.concert.IloException;
import ilog.opl.IloOplException;

/**
 * This test act as setup for some examples using UCanAccess.
 * Basically it will create a test oil database in ${project}/src/test/examples/UcanAccess
 * @author ViuLongKong
 *
 */
public class UCanAccessTest {
  public static String CREATE_OIL_MOD="ucanaccess/oil_create_db.mod";
  public static String CREATE_CONFIG_RESOURCE = "ucanaccess/oil_create_db.xml";

		
	@Test
	public void createUcanAccessDb() {
	    File tempdb = null;
	    try {
	      // create temp db
	      tempdb = File.createTempFile("testApiCall", ".db");
	      String projectRoot = TestUtils.GetProjectRoot().getAbsolutePath();
	      String target = projectRoot + "/src/test/examples/UcanAccess/oil.accdb";
	      target = target.replace("\\", "//");
	      String connectionString = "jdbc:ucanaccess://" + target + ";newdatabaseversion=v2010";
	      System.out.println("Writing test database to " + connectionString);

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
