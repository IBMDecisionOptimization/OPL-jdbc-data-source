# Run sample with DB2


## Setup the sample database

To run the sample with DB2. you need to install DB2. DB2 Express-C is a free
community edition of DB2. DB2 Express-C is available on Microsoft Windows,
Linux and Mac OS.

You can download and install DB2 Express-C from [here](https://www.ibm.com/developerworks/downloads/im/db2express/).


In a <em>DB2 Command Window</em>:

Create database using `db2 create database CUSTOMDB`

Run the following SQL script to create and populate the example database:
```
db2 -tvmf data/oil_db2.sql
```

You can download the DB2 jdbc driver [here](http://www-01.ibm.com/support/docview.wss?uid=swg21363866).
Note that if you installed DB2 Express-C, your JDBC driver is `db2jcc4.jar`
in `<DB2 installdir>/SQLLIB/java`.

In sample [studio_integration](examples/studio_integration), you will need to edit `jdbc.js` to point
to your jdbc driver, *or* add an `OPL_JDBC_DRIVER` environment variable pointing to it:

```
	// EDIT: you want to change this for your actual driver
	var jdbc_driver = "mssql-jdbc-7.4.1.jre8.jar"
	// EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
	var jdbc_driver_path = ".;../../external_libs"
```

Then you need to edit [demo.dat](example/studio_integration/demo.dat) for your JDBC connection string and credentials.
Your connection string looks like `db2://localhost:<port>/<database_name>`
where `port` is the DB2 port (default is 50000), `<database_name>` is the name
of your database (default is `CUSTOMDB`).

## Run the sample

```
oplrun demo.mod demo.dat
```