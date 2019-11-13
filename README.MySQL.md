# Run sample with MySQL

## Setup the sample database
To run the sample with MySQL, you need to install MySQL. MySQL Community Server is a free edition of MySQL.

On Microsoft Windows, you can download and install it from [here](https://dev.mysql.com/downloads/mysql/).

On other plateforms, MySQL Community Server is available with most package
managers. Please refer to the [installation instructions](https://dev.mysql.com/doc/refman/5.7/en/installing.html).

You can check your MySQL installation by running <code>mysqladmin</code>.
This binary would be available in /usr/bin on linux and <msysql install dir>/bin
on Windows.
	  
```
[root@host]# mysqladmin --version
```

Before you run the sample, you need to run the script to create and populate
sample tables.

Edit `data\oil_mysql.sql` for your database name. The default for the script is
to create a new database. If you are not an administrator or if you don't
have the permissions to create database, edit the first lines to use your
database.

Run the script with:

```
$ mysql < data\oil_mysql.sql
```

You also need to download the [JDBC driver for MySQL: MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)


In sample [studio_integration](examples/studio_integration), you will need to edit `jdbc.js` to point
to your jdbc driver, *or* add an `OPL_JDBC_DRIVER` environment variable pointing to it:

```
	// EDIT: you want to change this for your actual driver
	var jdbc_driver = "mssql-jdbc-7.4.1.jre8.jar"
	// EDIT: specify where to look for the jdbc driver. Default is in . (besides this .js script) and in ../../external_libs
	var jdbc_driver_path = ".;../../external_libs"
```


Then you need to edit [demo.dat](example/studio_integration/demo.dat) for your JDBC connection string and credentials.
Your connection string looks like `jdbc:mysql://localhost:3306/<database_name>?useSSL=false`
where `<database_name>` is the name of your database (default is `custom_data_source`).

## Run the sample

```
oplrun demo.mod demo.dat
```