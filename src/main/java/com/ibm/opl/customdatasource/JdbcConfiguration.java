package com.ibm.opl.customdatasource;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The class to store JDBC custom data source connection parameters.
 */
public class JdbcConfiguration {
    Properties _readProperties = new Properties();
    
    public static class OutputParameters {
      public OutputParameters(boolean autodrop,
          String createStatement,String insertStatement, String target) {
        this.autodrop = autodrop;
        this.createStatement = createStatement;
        this.insertStatement = insertStatement;
        this.outputTable = target;
      }
      boolean autodrop = true;
      // if a statement is null but outputTable is not, generate automatically
      String createStatement = null;
      String insertStatement = null;
      // parameters for automatic generation, used in jdbcWriter
      String outputTable = null;
    }
    
    Map<String, OutputParameters> _outputMapping = new HashMap<String, OutputParameters>();

    private final static String URL = "url";
    private final static String USER = "user";
    private final static String PASSWORD = "password";
    private final static String IS_MAPPING_NAME = "is_mapping_name";
    
    private final static String READ = "read";
    private final static String WRITE = "write";

    private final static String QUERY = "query";
    private final static String NAME = "name";
    private final static String TABLE = "table";
    private final static String TARGET = "target";
    
    private String _url = null;
    private String _user = null;
    private String _password = null;
    private boolean _is_mapping_name = false;

    /** Resolve <code>s</code> using environment variables.
     * If <code>s</code> starts with "$" and is the name of an existing
     * environment variable then return the value of that variable, otherwise
     * return <code>s</code>.
     * @param s The string to resolve.
     * @return The value of environment variable <code>s</code> if such a
     *         variable exists, <code>s</code> otherwise.
     */
    private static String resolveString(String s) {
        if ( s == null || s.length() < 2 || s.charAt(0) != '$' )
            return s;
        final String value = System.getenv(s.substring(1));
        if ( value != null )
            return value;
        return s;
    }
    
    /**
     * Creates a new JDBC configuration.
     */
    public JdbcConfiguration() {
    }
    
    public boolean isMappingName() {
      return _is_mapping_name;
    }
    public void setMappingName(boolean value)
    {
      _is_mapping_name = value;
    }
    
    public String getUrl() {
        return _url;
    }
    
    public void setUrl(String url) {
      _url = url;
    }
    
    public String getUser() {
        return resolveString(_user);
    }

    public void setUser(String user) {
      _user = user;
    }

    public String getPassword() {
        return resolveString(_password);
    }

    public void setPassword(String password) {
      _password = password;
    }

    public Properties getReadQueries() {
        return _readProperties;
    }
    
    /**
     * Adds a read query to the datasource.
     * 
     * The specified query is used to populate the OPL data which name is specified.
     * @param name The OPL data
     * @param query The read query
     */
    public void addReadQuery(String name, String query) {
      _readProperties.setProperty(name, query);
    }
    
    public Map<String, OutputParameters> getOutputMapping() {
      return _outputMapping;
    }
    
    /**
     * Adds a write mapping to the datasource.
     * 
     * Using this method is equivalent to configure the output source with:
     * 
     * - Autodrop: The output table is dropped if it exists
     * - A default CREATE TABLE statement is generated for that table.
     * - A default INSERT statement is generated for that table.
     * @param name The OPL output name.
     * @param target The database table to map the output to.
     */
    public void addWriteMapping(String name, String target) {
      addOutputParameters(name, true, null, null, target);
    }
    
    public void addOutputParameters(String name, boolean autodrop,
      String createStatement, String insertStatement, String target) {
      _outputMapping.put(name, new OutputParameters(autodrop, createStatement, insertStatement, target));
    }
    
    public void addInsertStatement(String name, String insertStatement) {
      _outputMapping.put(name, new OutputParameters(false, null, insertStatement, null));
    }
    
    /**
     * Reads the configuration from the specified file.
     * 
     * Supported files are .properties and .XML files.
     * See also {@link #readXML(InputStream)} and {@link #readProperties(InputStream)}
     * @param filename The configuration file name.
     * @throws IOException
     */
    public void read(String filename) throws IOException {
        InputStream input = new FileInputStream(filename);
        try {
            if (filename.toLowerCase().endsWith(".properties"))
                this.readProperties(input);
            else if (filename.toLowerCase().endsWith(".xml"))
                this.readXML(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * Read and parse JDBC configuration from the specified property file.
     * 
     * Example:
     * <pre>
     * # The connection string
     * # The default url connects to mysql on default port, using database
     * # 'custom_data_source'
     * url=jdbc:mysql://localhost:3306/custom_data_source?useSSL=false
     * 
     * # Your connection credentials
     * user=sql_user
     * password=mysql
     * 
     * # Read queries. Those are the SQL queries to create OPL data sets.
     * # Each read query has the form: read.<table name>
     * read.Gasolines=SELECT name FROM GasData
     * read.Oils=SELECT name FROM OilData
     * read.GasData=SELECT * FROM GasData
     * read.OilData=SELECT * FROM OilData
     * 
     * # Result table name. This define the name of the table to write results in.
     * # table will be dropped.
     * # table will be created with fields names and types.
     * # table will be updated.
     * # Elements must be a tuplesets.
     * write.Result=result
     * </pre>
     * @param input The InputStream to read the configuration from
     * @throws IOException If an exception occurs while reading the configuration
     */
    public void readProperties(InputStream input) throws IOException {
        Properties properties = new Properties();
        properties.load(input);

        this._url = properties.getProperty(URL);
        this._user = properties.getProperty(USER);
        this._password = properties.getProperty(PASSWORD);
        String v = properties.getProperty(IS_MAPPING_NAME);
        if (v != null) {
          this._is_mapping_name = Boolean.valueOf(v);
        }
        
        
        // iterate properties to find read and write
        Enumeration<?> propertyNames = properties.propertyNames();
        String read = READ + ".";
        String write = WRITE + ".";
        while (propertyNames.hasMoreElements()) {
            String name = (String) propertyNames.nextElement();
            if (name.startsWith(read)) {
                int pos = read.length();
                String element = name.substring(pos);
                _readProperties.setProperty(element, (String) properties.getProperty(name));
            } else if (name.startsWith(write)) {
                int pos = write.length();
                String element = name.substring(pos);
                addWriteMapping(element, (String)properties.getProperty(name));
            }
        }
    }

    /**
     * Read and parse JDBC configuration from the specified XML file.
     *
     * Example:
     * <pre>
     * {@code
     * <datasource>
     *     <!-- The connection string
     *          The default url connects to mysql on default port, using database
     *          'custom_data_source'
     *     -->
     *     <url>jdbc:mysql://localhost:3306/custom_data_source?useSSL=false</url>
     * 
     *     <!-- Your connection credentials -->
     *     <user>root</user>
     *     <password>mysql</password>
     * 
     *     <!-- The read queries
     *          The name attribute is used to populate the corresponding Data Element.
     *     -->
     *     <read>
     *         <query name="Gasolines">SELECT name FROM GasData</query>
     *         <query name="Oils"> SELECT name FROM OilData</query>
     *         <query name="GasData">SELECT * FROM GasData</query>
     *        <query name="OilData">SELECT * FROM OilData</query>
     *     </read>
     *  
     *     <!-- The output table mapping.
     *          This mapping define how output data sets are exported to the database.
     *     -->
     *     <write>
     *         <!-- This maps the output dataset "Result" to the "result" table -->
     *         <table name="Result" target="result"/>
     *     </write>
     * </datasource>
     * }
     * </pre>
     * @param input The InputStream to read the configuration from
     * @throws IOException If an exception occurs while reading the configuration
     * @throws RuntimeException if an XML parse exception occurs.
     */
    public void readXML(InputStream input) throws IOException {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            Element root = doc.getDocumentElement();
            // connection parameters
            _url = doc.getElementsByTagName(URL).item(0).getTextContent();
            _user = doc.getElementsByTagName(USER).item(0).getTextContent();
            _password = doc.getElementsByTagName(PASSWORD).item(0).getTextContent();
            
            NodeList nl = doc.getElementsByTagName(IS_MAPPING_NAME);
            if (nl.getLength() > 0) {
              String v = nl.item(0).getTextContent();
              _is_mapping_name = Boolean.valueOf(v);
            }
            
            // input parameters
            Node readNode = doc.getElementsByTagName(READ).item(0);
            if (readNode != null && readNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList queries = ((Element)readNode).getElementsByTagName(QUERY);
                for (int iquery = 0; iquery < queries.getLength(); iquery++) {
                    Node qNode = queries.item(iquery);
                    if (qNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element qElement = (Element)qNode;
                        String name = qElement.getAttribute(NAME);
                        String query = qElement.getTextContent();
                        _readProperties.setProperty(name, query);
                    }
                }
            }
            
            // write parameters
            Node writeNode = doc.getElementsByTagName(WRITE).item(0);
            if (writeNode != null && writeNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList tables = ((Element)writeNode).getElementsByTagName(TABLE);
                for (int itable = 0; itable < tables.getLength(); itable++) {
                    Node tNode = tables.item(itable);
                    if (tNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tElement = (Element)tNode;
                        String name = tElement.getAttribute(NAME);
                        String target = tElement.getAttribute(TARGET);
                        addWriteMapping(name, target);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("Could not read XML configuration");
        }
    }
    
    /**
     * Utility method to execute a statement using the jdbc connection configured for this source
     * @param sql The SQL query or statement
     * @return true if the query or statement returned a ResultSet, false if it is an update count
     *         or there are no results.
     */
    public void execute(String sql) {
      ExecuteStatement s = null;
      try {
        s = new ExecuteStatement(sql);
        s.getResult();
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (s != null)
          try {
            s.close();
          } catch (SQLException e) {
            e.printStackTrace();
          }
      }
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
          Connection conn = DriverManager.getConnection(JdbcConfiguration.this.getUrl(),
              JdbcConfiguration.this.getUser(),
              JdbcConfiguration.this.getPassword());
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
          if (rs != null)  rs.close();
          if (stmt != null)  stmt.close();
          if (conn != null)  conn.close();
       }
       boolean getResult() { return result; }
       ResultSet getResultSet() { return rs; }
    }
}
