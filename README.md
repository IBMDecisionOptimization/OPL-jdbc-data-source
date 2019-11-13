# JDBC Data source for IBM OPL

This repository provides an OPL extension that provides database connectivity using a JDBC custom datasource.
This extension allows read and write of tuplesets from/to a database.

The extension is based on a JDBC custom datasource. For more information and a java sample,
you can refer to [OPL-jdbc-custom-data-source](https://github.com/IBMDecisionOptimization/OPL-jdbc-custom-data-source/).


This example will work with OPL versions greater than 12.6, even if it is configured to run with 12.9.0 version.


## Table of Contents
   - [Prerequisites](#prerequisites)
   - [Build and run the sample](#build-and-run-the-sample)
      - [Run the sample from OPL](#run-the-sample-from-opl)
   - [License](#license)   
   
### Prerequisites

1. This sample assumes that IBM ILOG CPLEX Optimization Studio 12.9.0 is
   installed and configured in your environment.

2. Install [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).  
   Once installed, you can check that it is accessible using this command:

	```
	java -version
	```

3. The sample assumes you have a database with JDBC drivers installed. This
   sample specifically provides instructions for IBM DB2 Express-C and
   MySQL Comunity Server, but is compatible with minimal changes with other JDBC
   compatible databases.

## Build and run the sample

Before you run, you need to populate the database. See details in subsections:

- [Run sample with DB2](README.DB2.md)
- [Run sample with MySQL](README.MySQL.md)
- [Run sample with MS SQL Server](README.SQLServer.md)


### Run the sample from OPL

The [examples/studio_integration](examples/studio_integration) sample shows how to
use the jdbc custom data source as a library, without having the need to
invoke OPL runtime from java. You can use this method to access database
using a jdbc-custom-data-source from `oplrun` or OPL Studio.


## Limitations

* The custom data source reader supports scalar values, sets and tuplessets. Arrays are not supported.
* Inner tuples are not supported.

## License

This sample is delivered under the Apache License Version 2.0, January 2004 (see LICENSE.txt).
