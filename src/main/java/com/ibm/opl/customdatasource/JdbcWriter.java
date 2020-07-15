package com.ibm.opl.customdatasource;

import ilog.concert.IloException;
import ilog.concert.IloTuple;
import ilog.opl.IloOplElement;
import ilog.opl.IloOplElementDefinition;
import ilog.opl.IloOplElementDefinitionType.Type;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplTupleSchemaDefinition;
import ilog.opl_core.cppimpl.IloTupleSchema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.ibm.opl.customdatasource.JdbcConfiguration.OutputParameters;
import com.ibm.opl.customdatasource.sql.NamedParametersPreparedStatement;

/**
 * The class to write data using JDBC.
 *
 */
public class JdbcWriter {
    private static long DEFAULT_BATCH_SIZE = 10000;
    private static long _num = 0;
    private String _name;
    private JdbcConfiguration _configuration;
    private IloOplModelDefinition _def;
    private IloOplModel _model;
    private long _batch_size;
    
    private static String _nextName() {
    	synchronized (JdbcWriter.class) {
    		_num ++;
    		return "writer" + _num;
    	}
    }
    
    /**
     * Convenience method to write the output of a model to a database.
     * 
     * @param config The database connection configuration.
     * @param model The OPL model.
     */
    public static void writeOutput(JdbcConfiguration config, IloOplModel model) {
      IloOplModelDefinition definition = model.getModelDefinition();
      JdbcWriter writer = new JdbcWriter(config, definition, model);
      writer.customWrite();
  }

    public JdbcWriter(String name, JdbcConfiguration configuration, IloOplModelDefinition def, IloOplModel model) {
    	_name = name;
        _configuration = configuration;
        _def = def;
        _model = model;
        _batch_size = DEFAULT_BATCH_SIZE;
    }
    
    public JdbcWriter(JdbcConfiguration configuration, IloOplModelDefinition def, IloOplModel model) {
        this(JdbcWriter._nextName(), configuration, def, model);
    }
    
    
    public JdbcWriter(JdbcConfiguration configuration, IloOplModel model) {
        this(configuration, model.getModelDefinition(), model);
    }

    public String getName() {
    	return _name;
    }
    
    public void customWrite() {
        long startTime = System.currentTimeMillis();
        System.out.println("Writing elements to database");

        for(JdbcConfiguration.OutputParameters op: _configuration.getOutputParameters()) {
          System.out.println("Writing " + op.name);
          customWrite(op.name, op);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Done (" + (endTime - startTime)/1000.0 + " s)");
    }

    static final String CREATE_QUERY = "CREATE TABLE %(";

    String createTableQuery(IloTupleSchema schema, String table) {
        String query = CREATE_QUERY.replace("%", table);
        IloOplElementDefinition elementDefinition = _def.getElementDefinition(schema.getName());
        IloOplTupleSchemaDefinition tupleSchema = elementDefinition.asTupleSchema();
        for (int i = 0; i < schema.getSize(); i++) {
            String columnName = schema.getColumnName(i);
            Type type = tupleSchema.getComponent(i).getElementDefinitionType();
            query += columnName;
            query += " ";
            if (type == Type.INTEGER)
                query += "INT";
            else if (type == Type.FLOAT)
                query += "FLOAT";
            else if (type == Type.STRING)
                query += "VARCHAR(30)";
            if (i < (schema.getSize() - 1))
                query += ", ";
        }
        query += ")";
        return query;
    }

    static final String INSERT_QUERY = "INSERT INTO %(";

    String getPlaceholderString(int size) {
      StringBuffer b = new StringBuffer();
      for (int i=0; i < size-1; i++)
        b.append("?,");
      b.append("?");
      return b.toString();
    }
    
    String getInsertQuery(IloTupleSchema schema, String table) {
      String query = INSERT_QUERY.replace("%", table);
      for (int i = 0; i < schema.getSize(); i++) {
          String columnName = schema.getColumnName(i);
          query += columnName;
          if (i < (schema.getSize() - 1))
              query += ", ";
      }
      query += ") VALUES(" + getPlaceholderString(schema.getSize()) + ")";
      return query;
    }

  /**
   * ValuesUpdater update the values in a PreparedStatement with the contents of the specified IloTuple.
   *
   */
  public static interface ValuesUpdater {
    /**
     * Update the parameters in a PreparedStatement with the values of the specified tuple.
     * @param tuple
     * @throws SQLException
     */
    void updateValues(IloTuple tuple) throws SQLException;
  }

  public static class NullValuesUpdater implements ValuesUpdater {
    public void updateValues(IloTuple tuple) throws SQLException {
	  // do nothing
    }
  }
  
  /**
   * A ValuesUpdater updating values by name.
   *
   */
  public static class NamedValuesUpdater implements ValuesUpdater {
    String[] _names = null;
    Type[] _types = null;
    NamedParametersPreparedStatement _stmt;
    NamedValuesUpdater(IloTupleSchema schema, IloOplTupleSchemaDefinition tupleSchemaDef,
        NamedParametersPreparedStatement stmt) {
      _names = new String[schema.getSize()];
      _types = new Type[schema.getSize()];
      for (int i=0; i < schema.getSize(); i++) {
        _names[i] = tupleSchemaDef.getComponent(i).getName();
        _types[i] = tupleSchemaDef.getComponent(i).getElementDefinitionType();
      }
      _stmt = stmt;
    }
    public void updateValues(IloTuple tuple) throws SQLException {
      final NamedParametersPreparedStatement stmt = _stmt;
      for (int i=0; i < _names.length; i++) {
        final Type columnType = _types[i];
        final String name = _names[i];
        if (columnType == Type.INTEGER)
          stmt.setInt(name, tuple.getIntValue(i));
        else if (columnType == Type.FLOAT)
          stmt.setDouble(name, tuple.getNumValue(i));
        else if (columnType == Type.STRING)
          stmt.setString(name, tuple.getStringValue(i));
      }
    }
  }
  
  /**
   * A ValuesUpdater updating values by index.
   *
   */
  public static class IndexedValuesUpdater implements ValuesUpdater{
    Type[] _types = null;
    PreparedStatement _stmt;
    int _max;
    IndexedValuesUpdater(IloTupleSchema schema, IloOplTupleSchemaDefinition tupleSchemaDef,
        PreparedStatement stmt) {
      _types = new Type[schema.getSize()];
      for (int i=0; i < schema.getSize(); i++) {
        _types[i] = tupleSchemaDef.getComponent(i).getElementDefinitionType();
      }
      _stmt = stmt;
      try {
        _max = stmt.getParameterMetaData().getParameterCount();
      } catch (SQLException e) {
    	_max = 9999999;
      }
    }
    public void updateValues(IloTuple tuple) throws SQLException {
      PreparedStatement stmt = _stmt;
      for (int i=0; i < _types.length && i < this._max; i++) {
        int columnIndex = i + 1;
        Type columnType = _types[i];
        if (columnType == Type.INTEGER)
          stmt.setInt(columnIndex, tuple.getIntValue(i));
        else if (columnType == Type.FLOAT)
          stmt.setDouble(columnIndex, tuple.getNumValue(i));
        else if (columnType == Type.STRING)
          stmt.setString(columnIndex, tuple.getStringValue(i));
      }
    }
  }
  

    static final String DROP_QUERY = "DROP TABLE %";

    /**
     * Writes a model element to database.
     * 
     * @param name The model element name.
     * @param table The database table.
     */
    void customWrite(String name, OutputParameters op) {
        String table = op.outputTable;
        IloOplElement elt = _model.hasElement(name) ? _model.getElement(name) : null;

        ilog.opl_core.cppimpl.IloTupleSet tupleSet = (elt != null) ? (ilog.opl_core.cppimpl.IloTupleSet) elt.asTupleSet() : null;
        IloTupleSchema schema = (tupleSet != null) ? tupleSet.getSchema_cpp() : null;
        try (Connection conn = DriverManager.getConnection(_configuration.getUrl(), _configuration.getUser(),
                    _configuration.getPassword())) {
            try (Statement stmt = conn.createStatement()) {
              String sql;
              // drop existing table if exists
              if (op.autodrop) {
                DatabaseMetaData dbm = conn.getMetaData();
                boolean exists = false;
                try (ResultSet rs = dbm.getTables(null, null, table, null)) {
                  exists = rs.next();
                }
                if (exists) {
                  sql = DROP_QUERY.replaceFirst("%", table);
                  try (Statement drop = conn.createStatement()) {
                    drop.executeUpdate(sql);
                  }
                }
              }
              
              // create table using tuple fields
              // first create query
              sql = null;
              if (op.outputTable != null && op.createStatement == null) {
                sql = createTableQuery(schema, table);
              } else if (op.createStatement != null) {
                sql = op.createStatement;
              }
              if (sql != null) {
                stmt.execute(sql);
              }
            } 
            NamedParametersPreparedStatement np_stmt = null;
            try {
              IloOplTupleSchemaDefinition tupleSchemaDef = null;
              if (schema != null) {
            	 IloOplElementDefinition tupleDef = _def.getElementDefinition(schema.getName());
            	 tupleSchemaDef = tupleDef.asTupleSchema();
                 final Type[] columnType = new Type[schema.getSize()];
                 for (int i = 0; i < columnType.length; ++i)
                    columnType[i] = tupleSchemaDef.getComponent(i).getElementDefinitionType();
              }
           
              
              String psql = null;
              if (op.outputTable != null && op.insertStatement == null) {
                psql = getInsertQuery(schema, table);
              } else {
                psql = op.insertStatement;
              }
              np_stmt = new NamedParametersPreparedStatement(conn, psql);
              conn.setAutoCommit(false); // begin transaction

              // The helper to updater a statement given a tuple
              ValuesUpdater updater = null;

              if (tupleSet == null) {
                updater = new NullValuesUpdater();
              } else if (np_stmt.hasNamedParameters()) {
                updater = new NamedValuesUpdater(schema, tupleSchemaDef, np_stmt);
              } else {
                // the named parameters prepared statement did not parse any named parameters
                // assume this is then regular prepared statement, and use the statement instead.
                updater = new IndexedValuesUpdater(schema, tupleSchemaDef, np_stmt.getStatement());
              }
              
              // the insert loop
              long icount = 1;
              if (tupleSet != null) {
                for (java.util.Iterator it1 = tupleSet.iterator(); it1.hasNext();) {
                  IloTuple tuple = (IloTuple) it1.next();
                  updater.updateValues(tuple);
                  if (_batch_size == 0) {
                    np_stmt.executeUpdate(); // no batch
                  }
                  else {
                    np_stmt.addBatch();
                    if ((icount % _batch_size) == 0) {
                      np_stmt.executeBatch();
                    }
                  }
                  icount ++;
                }
              } else {
            	  np_stmt.executeUpdate();
              }
              
              // flush batches if any
              if (_batch_size != 0) {
                np_stmt.executeBatch();
              }
              conn.commit();
            } catch (SQLException e) {
              conn.rollback();
              throw e;
            } finally {
              if (np_stmt != null)
                np_stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
