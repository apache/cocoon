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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * This is the interface between the {@link IncludeCacheManager} and a
 * {@link Source} object that stores the cached content in a directory
 * manner.
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: ModifiableSourceIncludeCacheStorageProxy.java,v 1.2 2003/03/11 16:33:37 vgritsenko Exp $
 *  @since   2.1
 */
public final class ModifiableSourceIncludeCacheStorageProxy
    implements IncludeCacheStorageProxy {

    private SourceResolver resolver;
    private String         parentURI;
    private Logger         logger;
    
    /**
     * Constructor
     * @param resolver   For source resolving
     * @param parentURI  The "directory"
     * @param logger     A logger for debugging
     */
    public ModifiableSourceIncludeCacheStorageProxy(SourceResolver resolver,
                                             String         parentURI,
                                             Logger         logger) {
        this.resolver = resolver;
        this.parentURI= parentURI;
        this.logger = logger;
    }
    
    /**
     * Calculate the URI for a child
     * @param uri     Child URI
     * @return String Absolute URI
     */
    private String getURI(String uri) {
        final long hash = HashUtil.hash(uri);
        final StringBuffer buffer = new StringBuffer(this.parentURI);
        buffer.append('/');
        if (hash < 0) {
            buffer.append('M').append(hash * -1);
        } else {
            buffer.append(hash);
        }
        buffer.append(".cxml");
        return buffer.toString();
    }
    
    /**
     * @see IncludeCacheStorageProxy#get(java.lang.String)
     */
    public Serializable get(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("WSCProxy: Getting content for " + uri);
        }

        Source child = null;
        Serializable result = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));

            if (logger.isDebugEnabled()) {
                logger.debug("WSCProxy: Resolved to " + child.getURI());
            }

            if (child.exists()) {
                InputStream is = child.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                result = (Serializable)ois.readObject();
                ois.close();
            }
        } catch (Exception ignore) {
        } finally {
            this.resolver.release( child );
        }

        if (logger.isDebugEnabled()) {
            logger.debug("WSCProxy: Result for " + uri + " : " + (result == null ? "Not in cache" : "Found"));
        }
        return result;
    }

    /**
     * @see IncludeCacheStorageProxy#put(java.lang.String, java.io.Serializable)
     */
    public void put(String uri, Serializable object) 
    throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("WSCProxy: Storing content for " + uri);
        }
        Source child = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));

            if (logger.isDebugEnabled()) {
                logger.debug("WSCProxy: Resolved to " + child.getURI());
            }

            OutputStream os;
            if (child instanceof ModifiableSource) {
                os = ((ModifiableSource)child).getOutputStream();
            } else {
                throw new IOException("Source " + uri + " is not writeable.");
            }
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(object);
            oos.flush();
            oos.close();
        } catch (IOException io) {
            throw io;
        } catch (Exception ignore) {
            throw new CascadingIOException("Exception.", ignore);
        } finally {
            this.resolver.release( child );
        }
    }

    /**
     * @see IncludeCacheStorageProxy#remove(java.lang.String)
     */
    public void remove(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("WSCProxy: Removing content for " + uri);
        }
        Source child = null;
        try {
            child = this.resolver.resolveURI(this.getURI(uri));

            if (logger.isDebugEnabled()) {
                logger.debug("WSCProxy: Resolved to " + child.getURI());
            }

            if (child instanceof ModifiableSource) {
                ((ModifiableSource)child).delete();
            } else {
                throw new IOException("Source " + uri + " is not writeable.");
            }
        } catch (Exception ignore) {
        } finally {
            this.resolver.release( child );
        }
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof ModifiableSourceIncludeCacheStorageProxy) {
            return this.parentURI.equals(((ModifiableSourceIncludeCacheStorageProxy)object).parentURI);
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        return this.parentURI.hashCode();
    }

}
