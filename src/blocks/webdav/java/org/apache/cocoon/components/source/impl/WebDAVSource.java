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
package org.apache.cocoon.components.source.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.URIException;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A source implementation to get access to WebDAV repositories.
 * 
 * <h2>Protocol syntax</h2>
 * <p><code>webdav://[usr[:password]@]host[:port][/path][?cocoon:webdav-depth][&cocoon:webdav-action]</code></p>
 * <p>
 *  <ul>
 *   <li>
 *    <code>cocoon:webdav-depth</code> allows to specify the default depth
 *    to use during initialization of the webdav resource.
 *   </li>
 *   <li>
 *    <code>cocoon:webdav-action</code> allows to specify a default action
 *    to take upon initialization of the webdav resource.
 *   </li>
 *  </ul>
 * <p>
 * 
 * @version $Id: WebDAVSource.java,v 1.27 2004/04/13 14:20:35 stephan Exp $
*/
public class WebDAVSource extends AbstractLogEnabled 
implements Source, TraversableSource, ModifiableSource, ModifiableTraversableSource, InspectableSource {

    private static final String NAMESPACE = "http://apache.org/cocoon/webdav/1.0";

    private static final String PREFIX = "webdav";
    private static final String RESOURCE_NAME = "resource";
    private static final String COLLECTION_NAME = "collection";
    
    // the http url
    private final HttpURL url;
    
    // the scheme name
    private final String protocol;
    
    // cached uri and secureUri values
    private String uri;
    private String secureUri;
    
    // the SWCL resource
    private WebdavResource resource = null;
    
    // current resource initialization values
    private int depth = -1;
    private int action = -1;

    /**
     * Default constructor.
     */
    private WebDAVSource(HttpURL url, String protocol) throws URIException {
        this.protocol = protocol;
        this.url = url;
        
        String qs = url.getQuery();
        if (qs != null) {
            final SourceParameters sp = new SourceParameters(qs);
            
            // parse optional start depth and start action qs parameters
            this.depth = sp.getParameterAsInteger("cocoon:webdav-depth", DepthSupport.DEPTH_1);
            this.action = sp.getParameterAsInteger("cocoon:webdav-action", WebdavResource.NOACTION);
            
            // [UH] FIXME: Why this alternative way of passing in credentials?
            String principal = url.getUser();
            String password = url.getPassword();
            if (principal == null || password == null) {
                principal = sp.getParameter("cocoon:webdav-principal", principal);
                password = sp.getParameter("cocoon:webdav-password", password);
                if (principal != null) {
                    url.setUser(principal);
                    url.setPassword(password);
                }
            }

            sp.removeParameter("cocoon:webdav-depth");
            sp.removeParameter("cocoon:webdav-action");
            sp.removeParameter("cocoon:webdav-principal");
            sp.removeParameter("cocoon:webdav-password");
            
            // set the qs without WebdavSource specific parameters
            url.setQuery(sp.getQueryString());
        }
    }

    /**
     * Constructor used by getChildren() method.
     */
    private WebDAVSource (WebdavResource resource, HttpURL url, String protocol) 
    throws URIException {
        this(url, protocol);
        this.resource = resource;
    }
    
    /**
     * Initialize the SWCL WebdavResource.
     * <p>
     * The action argument specifies a set of properties to load during initialization. 
     * Its value is one of WebdavResource.NOACTION, WebdavResource.NAME,
     * WebdavResource.BASIC, WebdavResource.DEFAULT, WebdavResource.ALL.
     * Similarly the depth argument specifies the depth header of the PROPFIND 
     * method that is executed upon initialization.
     * </p>
     * <p>
     * The different methods of this Source implementation call this method to 
     * initialize the resource using their minimal action and depth requirements.
     * For instance the WebDAVSource.getMimeType() method requires WebdavResource.BASIC
     * properties and a search depth of 0 is sufficient.
     * </p>
     * <p>
     * However it may be that a later call (eg. WebDAVSource.getChildren()) requires more
     * information. In that case the WebdavResource would have to make another call to the Server.
     * It would be more efficient if previous initialization had been done using depth 1 instead.
     * In order give the user more control over this the WebDAVSource can be passed a minimal
     * action and depth using cocoon:webdav-depth and cocoon:webdav-action query string parameters.
     * By default the mimimum action is WebdavResource.BASIC (which loads all the following basic 
     * webdav properties: DAV:displayname, DAV:getcontentlength, DAV:getcontenttype DAV:resourcetype,
     * DAV:getlastmodified and DAV:lockdiscovery). The default minimum depth is 1.
     * </p>
     * 
     * @param action  the set of propterties the WebdavResource should load.
     * @param depth  the webdav depth.
     * @throws SourceException
     * @throws SourceNotFoundException
     */
    private void initResource(int action, int depth) throws SourceException, SourceNotFoundException {
        try {
            boolean update = false;
            if (action != WebdavResource.NOACTION) {
                if (action > this.action) {
                    this.action = action;
                    update = true;
                } else {
                    action = this.action;
                }
            }
            if (depth > this.depth) {
                this.depth = depth;
                update = true;
            } else {
                depth = this.depth;
            }
            if (this.resource == null) {
                this.resource = new WebdavResource(this.url, action, depth);
            } else if (update) {
                this.resource.setProperties(action, depth);
            }
            if (this.action > WebdavResource.NOACTION) {
                if (this.resource.isCollection()) {
                    String path = this.url.getPath();
                    if (path.charAt(path.length()-1) != '/') {
                        this.url.setPath(path + "/");
                    }
                }
            }
       } catch (HttpException e) {
            if (e.getReasonCode() == HttpStatus.SC_NOT_FOUND) {
                throw new SourceNotFoundException("Not found: " + getSecureURI(), e);
            }
            final String msg = "Could not initialize webdav resource. Server responded " 
                + e.getReasonCode() + " (" + e.getReason() + ") - " + e.getMessage();
            throw new SourceException(msg, e);
       } catch (IOException e) {
            throw new SourceException("Could not initialize webdav resource", e);
       }
    }

    /**
     * Static factory method to obtain a Source.
     */
    public static WebDAVSource newWebDAVSource(HttpURL url,
                                               String protocol,
                                               Logger logger) 
    throws URIException {
        final WebDAVSource source = new WebDAVSource(url, protocol);
        source.enableLogging(logger);
        return source;
    }
    
    /**
     * Static factory method to obtain a Source.
     */
    private static WebDAVSource newWebDAVSource(WebdavResource resource,
                                                HttpURL url,
                                                String protocol,
                                                Logger logger) 
    throws URIException {
        final WebDAVSource source = new WebDAVSource(resource, url, protocol);
        source.enableLogging(logger);
        return source;
    }
    
    // ---------------------------------------------------- Source implementation
    
    /**
     * Get the scheme for this Source.
     */
    public String getScheme() {
        return this.protocol;
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        if (this.uri == null) {
            String uri = this.url.toString();
            final int index = uri.indexOf("://");
            if (index != -1) {
                uri = uri.substring(index+3);
            }
            final String userinfo = this.url.getEscapedUserinfo();
            if (userinfo != null) {
                uri = this.protocol + "://" + userinfo + "@" + uri;
            } else {
                uri = this.protocol + "://" + uri;
            }
            this.uri = uri;
        }
        return this.uri;
    }
    
    /**
     * Return the URI securely, without username and password
     */
    protected String getSecureURI() {
        if (this.secureUri == null) {
            String uri = this.url.toString();
            int index = uri.indexOf("://");
            if (index != -1) {
                uri = uri.substring(index+3);
            }
            uri = this.protocol + "://" + uri;
            this.secureUri = uri;
        }
        return this.secureUri;
    }
    
    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        final long lm = getLastModified();
        if (lm > 0) {
            return new TimeStampValidity(lm);
        }
        return null;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {
        this.resource = null;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     * This is the data at the point of invocation of this method,
     * so if this is Modifiable, you might get different content
     * from two different invocations.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        try {
            if (this.resource.isCollection()) {
                // [UH] FIXME: why list collection as XML here?
                // I think its a concern for the TraversableGenerator.
                WebdavResource[] resources = this.resource.listWebdavResources();
                return resourcesToXml(resources);
            } else {
                BufferedInputStream bi = null;
                bi = new BufferedInputStream(this.resource.getMethodData());
                if (!this.resource.exists()) {
                    throw new HttpException(getSecureURI() + " does not exist");
                }
                return bi;
            }
        } catch (HttpException he) {
            throw new SourceException("Could not get WebDAV resource " + getSecureURI(), he);
        } catch (Exception e) {
            throw new SourceException("Could not get WebDAV resource" + getSecureURI(), e);
        }
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be <code>null</code>.
     */
    public String getMimeType() {
        try {
            initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        } catch (IOException e) {
            return null;
        }
        return this.resource.getGetContentType();
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
        try {
            initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        }
        catch(IOException e) {
            return -1;
        }
        if (this.resource.isCollection()) {
            return -1;
        }
        return this.resource.getGetContentLength();
    }

    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        try {
            initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        } catch(IOException e) {
            return 0;
        }
        return this.resource.getGetLastModified();
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        try {
            initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        } catch (SourceNotFoundException e) {
            return false;
        } catch(IOException e) {
            return true;
        }
        return this.resource.getExistence();
    }

    private InputStream resourcesToXml(WebdavResource[] resources)
        throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        TransformerHandler th =
            ((SAXTransformerFactory) tf).newTransformerHandler();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(bOut);
        th.setResult(result);
        th.startDocument();
        th.startPrefixMapping(PREFIX, NAMESPACE);
        th.startElement(NAMESPACE, COLLECTION_NAME,
                        PREFIX + ":" + COLLECTION_NAME, new AttributesImpl());
        this.resourcesToSax(resources, th);
        th.endElement(NAMESPACE, COLLECTION_NAME,
                      PREFIX + ":" + COLLECTION_NAME);
        th.endPrefixMapping(PREFIX);
        th.endDocument();
        return new ByteArrayInputStream(bOut.toByteArray());
    }

    private void resourcesToSax(
        WebdavResource[] resources,
        ContentHandler handler)
        throws SAXException {
        for (int i = 0; i < resources.length; i++) {
            if (getLogger().isDebugEnabled()) {
                final String message =
                    "RESOURCE: " + resources[i].getDisplayName();
                getLogger().debug(message);
            }
            if (resources[i].isCollection()) {
                try {
                    WebdavResource[] childs =
                        resources[i].listWebdavResources();
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute(
                        NAMESPACE,
                        COLLECTION_NAME,
                        PREFIX + ":name",
                        "CDATA",
                        resources[i].getDisplayName());
                    handler.startElement(
                        NAMESPACE,
                        COLLECTION_NAME,
                        PREFIX + ":" + COLLECTION_NAME,
                        attrs);
                    this.resourcesToSax(childs, handler);
                    handler.endElement(
                        NAMESPACE,
                        COLLECTION_NAME,
                        PREFIX + ":" + COLLECTION_NAME);
                } catch (HttpException e) {
                    if (getLogger().isDebugEnabled()) {
                        final String message =
                            "Unable to get WebDAV children. Server responded " +
                            e.getReasonCode() + " (" + e.getReason() + ") - " 
                            + e.getMessage();
                        getLogger().debug(message);
                    }
                } catch (SAXException e) {
                    if (getLogger().isDebugEnabled()) {
                        final String message =
                            "Unable to get WebDAV children: " 
                            + e.getMessage();
                        getLogger().debug(message,e);
                    }
                } catch (IOException e) {
                    if (getLogger().isDebugEnabled()) {
                        final String message =
                            "Unable to get WebDAV children: " 
                            + e.getMessage();
                        getLogger().debug(message,e);
                    }
                } catch (Exception e) {
                    if (getLogger().isDebugEnabled()) {
                        final String message =
                            "Unable to get WebDAV children: " 
                            + e.getMessage();
                        getLogger().debug(message,e);
                    }
                }
            } else {
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute(
                    NAMESPACE,
                    "name",
                    PREFIX + ":name",
                    "CDATA",
                    resources[i].getDisplayName());
                handler.startElement(
                    NAMESPACE,
                    RESOURCE_NAME,
                    PREFIX + ":" + RESOURCE_NAME,
                    attrs);
                handler.endElement(
                    NAMESPACE,
                    RESOURCE_NAME,
                    PREFIX + ":" + RESOURCE_NAME);
            }
        }
    }

    // ---------------------------------------------------- TraversableSource implementation
    
    /**
     * Get a collection child.
     *
     * @see org.apache.excalibur.source.TraversableSource#getChild(java.lang.String)
     */
    public Source getChild(String childName) throws SourceException {
        if (!isCollection()) {
            throw new SourceException(getSecureURI() + " is not a collection.");
        }
        try {
            HttpURL childURL;
            if (this.url instanceof HttpsURL) {
                childURL = new HttpsURL((HttpsURL) this.url, childName);
            } else {
                childURL = new HttpURL(this.url, childName);
            }
            return WebDAVSource.newWebDAVSource(childURL, this.protocol, getLogger());
        } catch (URIException e) {
            throw new SourceException("Failed to create child", e);
        }        
    }

    /**
     * Get the collection children.
     *
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        initResource(WebdavResource.BASIC, DepthSupport.DEPTH_1);
        ArrayList children = new ArrayList();
        try {
            WebdavResource[] resources = this.resource.listWebdavResources();
            for (int i = 0; i < resources.length; i++) {
                HttpURL childURL;
                if (this.url instanceof HttpsURL) {
                    childURL = new HttpsURL((HttpsURL) this.url,resources[i].getName());
                } else {
                    childURL = new HttpURL(this.url,resources[i].getName());
                }
                WebDAVSource src = WebDAVSource.newWebDAVSource(resources[i],
                                                                childURL,
                                                                this.protocol,
                                                                getLogger());
                src.enableLogging(getLogger());
                children.add(src);
            }
        } catch (HttpException e) {
            if (getLogger().isDebugEnabled()) {
                final String message =
                    "Unable to get WebDAV children. Server responded " +
                    e.getReasonCode() + " (" + e.getReason() + ") - " 
                    + e.getMessage();
                getLogger().debug(message);
            }
            throw new SourceException("Failed to get WebDAV collection children.", e);
        } catch (SourceException e) {
            throw e;
        } catch (IOException e) {
            throw new SourceException("Failed to get WebDAV collection children.", e);
        }
        return children;
    }

    /**
     * Get the name of this resource.
     * @see org.apache.excalibur.source.TraversableSource#getName()
     */
    public String getName() {
        try {
            initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        }
        catch (IOException e) {
            return "";
        }
        return this.resource.getName();
    }

    /**
     * Get the parent.
     * 
     * @see org.apache.excalibur.source.TraversableSource#getParent()
     */
    public Source getParent() throws SourceException {
        String path = isCollection()?"..":".";
      
        try {
            HttpURL parentURL;
            if (url instanceof HttpsURL) {
                parentURL = new HttpsURL((HttpsURL) this.url, path);
            } else {
                parentURL = new HttpURL(this.url, path);
            }
            return WebDAVSource.newWebDAVSource(parentURL, this.protocol, getLogger());
        } catch (URIException e) {
            throw new SourceException("Failed to create parent", e);
        }
    }

    /**
     * Check if this source is a collection.
     * @see org.apache.excalibur.source.TraversableSource#isCollection()
     */
    public boolean isCollection() {
        try {
            initResource(WebdavResource.BASIC, DepthSupport.DEPTH_0);
        }
        catch (IOException e) {
            return false;
        }
        return this.resource.isCollection();
    }
    
    // ---------------------------------------------------- ModifiableSource implementation
    
    /**
     * Get an <code>OutputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * @return a stream to write to
     */
    public OutputStream getOutputStream() throws IOException {
        return new WebDAVSourceOutputStream(this);
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        if (stream instanceof WebDAVSourceOutputStream) {
            WebDAVSourceOutputStream wsos = (WebDAVSourceOutputStream) stream;
            if (wsos.source == this) {
                return wsos.canCancel();
            }
        }
        throw new IllegalArgumentException("The stream is not associated to this source");
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     */
    public void cancel(OutputStream stream) throws SourceException {
        if (stream instanceof WebDAVSourceOutputStream) {
            WebDAVSourceOutputStream wsos = (WebDAVSourceOutputStream) stream;
            if (wsos.source == this) {
                try {
                    wsos.cancel();
                }
                catch (Exception e) {
                    throw new SourceException("Failure cancelling Source", e);
                }
            }
        }
        throw new IllegalArgumentException("The stream is not associated to this source");
    }

    /** 
     * Delete this source (unimplemented).
     * @see org.apache.excalibur.source.ModifiableSource#delete()
     */
    public void delete() throws SourceException {
      initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        try {
            this.resource.deleteMethod();
        } catch (HttpException e) {
            throw new SourceException("Unable to delete source: " + getSecureURI(), e);
        } catch (IOException e) {
            throw new SourceException("Unable to delete source: " + getSecureURI(), e);
        }
    }

    private static class WebDAVSourceOutputStream extends ByteArrayOutputStream {

        private WebDAVSource source = null;
        private boolean isClosed = false;

        private WebDAVSourceOutputStream(WebDAVSource source) {
            this.source = source;
        }

        public void close() throws IOException {
            if (!isClosed) {
                try {
                    super.close();
                    this.source.initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
                    this.source.resource.putMethod(toByteArray());
                } catch (HttpException he) {
                    final String message =
                        "Unable to close output stream. Server responded " +
                        he.getReasonCode() + " (" + he.getReason() + ") - " 
                        + he.getMessage();
                    this.source.getLogger().debug(message);
                    throw new IOException(he.getMessage());
                }
                finally {
                    this.isClosed = true;
                }
            }
        }
        
        private boolean canCancel() {
            return !isClosed;
        }
        
        private void cancel() {
            if (isClosed) {
                throw new IllegalStateException("Cannot cancel: outputstream is already closed");
            }
            this.isClosed = true;
        }
    }
    
    // ---------------------------------------------------- ModifiableTraversableSource implementation
    
    /**
     * Create the collection, if it doesn't exist.
     * @see org.apache.excalibur.source.ModifiableTraversableSource#makeCollection()
     */
    public void makeCollection() throws SourceException {
        initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        if (this.resource.exists()) return;
        try {
            if (!this.resource.mkcolMethod()) {
                int status = this.resource.getStatusCode();
                // Ignore status 405 - Not allowed: collection already exists
                if (status != 405) {
                    final String msg = 
                        "Unable to create collection " + getSecureURI()
                        + ". Server responded " + this.resource.getStatusCode()
                        + " (" + this.resource.getStatusMessage() + ")";
                    throw new SourceException(msg);
                }
            }
        } catch (HttpException e) {
            throw new SourceException("Unable to create collection(s) " + getSecureURI(), e);
        } catch (SourceException e) {
            throw e;
        } catch (IOException e) {
            throw new SourceException("Unable to create collection(s)"  + getSecureURI(), e);      
        }
    }
    
    // ---------------------------------------------------- InspectableSource implementation
    
    /**
     * Returns a enumeration of the properties
     *
     * @return Enumeration of SourceProperty
     *
     * @throws SourceException If an exception occurs.
     */
     public SourceProperty[] getSourceProperties() throws SourceException {
         
         initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
         
         Vector sourceproperties = new Vector();
         Enumeration props= null;
         org.apache.webdav.lib.Property prop = null;
         
         try {
             Enumeration responses = this.resource.propfindMethod(0);
             while (responses.hasMoreElements()) {

                 ResponseEntity response = (ResponseEntity)responses.nextElement();
                 props = response.getProperties();
                 while (props.hasMoreElements()) {
                     prop = (Property) props.nextElement();
                     SourceProperty srcProperty = new SourceProperty(prop.getElement());
                     sourceproperties.addElement(srcProperty);
                 }
             }

         } catch (Exception e) {
             throw new SourceException("Error getting properties", e);
         }
         SourceProperty[] sourcepropertiesArray = new SourceProperty[sourceproperties.size()];
         for (int i = 0; i<sourceproperties.size(); i++) {
             sourcepropertiesArray[i] = (SourceProperty) sourceproperties.elementAt(i);
         }
         return sourcepropertiesArray;
    }

    /**
     * Returns a property from a source.
     *
     * @param namespace Namespace of the property
     * @param name Name of the property
     *
     * @return Property of the source.
     *
     * @throws SourceException If an exception occurs.
     */
    public SourceProperty getSourceProperty (String namespace, String name) throws SourceException {
        
        initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        
        Vector propNames = new Vector(1);
        propNames.add(new PropertyName(namespace,name));
        Enumeration props= null;
        org.apache.webdav.lib.Property prop = null;
        try {
            Enumeration responses = this.resource.propfindMethod(0, propNames);
            while (responses.hasMoreElements()) {
                ResponseEntity response = (ResponseEntity) responses.nextElement();
                props = response.getProperties();
                if (props.hasMoreElements()) {
                    prop = (Property) props.nextElement();
                    return new SourceProperty(prop.getElement());
                }
            }
        } catch (Exception e) {
            throw new SourceException("Error getting property: "+name, e);
        }
        return null;
    }

    /**
     * Remove a specified source property.
     *
     * @param namespace Namespace of the property.
     * @param name Name of the property.
     *
     * @throws SourceException If an exception occurs.
     */
    public void removeSourceProperty(String namespace, String name)
    throws SourceException {
        
        initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        
        try {
            this.resource.proppatchMethod(new PropertyName(namespace, name), "", false);
        } catch (Exception e) {
            throw new SourceException("Could not remove property ", e);
        }
    }

    /**
     * Sets a property for a source.
     *
     * @param sourceproperty Property of the source
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void setSourceProperty(SourceProperty sourceproperty) throws SourceException {
        
        initResource(WebdavResource.NOACTION, DepthSupport.DEPTH_0);
        
        try {
            Node node = null;
            NodeList list = sourceproperty.getValue().getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                if ((list.item(i) instanceof Text && !"".equals(list.item(i).getNodeValue()))
                    || list.item(i) instanceof Element) {
                    
                    node = list.item(i);
                    break;
                }
            }

            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            String prop = XMLUtils.serializeNode(node, format);
            
            this.resource.proppatchMethod(
                   new PropertyName(sourceproperty.getNamespace(),sourceproperty.getName()),
                   prop, true);

        } catch(HttpException e) {
            final String message =
                "Unable to set property. Server responded " +
                e.getReasonCode() + " (" + e.getReason() + ") - " 
                + e.getMessage();
            getLogger().debug(message);
            throw new SourceException("Could not set property ", e);
        } catch (Exception e) {
            throw new SourceException("Could not set property ", e);
        }
    }
    
    /** 
     * Get the current credential for the source
     */
//    public SourceCredential getSourceCredential() throws SourceException {
//        if (this.principal != null) {
//            return new SourceCredential(this.principal, this.password);
//        }
//        return null;
//    }

    /** 
     * Set the credential for the source
     */
//    public void setSourceCredential(SourceCredential sourcecredential)
//        throws SourceException {
//        if (sourcecredential != null) {
//            this.password = sourcecredential.getPassword();
//            this.principal = sourcecredential.getPrincipal();
//            refresh();
//        }
//    }
}
