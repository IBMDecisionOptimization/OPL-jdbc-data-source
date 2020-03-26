@echo off

REM This is an example using ODMS_JAVA_ARGS to set up classpath and jdbc.drivers

set UCANPATH=%cd%\ucanaccess-5.0.0.jar
set UCANPATH=%UCANPATH%;%cd%\commons-lang3-3.8.1.jar
set UCANPATH=%UCANPATH%;%cd%\commons-logging-1.2.jar
set UCANPATH=%UCANPATH%;%cd%\hsqldb-2.5.0.jar
set UCANPATH=%UCANPATH%;%cd%\jackcess-3.0.1.jar

@echo on

set ODMS_JAVA_ARGS='-cp %UCANPATH% -Djdbc.drivers=net.ucanaccess.jdbc.UcanaccessDriver'

oplrun demo.mod demo.dat
