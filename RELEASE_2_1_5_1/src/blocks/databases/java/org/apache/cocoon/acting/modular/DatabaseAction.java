/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.acting.modular;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.AbstractComplementaryConfigurableAction;
import org.apache.cocoon.components.modules.database.AutoIncrementModule;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashMap;
import org.apache.cocoon.util.JDBCTypeConversions;
import org.apache.commons.lang.BooleanUtils;

/**
 * Abstract action for common function needed by database actions.
 * The difference to the other Database*Actions is, that the actions
 * in this package use additional components ("modules") for reading
 * and writing parameters. In addition the descriptor format has
 * changed to accomodate the new features.
 *
 * <p>This action is heavily based upon the original DatabaseAddActions.</p>
 *
 * <p>Modes have to be configured in cocoon.xconf. Mode names from
 * descriptor.xml file are looked up in the component service. Default
 * mode names can only be set during action setup. </p>
 *
 * <p>The number of affected rows is returned to the sitemap with the
 * "row-count" parameter if at least one row was affected.</p>
 *
 * <p>All known column types can be found in 
 * {@link org.apache.cocoon.util.JDBCTypeConversions JDBCTypeConversions}.</p>
 *
 * <table>
 * <tr><td colspan="2">Configuration options (setup):</td></tr>
 * <tr><td>input            </td><td>default mode name for reading values (request-param)</td></tr>
 * <tr><td>autoincrement    </td><td>default mode name for obtaining values from autoincrement columns (auto)</td></tr>
 * <tr><td>append-row       </td><td>append row number in square brackets to column name for output (yes)</td></tr>
 * <tr><td>append-table-name</td><td>add table name to column name for both in- and output (yes)</td></tr>
 * <tr><td>first-row        </td><td>row index of first row (0)</td></tr>
 * <tr><td>path-separator   </td><td>string to separate table name from column name (.)</td></tr>
 * </table>
 *
 * <table>
 * <tr><td colspan="2">Configuration options (setup and per invocation):</td></tr>
 * <tr><td>throw-exception  </td><td>throw an exception when an error occurs (default: false)</td></tr>
 * <tr><td>descriptor       </td><td>file containing database description</td></tr>
 * <tr><td>table-set        </td><td>table-set name to work with         </td></tr>
 * <tr><td>output           </td><td>mode name for writing values (request-attr)</td></tr>
 * <tr><td>reloadable       </td><td>dynamically reload descriptor file if change is detected</td></tr>
 * <tr><td>use-transactions </td><td>defaults to yes</td></tr>
 * <tr><td>connection       </td><td>configured datasource connection to use (overrides value from descriptor file)</td></tr>
 * <tr><td>fail-on-empty    </td><td>(boolean) fail is statement affected zero rows (true)</td></tr>
 * </table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseAction.java,v 1.8 2004/04/27 22:23:08 haul Exp $
 * @see org.apache.cocoon.components.modules.input
 * @see org.apache.cocoon.components.modules.output
 * @see org.apache.cocoon.components.modules.database
 * @see org.apache.cocoon.util.JDBCTypeConversions
 */
public abstract class DatabaseAction  extends AbstractComplementaryConfigurableAction implements Configurable, Disposable, ThreadSafe {

    // ========================================================================
    // constants
    // ========================================================================

    static final Integer MODE_AUTOINCR = new Integer( 0 );
    static final Integer MODE_OTHERS = new Integer( 1 );
    static final Integer MODE_OUTPUT = new Integer( 2 );

    static final String ATTRIBUTE_KEY = "org.apache.cocoon.action.modular.DatabaseAction.outputModeName";

    // These can be overidden from cocoon.xconf
    static final String inputHint = "request-param"; // default to request parameters
    static final String outputHint = "request-attr"; // default to request attributes
    static final String databaseHint = "manual"; // default to manual auto increments

    static final String INPUT_MODULE_SELECTOR = InputModule.ROLE + "Selector";
    static final String OUTPUT_MODULE_SELECTOR = OutputModule.ROLE + "Selector";
    static final String DATABASE_MODULE_SELECTOR = AutoIncrementModule.ROLE + "Selector";


    // ========================================================================
    // instance vars
    // ========================================================================

    protected ServiceSelector dbselector;
    protected Map defaultModeNames = new HashMap( 3 );
    protected final HashMap cachedQueryData = new HashMap();
    protected String pathSeparator = ".";
    protected int firstRow = 0;
    protected boolean failOnEmpty = true;

    // ========================================================================
    // inner helper classes
    // ========================================================================

    /**
     * Structure that takes all processed data for one column.
     */
    protected class Column {
        boolean isKey = false;
        boolean isSet = false;
        boolean isAutoIncrement = false;
        String mode = null;
        Configuration modeConf = null;
        Configuration columnConf = null;
    }


    /**
     * Structure that takes all processed data for a table depending
     * on current default modes
     */
    protected class CacheHelper {
        /**
         * Generated query string
         */
        public String queryString = null;
        /**
         * if a set is used, column number which is used to determine
         * the number of rows to insert.
         */
        public int setMaster = -1;
        public boolean isSet = false;
        public int noOfKeys = 0;
        public Column[] columns = null;

        public CacheHelper( int cols ) {
            this(0,cols);
        }

        public CacheHelper( int keys, int cols ) {
            noOfKeys = keys;
            columns = new Column[cols];
            for ( int i=0; i<cols; i++ ) {
                columns[i] = new Column();
            }
        }
    }


    /**
     * Structure that takes up both current mode types for database
     * operations and table configuration data. Used to access parsed
     * configuration data.
     */
    protected class LookUpKey {
        public Configuration tableConf = null;
        public Map modeTypes = null;

        public LookUpKey( Configuration tableConf, Map modeTypes ) {
            this.tableConf = tableConf;
            this.modeTypes = modeTypes;
        }
        
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
            boolean result = false;
            if (obj != null && obj instanceof LookUpKey) {
                LookUpKey luk = (LookUpKey) obj;
                result = true;
                result = result && (luk.tableConf == null ? 
                            this.tableConf == null : luk.tableConf.equals(this.tableConf));
                result = result && (luk.modeTypes == null ?
                            this.modeTypes == null : luk.modeTypes.equals(this.modeTypes));
            }
            
			return result;
		}
        
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return (this.tableConf != null ? 
                    this.tableConf.hashCode() : 
                        (this.modeTypes != null ? this.modeTypes.hashCode() : super.hashCode()));
		}
    }



    // set up default modes
    // <input/>
    // <output/>
    // <autoincrement/>
    //
    // all other modes need to be declared in cocoon.xconf
    // no need to declare them per action (anymore!)
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        if (this.settings != null) {
            this.defaultModeNames.put(MODE_OTHERS,   this.settings.get("input",  inputHint));
            this.defaultModeNames.put(MODE_OUTPUT,   this.settings.get("output", outputHint));
            this.defaultModeNames.put(MODE_AUTOINCR, this.settings.get("autoincrement", databaseHint));
            this.pathSeparator = (String) this.settings.get("path-separator", this.pathSeparator);
            String tmp = (String) this.settings.get("first-row",null);
            if (tmp != null) { 
                try {
                        this.firstRow = Integer.parseInt(tmp);
                } catch (NumberFormatException nfe) {
                    if (getLogger().isWarnEnabled())
                        getLogger().warn("problem parsing first row option "+tmp+" using default instead.");
                }
            }
            tmp = (String) this.settings.get("fail-on-empty",String.valueOf(this.failOnEmpty));
            this.failOnEmpty = BooleanUtils.toBoolean(tmp);
        }
    }

    // ========================================================================
    // Avalon methods
    // ========================================================================

    /**
     * Compose the Actions so that we can select our databases.
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.dbselector = (ServiceSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
    }


    /**
     *  dispose
     */
    public void dispose() {
        this.manager.release(dbselector);
    }


    // ========================================================================
    // protected utility methods
    // ========================================================================

    /**
     * Get the Datasource we need.
     */
    protected DataSourceComponent getDataSource( Configuration conf, Parameters parameters )
        throws ServiceException {

        String sourceName = parameters.getParameter( "connection", (String) settings.get( "connection" ) );
        if ( sourceName == null ) {
            Configuration dsn = conf.getChild("connection");
            return (DataSourceComponent) this.dbselector.select(dsn.getValue(""));
        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Using datasource: "+sourceName);
            return (DataSourceComponent) this.dbselector.select(sourceName);
        }
    }

    /**
     * Return whether a type is a Large Object (BLOB/CLOB).
     */
    protected final boolean isLargeObject (String type) {
        if ("ascii".equals(type)) return true;
        if ("binary".equals(type)) return true;
        if ("image".equals(type)) return true;

        return false;
    }

    /**
     * Store a key/value pair in the output attributes. We prefix the key
     * with the name of this class to prevent potential name collisions.
     */
    protected void setOutputAttribute(Map objectModel, String outputMode, String key, Object value) {

        ServiceSelector outputSelector = null;
        OutputModule output = null;
        try {
            outputSelector = (ServiceSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
            if (outputMode != null && outputSelector != null && outputSelector.isSelectable(outputMode)){
                output = (OutputModule) outputSelector.select(outputMode);
            }
            output.setAttribute( null, objectModel, key, value );
        } catch (Exception e) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("Could not select output mode "
                                     + outputMode + ":" + e.getMessage());
                }
        } finally {
            if (outputSelector != null) {
                if (output != null)
                    outputSelector.release(output);
                this.manager.release(outputSelector);
            }
         }
    }



    /**
     * Inserts a row or a set of rows into the given table based on the
     * request parameters
     *
     * @param table the table's configuration
     * @param conn the database connection
     * @param objectModel the objectModel
     */
    protected int processTable( Configuration table, Connection conn, Map objectModel,
                                 Map results, Map modeTypes )
        throws SQLException, ConfigurationException, Exception {

        PreparedStatement statement = null;
        int rows = 0;
        try {
            LookUpKey luk = new LookUpKey(table, modeTypes);
            CacheHelper queryData = null;

            if (getLogger().isDebugEnabled())
                getLogger().debug("modeTypes : "+ modeTypes);

            // get cached data
            // synchronize complete block since we don't want 100s of threads
            // generating the same cached data set. In the long run all data
            // is cached anyways so this won't cost much.
            synchronized (this.cachedQueryData) {
                queryData = (CacheHelper) this.cachedQueryData.get(luk,null);
                if (queryData == null) {
                    queryData = this.getQuery( table, modeTypes, defaultModeNames );
                    this.cachedQueryData.put(luk,queryData);
                }
            }

            if (getLogger().isDebugEnabled())
                getLogger().debug("query: "+queryData.queryString);
            statement = conn.prepareStatement(queryData.queryString);

            Object[][] columnValues = this.getColumnValues( table, queryData, objectModel );

            int setLength = 1;
            if ( queryData.isSet ) {
                if ( columnValues[ queryData.setMaster ] != null ) {
                    setLength = columnValues[ queryData.setMaster ].length;
                } else {
                    setLength = 0;
                }
            }

            for ( int rowIndex = 0; rowIndex < setLength; rowIndex++ ) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug( "====> row no. " + rowIndex );
                rows += processRow( objectModel, conn, statement, (String) modeTypes.get(MODE_OUTPUT), table, queryData, columnValues, rowIndex, results );
            }

        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {}
        }
        return rows;
    }


    /**
     * Choose a mode configuration based on its name.
     * @param conf Configuration (i.e. a column's configuration) that might have
     * several children configurations named "mode".
     * @param type desired type (i.e. every mode has a type
     * attribute), find the first mode that has a compatible type.
     * Special mode "all" matches all queried types.
     * @return configuration that has desired type or type "all" or null.
     */
    protected Configuration getMode( Configuration conf, String type )
        throws ConfigurationException {

        String modeAll = "all";
        Configuration[] modes = conf.getChildren("mode");
        Configuration modeConfig = null;

        for ( int i=0; i<modes.length; i++ ) {
            String modeType = modes[i].getAttribute("type", "others");
            if ( modeType.equals(type) || modeType.equals(modeAll)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("requested mode was \""+type+"\" returning \""+modeType+"\"");
                modeConfig = modes[i];
                break;
            }
        }

        return modeConfig;
    }


    /**
     * compose name for output a long the lines of "table.column"
     */
    protected String getOutputName ( Configuration tableConf, Configuration columnConf ) {

        return getOutputName( tableConf, columnConf, -1 );
    }


    /**
     * compose name for output a long the lines of "table.column[row]" or
     * "table.column" if rowIndex is -1.
     * If the section of the sitemap corresponding to the action contains
     * <append-table-name>false</append-table-name>
     * the name for output is "column[row]"
     * If the section of the sitemap corresponding to the action contains
     * <append-row>false</append-row>
     * the name for output is "column"
     */
    protected String getOutputName ( Configuration tableConf, Configuration columnConf, int rowIndex ) {

        if ( rowIndex != -1 && this.settings.containsKey("append-row") && 
             (this.settings.get("append-row").toString().equalsIgnoreCase("false") || 
              this.settings.get("append-row").toString().equalsIgnoreCase("0")) ) {
            rowIndex = -1;
        } else {
            rowIndex = rowIndex + this.firstRow;
        }
        if ( this.settings.containsKey("append-table-name") && 
             (this.settings.get("append-table-name").toString().equalsIgnoreCase("false") || 
              this.settings.get("append-table-name").toString().equalsIgnoreCase("0")) )
            {
                return ( columnConf.getAttribute("name",null)
                         + ( rowIndex == -1 ? "" : "[" + rowIndex + "]" ) );
            }
        else
            {
                return ( tableConf.getAttribute("alias", tableConf.getAttribute("name", null) )
                         + this.pathSeparator + columnConf.getAttribute("name",null)
                         + ( rowIndex == -1 ? "" : "[" + rowIndex + "]" ) );
            }
    }


    /*
     * Read all values for a column from an InputModule
     *
     * If the given column is an autoincrement column, an empty array
     * is returned, otherwise if it is part of a set, all available
     * values are fetched, or only the first one if it is not part of
     * a set.
     *
     */
    protected Object[] getColumnValue( Configuration tableConf, Column column, Map objectModel )
        throws ConfigurationException, ServiceException {

        if ( column.isAutoIncrement ) {
            return new Object[1];
        } else {
            Object[] values;
            String cname = getOutputName( tableConf, column.columnConf );

            // obtain input module and read values
            ServiceSelector inputSelector = null;
            InputModule input = null;
            try {
                inputSelector = (ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
                if (column.mode != null && inputSelector != null && inputSelector.isSelectable(column.mode)){
                    input = (InputModule) inputSelector.select(column.mode);
                }

                if (column.isSet) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug( "Trying to set column " + cname +" from "+column.mode+" using getAttributeValues method");
                    values = input.getAttributeValues( cname, column.modeConf, objectModel );
                } else {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug( "Trying to set column " + cname +" from "+column.mode+" using getAttribute method");
                    values = new Object[1];
                    values[0] = input.getAttribute( cname, column.modeConf, objectModel );
                }

                if ( values != null ) {
                    for ( int i = 0; i < values.length; i++ ) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug( "Setting column " + cname + " [" + i + "] " + values[i] );
                    }
                }

            } finally {
                if (inputSelector != null) {
                    if (input != null)
                        inputSelector.release(input);
                    this.manager.release(inputSelector);
                }
            }

            return values;
        }
    }


    /**
     * Setup parsed attribute configuration object
     */
    protected void fillModes ( Configuration[] conf, boolean isKey, Map defaultModeNames,
                               Map modeTypes, CacheHelper set )
        throws ConfigurationException {

        String setMode = null;
        int offset = ( isKey ? 0: set.noOfKeys);

        for ( int i = offset; i < conf.length + offset; i++ ) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("i="+i);
            set.columns[i].columnConf =  conf[ i - offset ];
            set.columns[i].isSet = false;
            set.columns[i].isKey = isKey;
            set.columns[i].isAutoIncrement = false;
            if ( isKey & this.honourAutoIncrement() ) 
                set.columns[i].isAutoIncrement = set.columns[i].columnConf.getAttributeAsBoolean("autoincrement",false);
            
            set.columns[i].modeConf = getMode( set.columns[i].columnConf,
                                               selectMode( set.columns[i].isAutoIncrement, modeTypes ) );
            set.columns[i].mode = ( set.columns[i].modeConf != null ?
                                    set.columns[i].modeConf.getAttribute( "name", selectMode( isKey, defaultModeNames ) ) :
                                    selectMode( isKey, defaultModeNames ) );
            // Determine set mode for a whole column ...
            setMode = set.columns[i].columnConf.getAttribute("set", null);  // master vs slave vs null
            if ( setMode == null && set.columns[i].modeConf != null ) {
                // ... or for each mode individually
                setMode = set.columns[i].modeConf.getAttribute("set", null);
            }
            if ( setMode != null ) {
                set.columns[i].isSet = true;
                set.isSet = true;
                if ( setMode.equals("master") ) {
                    set.setMaster = i;
                }
            }
        }
    }

    /**
     * create a unique name using the getOutputName method and write
     * the value to the output module and the results map if present.
     *
     */
    protected void setOutput( Map objectModel, String outputMode, Map results,
                         Configuration table, Configuration column, int rowIndex, Object value ) {

        String param = this.getOutputName( table, column, rowIndex );
        if (getLogger().isDebugEnabled())
            getLogger().debug( "Setting column " + param + " to " + value );
        this.setOutputAttribute(objectModel, outputMode, param, value);
        if (results != null)
            results.put( param, String.valueOf( value ) );
    }

    /**
     * set a column in a statement using the appropriate JDBC setXXX method.
     *
     */
    protected void setColumn ( PreparedStatement statement, int position, Configuration entry, Object value ) throws Exception {

        JDBCTypeConversions.setColumn(statement, position, value, (Integer) JDBCTypeConversions.typeConstants.get(entry.getAttribute("type")));
    }


    /**
     * set a column in a statement using the appropriate JDBC setXXX
     * method and propagate the value to the output module and results
     * map if present. Effectively combines calls to setColumn and
     * setOutput.
     *
     */
    protected void setColumn ( Map objectModel, String outputMode, Map results,
                               Configuration table, Configuration column, int rowIndex, 
                               Object value, PreparedStatement statement, int position ) throws Exception {

        if (results!=null) this.setOutput( objectModel, outputMode, results, table, column, rowIndex, value );
        this.setColumn( statement, position, column, value );
    }


    // ========================================================================
    // main method
    // ========================================================================


    /**
     * Add a record to the database.  This action assumes that
     * the file referenced by the "descriptor" parameter conforms
     * to the AbstractDatabaseAction specifications.
     */
    public Map act( Redirector redirector, SourceResolver resolver, Map objectModel,
                    String source, Parameters param ) throws Exception {

        DataSourceComponent datasource = null;
        Connection conn = null;
        Map results = new HashMap();
        int rows = 0;
        boolean failed = false;

        // read global parameter settings
        boolean reloadable = Constants.DESCRIPTOR_RELOADABLE_DEFAULT;

        // call specific default modes apart from output mode are not supported
        // set request attribute
        String outputMode = param.getParameter("output", (String) defaultModeNames.get(MODE_OUTPUT));

        if (this.settings.containsKey("reloadable"))
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();

        // read local parameter settings
        try {
            Configuration conf =
                this.getConfiguration(param.getParameter("descriptor", (String) this.settings.get("descriptor")),
                                      resolver,
                                      param.getParameterAsBoolean("reloadable",reloadable));

            // get database connection and try to turn off autocommit
            datasource = this.getDataSource(conf, param);
            conn = datasource.getConnection();
            if (conn.getAutoCommit() == true) {
                try {
                    conn.setAutoCommit(false);
                } catch (Exception ex) {
                    String tmp = param.getParameter("use-transactions",(String) this.settings.get("use-transactions",null));
                    if (tmp != null &&  (tmp.equalsIgnoreCase("no") || tmp.equalsIgnoreCase("false") || tmp.equalsIgnoreCase("0"))) {
                        if (getLogger().isErrorEnabled())
                            getLogger().error("This DB connection does not support transactions. If you want to risk your data's integrity by continuing nonetheless set parameter \"use-transactions\" to \"no\".");
                        throw ex;
                    }
                }
            }

            // find tables to work with
            Configuration[] tables = conf.getChildren("table");
            String tablesetname = param.getParameter("table-set", (String) this.settings.get("table-set"));

            Map modeTypes = null;

            if (tablesetname == null) {
                modeTypes = new HashMap(6);
                modeTypes.put( MODE_AUTOINCR, "autoincr" );
                modeTypes.put( MODE_OTHERS, "others" );
                modeTypes.put( MODE_OUTPUT, outputMode );
                for (int i=0; i<tables.length; i++) {
                    rows += processTable( tables[i], conn, objectModel, results, modeTypes );
                }
            } else {
                // new set based behaviour

                // create index for table names / aliases
                Map tableIndex = new HashMap(2*tables.length);
                String tableName = null;
                Object result = null;
                for (int i=0; i<tables.length; i++) {
                    tableName = tables[i].getAttribute("alias",tables[i].getAttribute("name",""));
                    result = tableIndex.put(tableName,new Integer(i));
                    if (result != null) {
                        throw new IOException("Duplicate table entry for "+tableName+" at positions "+result+" and "+i);
                    }
                }

                Configuration[] tablesets = conf.getChildren("table-set");
                String setname = null;
                boolean found = false;

                // find tables contained in tableset
                int j = 0;
                for (j=0; j<tablesets.length; j++) {
                    setname = tablesets[j].getAttribute ("name", "");
                    if (tablesetname.trim().equals (setname.trim ())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IOException(" given set " + tablesetname + " does not exists in a description file.");
                }

                Configuration[] set = tablesets[j].getChildren("table");

                for (int i=0; i<set.length; i++) {
                    // look for alternative mode types
                    modeTypes = new HashMap(6);
                    modeTypes.put( MODE_AUTOINCR, set[i].getAttribute( "autoincr-mode", "autoincr" ) );
                    modeTypes.put( MODE_OTHERS, set[i].getAttribute( "others-mode",   "others" ) );
                    modeTypes.put( MODE_OUTPUT, outputMode );
                    tableName=set[i].getAttribute("name","");
                    if (tableIndex.containsKey(tableName)) {
                        j = ((Integer)tableIndex.get(tableName)).intValue();
                        rows += processTable( tables[j], conn, objectModel, results, modeTypes );
                    } else {
                        throw new IOException(" given table " + tableName + " does not exists in a description file.");
                    }
                }
            }

            if (conn.getAutoCommit()==false)
                conn.commit();

            // obtain output mode module and rollback output
            ServiceSelector outputSelector = null;
            OutputModule output = null;
            try {
                outputSelector = (ServiceSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
                if (outputMode != null && outputSelector != null && outputSelector.isSelectable(outputMode)){
                    output = (OutputModule) outputSelector.select(outputMode);
                }
                output.commit(null, objectModel);
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("Could not select output mode "
                                     + outputMode + ":" + e.getMessage());
                }
            } finally {
                if (outputSelector != null) {
                    if (output != null)
                        outputSelector.release(output);
                    this.manager.release(outputSelector);
                }
            }

        } catch (Exception e) {
            failed = true;
            if ( conn != null ) {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "Rolling back transaction. Caused by " + e.getMessage() );
                        e.printStackTrace();
                    }
                    conn.rollback();
                    results = null;

                    // obtain output mode module and commit output
                    ServiceSelector outputSelector = null;
                    OutputModule output = null;
                    try {
                        outputSelector = (ServiceSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
                        if (outputMode != null && outputSelector != null && outputSelector.isSelectable(outputMode)){
                            output = (OutputModule) outputSelector.select(outputMode);
                        }
                        output.rollback( null, objectModel, e);
                    } catch (Exception e2) {
                        if (getLogger().isWarnEnabled()) {
                            getLogger().warn("Could not select output mode "
                                       + outputMode + ":" + e2.getMessage());
                        }
                    } finally {
                        if (outputSelector != null) {
                            if (output != null)
                                outputSelector.release(output);
                            this.manager.release(outputSelector);
                        }
                    }

                } catch (SQLException se) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("There was an error rolling back the transaction", se);
                }
            }

            //throw new ProcessingException("Could not add record :position = " + currentIndex, e);

            // don't throw an exception, an error has been signalled, that should suffice

            String throwException = (String) this.settings.get( "throw-exception",
                                                                param.getParameter( "throw-exception", null ) );
            if ( throwException != null && BooleanUtils.toBoolean(throwException)) {
                throw new ProcessingException("Cannot process the requested SQL statement ",e);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqe) {
                    getLogger().warn("There was an error closing the datasource", sqe);
                }
            }

            if (datasource != null)
                this.dbselector.release(datasource);
        }
        if (results != null) {
            if (rows>0 || (!failed && !this.failOnEmpty)) {
                    results.put("row-count",new Integer(rows)); 
            } else {    
                results = null;
            }
        } else {
            if (rows>0) {
                results = new HashMap(1);
                results.put("row-count",new Integer(rows));
            }
        }

        return results; // (results == null? results : Collections.unmodifiableMap(results));
    }



    // ========================================================================
    // abstract methods
    // ========================================================================


    /**
     * set all necessary ?s and execute the query
     * return number of rows processed
     *
     * This method is intended to be overridden by classes that
     * implement other operations e.g. delete
     */
    protected abstract int processRow( Map objectModel, Connection conn, PreparedStatement statement, String outputMode,
                                       Configuration table, CacheHelper queryData, Object[][] columnValues,
                                       int rowIndex, Map results )
        throws SQLException, ConfigurationException, Exception;

    /**
     * determine which mode to use as default mode
     *
     * This method is intended to be overridden by classes that
     * implement other operations e.g. delete
     */
    protected abstract String selectMode( boolean isAutoIncrement, Map modes );


    /**
     * determine whether autoincrement columns should be honoured by
     * this operation. This is usually snsible only for INSERTs.
     *
     * This method is intended to be overridden by classes that
     * implement other operations e.g. delete
     */
    protected abstract boolean honourAutoIncrement();


    /**
     * Fetch all values for all columns that are needed to do the
     * database operation.
     *
     * This method is intended to be overridden by classes that
     * implement other operations e.g. delete
     */
    abstract Object[][] getColumnValues( Configuration tableConf, CacheHelper queryData, Map objectModel )
        throws ConfigurationException, ServiceException;

    /**
     * Get the String representation of the PreparedStatement.  This is
     * mapped to the Configuration object itself, so if it doesn't exist,
     * it will be created.
     *
     * This method is intended to be overridden by classes that
     * implement other operations e.g. delete
     *
     * @param table the table's configuration object
     * @return the insert query as a string
     */
    protected abstract CacheHelper getQuery( Configuration table, Map modeTypes, Map defaultModeNames )
        throws ConfigurationException, ServiceException;

}
