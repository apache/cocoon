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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;

/**
 * Experimental code for cleaning up the environment handling
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentHelper.java,v 1.3 2003/10/20 08:15:27 cziegeler Exp $
 * @since 2.2
 */

public class EnvironmentHelper
extends AbstractLogEnabled
implements SourceResolver, Serviceable, Disposable {

    /** The real source resolver */
    protected org.apache.excalibur.source.SourceResolver resolver;
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The current prefix to strip off from the request uri */
    protected String prefix;

     /** The Context path */
    protected String context;

    /** The root context path */
    protected String rootContext;

    /** The environment information */
    private static InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();

    /**
     * Constructor
     *
     */
    public EnvironmentHelper(String context) {
        this.context = context;
        this.rootContext = context;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (org.apache.excalibur.source.SourceResolver)
                          this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        if (this.context != null) {
            Source source = null;
            try {
                source = this.resolver.resolveURI(this.context);
                this.context = source.getURI();
                    
                if (this.rootContext == null) // hack for EnvironmentWrapper
                    this.rootContext = this.context;
            } catch (IOException ioe) {
                throw new ServiceException("Unable to resolve environment context. ", ioe);
            } finally {
                this.resolver.release(source);
            }
            this.context = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        this.resolver.release(source);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public Source resolveURI(final String location,
                             String baseURI,
                             final Map    parameters)
    throws MalformedURLException, IOException {
        return this.resolver.resolveURI(location, 
                                        (baseURI == null ? this.context : baseURI),
                                        parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public Source resolveURI(final String location)
    throws MalformedURLException, IOException {
        return this.resolveURI(location, null, null);
    }

    public void changeContext(Environment env) 
    throws ProcessingException {
        String uris = env.getURI();
        if ( this.prefix != null ) {
            if (!uris.startsWith(this.prefix)) {
                String message = "The current URI (" + uris +
                                 ") doesn't start with given prefix (" + prefix + ")";
                getLogger().error(message);
                throw new ProcessingException(message);
            }      
            // we don't need to check for slash at the beginning
            // of uris - the prefix always ends with a slash!
            final int l = this.prefix.length();
            uris = uris.substring(l);
            // TODO (CZ) Implement this in the environment
            // env.setURI(uris);
        }
    }
    
    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String newPrefix, String newContext)
    throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Changing Cocoon context");
            getLogger().debug("  from context(" + this.context + ") and prefix(" + this.prefix + ")");
            getLogger().debug("  to context(" + newContext + ") and prefix(" + newPrefix + ")");
        }
        int l = newPrefix.length();
        if (l >= 1) {
            if ( this.prefix == null ) {
                this.prefix = "";
            }
            final StringBuffer buffer = new StringBuffer(this.prefix);
            buffer.append(newPrefix);
            // check for a slash at the beginning to avoid problems with subsitemaps
            if ( buffer.charAt(buffer.length()-1) != '/') {
                buffer.append('/');
            }
            this.prefix = buffer.toString();
        }

        if (SourceUtil.getScheme(this.context).equals("zip")) {
            // if the resource is zipped into a war file (e.g. Weblogic temp deployment)
            // FIXME (VG): Is this still required? Better to unify both cases.
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Base context is zip: " + this.context);
            }
            
            org.apache.excalibur.source.Source source = null;
            try {
                source = this.resolver.resolveURI(this.context + newContext);
                this.context = source.getURI();
            } finally {
                this.resolver.release(source);
            }
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
                sContext = this.context + '/' + newContext;
            }

            // Cut the file name part from context (if present)
            int i = sContext.lastIndexOf('/');
            if (i != -1 && i + 1 < sContext.length()) {
                sContext = sContext.substring(0, i + 1);
            }
            
            Source source = null;
            try {
                source = this.resolver.resolveURI(sContext);
                this.context = source.getURI();
            } finally {
                this.resolver.release(source);
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New context is " + this.context);
        }
    }
    
    /**
     * This hook must be called by the sitemap each time a sitemap is entered
     * This method should never raise an exception, except when the
     * parameters are not set!
     */
    public static void enterProcessor(Processor processor) {
        if ( null == processor) {
            throw new IllegalArgumentException("Processor is not set.");
        }

        EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack == null) {
            stack = new EnvironmentStack();
            environmentStack.set(stack);
        }
        stack.push(new Object[] {processor, new Integer(stack.getOffset())});
        stack.setOffset(stack.size()-1);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     * It's the counterpart to {@link #enterProcessor(Processor)}.
     */
    public static void leaveProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final Object[] objs = (Object[])stack.pop();
        stack.setOffset(((Integer)objs[1]).intValue());
    }

    /**
     * Create an environment aware xml consumer for the cocoon
     * protocol
     */
    public static XMLConsumer createEnvironmentAwareConsumer(XMLConsumer consumer) {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final Object[] objs = (Object[])stack.getCurrent();
        return stack.getEnvironmentAwareConsumerWrapper(consumer, ((Integer)objs[1]).intValue());
    }
}

final class CloningInheritableThreadLocal
    extends InheritableThreadLocal {

    /**
     * Computes the child's initial value for this InheritableThreadLocal
     * as a function of the parent's value at the time the child Thread is
     * created.  This method is called from within the parent thread before
     * the child is started.
     * <p>
     * This method merely returns its input argument, and should be overridden
     * if a different behavior is desired.
     *
     * @param parentValue the parent thread's value
     * @return the child thread's initial value
     */
    protected Object childValue(Object parentValue) {
        if ( null != parentValue) {
            return ((EnvironmentStack)parentValue).clone();
        } else {
            return null;
        }
    }
}

