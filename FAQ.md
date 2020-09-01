FAQ
---

How to setup a custom datasource ?
----------------------------------

-  Step 1:

	JDBC drivers needs to be present in your classpath.
	There is two way to achieve this.

	   - Set environment variable ODMS_JAVA_ARGS='-cp /path/to/jar'

	Or

	   - copy your driver's jar file next to your model, then modify jdbc.js to include it.
	   
- Step 2:

	Use a `prepare` block in your `.dat` to configure the connector:
	```
	prepare {
	   includeScript("jdbc.js");
	
	   // Create the jdbc custom data source
	   var db = JDBCConnector("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=custom_data_source;integratedSecurity=true");
	
       // input data
  	   db.read("Gasolines", "SELECT NAME FROM GASDATA");
	   
	   ...
	}
	```
	The JDBC Connection string depends on your database. Please refer to your JDBC driver documentation from your database vendor.


My JDBC driver is in the classpath, but JDBC cannot find the driver
-------------------------------------------------------------------
On pre-JDBC 4.0 drivers, you need to explicitely specify the jdbc driver class.

Set environment variable ODMS_JAVA_ARGS='-cp /path/to/jar -Djdbc.drivers=com.your.driver.class'


How to use UCanAccess ?
----------------------
UCanAccess is an open source project for direct access to Microsoft Access databases (see http://ucanaccess.sourceforge.net/site.html).
In order to use UCanAccess:
   - The JDBC connection string is `jdbc:ucanaccess://path//to//your//file`
   - You need to add all jars in `UCanAccess/lib` directory to the `jdbc.js` driver path.
     For UCanAccess-5.0.0:
	 ```
     // EDIT: you want to change this for your actual driver.
     // If you have multiple drivers, list them separated by ;
     var jdbc_drivers = "commons-lang3-3.8.1jar;commons-logging-1.2.jar;hsqldb-2.5.0.jar;jackcess-3.0.1.jar;ucanaccess-5.0.0.jar"
     // EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
	 // Of course, replace UCanAccess-5.0.0-bin with your actual path to UCanAccess 5.0
     var jdbc_driver_path = ".;UCanAccess-5.0.0-bin;UCanAccess-5.0.0-bin/lib"
     ```
