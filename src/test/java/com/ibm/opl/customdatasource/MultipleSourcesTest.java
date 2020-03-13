package com.ibm.opl.customdatasource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ilog.concert.IloException;

public class MultipleSourcesTest {
	 public static String SRC1="multiple_sources_test/multiple_sources_1.csv";
	 public static String SRC2="multiple_sources_test/multiple_sources_2.csv";
	 public static String INPUT_DAT="multiple_sources_test/multiple_sources.dat";
	 public static String INPUT_MOD="multiple_sources_test/multiple_sources.mod";

	 /**
	  * Reads a csv file as a list of String[].
	  * 
	  * @param path The path
	  * @return the csv contents as a list of String[]
	  * @throws IOException
	  */
	 final List<String[]> simpleReadCsv(String path) throws IOException {
		 List<String[]> lines = new ArrayList<>();
		 BufferedReader csvReader = new BufferedReader(new FileReader(path));
		 String row;
		 while ((row = csvReader.readLine()) != null) {
		     String[] data = row.split(",");
		     lines.add(data);
		 }
		 csvReader.close();
		 return lines;
	 }
	 
	 final String urlize(String path) {
		 return path.replace('\\', '/');
	 }

	 /**
	  * Copy the file from <code>src</code> to <code>dst</code>.
	  * If dst is a directory, destination filename is <code>dst/filename</code> where filename is <code>src.getName()</code>
	  * replaceStrings is a map of string to be replaced. For instance, if replaceStrings contains { "foo": "bar" },
	  * strings matching "foo" in src are replaced by bar.
	  * @param src The source File
	  * @param dst The destination File
	  * @param replaceString a map of strings to be replaced.
	  * @return the destination File
	  * @throws IOException
	  */
	 final File copyFile(File src, File dst, Map<String,String> replaceStrings) throws IOException {
		 BufferedReader inputReader = null;
		 BufferedWriter output = null;
		 
		 if (dst.isDirectory()) {
			 dst = new File(dst.getAbsolutePath() + File.separator + src.getName());
		 }

		 try {
			 inputReader = new BufferedReader(new FileReader(src));
			 output = new BufferedWriter(new FileWriter(dst));
			 String line;
			 while ((line = inputReader.readLine()) != null) {
				 if (replaceStrings != null) {
					 for (String k: replaceStrings.keySet()) {
						 String v = replaceStrings.get(k);
						 line = line.replace(k, v);
					 }
				 }
				 output.write(line);
				 output.write('\n');
			 }
		 } finally {
			 if (inputReader != null)
				 inputReader.close();
			 if (output != null)
				 output.close();
		 }
		 return dst;
	 }
	 
	 final void check_that(File directory, String dbname, String query, List<Integer> expected) throws SQLException {
	     File dbFile = new File(directory.getAbsolutePath() + File.separator + dbname + ".db");
	     String url = "jdbc:sqlite:" + urlize(dbFile.getAbsolutePath());
	     Connection conn = null;
	     Statement stmt = null;
	     try {
	    	 conn = DriverManager.getConnection(url);
	    	 stmt = conn.createStatement();
	    	 
	    	 ResultSet rs = stmt.executeQuery(query);
	    	 List<Integer> result = new ArrayList<>();
	    	 while (rs.next()) {
	    		 result.add(rs.getInt(1));
	    	 }
	    	 assertEquals(result.size(), expected.size());
	    	 for (Integer i : expected) {
	    		 assertTrue(result.contains(i));
	    	 }
	     } finally {
	    	 if (stmt != null)
	    		 stmt.close();
	    	 if (conn != null)
	    		 conn.close();
	     }
	 }
	 
	 /**
	  * Creates a SQLite database which name is dbname.
	  * The database is saved in the specified <code>directory</code>.
	  * The source for the database is a csv file pointed by <code>source</code>.
	  * That data is saved in the database as a table named tableName
	  * @param directory
	  * @param dbname
	  * @param tableName
	  * @param source
	  * @return The .db file containing the database
	  * @throws IOException
	  * @throws SQLException
	  */
	 final File createDB(File directory, String dbname, String tableName, String source) throws IOException, SQLException {
	     String inputFile = new File(getClass().getResource(source).getFile()).getAbsolutePath();
	     System.err.println("Reading " + inputFile);
	     List<String[]> contents = this.simpleReadCsv(inputFile);
	     String[] columns = contents.get(0);
	     for (String[] r: contents.subList(1, contents.size())) {
	    	 for (String t: r) {
	    		 System.err.print(t + ",");
	    	 }
	    	 System.err.println();
	     }
	     
	     Connection conn = null;
	     Statement stmt = null;
	     PreparedStatement pstmt = null;
	     File resultFile = new File(directory.getAbsolutePath() + File.separator + dbname + ".db");

	     String url = "jdbc:sqlite:" + urlize(resultFile.getAbsolutePath());
	     try {
	    	 conn = DriverManager.getConnection(url);
	    	 stmt = conn.createStatement();
	    	 // create table
	    	 StringBuffer qb = new StringBuffer("CREATE TABLE " + tableName + "(");
	    	 for (int i=0; i < columns.length; i++) {
	    		 qb.append(columns[i]);
	    		 qb.append(" integer");
	    		 if (i != columns.length-1) {
	    			 qb.append(", ");
	    		 }
	    	 }
	    	 qb.append(")");
	    	 String q = qb.toString();
	    	 System.out.println("Executing " + q + " on  " + dbname);
	    	 stmt.execute(q);
	    	 
	    	 // prepare insert statement
	    	 qb = new StringBuffer("INSERT INTO " + tableName + "(");
	    	 qb.append(String.join(",", columns));
	    	 String[] place_holder = new String[columns.length];
	    	 for (int i=0; i < place_holder.length; i++) {
	    		 place_holder[i] = "?";
	    	 }
	    	 qb.append(") VALUES (");
	    	 qb.append(String.join(",", place_holder));
	    	 qb.append(")");
	    	 q = qb.toString();
	    	 System.out.println("Insert statement: " + q);
	    	 pstmt = conn.prepareStatement(q);
	    	 // update
	    	 for (String[] r: contents.subList(1, contents.size())) {
		    	 for (int i=0; i < r.length; i++) {
		    		 pstmt.setInt(i+1, Integer.parseInt(r[i]));
		    	 }
		    	 pstmt.executeUpdate();
		     }
	     } finally {
	    	 if (stmt != null) {
	    		 stmt.close();
	    	 }
    		 if (conn != null) {
    			 conn.close();
    		 }
	     }
	     return resultFile;
	 }
	 
	/**
	 * Test that we can use a .dat file with different datasources
	 * @throws IOException 
	 * @throws IloException 
	 */
	@Test
	public void testMultipleSources() throws IOException, SQLException, IloException {
		// get tmp working directory
		final File temp;
		temp = File.createTempFile("doopl_test", null);
		try {
			if (!temp.delete()) {
				throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
			}
			if (!temp.mkdir()) {
				throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
			}
			System.err.println("Using temp directory: " + temp.getAbsolutePath());
			
			// create db1
			File db1_file = createDB(temp, "db1", "table1", SRC1);
			// create db2
			File db2_file = createDB(temp, "db2", "table2", SRC2);
			// substitution
			HashMap<String, String> replacement = new HashMap<>();
			replacement.put("{db1_path}", urlize(db1_file.getAbsolutePath()));
			replacement.put("{db2_path}", urlize(db2_file.getAbsolutePath()));

			// copy dat and mod
		    File inputDat = new File(getClass().getResource(INPUT_DAT).getFile());
		    File finalDat = this.copyFile(inputDat, temp, replacement);
		    System.err.println("RES = " + getClass().getResource(INPUT_MOD));
		    File inputMod = new File(getClass().getResource(INPUT_MOD).getFile());
		    File finalMod = this.copyFile(inputMod, temp, null);
		    // copy jdbc.js
		    String jdbcJs = Thread.currentThread().getContextClassLoader().getResource("studio_integration/jdbc.js").getFile();
		    copyFile(new File(jdbcJs), temp, null);
			// run mod
		    ModRunner runner = new ModRunner();
		    runner.run(finalMod, finalDat);
		    
		    // now test that tables contains some values
		    check_that(temp, "db1", "SELECT * from result1", Arrays.asList(6, 15));
		    check_that(temp, "db2", "SELECT * from result2", Arrays.asList(6000, 120000));
		} finally {
			if (temp.isDirectory()) {
				FileUtils.deleteDirectory(temp);
			}
		}
	}
}
