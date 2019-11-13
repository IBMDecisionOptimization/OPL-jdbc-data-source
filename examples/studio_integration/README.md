# Example with integration in CPLEX Optimization Studio IDE


## Setup

Before you run the sample, please make sure you created an example database.
See the *Setup the sample database* section for [DB2](../../README.DB2.md),
[MySQL](../../README.MySQL.md), [Microsoft SQL Server](../../README.SQLServer.md).

Then, you will need to edit [jdbc.js](jdbc.js), and configure paths.
In particular, you want to add the JDBC connector jar. For convenience you can
also just set environment variables OPL_JDBC_DRIVER to point to your driver location
and OPL_JDC_LIBS to the directory containing `jdbc-custom-data-source.jar`

```
// OPL_JDBC_DRIVER points to the jar for the jdbc driver you want to use.
var jdbc_driver = IloOplGetEnv("OPL_JDBC_DRIVER");
if (! jdbc_driver ) {
	jdbc_driver = "../../external_libs/mssql-jdbc-7.2.2.jre8.jar";  // default for this project
}

// OPL_JDBC_LIBS points to the directory containing the library needed for this sample.
// You want to put jdbc-custom-data-source.jar there.
var libs = IloOplGetEnv("OPL_JDBC_LIBS");
if (! libs ) {
	libs = "../../lib";  // default value use the lib at the root of this project
}
```

## Data definition

The data input definition relies on support functions defined in [jdbc.js](jdbc.js).

You include that script using:

```
	includeScript("jdbc.js");
```

Then use the defined functions to create a connector and define inputs:

```
	// Create the jdbc custom data source
	var db = JDBCConnector("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=custom_data_source;integratedSecurity=true");
	
	// input data
	db.read("Gasolines", "SELECT NAME FROM GASDATA");
	db.read("Oils", "SELECT NAME FROM OILDATA");
	db.read("GasData", "SELECT * FROM GASDATA");
	db.read("OilData", "SELECT * FROM OILDATA");
	
	// create result table
	db.execute("DROP TABLE result");
	db.execute("CREATE TABLE result(oil VARCHAR(30), gas VARCHAR(30), blend FLOAT, a FLOAT)");
	
	// write results to database
	db.update("items", "INSERT INTO result(oil, gas, blend, a) VALUES (?,?,?,?)");
```


## Running the sample

Import the project in your studio. Create a new *Run Configuration*.
In the Projects view, drag and drop the `integration.mod` and `integration.dat` to the configuration.
You are now ready to run the sample in the studio.