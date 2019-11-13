# Run sample with MS SQL Server


## Setup the sample database

To run the sample with MS SQL Server, you need to install Microsoft SQL Server.
After it is installed, you are ready to setup the sample database.

To create a sample database, open a <em>Commnad Prompt</em> window, and Provided your
SQL Server instance name is SQLEXPRESS, create database using:

```
C:\>sqlcmd -S .\SQLEXPRESS -i data\oil_mssql.sql
```

Before you run the sample, you need to download the [Microsoft JDBC Driver for SQL Server](https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-2017).

Once your download finished, decompress the archive. The archive contains jar files that
look like `mssql-jdbc-7.2.2.jre8.jar`. 

In sample [studio_integration](examples/studio_integration), you will need to edit `jdbc.js` to point
to your jdbc driver, *or* add an `OPL_JDBC_DRIVER` environment variable pointing to it:

```
	// EDIT: you want to change this for your actual driver
	var jdbc_driver = "mssql-jdbc-7.4.1.jre8.jar"
	// EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
	var jdbc_driver_path = ".;../../external_libs"
```

Then you need to edit [demo.dat](examples/studio_integration/demo.dat) for your JDBC connection string and credentials.
Your connection string looks like `	jdbc:sqlserver://localhost;instanceName=<instance>;databaseName=<database_name>;integratedSecurity=true`

where `instance` is the mssql instance name (default is SQLEXPRESS), `<database_name>` is the name
of your database (default is `custom_data_source`).

## Run the oil sample


```
oplrun demo.mod demo.dat
```

