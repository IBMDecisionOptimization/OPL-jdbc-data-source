# Maximizing the profit of an oil company

## Describe the business problem

* An oil company manufactures different types of gasoline and diesel. Each type of gasoline is produced by blending different types of crude oils that must be purchased. The company must decide how much crude oil to buy in order to maximize its profit while respecting processing capacities and quality levels as well as satisfying customer demand.
* Blending problems are a typical industry application of Linear Programming (LP). LP represents real life problems mathematically using an objective function to represent the goal that is to be minimized or maximized, together with a set of linear constraints which define the conditions to be satisfied and the limitations of the real life problem. The function and constraints are expressed in terms of decision variables and the solution, obtained from optimization engines such as IBM ILOG CPLEX, provides the best values for these variables so that the objective function is optimized.
* The oil-blending problem consists of calculating different blends of gasoline according to specific quality criteria.
* Three types of gasoline are manufactured: super, regular, and diesel.
* Each type of gasoline is produced by blending three types of crude oil: crude1, crude2, and crude3.
* The gasoline must satisfy some quality criteria with respect to their lead content and their octane ratings, thus constraining the possible blendings.
* The company must also satisfy its customer demand, which is 3,000 barrels a day of super, 2,000 of regular, and 1,000 of diesel.
* The company can purchase 5,000 barrels of each type of crude oil per day and can process at most 14,000 barrels a day.
* In addition, the company has the option of advertising a gasoline, in which case the demand for this type of gasoline increases by ten barrels for every dollar spent.
* Finally, it costs four dollars to transform a barrel of oil into a barrel of gasoline.

## Files

`oil.mod` contains the OPL Model.

`oil.dat` contains part of the input data. Mostly parameters for the problem, like max production and production cost. The missing input data sets (OilData and GasData are read from the database at run time using the sql queries defined in the database connection xml configuration file)


## Build the sample

The source of the oil sample is part of the root project.

Before you build the sample, you must edit [build.properties](../../build.properties) for the appropriate path locations:

* Edit the appropriate `*.jdbc.connector.path` point to your JDBC driver location.
* `opl.home` should point to your OPL home, unless you have a `CPLEX_STUDIO_DIR128` set. (this variable should exists if you installed on a Windows machine).

The build file, [build.xml](../../build.xml), imports the build file from the OPL samples,
in `<opl home>/examples/opl_interfaces/java/build_common.xml`.
This build file defines all variables that are needed to configure the execution.

The example is compiled using the `compile` Ant target:
```
ant compile
```
The example is automatically compiled with the run Ant targets is invoked.