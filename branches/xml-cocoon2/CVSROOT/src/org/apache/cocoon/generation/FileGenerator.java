/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.File;
import java.util.Map;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.TimeStampCacheValidity;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.util.HashUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.parameters.Parameters;
import org.apache.excalibur.pool.Poolable;
import org.apache.avalon.component.Component;

/**
 *
 *
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events.
 * The FileGenerator implements the <code>Cacheable</code> interface.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.30 $ $Date: 2001-04-20 20:50:06 $
 */
public class FileGenerator extends ComposerGenerator
implements Cacheable {

    /** The input source */
    private InputSource inputSource;
    /** The system ID of the input source */
    private String      systemID;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager) {
        super.compose(manager);
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        super.recycle();
        this.inputSource = null;
        this.systemID = null;
    }

    /**
     * Setup the file generator.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.inputSource = super.resolver.resolveEntity(null, super.source);
        this.systemID = this.inputSource.getSystemId();
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
        }
        return 0;
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
        Parser parser = null;
        try {
            getLogger().debug("processing file " + super.source);
            getLogger().debug("file resolved to " + this.systemID);

            parser = (Parser)this.manager.lookup(Roles.PARSER);

            parser.setContentHandler(super.contentHandler);
            parser.setLexicalHandler(super.lexicalHandler);

            parser.parse(this.inputSource);
        } catch (IOException e) {
            getLogger().error("FileGenerator.generate()", e);
            throw new ResourceNotFoundException("FileGenerator could not find resource", e);
        } catch (SAXException e) {
            getLogger().error("FileGenerator.generate()", e);
            throw(e);
        } catch (Exception e){
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in FileGenerator.generate()",e);
        } finally {
            if (parser != null) this.manager.release((Component) parser);
        }
    }
}
