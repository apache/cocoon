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
package org.apache.cocoon.environment.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.BufferedOutputStream;


/**
 * This is a wrapper class for the <code>Environment</code> object.
 * It has the same properties except that the object model
 * contains a <code>RequestWrapper</code> object.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentWrapper.java,v 1.14 2003/10/30 12:31:05 cziegeler Exp $
 */
public class EnvironmentWrapper 
    extends AbstractEnvironment 
    implements Environment {

    /** The wrapped environment */
    protected Environment environment;

    /** The object model */
    protected Map objectModel;

    /** The redirect url */
    protected String redirectURL;

    /** The request object */
    protected Request request;

    /** The last uri */
    protected String lastURI;
    
    /** The last prefix */
    protected String lastPrefix;

    /** The stream to output to */
    protected OutputStream outputStream;

    /**
     * Constructs an EnvironmentWrapper object from a Request
     * and Response objects
     */
    public EnvironmentWrapper(Environment env,
                              String      requestURI,
                              String      queryString,
                              Logger      logger)
    throws MalformedURLException {
        this(env, requestURI, queryString, logger, false);
    }

    /**
     * Constructs an EnvironmentWrapper object from a Request
     * and Response objects
     */
    public EnvironmentWrapper(Environment env,
                              String      requestURI,
                              String      queryString,
                              Logger      logger,
                              boolean     rawMode)
    throws MalformedURLException {
        this(env, requestURI, queryString, logger, null, rawMode);
    }

    /**
     * Constructs an EnvironmentWrapper object from a Request
     * and Response objects
     */
    public EnvironmentWrapper(Environment      env,
                              String           requestURI,
                              String           queryString,
                              Logger           logger,
                              ServiceManager   manager,
                              boolean          rawMode)
    throws MalformedURLException {
        this(env, requestURI, queryString, logger, null, rawMode,env.getView());
    }
    
    /**
     * Constructs an EnvironmentWrapper object from a Request
     * and Response objects
     */
    public EnvironmentWrapper(Environment      env,
                              String           requestURI,
                              String           queryString,
                              Logger           logger,
                              ServiceManager manager,
                              boolean          rawMode,
                              String           view)
    throws MalformedURLException {
        super(env.getURI(), view, env.getAction());
        init(env, requestURI, queryString, logger, manager, rawMode, view);
    }

    private void init(Environment      env,
                              String           requestURI,
                              String           queryString,
                              Logger           logger,
                              ServiceManager manager,
                              boolean          rawMode,
                              String           view)
    throws MalformedURLException {

        this.enableLogging(logger);
        this.environment = env;
        this.view = view;

        // create new object model and replace the request object
        Map oldObjectModel = env.getObjectModel();
        if (oldObjectModel instanceof HashMap) {
            this.objectModel = (Map)((HashMap)oldObjectModel).clone();
        } else {
            this.objectModel = new HashMap(oldObjectModel.size()*2);
            Iterator entries = oldObjectModel.entrySet().iterator();
            Map.Entry entry;
            while (entries.hasNext()) {
                entry = (Map.Entry)entries.next();
                this.objectModel.put(entry.getKey(), entry.getValue());
            }
        }
        this.request = new RequestWrapper(ObjectModelHelper.getRequest(oldObjectModel),
                                          requestURI,
                                          queryString,
                                          this,
                                          rawMode);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, this.request);
    }
   
    public EnvironmentWrapper(Environment env, ServiceManager manager, String uri,  Logger logger)  throws MalformedURLException {
        super(env.getURI(), env.getView(), env.getAction());

        // FIXME(SW): code stolen from SitemapSource. Factorize somewhere...
        boolean rawMode = false;

        // remove the protocol
        int position = uri.indexOf(':') + 1;
        if (position != 0) {
//            this.protocol = uri.substring(0, position-1);
            // check for subprotocol
            if (uri.startsWith("raw:", position)) {
                position += 4;
                rawMode = true;
            }
        } else {
            throw new MalformedURLException("No protocol found for sitemap source in " + uri);
        }

        // does the uri point to this sitemap or to the root sitemap?
        String prefix;
        if (uri.startsWith("//", position)) {
            position += 2;
//            try {
//                this.processor = (Processor)this.manager.lookup(Processor.ROLE);
//            } catch (ComponentException e) {
//                throw new MalformedURLException("Cannot get Processor instance");
//            }
            prefix = ""; // start at the root
        } else if (uri.startsWith("/", position)) {
            position ++;
            prefix = null;
//            this.processor = CocoonComponentManager.getCurrentProcessor();
        } else {
            throw new MalformedURLException("Malformed cocoon URI: " + uri);
        }

        // create the queryString (if available)
        String queryString = null;
        int queryStringPos = uri.indexOf('?', position);
        if (queryStringPos != -1) {
            queryString = uri.substring(queryStringPos + 1);
            uri = uri.substring(position, queryStringPos);
        } else if (position > 0) {
            uri = uri.substring(position);
        }

        
        // determine if the queryString specifies a cocoon-view
        String view = null;
        if (queryString != null) {
            int index = queryString.indexOf(Constants.VIEW_PARAM);
            if (index != -1 
                && (index == 0 || queryString.charAt(index-1) == '&')
                && queryString.length() > index + Constants.VIEW_PARAM.length() 
                && queryString.charAt(index+Constants.VIEW_PARAM.length()) == '=') {
                
                String tmp = queryString.substring(index+Constants.VIEW_PARAM.length()+1);
                index = tmp.indexOf('&');
                if (index != -1) {
                    view = tmp.substring(0,index);
                } else {
                    view = tmp;
                }
            } else {
                view = env.getView();
            }
        } else {
            view = env.getView();
        }

        // build the request uri which is relative to the context
        String requestURI = (prefix == null ? env.getURIPrefix() + uri : uri);

//        // create system ID
//        this.systemId = queryString == null ?
//            this.protocol + "://" + requestURI :
//            this.protocol + "://" + requestURI + "?" + queryString;

        this.init(env, requestURI, queryString, logger, manager, rawMode, view);
        this.setURI((prefix == null ? env.getURIPrefix() : ""), uri);
        
    }

    /**
     * Redirect the client to a new URL is not allowed
     */
    public void redirect(String newURL, boolean global, boolean permanent)
    throws IOException {
        if ( !global ) {
            this.redirectURL = newURL;
        } else {
            this.environment.redirect(newURL, global, permanent);
        }
    }

    /**
     * Get the output stream
     */
    public OutputStream getOutputStream(int bufferSize)
    throws IOException {
        return this.outputStream == null
                ? this.environment.getOutputStream(bufferSize)
                : this.outputStream;
    }

    /**
     * Set the output stream for this environment. It hides the one of the
     * wrapped environment.
     */
    public void setOutputStream(OutputStream stream) {
        this.outputStream = stream;
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
        if (getOutputStream(0) != null
            && getOutputStream(0) instanceof BufferedOutputStream) {
            ((BufferedOutputStream)getOutputStream(0)).clearBuffer();
            return true;
        }
        else
          return super.tryResetResponse();
    }

    /**
     * Commit the response
     */
    public void commitResponse() 
    throws IOException {
        if (getOutputStream(0) != null
            && getOutputStream(0) instanceof BufferedOutputStream) {
            ((BufferedOutputStream)getOutputStream(0)).realFlush();
        }
        else
          super.commitResponse();
    }

    /**
     * if a redirect should happen this returns the url,
     * otherwise <code>null</code> is returned
     */
    public String getRedirectURL() {
        return this.redirectURL;
    }
    
    public void reset() {
        this.redirectURL = null;
    }

    /**
     * Set the StatusCode
     */
    public void setStatus(int statusCode) {
        // ignore this
    }

    public void setContentLength(int length) {
        // ignore this
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        // ignore this
    }

    /**
     * Get the ContentType
     */
    public String getContentType() {
        // ignore this
        return null;
    }

    /**
     * Get the underlying object model
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    /**
     * Change the current context to the last one set by changeContext()
     * and return last processor
     */
    public void changeToLastContext() {
        this.uris = this.lastURI;
        this.prefix = this.lastPrefix;
    }

    /**
     * Lookup an attribute in this instance, and if not found search it
     * in the wrapped environment.
     *
     * @param name a <code>String</code>, the name of the attribute to
     * look for
     * @return an <code>Object</code>, the value of the attribute or
     * null if no such attribute was found.
     */
    public Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        if (value == null)
            value = this.environment.getAttribute(name);

        return value;
    }

    /**
     * Remove attribute from the current instance, as well as from the
     * wrapped environment.
     *
     * @param name a <code>String</code> value
     */
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        this.environment.removeAttribute(name);
    }

    /**
     * Always return <code>false</code>.
     */
    public boolean isExternal() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setURI(java.lang.String)
     */
    public void setURI(String prefix, String value) {
        super.setURI(prefix, value);
        this.lastURI = this.uris;
        this.lastPrefix = this.prefix;
    }

}
