/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Iterator;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;
import java.util.Hashtable;
import java.text.StringCharacterIterator;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.Component;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Composer;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Poolable;
import org.apache.avalon.Recyclable;
import org.apache.avalon.Parameters;
import org.apache.avalon.Loggable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.Roles;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.ContentHandlerWrapper;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.TransformerException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-02-22 17:10:56 $
 */
public class TraxTransformer extends ContentHandlerWrapper
implements Transformer, Composer, Poolable, Recyclable, Configurable {
    private static String FILE = "file:/";

    /** The store service instance */
    private Store store = null;

    /** The trax TransformerFactory */
    private SAXTransformerFactory tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();

    /** The trax TransformerHandler */
    private TransformerHandler transformerHandler = null;

    /** Is the store turned on? (default is on) */
    private boolean useStore = true;

    TransformerHandler getTransformerHandler(EntityResolver resolver, String xsluri)
      throws SAXException, ProcessingException, IOException, TransformerConfigurationException
    {
        // Only local files are checked for modification for compatibility reasons!
        // Using the entity resolver we get the filename of the current file:
        // The systemID if such a resource starts with file://.
        Templates templates = null;
        InputSource src = resolver.resolveEntity(null, xsluri);
        String      systemID = src.getSystemId();

        if (this.useStore == true)
        {
            // Is this a local file
            if (systemID.startsWith(FILE) == true) {
                // Stored is an array of the template and the caching time
                if (store.containsKey(xsluri) == true) {
                    Object[] templateAndTime = (Object[])store.get(xsluri);
                    File xslFile = new File(systemID.substring(FILE.length()));
                    long storedTime = ((Long)templateAndTime[1]).longValue();
                    if (storedTime < xslFile.lastModified()) {
                        templates = null;
                    } else {
                        templates = (Templates)templateAndTime[0];
                    }
                }
            } else {
                // only the template is stored
                if (store.containsKey(xsluri) == true) {
                   templates = (Templates)store.get(xsluri);
                }
            }
        }
        if(templates == null)
        {
            templates = tfactory.newTemplates(new SAXSource(new InputSource(systemID)));
            if (this.useStore == true)
            {
                // Is this a local file
                if (systemID.startsWith(FILE) == true) {
                    // Stored is an array of the template and the current time
                    Object[] templateAndTime = new Object[2];
                    templateAndTime[0] = templates;
                    templateAndTime[1] = new Long(System.currentTimeMillis());
                    store.hold(xsluri, templateAndTime);
                } else {
                    store.hold(xsluri,templates);
                }
            }
        }
        return tfactory.newTransformerHandler(templates);
    }

    /**
     * Configure this transformer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        if (conf != null) {
            Configuration child = conf.getChild("use-store");
            this.useStore = child.getValueAsBoolean(true);
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        try {
            log.debug("Looking up " + Roles.STORE);
            this.store = (Store) manager.lookup(Roles.STORE);
        } catch (Exception e) {
            log.error("Could not find component", e);
        }
    }

    /**
     * Set the <code>EntityResolver</code>, the <code>Map</code> with
     * the object model, the source and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {

        /** The Request object */
        HttpServletRequest request = (HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);

        // Check the stylesheet uri
        String xsluri = src;
        if (xsluri == null) {
            throw new ProcessingException("Stylesheet URI can't be null");
        }

        /** Get a Transformer Handler */
        try {
            transformerHandler = getTransformerHandler(resolver,xsluri);
        } catch (TransformerConfigurationException e){
            log.error("Problem in getTransformer:", e);
            throw new ProcessingException("Problem in getTransformer:" + e.getMessage());
        }

        if (request != null) {
            Enumeration parameters = request.getParameterNames();
            if ( parameters != null ) {
                while (parameters.hasMoreElements()) {
                    String name = (String) parameters.nextElement();
                    if (isValidXSLTParameterName(name)) {
                        String value = request.getParameter(name);
                        transformerHandler.getTransformer().setParameter(name,value);
                    }
                }
            }
        }

        if (par != null) {
            Iterator params = par.getParameterNames();
            while (params.hasNext()) {
                String name = (String) params.next();
                if (isValidXSLTParameterName(name)) {
                    String value = par.getParameter(name,null);
                    if (value != null) {
                        transformerHandler.getTransformer().setParameter(name,value);
                    }
                }
            }
        }

        super.setContentHandler(transformerHandler);
        if(transformerHandler instanceof Loggable) {
            ((Loggable)transformerHandler).setLogger(this.log);
        }
        if(transformerHandler instanceof org.xml.sax.ext.LexicalHandler)
            this.setLexicalHandler((org.xml.sax.ext.LexicalHandler)transformerHandler);
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.setContentHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler content) {
        transformerHandler.setResult(new SAXResult(content));
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler lexical) {
    }

    // FIXME (SM): this method may be a hotspot for requests with many
    //             parameters we should try to optimize it further
    static boolean isValidXSLTParameterName(String name) {
        StringCharacterIterator iter = new StringCharacterIterator(name);
        char c = iter.first();
        if (!(Character.isLetter(c) || c == '_')) {
            return false;
        } else {
            c = iter.next();
        }
        while (c != iter.DONE) {
            if (!(Character.isLetterOrDigit(c) ||
                c == '-' ||
                c == '_' ||
                c == '.')) {
                return false;
            } else {
                c = iter.next();
            }
        }

        return true;
    }

    public void recycle()
    {
        //FIXME: Patch for Xalan2J, to stop transform threads if
        //       there is a failure in the pipeline.
        try {
            Class clazz =
                Class.forName("org.apache.xalan.stree.SourceTreeHandler");
            Class  paramTypes[] =
                    new Class[]{ Exception.class };
            Object params[] =
                    new Object[] { new SAXException("Dummy Exception") };
            if(clazz.isInstance(transformerHandler)) {
                Method method = clazz.getMethod("setExceptionThrown",paramTypes);
                method.invoke(transformerHandler,params);
            }
        } catch (Exception e){
            log.debug("Exception in recycle:", e);
        }
        this.transformerHandler = null;
        super.recycle();
    }
}
