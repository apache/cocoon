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
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3RfcTransformer.java,v 1.8 2004/02/06 22:54:05 joerg Exp $
 */
public class Web3RfcTransformer extends AbstractTransformer 
implements Serviceable, Disposable, Configurable, Poolable, Recyclable {
    
    /** The service manager instance */
    protected ServiceManager  manager             = null;
    protected Web3DataSource    web3source          = null;
    
    protected Web3Client        connection          = null;
    protected JCO.Repository    repository          = null;
    protected IFunctionTemplate functionT           = null;
    protected JCO.Function      function            = null;
    protected JCO.ParameterList importParameterList = null;
    protected JCO.ParameterList tablesParameterList = null;
    protected JCO.Record        theRecord           = null;
    protected JCO.Field         fillMe              = null;
    
    protected AttributesImpl    attributes          = new AttributesImpl();
    protected int               startcount          = 0;
    protected boolean           error               = false;
    protected String            backend             = null;
    protected String            default_backend     = null;
    protected String            streamer            = null;
    protected HashMap           tags                = new HashMap();
    
    public void setup(SourceResolver resolver, Map objectModel,
                    String source, Parameters parameters) 
    throws SAXException {
        
        try {
            backend = parameters.getParameter("system");
        }
        catch (Exception x) {
            if ( null == backend ) {
                getLogger().warn("No backend configured! Try to use configuration");
                backend = default_backend;
            }
        }
    }

    public void service(ServiceManager manager) {
        this.manager = manager;
        initTags();
    }
    
    public void configure(final Configuration configuration)
        throws ConfigurationException {
            
        this.default_backend = configuration.getChild("system").getValue(null);
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
    * Receive notification of the beginning of a document.
    */
    public void startDocument() 
    throws SAXException {
        
        if ( null != super.contentHandler ) {
            super.contentHandler.startDocument();
        }    
    }

    /**
    * Receive notification of the end of a document.
    */
    public void endDocument() 
    throws SAXException {

        if ( null != super.contentHandler) {
            super.contentHandler.endDocument();
        }  
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
                        attributes.clear();
                        super.contentHandler.startElement(uri, loc, raw, a);                
                        super.contentHandler.startElement(uri, Web3.PROCESSING_X_ELEM, 
                            Web3.PROCESSING_X_ELEM, attributes);
                        super.contentHandler.characters(error.toCharArray(), 0, 
                            error.length());
                        super.contentHandler.endElement(uri, Web3.PROCESSING_X_ELEM, 
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
        else if (super.contentHandler != null) {
            super.contentHandler.startElement(uri, loc, raw, a);
        }
    }

    /**
    * Receive notification of the end of an element.
    */
    public void characters(char c[], int start, int len)
    throws SAXException {
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
            if (super.contentHandler != null) {
                super.contentHandler.characters(c, start, len);
            }
        }
    }

    /**
    * Receive notification of the end of an element.
    */
    public void endElement(String uri, String loc, String raw)
        throws SAXException 
    {
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
                        w3s.stream( this.function,  super.contentHandler );
                    } 
                    catch (Exception x) {
                        this.attributes.clear();
                        super.contentHandler.startElement(uri, Web3.ABAP_EXCEPTION_ELEM, 
                            Web3.ABAP_EXCEPTION_ELEM, this.attributes);
                        super.contentHandler.characters(x.getMessage ().toCharArray(), 
                            0, x.getMessage ().length());
                        super.contentHandler.endElement(uri, Web3.ABAP_EXCEPTION_ELEM, 
                            Web3.ABAP_EXCEPTION_ELEM);                    
                        getLogger().error(x.getMessage(), x);
                    } 
                    finally {
                        this.web3source.releaseWeb3Client( this.connection );
                        if ( null != streamerSelector ) {
                            streamerSelector.release( w3s );
                        }
                        manager.release( streamerSelector );
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
        else if (super.contentHandler != null) {
            super.contentHandler.endElement(uri,loc,raw);
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

