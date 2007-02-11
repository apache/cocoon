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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLDeserializer;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.store.Store;

import org.xml.sax.SAXException;

/**
 * The generation half of FragmentExtractor.
 *
 * FragmentExtractor is a transformer-generator pair which is designed to allow
 * sitemap managers to extract certain nodes from a SAX stream and move them
 * into a separate pipeline. The main use for this is to extract inline SVG
 * images and serve them up through a separate pipeline, usually serializing
 * them to PNG or JPEG format first.
 *
 * This is by no means complete yet, but it should prove useful, particularly
 * for offline generation.
 *
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Id: FragmentExtractorGenerator.java,v 1.4 2004/01/22 00:02:34 vgritsenko Exp $
 */
public class FragmentExtractorGenerator extends ServiceableGenerator
                                        implements CacheableProcessingComponent {

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return this.source;
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    public void generate() throws SAXException, ProcessingException {
        // Obtain the fragmentID  (which is simply the filename portion of the source)
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Retrieving fragment " + source + ".");
        }

        Store store = null;
        XMLDeserializer deserializer = null;
        Object fragment = null;
        try {
            store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
            fragment = store.get(source);
            if (fragment == null) {
                throw new ResourceNotFoundException("Could not find fragment " + source + " in store");
            }

            deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
            deserializer.setConsumer(this.xmlConsumer);
            deserializer.deserialize(fragment);

        } catch (ServiceException ce) {
            getLogger().error("Could not lookup for component.", ce);
            throw new SAXException("Could not lookup for component.", ce);
        } finally {
            this.manager.release(store);
            this.manager.release(deserializer);
        }
    }
}
