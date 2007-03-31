/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.web3.Web3;
import org.apache.cocoon.components.web3.Web3Client;
import org.apache.cocoon.components.web3.Web3DataSource;
import org.apache.cocoon.components.web3.Web3Streamer;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.JCO;

/**
 * TBD
 *
 * @since 2.1
 * @version $Id$
 */
public class Web3RfcTransformer extends AbstractTransformer 
implements Serviceable, Disposable, Configurable, Poolable, Recyclable {
    
    /** The service manager instance */
    protected ServiceManager    manager;
    protected Web3DataSource    web3source;
    
    protected Web3Client        connection;
    protected JCO.Repository    repository;
    protected IFunctionTemplate functionT;
    protected JCO.Function      function;
    protected JCO.ParameterList importParameterList;
    protected JCO.ParameterList tablesParameterList;
    protected JCO.Record        theRecord;
    protected JCO.Field         fillMe;
    
    protected AttributesImpl    attributes          = new AttributesImpl();
    protected int               startcount          = 0;
    protected boolean           error               = false;
    protected String            backend;
    protected String            default_backend;
    protected String            streamer;
    protected HashMap           tags                = new HashMap();

    public void configure(final Configuration configuration) throws ConfigurationException {
        this.default_backend = configuration.getChild("system").getValue(null);
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        initTags();
    }

    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters) throws SAXException {
        try {
            this.backend = parameters.getParameter("system");
        }
        catch (Exception x) {
            getLogger().warn("No backend configured! Try to use configuration");
            this.backend = this.default_backend;
        }
    }

    public void recycle() {
        this.connection            = null;
        this.repository            = null;
        this.functionT             = null;
        this.function              = null;
        this.importParameterList   = null;
        this.tablesParameterList   = null;
        this.theRecord             = null;
        this.backend               = null;
        this.streamer              = null;
        this.error                 = false;
        this.startcount            = 0;
        super.recycle();
    }

    /** Free all ressources */
    public void dispose() {
        this.manager    = null;
        this.attributes = null;
        this.web3source = null;
        this.tags       = null;
    }
    
    /**
    * Receive notification of the beginning of an element.
    */
    public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
        if ( Web3.URI.equals( uri ) && !this.error ) { 
            switch ( Integer.parseInt( (String) this.tags.get( loc ))) {
                case INCLUDE_ELEM: 
                    ServiceSelector r3sc = null;
                    try {
                        r3sc = (ServiceSelector) 
                            this.manager.lookup ( Web3DataSource.ROLE + "Selector");
                        this.web3source = (Web3DataSource) r3sc.select( this.backend );
                        this.connection = this.web3source.getWeb3Client();
                        this.repository = (JCO.Repository) this.connection.getRepository();
                        this.functionT = this.repository.getFunctionTemplate( 
                            a.getValue( Web3.INCLUDE_NAME_ATTR ) );
                        this.streamer  = (null == a.getValue( Web3.INCLUDE_CLASS_ATTR )) ? 
                            "default" : a.getValue( Web3.INCLUDE_CLASS_ATTR );
                        this.function  = this.functionT.getFunction();                    
                    } 
                    catch (Exception ex) {
                        String error = "Problems getting client for backend: '" 
                            + this.backend + "'";
                        getLogger().error (error, ex);   
                        
                        error = ex.getMessage();
                        this.attributes.clear();
                        super.startElement(uri, loc, raw, a);                
                        super.startElement(uri, Web3.PROCESSING_X_ELEM, 
                            Web3.PROCESSING_X_ELEM, this.attributes);
                        super.characters(error.toCharArray(), 0, 
                            error.length());
                        super.endElement(uri, Web3.PROCESSING_X_ELEM, 
                            Web3.PROCESSING_X_ELEM);
                        this.error = true;
                    } 
                    finally {
                        this.manager.release ( r3sc );
                    }
                break;
                case IMPORT_ELEM:
                    this.importParameterList = this.function.getImportParameterList();
                    this.theRecord = this.importParameterList;
                break;
                case FIELD_ELEM: 
                    this.fillMe = this.theRecord.getField( 
                        a.getValue( Web3.FIELD_NAME_ATTR ));
                break;
                case STRUCTURE_ELEM: 
                    this.theRecord = this.importParameterList.getStructure( 
                        a.getValue( Web3.STRUCTURE_NAME_ATTR ));
                break;
                case TABLES_ELEM:
                    this.tablesParameterList = this.function.getTableParameterList();
                break;
                case TABLE_ELEM:
                    this.theRecord = this.tablesParameterList.getTable( 
                        a.getValue( Web3.TABLE_NAME_ATTR ));
                break;
                case ROW_ELEM:
                    if (null != this.theRecord) {
                        try {
                            JCO.Table tmpTable = (JCO.Table) this.theRecord;
                            tmpTable.appendRow();
                        } 
                        catch (ClassCastException x) {
                            getLogger().error("Not a table! " + x.getMessage(), x);
                        }
                    }
                break;
                default:
                    getLogger().error("Invalid element " + loc);
            }
        } 
        else {
            super.startElement(uri, loc, raw, a);
        }
    }

    /**
    * Receive notification of the end of an element.
    */
    public void characters(char c[], int start, int len) throws SAXException {
        String theValue = new String(c, start, len).trim();
        if ( null != this.fillMe ) {
            if ( "".equals( theValue )) {
                theValue = null;
            }
            try {
                this.fillMe.setValue( theValue );
                this.fillMe = null;
                if( getLogger().isDebugEnabled() ) {
                    getLogger().debug("set value = " + theValue);
                }
            } 
            catch (JCO.ConversionException x) {
                getLogger().error( x.getMessage(), x);
            }
        } 
        else {
            super.characters(c, start, len);
        }
    }

    /**
    * Receive notification of the end of an element.
    */
    public void endElement(String uri, String loc, String raw) throws SAXException {
        if ( Web3.URI.equals(uri) && !this.error ) {
            switch ( Integer.parseInt( (String) this.tags.get( loc ))) {
                case INCLUDE_ELEM: 
                    Web3Streamer w3s = null;
                    ServiceSelector streamerSelector = null;
                    try {
                        this.connection.execute( this.function );
                        streamerSelector = 
                            (ServiceSelector) 
                            this.manager.lookup( Web3Streamer.ROLE + "Selector" );
                        w3s = (Web3Streamer) streamerSelector.select( this.streamer );
                        w3s.stream( this.function,  this.contentHandler );
                    } 
                    catch (Exception x) {
                        this.attributes.clear();
                        super.startElement(uri, Web3.ABAP_EXCEPTION_ELEM, 
                            Web3.ABAP_EXCEPTION_ELEM, this.attributes);
                        super.characters(x.getMessage ().toCharArray(), 
                            0, x.getMessage ().length());
                        super.endElement(uri, Web3.ABAP_EXCEPTION_ELEM, 
                            Web3.ABAP_EXCEPTION_ELEM);                    
                        getLogger().error(x.getMessage(), x);
                    } 
                    finally {
                        this.web3source.releaseWeb3Client( this.connection );
                        if ( null != streamerSelector ) {
                            streamerSelector.release( w3s );
                        }
                        this.manager.release( streamerSelector );
                    }
                    this.connection = null;
                    this.repository = null;
                    this.functionT = null;
                    this.function = null;
                    this.importParameterList = null;
                    this.tablesParameterList = null;
                    this.theRecord = null;        
                break;
                case STRUCTURE_ELEM: 
                    this.theRecord = this.importParameterList;
                break;
            } 
        }
        else {
            super.endElement(uri,loc,raw);
        }
    }

    protected final static int INCLUDE_ELEM     = 1;
    protected final static int IMPORT_ELEM      = 2;
    protected final static int EXPORT_ELEM      = 3;
    protected final static int TABLES_ELEM      = 4;
    protected final static int FIELD_ELEM       = 5;
    protected final static int ROW_ELEM         = 6;
    protected final static int STRUCTURE_ELEM   = 7;
    protected final static int TABLE_ELEM       = 8;
    
    protected void initTags() {
        this.tags.put( Web3.INCLUDE_ELEM,   "1" );
        this.tags.put( Web3.IMPORT_ELEM,    "2" );        
        this.tags.put( Web3.EXPORT_ELEM,    "3" );        
        this.tags.put( Web3.TABLES_ELEM,    "4" );        
        this.tags.put( Web3.FIELD_ELEM,     "5" );           
        this.tags.put( Web3.ROW_ELEM,       "6" );           
        this.tags.put( Web3.STRUCTURE_ELEM, "7" );     
        this.tags.put( Web3.TABLE_ELEM,     "8" );         
    }

}
