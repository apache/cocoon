/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.util.BufferedOutputStream;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.Deprecation;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.SAXException;

/**
 * Base class for any environment
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version $Id$
 */
public abstract class AbstractEnvironment extends AbstractLogEnabled implements Environment {

    /** The current uri in progress */
    protected String uris;

    /** The current prefix to strip off from the request uri */
    protected StringBuffer prefix = new StringBuffer();

    /** The View requested */
    protected String view;

    /** The Action requested */
    protected String action;

     /** The Context path */
    protected String context;

    /** The context path stored temporarily between constructor and initComponents */
    private String tempInitContext;

    /** The root context path */
    protected String rootContext;

    /** The servlet object model */
    protected HashMap objectModel;

    /** The real source resolver */
    protected org.apache.excalibur.source.SourceResolver sourceResolver;

    /** The component manager */
    protected ComponentManager manager;

    /** The attributes */
    private Map attributes = new HashMap();

    /** The secure Output Stream */
    protected BufferedOutputStream secureOutputStream;

    /** The real output stream */
    protected OutputStream outputStream;

    /** The AvalonToCocoonSourceWrapper (this is for the deprecated support) */
    static protected Method avalonToCocoonSourceWrapper;

    /** Do we have our components ? */
    protected boolean initializedComponents = false;

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
        this(uri, view, file.toURL().toExternalForm(), action);
    }

    /**
     * Constructs the abstract environment
     */
    public AbstractEnvironment(String uri, String view, String context, String action)
    throws MalformedURLException {
        this.uris = uri;
        this.view = view;
        this.tempInitContext = context;
        this.action = action;
        this.objectModel = new HashMap();
    }

    /**
     * Allow implementations to set view later than in super() constructor.
     * View can be set only once, and should be set in implementation's constructor.
     */
    protected void setView(String view) {
        if (this.view != null) {
            throw new IllegalStateException("View was already set on this environment");
        }
        this.view = view;
    }

    /**
     * Allow implementations to set action later than in super() constructor
     * Action can be set only once, and should be set in implementation's constructor.
     */
    protected void setAction(String action) {
        if (this.action != null) {
            throw new IllegalStateException("Action was already set on this environment");
        }
        this.action = action;
    }

    /**
     * Helper method to extract the view name from the request.
     */
    protected static String extractView(Request request) {
        return request.getParameter(Constants.VIEW_PARAM);
    }

    /**
     * Helper method to extract the action name from the request.
     */
     protected static String extractAction(Request req) {
         String action = req.getParameter(Constants.ACTION_PARAM);
         if (action != null) {
             /* TC: still support the deprecated syntax */
             return action;
         } else {
             for(Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
                 String name = (String)e.nextElement();
                 if (name.startsWith(Constants.ACTION_PARAM_PREFIX)) {
                     if (name.endsWith(".x") || name.endsWith(".y")) {
                         return name.substring(Constants.ACTION_PARAM_PREFIX.length(),name.length()-2);
                     } else {
                         return name.substring(Constants.ACTION_PARAM_PREFIX.length());
                     }
                 }
             }
             return null;
         }
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
    public String getRootContext() {
        if ( !this.initializedComponents) {
            this.initComponents();
        }
        return this.rootContext;
    }

    /**
     * Get the current Context
     */
    public String getContext() {
        if (!this.initializedComponents) {
            this.initComponents();
        }
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
    protected void setContext(String context) {
        this.context = context;
    }

    /**
     * Set the context. This is similar to changeContext()
     * except that it is absolute.
     */
    public void setContext(String prefix, String uri, String context) {
        this.setContext(context);
        this.setURIPrefix(prefix == null ? "" : prefix);
        this.uris = uri;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Reset context to " + this.context);
        }
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String newPrefix, String newContext)
    throws IOException {
        if (!this.initializedComponents) {
            this.initComponents();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Changing Cocoon context");
            getLogger().debug("  from context(" + this.context + ") and prefix(" + this.prefix + ")");
            getLogger().debug("  to context(" + newContext + ") and prefix(" + newPrefix + ")");
            getLogger().debug("  at URI " + this.uris);
        }

        int l = newPrefix.length();
        if (l >= 1) {
            if (!this.uris.startsWith(newPrefix)) {
                String message = "The current URI (" + this.uris +
                                 ") doesn't start with given prefix (" + newPrefix + ")";
                getLogger().error(message);
                throw new RuntimeException(message);
            }
            this.prefix.append(newPrefix);
            this.uris = this.uris.substring(l);

            // check for a slash at the beginning to avoid problems with subsitemaps
            if (this.uris.startsWith("/")) {
                this.uris = this.uris.substring(1);
                this.prefix.append('/');
            }
        }

        if (this.context.startsWith("zip:")) {
            // if the resource is zipped into a war file (e.g. Weblogic temp deployment)
            // FIXME (VG): Is this still required? Better to unify both cases.
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Base context is zip: " + this.context);
            }

            org.apache.excalibur.source.Source source = null;
            try {
                source = this.sourceResolver.resolveURI(this.context + newContext);
                this.context = source.getURI();
            } finally {
                this.sourceResolver.release(source);
            }
        } else if (newContext.length() > 0){
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
                sContext = this.context + '/' + newContext;
            }

            // Cut the file name part from context (if present)
            int i = sContext.lastIndexOf('/');
            if (i != -1 && i + 1 < sContext.length()) {
                sContext = sContext.substring(0, i + 1);
            }

            org.apache.excalibur.source.Source source = null;
            try {
                source = this.sourceResolver.resolveURI(sContext);
                this.context = source.getURI();
            } finally {
                this.sourceResolver.release(source);
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New context is " + this.context);
        }
    }

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
        Deprecation.logger.warn("The method SourceResolver.resolve(String) is "
                              + "deprecated. Use resolveURI(String) instead.");
        if (!this.initializedComponents) {
            initComponents();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Resolving '" + systemId + "' in context '" + this.context + "'");
        }

        if (systemId == null) {
            throw new SAXException("Invalid System ID");
        }

        // get the wrapper class - we don't want to import the wrapper directly
        // to avoid a direct dependency from the core to the deprecation package
        Class clazz;
        try {
            clazz = ClassUtils.loadClass("org.apache.cocoon.components.source.impl.AvalonToCocoonSourceInvocationHandler");
        } catch (Exception e) {
            throw new ProcessingException("The deprecated resolve() method of the environment was called."
                                          +"Please either update your code to use the new resolveURI() method or"
                                          +" install the deprecation support.", e);
        }

        if (null == avalonToCocoonSourceWrapper) {
            synchronized (getClass()) {
                try {
                    avalonToCocoonSourceWrapper = clazz.getDeclaredMethod("createProxy",
                           new Class[] {ClassUtils.loadClass("org.apache.excalibur.source.Source"),
                                        ClassUtils.loadClass("org.apache.excalibur.source.SourceResolver"),
                                        ClassUtils.loadClass(Environment.class.getName()),
                                        ClassUtils.loadClass(ComponentManager.class.getName())});
                } catch (Exception e) {
                    throw new ProcessingException("The deprecated resolve() method of the environment was called."
                                                  +"Please either update your code to use the new resolveURI() method or"
                                                  +" install the deprecation support.", e);
                }
            }
        }

        try {
            org.apache.excalibur.source.Source source = resolveURI(systemId);
            Source wrappedSource = (Source)avalonToCocoonSourceWrapper.invoke(
                    clazz,
                    new Object[] {source, this.sourceResolver, this, this.manager});
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

    protected boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
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
        Deprecation.logger.warn("The method Environment.getOutputStream() " +
                              "is deprecated. Use getOutputStream(-1) instead.");
        // by default we use the complete buffering output stream
        return getOutputStream(-1);
    }

    /**
     * Get the output stream where to write the generated resource.
     * The returned stream is buffered by the environment. If the
     * buffer size is -1 then the complete output is buffered.
     * If the buffer size is 0, no buffering takes place.
     *
     * <br>This method replaces {@link #getOutputStream()}.
     */
    public OutputStream getOutputStream(int bufferSize)
    throws IOException {

        // This method could be called several times during request processing
        // with differing values of bufferSize and should handle this situation
        // correctly.

        if (bufferSize == -1) {
            if (this.secureOutputStream == null) {
                this.secureOutputStream = new BufferedOutputStream(this.outputStream);
            }
            return this.secureOutputStream;
        } else if (bufferSize == 0) {
            // Discard secure output stream if it was created before.
            if (this.secureOutputStream != null) {
                this.secureOutputStream = null;
            }
            return this.outputStream;
        } else {
            // FIXME Triple buffering, anyone?
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
            this.setContentLength(this.secureOutputStream.getCount());
            this.secureOutputStream.realFlush();
        } else if (this.outputStream != null) {
            this.outputStream.flush();
        }
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location)
    throws MalformedURLException, IOException, SourceException {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public org.apache.excalibur.source.Source resolveURI(final String location,
                                                         String baseURI,
                                                         final Map parameters)
    throws MalformedURLException, IOException, SourceException {
        if (!this.initializedComponents) {
            this.initComponents();
        }
        return this.sourceResolver.resolveURI(location, baseURI, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release(final org.apache.excalibur.source.Source source) {
        if (null != source) {
            this.sourceResolver.release(source);
        }
    }

    /**
     * Initialize the components for the environment
     * This gets the source resolver and the xmlizer component
     */
    protected void initComponents() {
        this.initializedComponents = true;
        try {
            this.manager = CocoonComponentManager.getSitemapComponentManager();
            this.sourceResolver = (org.apache.excalibur.source.SourceResolver)this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
            if (this.tempInitContext != null) {
                org.apache.excalibur.source.Source source = null;
                try {
                    source = this.sourceResolver.resolveURI(this.tempInitContext);
                    this.context = source.getURI();

                    if (this.rootContext == null) // hack for EnvironmentWrapper
                        this.rootContext = this.context;
                } finally {
                    this.sourceResolver.release(source);
                }
                this.tempInitContext = null;
            }
        } catch (ComponentException ce) {
            // this should never happen!
            throw new CascadingRuntimeException("Unable to lookup component.", ce);
        } catch (IOException ie) {
            throw new CascadingRuntimeException("Unable to resolve URI: "+this.tempInitContext, ie);
        }
    }

    /**
     * Notify that the processing starts.
     */
    public void startingProcessing() {
        // do nothing here
    }

    /**
     * Notify that the processing is finished
     * This can be used to cleanup the environment object
     */
    public void finishingProcessing() {
        if (null != this.manager) {
            this.manager.release(this.sourceResolver);
            this.manager = null;
            this.sourceResolver = null;
        }
        this.initializedComponents = false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isInternRedirect()
     */
    public boolean isInternalRedirect() {
        return false;
    }
}
