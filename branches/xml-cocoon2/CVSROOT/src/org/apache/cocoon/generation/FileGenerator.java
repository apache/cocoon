/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import org.apache.avalon.ThreadSafe;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.xml.XMLCompiler;
import org.apache.cocoon.xml.XMLInterpreter;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.cocoon.util.HashUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.Parameters;
import org.apache.avalon.Poolable;
import org.apache.avalon.Component;

/**
 *
 *
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events.
 *
 * The generator can use a store to store the compiled xml. The compiled
 * xml reduces the processing time as no second parsing for that xml is required.
 * The compiled xml is only generated for local files and can be configured in
 * the sitemap. The generator can get a default behaviour and each sitemap
 * pipeline can override this behaviour.
 * <p>
 * <code>
 * &lt;map:generator name="file" src="org.apache.cocoon.generation.FileGenerator"&gt;<br>
 * &nbsp;&nbsp;&lt;use-store map:value="false"/&gt;<br>
 * &lt;/map:generator&gt;
 * &lt:map:generate type="file"&gt;<br>
 * &nbsp;&nbsp;&lt;parameter name="use-store" value="true"/&gt;<br>
 * &lt;/map:generate&gt;<br>
 * </code>
 * </p>
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.26 $ $Date: 2001-04-11 10:52:59 $
 */
public class FileGenerator extends ComposerGenerator
implements Configurable, Cacheable {

    /** The store service instance */
    private Store store = null;

    /** Is the store turned on? (default is off) */
    private boolean useStore;

    /** The default configuration for useStore */
    private boolean defaultUseStore;

    /** The input source */
    private InputSource inputSource;
    private String      systemID;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        super.compose(manager);
        try {
            getLogger().debug("Looking up " + Roles.STORE);
            this.store = (Store) manager.lookup(Roles.STORE);
        } catch (Exception e) {
            getLogger().error("Could not find component", e);
        }
    }

    public void recycle() {
        super.recycle();
        this.inputSource = null;
        this.systemID = null;
    }

    /**
     * Configure this generator.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        if (conf != null) {
            Configuration child = conf.getChild("use-store");
            this.defaultUseStore = child.getValueAsBoolean(false);
        }
    }

    /**
     * Set the use-store parameter
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.inputSource = super.resolver.resolveEntity(null, super.source);
        this.systemID = this.inputSource.getSystemId();
        this.useStore = par.getParameterAsBoolean("use-store", this.defaultUseStore);
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public long generateKey() {
        if (this.systemID.startsWith("file:") == true) {
            return HashUtil.hash(super.source);
        } else {
            return 0;
        }
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        if (this.systemID.startsWith("file:") == true) {
            File xmlFile = new File(this.systemID.substring("file:".length()));
            return new TimeStampCacheValidity(xmlFile.lastModified());
        }
        return null;
    }

   /**
     * Generate XML data.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        try {
            // Only local files are checked for modification
            // External files are never stored
            // Using the entity resolver we get the filename of the current file:
            // The systemID of such a resource starts with file:.
            getLogger().debug("processing file " + super.source);
            getLogger().debug("file resolved to " + this.systemID);
            byte[]      cxml = null;

            if (this.useStore == true)
            {
                // Is this a local file
                if (this.systemID.startsWith("file:") == true) {
                    // Stored is an array of the compiled xml and the caching time
                    if (store.containsKey(this.systemID) == true) {
                        Object[] cxmlAndTime = (Object[])store.get(this.systemID);
                        File xmlFile = new File(this.systemID.substring("file:".length()));
                        long storedTime = ((Long)cxmlAndTime[1]).longValue();
                        if (storedTime >= xmlFile.lastModified()) {
                            cxml = (byte[])cxmlAndTime[0];
                        }
                    }
                }
            }

            if(cxml == null)
            {
                Parser parser = (Parser)this.manager.lookup(Roles.PARSER);
                try {
                    // use the xmlcompiler for local files if storing is on
                    if (this.useStore == true && this.systemID.startsWith("file:") == true)
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        XMLCompiler compiler = new XMLCompiler();
                        compiler.setOutputStream(baos);
                        XMLMulticaster multicaster = new XMLMulticaster(compiler, null,
                              this.contentHandler, this.lexicalHandler);

                        parser.setContentHandler(multicaster);
                        parser.setLexicalHandler(multicaster);
                        parser.parse(this.inputSource);

                        // Stored is an array of the cxml and the current time
                        Object[] cxmlAndTime = new Object[2];
                        cxmlAndTime[0] = baos.toByteArray();
                        cxmlAndTime[1] = new Long(System.currentTimeMillis());
                        store.hold(this.systemID, cxmlAndTime);
                    } else {
                        parser.setContentHandler(this.contentHandler);
                        parser.setLexicalHandler(this.lexicalHandler);
                        parser.parse(this.inputSource);
                    }
                } finally {
                    this.manager.release((Component) parser);
                }
            } else {
                // use the stored cxml
                ByteArrayInputStream bais = new ByteArrayInputStream(cxml);
                XMLInterpreter interpreter = new XMLInterpreter();
                interpreter.setContentHandler(this.contentHandler);
                interpreter.setEntityResolver(super.resolver);
                interpreter.parse(bais);
            }
        } catch (IOException e) {
            getLogger().error("FileGenerator.generate()", e);
            throw new ResourceNotFoundException("FileGenerator could not find resource", e);
        } catch (SAXException e) {
            getLogger().error("FileGenerator.generate()", e);
            throw(e);
        } catch (Exception e){
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in FileGenerator.generate()",e);
        }
    }
}
