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
package org.apache.cocoon.environment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.util.BufferedOutputStream;
import org.apache.cocoon.util.ClassUtils;
import org.apache.commons.collections.IteratorEnumeration;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Base class for any environment
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractEnvironment.java,v 1.5 2003/03/18 18:24:27 bruno Exp $
 */
public abstract class AbstractEnvironment extends AbstractLogEnabled implements Environment {

    /** The current uri in progress */
    protected String uris;

    /** The current prefix to strip off from the request uri */
    protected StringBuffer prefix = new StringBuffer();

    /** The View requested */
    protected String view = null;

    /** The Action requested */
    protected String action = null;

     /** The Context path */
    protected URL context = null;

    /** The root context path */
    protected URL rootContext = null;

    /** The servlet object model */
    protected HashMap objectModel = null;

    /** The real source resolver */
    protected org.apache.excalibur.source.SourceResolver sourceResolver;

    /** The real xmlizer */
    protected org.apache.excalibur.xmlizer.XMLizer xmlizer;

    /** The component manager */
    protected ComponentManager manager;

    /** The attributes */
    private Map attributes = new HashMap();

    /** The secure Output Stream */
    protected BufferedOutputStream secureOutputStream;

    /** The real output stream */
    protected OutputStream outputStream;

    /** The AvalonToCocoonSourceWrapper (this is for the deprecated support) */
    static protected Constructor avalonToCocoonSourceWrapper;

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, String file)
    throws MalformedURLException {
        this(uri, view, new File(file), null);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, String file, String action)
    throws MalformedURLException {
        this(uri, view, new File(file), action);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, File file)
    throws MalformedURLException {
        this(uri, view, file, null);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, File file, String action)
    throws MalformedURLException {
        this(uri, view, file.toURL(), action);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, URL context, String action)
    throws MalformedURLException {
        this.uris = uri;
        this.view = view;
        this.context = context;
        this.action = action;
        this.objectModel = new HashMap();
        this.rootContext = context;
    }

    // Sitemap methods

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getURI() {
        return this.uris;
    }

    /**
     * Get the Root Context
     */
    public URL getRootContext() {
        return this.rootContext;
    }

    /**
     * Get the current Context
     */
    public URL getContext() {
        return this.context;
    }

    /**
     * Get the prefix of the URI in progress
     */
    public String getURIPrefix() {
        return this.prefix.toString();
    }

    /**
     * Set the prefix of the URI in progress
     */
    protected void setURIPrefix(String prefix) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Set the URI Prefix (OLD=" + getURIPrefix() + ", NEW=" +  prefix + ")");
        }
        this.prefix = new StringBuffer(prefix);
    }

    /**
     * Set the context.
     */
    protected void setContext(URL context) {
        this.context = context;
    }

    /**
     * Set the context. This is similar to changeContext()
     * except that it is absolute.
     */
    public void setContext(String prefix, String uri) {
        this.setContext(getRootContext());
        this.setURIPrefix(prefix == null ? "" : prefix);
        this.uris = uri;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Reset context to " + this.context);
        }
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String prefix, String newContext)
    throws MalformedURLException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Changing Cocoon context");
            getLogger().debug("  from context(" + this.context.toExternalForm() + ") and prefix(" + this.prefix + ")");
            getLogger().debug("  to context(" + newContext + ") and prefix(" + prefix + ")");
            getLogger().debug("  at URI " + this.uris);
        }
        int l = prefix.length();
        if (l >= 1) {
            if (!this.uris.startsWith(prefix)) {
                String message = "The current URI (" + this.uris +
                                 ") doesn't start with given prefix (" + prefix + ")";
                getLogger().error(message);
                throw new RuntimeException(message);
            }
            this.prefix.append(prefix);
            this.uris = this.uris.substring(l);

            // check for a slash at the beginning to avoid problems with subsitemaps
            if (this.uris.startsWith("/")) {
                this.uris = this.uris.substring(1);
                this.prefix.append('/');
            }
        }

        if (this.context.getProtocol().equals("zip")) {
            // if the resource is zipped into a war file (e.g. Weblogic temp deployment)
            // FIXME (VG): Is this still required? Better to unify both cases.
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Base context is zip: " + this.context);
            }
            this.context = new URL(this.context.toString() + newContext);
        } else {
            String sContext;
            // if we got a absolute context or one with a protocol resolve it
            if (newContext.charAt(0) == '/') {
                // context starts with the '/' - absolute file URL
                sContext = "file:" + newContext;
            } else if (newContext.indexOf(':') > 1) {
                // context have ':' - absolute URL
                sContext = newContext;
            } else {
                // context is relative to old one
                sContext = new URL(this.context, newContext).toString();
            }

            // Cut the file name part from context (if present)
            int i = sContext.lastIndexOf('/');
            if (i != -1 && i + 1 < sContext.length()) {
                sContext = sContext.substring(0, i + 1);
            }
            this.context = new URL(sContext);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New context is " + this.context.toExternalForm());
        }
    }

    /**
     * Redirect the client to a new URL
     */
    public abstract void redirect(boolean sessionmode, String newURL) throws IOException;

    public void globalRedirect(boolean sessionmode, String newURL) throws IOException {
        redirect(sessionmode, newURL);
    }

    // Request methods

    /**
     * Returns the request view
     */
    public String getView() {
        return this.view;
    }

    /**
     * Returns the request action
     */
    public String getAction() {
        return this.action;
    }

    // Response methods

    /**
     * Set a status code
     */
    public void setStatus(int statusCode) {
    }

    // Object model method

    /**
     * Returns a Map containing environment specific objects
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    /**
     * Resolve an entity.
     * @deprecated Use the resolveURI methods instead
     */
    public Source resolve(String systemId)
    throws ProcessingException, SAXException, IOException {
        if (getLogger().isDebugEnabled()) {
            this.getLogger().debug("Resolving '"+systemId+"' in context '" + this.context + "'");
        }
        if (systemId == null) throw new SAXException("Invalid System ID");

        // get the wrapper class - we don't want to import the wrapper directly
        // to avoid a direct dependency from the core to the deprecation package
        if ( null == avalonToCocoonSourceWrapper ) {
            synchronized (this.getClass()) {
                try {
                    Class clazz = ClassUtils.loadClass("org.apache.cocoon.components.source.impl.AvalonToCocoonSource");
                    avalonToCocoonSourceWrapper = clazz.getConstructor(new Class[] {ClassUtils.loadClass("org.apache.excalibur.source.Source"),
                                                                                    ClassUtils.loadClass("org.apache.excalibur.source.SourceResolver"),
                                                                                    ClassUtils.loadClass("org.apache.cocoon.environment.Environment")});
                } catch (Exception e) {
                    throw new ProcessingException("The deprecated resolve() method of the environment was called."
                                                  +"Please either update your code to use the new resolveURI() method or"
                                                  +" install the deprecation support.", e);
                }
            }
        }
        try {
            org.apache.excalibur.source.Source source = this.resolveURI( systemId );
            Source wrappedSource;
            wrappedSource = (Source)avalonToCocoonSourceWrapper.newInstance(new Object[] {source, this.sourceResolver, this});
            return wrappedSource;
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (Exception e) {
            throw new ProcessingException("Unable to create source wrapper.", e);
        }
    }

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @return true if the response is modified or if the
     *         environment is not able to test it
     */
    public boolean isResponseModified(long lastModified) {
        return true; // always modified
    }

    /**
     * Mark the response as not modified.
     */
    public void setResponseIsNotModified() {
        // does nothing
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(this.attributes.keySet().iterator());
    }

    /**
     * Get the output stream where to write the generated resource.
     * @deprecated Use {@link #getOutputStream(int)} instead.
     */
    public OutputStream getOutputStream() throws IOException {
        // by default we use the complete buffering output stream
        return this.getOutputStream(-1);
    }

    /**
     * Get the output stream where to write the generated resource.
     * The returned stream is buffered by the environment. If the
     * buffer size is -1 then the complete output is buffered.
     * If the buffer size is 0, no buffering takes place.
     * This method replaces {@link #getOutputStream()}.
     */
    public OutputStream getOutputStream(int bufferSize)
    throws IOException {
        if (bufferSize == -1) {
            if (this.secureOutputStream == null) {
                this.secureOutputStream = new BufferedOutputStream(this.outputStream);
            }
            return this.secureOutputStream;
        } else if (bufferSize == 0) {
            return this.outputStream;
        } else {
            this.outputStream = new java.io.BufferedOutputStream(this.outputStream, bufferSize);
            return this.outputStream;
        }
    }

    /**
     * Reset the response if possible. This allows error handlers to have
     * a higher chance to produce clean output if the pipeline that raised
     * the error has already output some data.
     *
     * @return true if the response was successfully reset
    */
    public boolean tryResetResponse()
    throws IOException {
        if (this.secureOutputStream != null) {
            this.secureOutputStream.clearBuffer();
            return true;
        }
        return false;
    }

    /**
     * Commit the response
     */
    public void commitResponse()
    throws IOException {
        if (this.secureOutputStream != null) {
            this.secureOutputStream.realFlush();
        }
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location)
    throws MalformedURLException, IOException, SourceException
    {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location,
                                                         String baseURI,
                                                         final Map    parameters)
    throws MalformedURLException, IOException, SourceException {
        return this.sourceResolver.resolveURI(location, baseURI, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release( final org.apache.excalibur.source.Source source ) {
        if ( null != source )
            this.sourceResolver.release( source );
    }

    /**
     * Generates SAX events from the given source
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually
     * directly implement the LexicalHandler interface!
     * @param  source    the data
     * @throws ProcessingException if no suitable converter is found
     */
    public void toSAX( org.apache.excalibur.source.Source source,
                       ContentHandler handler )
    throws SAXException, IOException, ProcessingException {
        this.toSAX( source, null, handler);
    }

    public void toSAX( org.apache.excalibur.source.Source source,
                String         mimeTypeHint,
                ContentHandler handler )
    throws SAXException, IOException, ProcessingException {
        String mimeType = source.getMimeType();
        if (null == mimeType) {
            mimeType = mimeTypeHint;
        }

        try {
            if (source instanceof XMLizable) {
                ((XMLizable)source).toSAX( handler );
            } else {
                try {
                    xmlizer.toSAX( source.getInputStream(),
                                   mimeType,
                                   source.getURI(),
                                   handler );
                } catch (SourceException se) {
                    throw SourceUtil.handle(se);
                }
            }
        } catch (SAXException e) {
            final Exception cause = e.getException();
            if (cause != null) {
                if (cause instanceof ProcessingException) {
                    throw (ProcessingException)cause;
                }
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                }
                if (cause instanceof SAXException) {
                    throw (SAXException)cause;
                }
            }
            throw e;
        }
    }

	/**
	 * Notify that the processing starts.
	 */
	public void startingProcessing() {
		try {
			this.manager = CocoonComponentManager.getSitemapComponentManager();
			this.xmlizer = (XMLizer)this.manager.lookup(XMLizer.ROLE);
			this.sourceResolver = (org.apache.excalibur.source.SourceResolver)this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
		} catch (ComponentException ce) {
			// this should never happen!
			throw new CascadingRuntimeException("Unable to lookup component.", ce);
		}
	}

	/**
	 * Notify that the processing is finished
	 * This can be used to cleanup the environment object
	 */
	public void finishingProcessing() {
		if ( null != this.manager ) {
			this.manager.release( (Component)this.xmlizer );
			this.manager.release( this.sourceResolver );
			this.manager = null;
			this.xmlizer = null;
			this.sourceResolver = null;
		}
	}

}
