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

import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.environment.Request;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.Serializable;

/**
 * Base implementation of <code>ServerPagesGenerator</code>. This class
 * declares variables that must be explicitly initialized by code generators.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: AbstractServerPage.java,v 1.3 2003/03/19 15:53:19 cziegeler Exp $
 */
public abstract class AbstractServerPage
  extends ServletGenerator 
  implements CompiledComponent, CacheableProcessingComponent, Cacheable, Recomposable {
    /**
     * Code generators should produce a constructor
     * block that initializes the generator's
     * creation date and file dependency list.
     * Example:
     *
     *  {
     *    this.dateCreated = 958058788948L;
     *    this.dependencies = new File[] {
     *      new File("source.xml"),
     *    };
     *  }
     *
     */

    /** The creation date */
    protected long dateCreated = -1L;
    /** The dependency file list */
    protected File[] dependencies = null;

    /**
     * Recompose with the actual <code>ComponentManager</code> that should
     * be used.
     */
    public void recompose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /**
     * Determines whether this generator's source files have changed
     *
     * @return Whether any of the files this generator depends on has changed
     * since it was created
     */
    public boolean modifiedSince(long date) {
        if (date == 0 || dateCreated < date) {
            return true;
        }

        for (int i = 0; i < dependencies.length; i++) {
            if (dateCreated < dependencies[i].lastModified()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether generated content has changed since
     * last invocation. Users may override this method to take
     * advantage of SAX event cacheing
     *
     * @param request The request whose data must be inspected to assert whether
     * dynamically generated content has changed
     * @return Whether content has changes for this request's data
     */
    public boolean hasContentChanged(Request request) {
      return true;
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>null</code> if the component
     *         is currently not cacheable.
     */
    public Serializable getKey() {
        return null;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object, <code>NOPCacheValidity</code>
     *         is the default if hasContentChange() gives false otherwise
     *         <code>null</code> will be returned.
     */
    public SourceValidity getValidity() {
        if (hasContentChanged(request))
            return null;
        else
            return NOPValidity.SHARED_INSTANCE;
    }

    // FIXME: Add more methods!
    /* SAX Utility Methods */
    /**
     * Add an attribute
     *
     * @param attr The attribute list to add to
     * @param name The attribute name
     * @param value The attribute value
     */
    protected void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("", name, name, "CDATA", value);
    }

    /**
     * Start an element
     *
     * @param name The element name
     * @param attr The element attributes
     */
    protected void start(String name, AttributesImpl attr) throws SAXException {
        this.contentHandler.startElement("", name, name, attr);
        attr.clear();
    }

    /**
     * End an element
     *
     * @param name The element name
     */
    protected void end(String name) throws SAXException {
        this.contentHandler.endElement("", name, name);
    }

    /**
     * Add character data
     *
     * @param data The character data
     */
    protected void characters(String data) throws SAXException {
        this.contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    /**
     * Add a comment
     *
     * @param data The comment data
     */
    protected void comment(String data) throws SAXException {
        this.lexicalHandler.comment(data.toCharArray(), 0, data.length());
    }

    /**
     * Generates the unique key.
     * This key must be unique inside the space of this component.
     * Users may override this method to take
     * advantage of SAX event cacheing
     *
     * @return A long representing the cache key (defaults to not cachable)
     */
    public long generateKey() {
        return 0;
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object, <code>NOPCacheValidity</code>
     *         is the default if hasContentChange() gives false otherwise
     *         <code>null</code> will be returned.
     */
    public CacheValidity generateValidity() {
        if (hasContentChanged(request))
            return null;
        else
            return NOPCacheValidity.CACHE_VALIDITY;
    }

}

/** 
 * This is here to avaid references to the deprecated package.
 * It is required to support the deprecated caching algorithm
 */
final class NOPCacheValidity
implements CacheValidity {

    public static final CacheValidity CACHE_VALIDITY = new NOPCacheValidity();

    public boolean isValid(CacheValidity validity) {
        return validity instanceof NOPCacheValidity;
    }

    public String toString() {
        return "NOP Validity";
    }
}