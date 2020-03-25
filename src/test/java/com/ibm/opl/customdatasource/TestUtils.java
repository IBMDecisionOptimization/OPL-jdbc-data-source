package com.ibm.opl.customdatasource;

import java.io.File;
import java.io.IOException;

import ilog.concert.IloException;

public class TestUtils {
  /**
   * Runs a model as a .mod file, using a set of .dat files, and a given jdbc configuration.
   * 
   * If connectionString is specified, it will be used instead of the url in the jdbc configuration,
   * allowing for tests with database which url is not static (ex: temporary test databases).
   * @param modFilename The .mod file
   * @param datFilenames An array of .dat files
   * @param jdbcConfigurationFile The jdbc configuration file
   * @param connectionString An override url
   * @throws IOException
   * @throws IloException
   */
	public final static void runMod(String modFilename, String[] datFilenames, String jdbcConfigurationFile,
			String connectionString) throws IOException, IloException {
		ModRunner runner = new ModRunner();
		runner.run(modFilename, datFilenames, jdbcConfigurationFile, connectionString);
	}
	
	/**
	 * Returns the project root. Only works if the tests are run with maven.
	 * @return
	 */
	public static File GetProjectRoot() {
		String f = new File(TestUtils.class.getResource("models").getFile()).getAbsolutePath();
		// The standard run time for us is to have test classes in ${project}\target\test-classes
		String p = f.replace("\\", "/");  // normalize
		int index = p.indexOf("target/test-classes");
		if (index != -1) {
			String loc = p.substring(0, index);
			return new File(loc);
		}
		else
			return null;
	}
}
