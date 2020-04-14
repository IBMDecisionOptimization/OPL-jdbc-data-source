// ----------------------------------------------------------------------------
// Sample material distributed under Apache 2.0 license.
//
// Copyright IBM Corporation 2019. All Rights Reserved.
// ----------------------------------------------------------------------------

//
// Before running this sample, please review and setup the following
//

// EDIT: you want to change this for your actual driver.
// If you have multiple drivers, list them separated by ;
var jdbc_drivers = "mssql-jdbc-7.4.1.jre8.jar"
// EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
var jdbc_driver_path = ".;../../external_libs"


// EDIT: specfify where jdbc-custom-data-source is of the github project.
// default is to look in . and ../../lib
var libs = ".;../../lib";

//
// From this point, nothing is to be edited.
//
var drivers_path = jdbc_driver_path.split(";");
for (var i=0; i < drivers_path.length; i++) {
	var drivers;
	// split jdbc_drivers if it contains ';'
	if (jdbc_drivers.indexOf(";") != -1) {
		drivers = jdbc_drivers.split(";");
	} else {
		drivers = new Array(jdbc_drivers);
	}
	// load each driver that actually exist on disk
	for (var j=0; j < drivers.length; j++) {
		var f = new IloOplFile(drivers_path[i] + "/" + drivers[j]);	
		if (f.exists && !f.isDirectory) {
			IloOplImportJava(drivers_path[i] + "/" + drivers[j])
		}
	}

}


// import all drivers in jdbc_driver, separated by ;. This allows to be stored at different default locations.
var drivers_path = jdbc_driver_path.split(";");
for (var i=0; i < drivers_path.length; i++) {
	var drivers = jdbc_drivers.split(";");
	for (var j=0; j < drivers.length; j++) {
		var f = new IloOplFile(drivers_path[i] + "/" + drivers[j]);
		writeln(f);
		if (f.exists) {
			writeln("LOAD " + f);
			IloOplImportJava(drivers_path[i] + "/" + drivers[j])
		} 
	}
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