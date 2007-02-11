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
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ArrayList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.httpclient.HttpException;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.ResponseEntity;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *  A source implementation to get access to WebDAV repositories. Use it
 *  as webdav://[usr]:[password]@[host][:port]/path.
 *
 *  @author <a href="mailto:g.casper@s-und-n.de">Guido Casper</a>
 *  @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 *  @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 *  @version $Id: WebDAVSource.java,v 1.20 2004/03/05 13:02:26 bdelacretaz Exp $
*/
public class WebDAVSource extends AbstractLogEnabled implements Source,
    RestrictableSource, ModifiableTraversableSource, InspectableSource {


    private static final String NAMESPACE = "http://apache.org/cocoon/webdav/1.0";

    private static final String PREFIX = "webdav";

    private static final String RESOURCE_NAME = "resource";

    private static final String COLLECTION_NAME = "collection";

    static {
        WebdavResource.setGetUseDisk(false);
    }
    
    private String systemId;
    
    private String location;
    private String principal;
    private String password;

    private SourceValidity validity = null;
    private long cachedLastModificationDate;
    private SourceCredential sourcecredential = null;

    private WebdavResource resource = null;
    private String protocol;

    private WebDAVSource(
        String location,
        String principal,
        String password,
        String protocol,
        boolean createNew)
        throws HttpException, IOException {
            
        this.location = location;
        this.principal = principal;
        this.password = password;
        this.protocol = protocol;
        
        this.systemId = "http://" + location;
        
        HttpURL httpURL = new HttpURL(this.systemId);
        httpURL.setUserInfo(principal, password);
        
        if (createNew) {
            this.resource = new WebdavResource(httpURL, 
                WebdavResource.NOACTION, 
                DepthSupport.DEPTH_1);
        }
        else {
            this.resource = new WebdavResource(httpURL);
        }
        
    }

    /**
     * Static factory method to obtain a Source.
     */
    public static WebDAVSource newWebDAVSource(String location,
                                               String principal,
                                               String password,
                                               String protocol,
                                               Logger logger) throws SourceException {
        // FIXME: wild hack needed for writing to a new resource.
        // if a resource doesn't exist, an exception
        // will be thrown, unless such resource isn't created with the
        // WebdavResouce.NOACTION flag. However, such flag cannot be
        // used by default, since it won't allow to discover the
        // properties of an exixting resource. So either we do this
        // hack here or we fill properties on the fly when requested.
        // This "solution" is scary, but the SWCL is pretty dumb.
        WebDAVSource source;
        try {
            source = new WebDAVSource(location, principal, password, protocol, false);
        }  catch (HttpException he) {
            try {
                source = new WebDAVSource(location, principal, password, protocol, true);
            } catch (HttpException finalHe) {
                final String message = "Error creating the source.";
                throw new SourceException(message, finalHe);
            } catch (IOException e) {
                final String message = "Error creating the source.";
                throw new SourceException(message, e);
            }
        } catch (IOException e) {
            final String message = "Error creating the source.";
            throw new SourceException(message, e);
        }
        source.enableLogging(logger);
        return source;
    }

    /**
     * Constructor used by the Traversable methods to build children.
     */
    private WebDAVSource (WebdavResource source)
    throws HttpException, IOException {
    	this.resource = source;
    	this.systemId = source.getHttpURL().getURI();

    	//fix trailing slash
        if (this.resource.isCollection() && (this.systemId.endsWith("/") == false)) {
            this.systemId = this.systemId+"/";
            HttpURL httpURL = new HttpURL(this.systemId);
            this.resource.setHttpURL(httpURL);
        }
    }

    /**
     * Get the scheme for this Source (webdav://).
     */

    public String getScheme() {
        return this.protocol;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     * This is the data at the point of invocation of this method,
     * so if this is Modifiable, you might get different content
     * from two different invocations.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            if (this.resource.isCollection()) {
                WebdavResource[] resources =
                    this.resource.listWebdavResources();
                return resourcesToXml(resources);
            } else {
                BufferedInputStream bi = null;
                bi = new BufferedInputStream(this.resource.getMethodData());
                if (!this.resource.exists()) {
                    throw new HttpException(this.systemId + " does not exist");
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
     * Return the unique identifer for this source
     */
    public String getURI() {
        // Change http: to webdav:
        //return "webdav:" + this.systemId.substring(5);
        //add Source credentials
        if (principal != null)
            return "webdav://" + this.principal + ":" + this.password + "@" +  this.systemId.substring(7);
        else
            return "webdav://"  +  this.systemId.substring(7);
    }
    
    /**
     * Return the URI securely, without username and password
     * 
     */
    protected String getSecureURI() {
		return "webdav://"  +  this.systemId.substring(7);    	
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
    	// TODO: Implementation taken from HttpClientSource, who took it from URLSource: time for a separate impl?
		final long lm = getLastModified();

		if ( lm > 0 )
		{
			if ( lm == cachedLastModificationDate )
			{
				return validity;
			}

			cachedLastModificationDate = lm;
			validity = new TimeStampValidity( lm );
			return validity;
		}
		return null;
    }

    /**
     * Refresh the content of this object after the underlying data
     * content has changed.
     */
    public void refresh() {
        try {
            this.resource = new WebdavResource(this.systemId);

            if (sourcecredential != null)
                resource.setUserInfo(
                    sourcecredential.getPrincipal(),
                    sourcecredential.getPassword());
        } catch (HttpException he) {
            throw new IllegalStateException(he.getMessage());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe.getMessage());
        }

        this.validity = null;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be <code>null</code>.
     */
    public String getMimeType() {
        return this.resource.getGetContentType();
    }

    /**
     * Return the content length of the content or -1 if the length is
     * unknown
     */
    public long getContentLength() {
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
        return this.resource.getGetLastModified();
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return -1;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return null;
    }

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    public boolean exists() {
        return this.resource.getExistence();
    }

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * @return a stream to write to
     */
    public OutputStream getOutputStream() throws IOException {
        return new WebDAVSourceOutputStream(this.resource);
    }

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    public boolean canCancel(OutputStream stream) {
        return true;
    }

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     */
    public void cancel(OutputStream stream) throws SourceException {
        // The content will only be send, if outputstream.close() executed.
    }

    /** 
     * Get the current credential for the source
     */
    public SourceCredential getSourceCredential() throws SourceException {
        return this.sourcecredential;
    }

    /** 
     * Set the credential for the source
     */
    public void setSourceCredential(SourceCredential sourcecredential)
        throws SourceException {
        this.sourcecredential = sourcecredential;
        if (sourcecredential == null) return;
        try {
            HttpURL httpURL = new HttpURL(this.systemId);
            httpURL.setUserInfo(
                sourcecredential.getPrincipal(),
                sourcecredential.getPassword());
            this.resource = new WebdavResource(httpURL);
        } catch (HttpException he) {
            throw new SourceException("Could not set credentials", he);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe.getMessage());
        }
    }

    /**
     * Set a permission to this source
     *
     * @param sourcepermission Permission, which should be set
     *
     * @throws SourceException If an exception occurs during this operation
     */
    public void setSourcePermission(SourcePermission sourcepermission)
        throws SourceException {
        //FIXME
    }

    /**
     * Returns a list of the existing permissions
     *
     * @return Array of SourcePermission
     */
    public SourcePermission[] getSourcePermissions() throws SourceException {
        //FIXME
        return null;
    }

    public class WebDAVSourceOutputStream extends ByteArrayOutputStream {

        private WebdavResource resource = null;

        protected WebDAVSourceOutputStream(WebdavResource resource) {
            this.resource = resource;
            WebdavResource.setGetUseDisk(false);
        }

        public void close() throws IOException {
            super.close();

            try {
                this.resource.putMethod(toByteArray());
            } catch (HttpException he) {
                final String message =
                    "Unable to close output stream. Server responded " +
                    he.getReasonCode() + " (" + he.getReason() + ") - " 
                    + he.getMessage();
                getLogger().debug(message);
                throw new IOException(he.getMessage());
            }
        }
    }

    /**
     * Add a permission to this source
     *
     * @param sourcepermission Permission, which should be set
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    public void addSourcePermission(SourcePermission sourcepermission)
        throws SourceException {
        // FIXME
    }

    /**
     * Remove a permission from this source
     *
     * @param sourcepermission Permission, which should be removed
     *
     * @throws SourceException If an exception occurs during this operation
     **/
    public void removeSourcePermission(SourcePermission sourcepermission)
        throws SourceException {
        // FIXME
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

    /**
     * Get a collection child.
     *
     * @see org.apache.excalibur.source.TraversableSource#getChild(java.lang.String)
     */
    public Source getChild(String childName) throws SourceException {    	
        String childLocation = this.location + "/" + childName;
        WebDAVSource source = WebDAVSource.newWebDAVSource(
            childLocation, 
            this.principal, 
            this.password, 
            this.protocol, 
            this.getLogger());
        source.setSourceCredential(this.getSourceCredential());
        return source;
    }

    /**
     * Get the collection children.
     *
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        ArrayList children = new ArrayList();
        try {
            WebdavResource[] resources = this.resource.listWebdavResources();
            for (int i = 0; i < resources.length; i++) {
                WebDAVSource src = new WebDAVSource(resources[i]);
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
        return this.resource.getDisplayName();
    }

    /**
     * Get the parent.
     * @see org.apache.excalibur.source.TraversableSource#getParent()
     */
    public Source getParent() throws SourceException {
        int last = this.location.lastIndexOf("/");
        String myLocation = this.location.substring(0, last);
        WebDAVSource wds = WebDAVSource.newWebDAVSource(
                myLocation,
                this.principal,
                this.password,
                this.protocol,
                this.getLogger());
        wds.setSourceCredential(this.getSourceCredential());
        return wds;
    }

    /**
     * Check if this source is a collection.
     * @see org.apache.excalibur.source.TraversableSource#isCollection()
     */
    public boolean isCollection() {
        return this.resource.isCollection();
    }

    /** 
     * Delete this source (unimplemented).
     * @see org.apache.excalibur.source.ModifiableSource#delete()
     */
    public void delete() throws SourceException {
    	try {
            this.resource.deleteMethod();
        } catch (HttpException e) {
        	throw new SourceException("Unable to delete source: " + getSecureURI(), e);
        } catch (IOException e) {
			throw new SourceException("Unable to delete source: " + getSecureURI(), e);
        }
    }

    /**
     * Create the collection, if it doesn't exist.
     * @see org.apache.excalibur.source.ModifiableTraversableSource#makeCollection()
     */
    public void makeCollection() throws SourceException {
    	if (resource.exists()) return;
    	try {
            resource.mkcolMethod();
        } catch (HttpException e) {
            throw new SourceException("Unable to create collection(s) " + getSecureURI(), e);
        } catch (IOException e) {
            throw new SourceException("Unable to create collection(s)"  + getSecureURI(), e);			
        }
    }
    
    /**
     * Returns a enumeration of the properties
     *
     * @return Enumeration of SourceProperty
     *
     * @throws SourceException If an exception occurs.
     */
     public SourceProperty[] getSourceProperties() throws SourceException {

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
    public SourceProperty getSourceProperty (String namespace, String name)
    throws SourceException {

          Vector propNames = new Vector(1);
          propNames.add(new PropertyName(namespace,name));
          Enumeration props= null;
          org.apache.webdav.lib.Property prop = null;
          try {
              Enumeration responses = this.resource.propfindMethod(0, propNames);
              while (responses.hasMoreElements()) {
                  ResponseEntity response = (ResponseEntity)responses.nextElement();
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
    public void setSourceProperty(SourceProperty sourceproperty)
    throws SourceException {

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
}
