package com.ibm.opl.customdatasource;

import ilog.opl.IloCustomOplDataSource;
import ilog.opl.IloCustomOplPostProcessListener;
import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplElementDefinition;
import ilog.opl.IloOplElementDefinitionType.Type;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplTupleSchemaDefinition;
import ilog.opl_core.cppimpl.IloTupleSchema;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;



/**
 * An custom data source reading data using JDBC.
 *
 */
public class JdbcCustomDataSource extends IloCustomOplDataSource {
    private JdbcConfiguration _configuration;
    private IloOplModelDefinition _def;
    
    /**
     * A post process listener to write output
     */
    private static class JdbcCustomDataSourcePublisher extends IloCustomOplPostProcessListener {
      IloOplModel _model;
      JdbcConfiguration _config;
      
      
      JdbcCustomDataSourcePublisher(IloOplModel model, JdbcConfiguration config) {
        super(IloOplFactory.getOplFactoryFrom(model));
        _model = model;
        _config = config;
      }

      @Override
      public void customStartPostProcess() {
      }
      
      @Override
      public void customEndPostProcess() {
        JdbcWriter.writeOutput(_config, _model);
      }
    }
    
    /**
     * Adds a custom data source to a model.
     *
     * @param xmlFile The xml configuration for the data source
     * @param model The OPL Model
     * @return the custom datasource
     */
    public static JdbcCustomDataSource addDataSourceXMLConfig(String xmlFile, IloOplModel model) throws IOException {
        JdbcConfiguration config = new JdbcConfiguration();
        config.read(xmlFile);
        return addDataSource(config, model);
    }
    
    /**
     * Adds a custom data source to a model.
     *
     * @param config The JDBC configuration object
     * @param model The OPL Model
     * @return the custom datasource
     */
    public static JdbcCustomDataSource addDataSource(JdbcConfiguration config, IloOplModel model) {
        IloOplFactory factory = IloOplFactory.getOplFactoryFrom(model);
        IloOplModelDefinition definition = model.getModelDefinition();
        JdbcCustomDataSource source = new JdbcCustomDataSource(config, factory, definition);
        model.addDataSource(source);
        model.registerPostProcessListener(new JdbcCustomDataSourcePublisher(model, config));
        return source;
    }
    
    /**
     * Creates a new JDBC custom data source, based on the specified configuration.
     * 
     * @param configuration The JDBC data source configuration.
     * @param oplF The OPL factory.
     * @param def The OPL Model definition.
     */
    public JdbcCustomDataSource(JdbcConfiguration configuration, IloOplFactory oplF, IloOplModelDefinition def) {
        super(oplF);
        _configuration = configuration;
        _def = def;
    }

    void fillNamesAndTypes(IloTupleSchema schema, String[] names, Type[] types) {
        IloOplElementDefinition elementDefinition = _def.getElementDefinition(schema.getName());
        IloOplTupleSchemaDefinition tupleSchema = elementDefinition.asTupleSchema();
        for (int i = 0; i < schema.getSize(); i++) {
            String columnName = schema.getColumnName(i);
            types[i] = tupleSchema.getComponent(i).getElementDefinitionType();
            names[i] = columnName;
        }
    }
    
    /**
     * Overrides the IloCustomOplDataSource method to read data when the model
     * is generated.
     */
    @Override
    public void customRead() {
        long startTime = System.currentTimeMillis();
        try {
            System.out.println("Reading elements from database");
            Properties prop = _configuration.getReadQueries();
            Enumeration<?> propertyNames = prop.propertyNames();
            while (propertyNames.hasMoreElements()) {
               String name = (String) propertyNames.nextElement();
               String query = prop.getProperty(name);
               System.out.println("Reading " + name + " using \"" + query + "\"");
               customRead(name, query);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Done (" + (endTime - startTime)/1000.0 + " s)");
        }
        catch (SQLException e) {
            // Since the superclass's method signature does not allow us to
            // throw an exception from here, we have to wrap the exception.
            long endTime = System.currentTimeMillis();
            System.err.println(e.getMessage() + " (after " + (endTime - startTime)/1000.0 + " s)");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void customRead(String name, String query) throws SQLException {
        IloOplElementDefinition def = _def.getElementDefinition(name);
        Type type = def.getElementDefinitionType();
        Type leaf = def.getLeaf().getElementDefinitionType();

        if (type == Type.SET) {
            if (leaf == Type.TUPLE) {
                readTupleSet(name, query);
            } else {
                readSet(leaf, name, query);
            }
        }
        else if ( type == Type.INTEGER || type == Type.FLOAT || type == Type.STRING ) {
           readValue(name, query);
        }
        else
           throw new IllegalArgumentException("Cannot read element " + name + " of type " + type);
    }

    /** Helper class to execute queries in an exception safe way.
     * Use the class via the following template:
     * <pre>
     final RunQuery q = new RunQuery("SELECT * FROM table;");
     try {
        ResultSet rs = q.getResult();
        ...
     }
     finally {
        q.close();
     }
     </pre>
     * This will correctly clean up and release all resources no matter whether
     * an exception is throw or not.
     */
    private final class RunQuery {
       private Connection conn = null;
       private Statement stmt = null;
       private ResultSet rs = null;
       public RunQuery(String query) throws SQLException {
          Connection conn = DriverManager.getConnection(_configuration.getUrl(),
                                                        _configuration.getUser(),
                                                        _configuration.getPassword());
          Statement stmt = null;
          ResultSet rs = null;
          try {
             stmt = conn.createStatement();
             rs = stmt.executeQuery(query);
             // Everything worked without problem. Transfer ownership of
             // the objects to the newly constructed instance.
             this.conn = conn; conn = null;
             this.stmt = stmt; stmt = null;
             this.rs = rs; rs = null;
          }
          finally {
             if ( rs != null )  rs.close();
             if ( stmt != null )  stmt.close();
             if ( conn != null )  conn.close();
          }
       }
       public void close() throws SQLException {
          rs.close();
          stmt.close();
          conn.close();
       }
       ResultSet getResult() { return rs; }
    }
    
    /** Helper class to execute statements in an exception safe way.
     * Use the class via the following template:
     * <pre>
     final ExecuteStatement q = new ExecuteStatement("CREATE TABLE t (x INT, y STRING);");
     try {
        boolean result = q.getResult();
        ...
     }
     finally {
        q.close();
     }
     </pre>
     * If getResult() is true, the ResultSet can be retrieved using getResultSet().
     *
     * This will correctly clean up and release all resources no matter whether
     * an exception is throw or not.
     */
    private final class ExecuteStatement {
       private Connection conn = null;
       private Statement stmt = null;
       private boolean result = false;
       private ResultSet rs = null;
       public ExecuteStatement(String query) throws SQLException {
          Connection conn = DriverManager.getConnection(_configuration.getUrl(),
                                                        _configuration.getUser(),
                                                        _configuration.getPassword());
          Statement stmt = null;
          ResultSet rs = null;
          try {
             stmt = conn.createStatement();
             result = stmt.execute(query);
             if (result)
               rs = stmt.getResultSet();
             // Everything worked without problem. Transfer ownership of
             // the objects to the newly constructed instance.
             this.conn = conn; conn = null;
             this.stmt = stmt; stmt = null;
             this.rs = rs; rs = null;
          }
          finally {
             if ( rs != null )  rs.close();
             if ( stmt != null )  stmt.close();
             if ( conn != null )  conn.close();
          }
       }
       public void close() throws SQLException {
          rs.close();
          stmt.close();
          conn.close();
       }
       boolean getResult() { return result; }
       ResultSet getResultSet() { return rs; }
    }
    
    
    /** Read the scalar value for <code>name</code> from <code>query</code>.
     * <b>Note:</b> the function will just use the first value produced by
     *              <code>query</code> and assign that to the element identified
     *              by <code>name</code>. If the query produces more than one
     *              value the surplus values are ignored.
     * @param name The name of the element to fill.
     * @param query The SQL query that produces the data for <code>name</code>.
     * @throws SQLException if querying the database fails or the query does
     *                      not produce at least one value.
     */
    public void readValue(String name, String query) throws SQLException {
        IloOplElementDefinition def = _def.getElementDefinition(name);
        IloOplDataHandler handler = getDataHandler();
        final RunQuery q = new RunQuery(query);
        try {
           ResultSet rs = q.getResult();
           rs.next();
           handler.startElement(name);
           Type type = def.getElementDefinitionType();
           if (type == Type.INTEGER) {
               handler.addIntItem(rs.getInt(1));
           }
           else if (type == Type.FLOAT) {
               handler.addNumItem(rs.getDouble(1));
           }
           else if (type == Type.STRING) {
               handler.addStringItem(rs.getString(1));
           }
           else
              throw new IllegalArgumentException("Cannot load element " + name + " of type " + type);
           handler.endElement();
        }
        finally {
            // We don't use try-with-resources so that we can compile
            // with pre-1.8 compilers as well.
            q.close();
        }
    }

    /**
     * Read the set for <code>name</code> from <code>query</code>.
     * <b>Note:</b> the function will just use the first value produced by each row of
     *              <code>query</code> and add that to the set.
     * @param leaf The type of set elements.
     * @param name The name of the element to fill.
     * @param query The SQL query that produces the data for <code>name</code>.
     * @throws SQLException if querying the database fails.
     */
    public void readSet(Type leaf, String name, String query) throws SQLException {
        IloOplDataHandler handler = getDataHandler();
        final RunQuery q = new RunQuery(query);
        try {
            ResultSet rs = q.getResult();
            handler.startElement(name);
            handler.startSet();

            while (rs.next()) {
                if (leaf == Type.INTEGER)
                    handler.addIntItem(rs.getInt(1));
                else if (leaf == Type.FLOAT)
                    handler.addNumItem(rs.getDouble(1));
                else if (leaf == Type.STRING)
                    handler.addStringItem(rs.getString(1));
            }
            handler.endSet();
            handler.endElement();
        }
        finally {
            q.close();
        }
    }

    /**
     * Read the tuple set for <code>name</code> from <code>query</code>.
     *
     * @param name The name of the element to fill.
     * @param query The SQL query that produces the data for <code>name</code>.
     * @throws SQLException if querying the database fails.
     */
    public void readTupleSet(String name, String query) throws SQLException {
        IloOplDataHandler handler = getDataHandler();
        IloOplElement elt = handler.getElement(name);
        ilog.opl_core.cppimpl.IloTupleSet tupleSet = (ilog.opl_core.cppimpl.IloTupleSet) elt.asTupleSet();
        IloTupleSchema schema = tupleSet.getSchema_cpp();
        int size = schema.getTotalColumnNumber();

        String[] oplFieldsName = new String[size];
        Type[] oplFieldsType = new Type[size];

        fillNamesAndTypes(schema, oplFieldsName, oplFieldsType);
        
        boolean is_mapping_name = _configuration.isMappingName();

        final RunQuery q = new RunQuery(query);
        try {
            ResultSet rs = q.getResult();

            handler.startElement(name);
            handler.startSet();
            while (rs.next()) {
                handler.startTuple();
                for (int column = 0; column < oplFieldsName.length; column++) {
                    if (is_mapping_name) {
                      String columnName = oplFieldsName[column];
                      if (oplFieldsType[column] == Type.INTEGER) {
                          handler.addIntItem(rs.getInt(columnName));
                      } else if (oplFieldsType[column] == Type.FLOAT) {
                          handler.addNumItem(rs.getDouble(columnName));
                      } else if (oplFieldsType[column] == Type.STRING) {
                          handler.addStringItem(rs.getString(columnName));
                      }
                    } else {
                      int sql_index = column+1;
                      if (oplFieldsType[column] == Type.INTEGER) {
                        handler.addIntItem(rs.getInt(sql_index));
                      } else if (oplFieldsType[column] == Type.FLOAT) {
                        handler.addNumItem(rs.getDouble(sql_index));
                      } else if (oplFieldsType[column] == Type.STRING) {
                        handler.addStringItem(rs.getString(sql_index));
                      }
                    }
                }
                handler.endTuple();
            }
            handler.endSet();
            handler.endElement();
        }
        finally {
            q.close();
        }
    }
    

};
