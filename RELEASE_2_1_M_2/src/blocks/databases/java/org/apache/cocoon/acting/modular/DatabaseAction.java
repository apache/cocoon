/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.acting.modular;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
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
 * </table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: DatabaseAction.java,v 1.1 2003/03/09 00:03:04 pier Exp $
 * @see org.apache.cocoon.components.modules.input
 * @see org.apache.cocoon.components.modules.output
 * @see org.apache.cocoon.components.modules.database
 * @see org.apache.cocoon.util.JDBCTypeConversions
 */
public abstract class DatabaseAction  extends AbstractComplementaryConfigurableAction implements Configurable, Disposable {

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

    protected ComponentSelector dbselector;
    protected Map defaultModeNames = new HashMap( 3 );
    protected final HashMap cachedQueryData = new HashMap();
    protected String pathSeparator = ".";
    protected int firstRow = 0;

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
                };
            }
        }
    }

    // ========================================================================
    // Avalon methods
    // ========================================================================

    /**
     * Compose the Actions so that we can select our databases.
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.dbselector = (ComponentSelector) manager.lookup(DataSourceComponent.ROLE + "Selector");
        super.compose(manager);
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
        throws ComponentException {

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

        ComponentSelector outputSelector = null;
        OutputModule output = null;
        try {
            outputSelector=(ComponentSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
            if (outputMode != null && outputSelector != null && outputSelector.hasComponent(outputMode)){
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
            };

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
        Configuration modeConfig = null;;

        for ( int i=0; i<modes.length; i++ ) {
            String modeType = modes[i].getAttribute("type", "others");
            if ( modeType.equals(type) || modeType.equals(modeAll)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("requested mode was \""+type+"\" returning \""+modeType+"\"");
                modeConfig = modes[i];
                break;
            };
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
        throws ConfigurationException, ComponentException {

        if ( column.isAutoIncrement ) {
            return new Object[1];
        } else {
            Object[] values;
            String cname = getOutputName( tableConf, column.columnConf );

            // obtain input module and read values
            ComponentSelector inputSelector = null;
            InputModule input = null;
            try {
                inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
                if (column.mode != null && inputSelector != null && inputSelector.hasComponent(column.mode)){
                    input = (InputModule) inputSelector.select(column.mode);
                }

                if ( column.isSet ){
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
            ComponentSelector outputSelector = null;
            OutputModule output = null;
            try {
                outputSelector=(ComponentSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
                if (outputMode != null && outputSelector != null && outputSelector.hasComponent(outputMode)){
                    output = (OutputModule) outputSelector.select(outputMode);
                }
                output.commit( null, objectModel );
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
            if ( conn != null ) {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug( "Rolling back transaction. Caused by " + e.getMessage() );
                        e.printStackTrace();
                    }
                    conn.rollback();
                    results = null;

                    // obtain output mode module and commit output
                    ComponentSelector outputSelector = null;
                    OutputModule output = null;
                    try {
                        outputSelector=(ComponentSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
                        if (outputMode != null && outputSelector != null && outputSelector.hasComponent(outputMode)){
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
            if ( throwException != null &&
                 ( throwException.equalsIgnoreCase( "true" ) || throwException.equalsIgnoreCase( "yes" ) ) ) {
                throw new ProcessingException("Could not add record",e);
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
            if (rows>0) {
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
        throws ConfigurationException, ComponentException;

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
        throws ConfigurationException, ComponentException;

}
