/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.NOPCacheValidity;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Options;
import org.apache.fop.apps.Version;
import org.apache.fop.messaging.MessageEvent;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.messaging.MessageListener;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * FOP serializer.
 *
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.20 $ $Date: 2001-05-04 00:51:37 $
 *
 * The use of a config file for FOP is enabled by adding a configuration
 * element to the serializer in the sitemap.
 * <user-config src="../webapps/cocoon/WEB-INF/fop_config.xml"/> 
 * note the path to the config file has to be relative to where the application
 * started the JVM, or absolute. If any one wants to fix this, go ahead!
 * (colin britton)
 */
public class FOPSerializer extends AbstractSerializer
implements MessageListener, Recyclable, Configurable, Cacheable {

    //Declare options for FOP 
    private Options options;
    
    //Declare the FOP driver
    private Driver driver;
    
   /**
    * Set the configurations for this serializer.
    */
    public void configure(Configuration conf) 
    throws ConfigurationException {
        String userConfig = null;
        File userConfigFile = null;

        if (conf != null) {
            Configuration child = conf.getChild("user-config");
            if (child != null) {
                try{
                    userConfig = child.getAttribute("src");
                } catch(Exception ex) {		
                    // getLogger().debug("FOPSerializer: No config file specified ");
                }
            }
        }
             
        // Check for null, use external or inbuilt config.
        if(userConfig != null){
            try {
                userConfigFile = new File(userConfig);
                options = new Options(userConfigFile);
                getLogger().debug("FOPSerializer: Using config file " + userConfig);
		    } catch (Exception ex) {
		        getLogger().error("FOPSerializer: Cannot load  config " + userConfig, ex);
		        throw new ConfigurationException("FOPSerializer: Cannot load config " + userConfig, ex);
            }
        } else {
       	    try {
	    	    options = new Options();
        	    getLogger().debug("FOPSerializer: Using default config file");
		    } catch (Exception e) {
    		    getLogger().error("FOPSerializer: Cannot load default config ", e);
            }
        }
    }
    

    /**
     * Create the FOP driver
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        // load the fop driver
        this.driver = new Driver();

        // the use of static resources sucks for servlet enviornments
        // since we could have multiple FOP that all logs in this pipe
        // It's a concurrency and security nightmare! (SM)
        MessageHandler.setOutputMethod(MessageHandler.EVENT);
        MessageHandler.addListener(this);

        this.driver.setRenderer(new org.apache.fop.render.pdf.PDFRenderer());
        this.driver.setOutputStream(out);
        this.setContentHandler(this.driver.getContentHandler());
     }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            this.driver.format();
            this.driver.render();
        } catch (IOException e) {
            getLogger().error("FOPSerializer.endDocument()", e);
            throw new SAXException (e);
        } catch (FOPException e) {
            getLogger().error("FOPSerializer.endDocument()", e);
            throw new SAXException (e);
        }
    }

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return "application/pdf";
    }

    /**
     * Receive FOP events.
     */
    public void processMessage (MessageEvent event) {
        // Output FOP messgaes to Cocoon logging system
        // Rather verbose,  so wrote all as debug (including errors)
        // Could be cleaned up to handle different messages as required (CB)
        getLogger().debug("FOP Message: "+event.getMessage());
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public long generateKey() {
        return 1;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        return new NOPCacheValidity();
    }

    /**
     * Recycle the component and remove it from the MessageList
     */
    public void recycle() {
        super.recycle();
        MessageHandler.removeListener(this);
        this.options = null;
        this.driver = null;
    }

}
