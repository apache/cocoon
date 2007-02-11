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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.serialization.Serializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This abstract class provides convenience methods to implement
 * a SAX based Source. Implement toSAX() and getSystemId() and
 * optionally override getLastModified() and getContentLength() to
 * obtain a valid Source implementation.
 *
 * @deprecated Use the new Avalon Excalibur Source Resolving
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version CVS $Id: AbstractSAXSource.java,v 1.3 2004/03/05 13:02:40 bdelacretaz Exp $
 */
public abstract class AbstractSAXSource
  implements Source {

    /** The Logger instance */
    protected Logger log;

    /** The ComponentManager instance */
    protected ComponentManager manager;

    /**
     * The constructor.
     *
     * @param environment the Cocoon Environment.
     * @param manager an Avalon Component Manager
     * @param logger A LogKit logger
     */

    public AbstractSAXSource(Environment environment,
                       ComponentManager manager,
                       Logger logger) {
      this.log = logger;
      this.manager = manager;

    }

    /**
     * Get an InputSource for the given URL. Shamelessly stolen
     * from SitemapSource.
     *
     */

    public InputStream getInputStream()
      throws ProcessingException, IOException {

        ComponentSelector serializerSelector = null;
        Serializer serializer = null;
        try {

            serializerSelector = (ComponentSelector) this.manager.lookup(Serializer.ROLE + "Selector");
            serializer = (Serializer)serializerSelector.select("xml");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            serializer.setOutputStream(os);

            this.toSAX(serializer);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (ComponentException cme) {
            throw new ProcessingException("could not lookup pipeline components", cme);
        } catch (Exception e) {
            throw new ProcessingException("Exception during processing of " + this.getSystemId(), e);
        } finally {
            if (serializer != null) serializerSelector.release(serializer);
            if (serializerSelector != null) this.manager.release(serializerSelector);
        }
    }

    /**
     * Get an InputSource for the given URL.
     *
     */

    public InputSource getInputSource()
      throws ProcessingException, IOException {
      InputSource is = new InputSource(this.getInputStream());
      is.setSystemId(this.getSystemId());

      return is;
    }

    /**
     * Implement this method to obtain SAX events.
     *
     */

    public abstract void toSAX(ContentHandler handler)
      throws SAXException;

    /**
     * Implement this method to set the unique identifier.
     *
     */

    public abstract String getSystemId();

    /**
     * Override this method to set the Content Length
     *
     */

    public long getContentLength() {
      return -1;
    }

    /**
     * Override this method to set the Last Modification date
     *
     */

    public long getLastModified() {
      return 0;
    }
}
