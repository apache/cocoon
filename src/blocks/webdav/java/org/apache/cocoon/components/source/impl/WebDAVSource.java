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
package org.apache.cocoon.components.source.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.cocoon.components.source.RestrictableSource;
import org.apache.cocoon.components.source.helpers.SourceCredential;
import org.apache.cocoon.components.source.helpers.SourcePermission;
import org.apache.commons.httpclient.HttpException;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *  A source implementation to get access to WebDAV repositories. Use it
 *  as webdav://[host][:port]/path[?principal=user&password=password].
 *
 *  @author <a href="mailto:g.casper@s-und-n.de">Guido Casper</a>
 *  @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 *  @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 *  @version $Id: WebDAVSource.java,v 1.3 2003/07/27 12:56:16 gianugo Exp $
*/
public class WebDAVSource
    implements Source, RestrictableSource, ModifiableTraversableSource {


    private final String NAMESPACE = "http://apache.org/cocoon/webdav/1.0";

    private final String PREFIX = "webdav";

    private final String RESOURCE_NAME = "resource";

    private final String COLLECTION_NAME = "collection";

    private String systemId;
    
    private String location;
    private String principal;
    private String password;

    private SourceValidity validity = null;
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
        
        //try {
            HttpURL httpURL = new HttpURL(this.systemId);
            httpURL.setUserInfo(principal, password);
            
            if (createNew)
               this.resource = new WebdavResource(httpURL, 
                   WebdavResource.NOACTION, 
                   DepthSupport.DEPTH_1);
            else 
               this.resource = new WebdavResource(httpURL);
            
            WebdavResource.setGetUseDisk(false);
        //} catch (IOException ioe) {
        //    throw new IllegalStateException(ioe.getMessage());
        //}
        
    }

    /**
     * Static factory method to obtain a Source.
     * 
     * 
     */
    
    public static WebDAVSource newWebDAVSource(
      String location,
      String principal,
      String password,
      String protocol) throws SourceException {
          // FIXME: wild hack needed for writing to a new resource.
          // if a resource doesn't exist, an exception
          // will be thrown, unless such resource isn't created with the
          // WebdavResouce.NOACTION flag. However, such flag cannot be
          // used by default, since it won't allow to discover the 
          // properties of an exixting resource. So either we do this
          // hack here or we fill properties on the fly when requested.
          // This "solution" is scary, but the SWCL is pretty dumb.
          try {
            return new WebDAVSource(location, principal, password, protocol, false);  
          }  catch (HttpException he) {
             try { 
                 return new WebDAVSource(location, principal, password, protocol, true);            
             } catch (HttpException finalHe) {
             	finalHe.printStackTrace(System.err);
                throw new SourceException("Error creating the source: ", finalHe);   
             } catch (IOException e) {
				e.printStackTrace(System.err);
                throw new SourceException("Error creating the source: ", e);                	
             }    
          } catch (IOException e) {
                throw new SourceException("Error creating the source: ", e);   
          }      
      }      

    /**
     * Constructor used by the Traversable methods to build children.
     */
    private WebDAVSource (WebdavResource source) {
    	this.resource = source;
    	this.systemId = source.getHttpURL().getURI();
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
                    throw new HttpException(this.systemId + "does not exist");
                }
                return bi;
            }
        } catch (HttpException he) {
            throw new SourceException("Could not get WebDAV resource", he);
        } catch (Exception e) {
            throw new SourceException("Could not get WebDAV resource", e);
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
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        if (this.validity == null)
            this.validity =
                new TimeStampValidity(this.resource.getGetLastModified());
        return this.validity;
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
        throw new SourceException("Operation not supported");
    }

    /**
     * Returns a list of the existing permissions
     *
     * @return Array of SourcePermission
     */
    public SourcePermission[] getSourcePermissions() throws SourceException {
        throw new SourceException("Operation not supported");
    }

    public class WebDAVSourceOutputStream extends ByteArrayOutputStream {

        private WebdavResource resource = null;

        private WebDAVSourceOutputStream(WebdavResource resource) {
            this.resource = resource;
            WebdavResource.setGetUseDisk(false);
        }

        public void close() throws IOException {
            super.close();

            try {
                this.resource.putMethod(toByteArray());
            } catch (HttpException he) {
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
        th.startElement(
            NAMESPACE,
            COLLECTION_NAME,
            PREFIX + ":" + COLLECTION_NAME,
            new AttributesImpl());
        this.resourcesToSax(resources, th);
        th.endElement(
            NAMESPACE,
            COLLECTION_NAME,
            PREFIX + ":" + COLLECTION_NAME);
        th.endDocument();
        return new ByteArrayInputStream(bOut.toByteArray());
    }

    private void resourcesToSax(
        WebdavResource[] resources,
        ContentHandler handler)
        throws SAXException {
        for (int i = 0; i < resources.length; i++) {
            System.out.println("RESOURCE: " + resources[i].getDisplayName());
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
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
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
        Source child = null;
        try {
            WebdavResource[] resources = this.resource.listWebdavResources();
            for (int i = 0; i < resources.length; i++) {
                if (childName.equals(resources[i].getDisplayName())) {
                	String childLocation = this.location + "/" + childName;
					WebDAVSource source = WebDAVSource.newWebDAVSource(childLocation, this.principal, this.password, this.protocol);
					source.setSourceCredential(this.getSourceCredential());
					return source;
                }
            }
        } catch (HttpException e) {
            e.printStackTrace();
            throw new SourceException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new SourceException(e.getMessage());
        }
        return child;
    }

    /**
     * Get the collection children.
     *
     * @see org.apache.excalibur.source.TraversableSource#getChildren()
     */
    public Collection getChildren() throws SourceException {
        Vector children = new Vector();
        try {
            WebdavResource[] resources = this.resource.listWebdavResources();
            for (int i = 0; i < resources.length; i++) {
                WebDAVSource src = new WebDAVSource(resources[i]);
                children.add(src);
            }
        } catch (HttpException e) {
            e.printStackTrace();
            throw new SourceException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new SourceException(e.getMessage());
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
                this.protocol);
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
        // TODO Auto-generated method stub
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
            throw new SourceException("Unable to create collection(s)", e);
        } catch (IOException e) {
            throw new SourceException("Unable to create collection(s)", e);			
        }
    }

}
