// ----------------------------------------------------------------------------
// Sample material distributed under Apache 2.0 license.
//
// Copyright IBM Corporation 2019. All Rights Reserved.
// ----------------------------------------------------------------------------

//
// Before running this sample, please review and setup the following
//

// EDIT: you want to change this for your actual driver
var jdbc_driver = "mssql-jdbc-7.4.1.jre8.jar"
// EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
var jdbc_driver_path = ".;../../external_libs"


// EDIT: specfify where jdbc-custom-data-source is of the github project.
// default is to look in . and ../../lib
var libs = ".;../../lib";

//
// From this point, nothing is to be edited.
//

// import all drivers in jdbc_driver, separated by ;. This allows to be stored at different default locations.
var drivers = jdbc_driver_path.split(";");
for (var i=0; i < drivers.length; i++) {
	IloOplImportJava(drivers[i] + "/" + jdbc_driver)
}

// load the driver specified by environment variable if this exists.
var jdbc_driver_env = IloOplGetEnv("OPL_JDBC_DRIVER");
if ( jdbc_driver_env ) {
	IloOplImportJava(jdbc_driver_env);
}

// The jar containing the jdbc custom data source
var lib_locations = libs.split(";");
for (var i=0; i < lib_locations.length; i++) {
	IloOplImportJava(lib_locations[i] +  "/jdbc-custom-data-source.jar");
}

function JDBCConnector(url) {
	// Now create JdbcConfiguration
	this.db = IloOplCallJava("com.ibm.opl.customdatasource.JdbcConfiguration", "<init>", "");
	this.db.setUrl(url);
	// add custom data source
	IloOplCallJava("com.ibm.opl.customdatasource.JdbcCustomDataSource",
         "addDataSource", "", this.db, thisOplModel);
	this.read = __JDBCConnector_read;
	this.execute = __JDBCConnector_execute;
	this.update = __JDBCConnector_update;
	return this;
}

function __JDBCConnector_read(name, query) {
	this.db.addReadQuery(name, query);
}
function __JDBCConnector_execute(statement) {
	this.db.execute(statement);
}
function __JDBCConnector_update(name, statement) {
	this.db.addInsertStatement(name, statement);
}