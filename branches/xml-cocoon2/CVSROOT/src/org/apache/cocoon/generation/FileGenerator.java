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
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.components.store.Store;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.xml.XMLCompiler;
import org.apache.cocoon.xml.XMLInterpreter;
import org.apache.cocoon.xml.XMLMulticaster;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.Parameters;
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
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.22 $ $Date: 2001-02-23 14:01:26 $
 */
public class FileGenerator extends ComposerGenerator implements Poolable, Configurable {

    /** The store service instance */
    private Store store = null;

    /** Is the store turned on? (default is off) */
    private boolean useStore;

    /** The default configuration for useStore */
    private boolean defaultUseStore;

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
        this.useStore = par.getParameterAsBoolean("use-store", this.defaultUseStore);
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
            InputSource src = super.resolver.resolveEntity(null, super.source);
            String      systemID = src.getSystemId();
            getLogger().debug("file resolved to " + systemID);
            byte[]      cxml = null;

            if (this.useStore == true)
            {
                // Is this a local file
                if (systemID.startsWith("file:") == true) {
                    // Stored is an array of the compiled xml and the caching time
                    if (store.containsKey(systemID) == true) {
                        Object[] cxmlAndTime = (Object[])store.get(systemID);
                        File xmlFile = new File(systemID.substring("file:".length()));
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
                // use the xmlcompiler for local files if storing is on
                if (this.useStore == true && systemID.startsWith("file:") == true)
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    XMLCompiler compiler = new XMLCompiler();
                    compiler.setOutputStream(baos);
                    XMLMulticaster multicaster = new XMLMulticaster(compiler, null,
                              this.contentHandler, this.lexicalHandler);

                    parser.setContentHandler(multicaster);
                    parser.setLexicalHandler(multicaster);
                    parser.parse(src);

                    // Stored is an array of the cxml and the current time
                    Object[] cxmlAndTime = new Object[2];
                    cxmlAndTime[0] = baos.toByteArray();
                    cxmlAndTime[1] = new Long(System.currentTimeMillis());
                    store.hold(systemID, cxmlAndTime);
                } else {
                    parser.setContentHandler(this.contentHandler);
                    parser.setLexicalHandler(this.lexicalHandler);
                    parser.parse(src);
                }
                this.manager.release((Component) parser);
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
