package com.ibm.opl.customdatasource;

import java.io.File;
import java.io.IOException;

import ilog.concert.IloException;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplRunConfiguration;

public class ModRunner {
	/**
	 * Runs a model as a .mod file, using a set of .dat files, and a given jdbc
	 * configuration.
	 * 
	 * If connectionString is specified, it will be used instead of the url in the
	 * jdbc configuration, allowing for tests with database which url is not static
	 * (ex: temporary test databases).
	 * 
	 * @param modFilename           The .mod file
	 * @param datFilenames          An array of .dat files
	 * @param jdbcConfigurationFile The jdbc configuration file
	 * @param connectionString      An override url
	 * @throws IOException
	 * @throws IloException
	 */
	public final boolean run(String modFilename, String[] datFilenames, String jdbcConfigurationFile,
			String connectionString) throws IOException, IloException {
		// create OPL
		IloOplFactory.setDebugMode(true);
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);

		IloOplRunConfiguration rc = null;
		System.out.println("Running " + modFilename);
		if (datFilenames == null || datFilenames.length == 0) {
			rc = oplF.createOplRunConfiguration(modFilename);
		} else {
			rc = oplF.createOplRunConfiguration(modFilename, datFilenames);
			for (String d: datFilenames) {
				System.out.println("   with " + d);
			}
		}

		rc.setErrorHandler(errHandler);
		IloOplModel opl = rc.getOplModel();

		IloOplModelDefinition def = opl.getModelDefinition();

		//
		// Reads the JDBC configuration, initialize a JDBC custom data source
		// and sets the source in OPL.
		//
		JdbcConfiguration jdbcProperties = null;
		if (jdbcConfigurationFile != null) {
			jdbcProperties = new JdbcConfiguration();
			jdbcProperties.read(jdbcConfigurationFile);
			// we want to override connection string with conn string that has the actual
			// temp db path
			if (connectionString != null)
				jdbcProperties.setUrl(connectionString);
			// Create the custom JDBC data source
			IloOplDataSource jdbcDataSource = new JdbcCustomDataSource(jdbcProperties, oplF, def);
			// Pass it to the model.
			opl.addDataSource(jdbcDataSource);
		}

		opl.generate();

		boolean success = false;
		if (opl.hasCplex()) {
			if (opl.getCplex().solve()) {
				success = true;
			}
		} else {
			if (opl.getCP().solve()) {
				success = true;
			}
		}
		if (success == true) {
			opl.postProcess();
			// write results
			if (jdbcProperties != null) {
				JdbcWriter writer = new JdbcWriter(jdbcProperties, def, opl);
				writer.customWrite();
			}
		}
		return success;
	}
	
	/**
	 * Run the mode which .mod and .dat files are specified.
	 * @param modFile
	 * @param datFile
	 * @return
	 * @throws IloException 
	 * @throws IOException 
	 */
	public final boolean run(File modFile, File datFile) throws IOException, IloException {
		String[] datFiles = null;
		if (datFile != null) {
			datFiles = new String[1];
			datFiles[0] = datFile.getAbsolutePath();
		}
		return run(modFile.getAbsolutePath(), datFiles, null, null);
	}
}
