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
 * @version CVS $Id: AbstractSAXSource.java,v 1.2 2003/03/16 17:49:10 vgritsenko Exp $
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
