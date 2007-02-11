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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentManager;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;

import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.jar.JarEntry;

/**
 * Description of a source which is described by an URL.
 *
 * @deprecated by the Avalon Exalibur Source Resolving
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: URLSource.java,v 1.2 2003/03/16 17:49:10 vgritsenko Exp $
 */
public class URLSource extends AbstractStreamSource {

    /** Identifier for file urls */
    private final String FILE = "file:";

    /** The last modification date or 0 */
    private long lastModificationDate;

    /** The content length */
    private long contentLength;

    /** Is the content html or xml? */
    private boolean isHTMLContent = false;

    /** The system id */
    private String systemId;

    /** The URL of the source */
    private URL url;

    /** The connection for a real URL */
    private URLConnection connection;

    /** Is this a file or a "real" URL */
    private boolean isFile;

    /** Are we initialized? */
    private boolean gotInfos;

    /** The <code>SourceParameters</code> for post */
    private SourceParameters postParameters;

    /**
     * Construct a new object
     */
    public URLSource(URL url, ComponentManager manager)
    throws IOException {
        super(manager);
        this.systemId = url.toExternalForm();
        this.isFile = systemId.startsWith(FILE);
        if (this.isFile == true) {
            if (systemId.endsWith(".htm") || systemId.endsWith(".html")) {
                this.isHTMLContent = true;
            }
        }
        this.url = url;
        this.gotInfos = false;
    }

    protected boolean isHTMLContent() {
        return this.isHTMLContent;
    }

    /**
     * Get the last modification date and content length of the source.
     * Any exceptions are ignored.
     */
    private void getInfos() {
        if (!this.gotInfos) {
            if (this.isFile) {
                File file = new File(systemId.substring(FILE.length()));
                this.lastModificationDate = file.lastModified();
                this.contentLength = file.length();
            } else {
                if (this.postParameters == null) {
                    try {
                        if (this.connection == null) {
                            this.connection = this.url.openConnection();
                            String userInfo = this.getUserInfo();
                            if (this.url.getProtocol().startsWith("http") && userInfo != null) {
                                this.connection.setRequestProperty("Authorization","Basic "+SourceUtil.encodeBASE64(userInfo));
                            }
                        }
                        if(this.connection instanceof JarURLConnection) {
                            JarEntry entry = ((JarURLConnection)this.connection).getJarEntry();
                            this.lastModificationDate = entry.getTime();
                        } else {
                            this.lastModificationDate = this.connection.getLastModified();
                        }
                        this.contentLength = this.connection.getContentLength();
                    } catch (IOException ignore) {
                        this.lastModificationDate = 0;
                        this.contentLength = -1;
                    }
                } else {
                        // do not open connection when using post!
                        this.lastModificationDate = 0;
                        this.contentLength = -1;
                }
            }
            this.gotInfos = true;
        }
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        this.getInfos();
        return this.lastModificationDate;
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        this.getInfos();
        return this.contentLength;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     *
     * @throws ResourceNotFoundException if file not found or
     *         HTTP location does not exist.
     * @throws IOException if I/O error occured.
     */
    public InputStream getInputStream()
    throws IOException, ProcessingException {
        this.getInfos();
        try {
            InputStream input = null;
            if ( this.isFile ) {
                input = new FileInputStream(this.systemId.substring(FILE.length()));
            } else {
                if (this.connection == null) {
                    this.connection = this.url.openConnection();
                    /* The following requires a jdk 1.3 */
                    String userInfo = this.getUserInfo();
                    if (this.url.getProtocol().startsWith("http") && userInfo != null) {
                        this.connection.setRequestProperty("Authorization","Basic "+SourceUtil.encodeBASE64(userInfo));
                    }
                    // do a post operation
                    if (this.connection instanceof HttpURLConnection
                        && this.postParameters != null) {
                        StringBuffer buffer = new StringBuffer(2000);
                        String key;
                        Iterator i = postParameters.getParameterNames();
                        Iterator values;
                        String value;
                        boolean first = true;
                        while ( i.hasNext() ) {
                            key = (String)i.next();
                            values = this.postParameters.getParameterValues(key);
                            while (values.hasNext() == true) {
                                value = SourceUtil.encode((String)values.next());
                                if (first == false) buffer.append('&');
                                first = false;
                                buffer.append(key.toString());
                                buffer.append('=');
                                buffer.append(value);
                            }
                        }
                        HttpURLConnection httpCon = (HttpURLConnection)connection;
                        httpCon.setDoInput(true);

                        if (buffer.length() > 1) { // only post if we have parameters
                            String postString = buffer.toString();
                            httpCon.setRequestMethod("POST"); // this is POST
                            httpCon.setDoOutput(true);
                            httpCon.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

                            // A content-length header must be contained in a POST request
                            httpCon.setRequestProperty("Content-length", Integer.toString(postString.length()));
                            java.io.OutputStream out = new java.io.BufferedOutputStream(httpCon.getOutputStream());
                            out.write(postString.getBytes());
                            out.close();
                        }
                        if ("text/html".equals(httpCon.getContentType()) == true) {
                            this.isHTMLContent = true;
                        }
                        input = httpCon.getInputStream();
                        this.connection = null; // make sure a new connection is created next time
                        return input;
                    }
                }
                if ("text/html".equals(this.connection.getContentType()) == true) {
                    this.isHTMLContent = true;
                }
                input = this.connection.getInputStream();
                this.connection = null; // make sure a new connection is created next time
            }
            return input;
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException("Resource not found "
                                                + this.systemId, e);
        }
    }

    private static boolean checkedURLClass = false;
    private static boolean urlSupportsGetUserInfo = false;
    private static Method  urlGetUserInfo = null;
    private static Object[] emptyParams = new Object[0];

    /**
     * Check if the <code>URL</code> class supports the getUserInfo()
     * method which is introduced in jdk 1.3
     */
    private String getUserInfo() {
        if (URLSource.checkedURLClass) {
            if (URLSource.urlSupportsGetUserInfo) {
                try {
                    return (String) URLSource.urlGetUserInfo.invoke(this.url, URLSource.emptyParams);
                } catch (Exception e){
                    // ignore this anyway
                }
            }
            return null;
        } else {
            // test if the url class supports the getUserInfo method
            try {
                URLSource.urlGetUserInfo = URL.class.getMethod("getUserInfo", null);
                String ui = (String)URLSource.urlGetUserInfo.invoke(this.url, URLSource.emptyParams);
                URLSource.checkedURLClass = true;
                URLSource.urlSupportsGetUserInfo = true;
                return ui;
            } catch (Exception e){
            }
            URLSource.checkedURLClass = true;
            URLSource.urlSupportsGetUserInfo = false;
            URLSource.urlGetUserInfo = null;
            return null;
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        // reset connection
        this.connection = null;
        this.gotInfos = false;
    }

    public void recycle() {
        refresh();
    }

    /**
     * Set the post parameters
     */
    public void setPostParameters(SourceParameters pars) {
        this.postParameters = pars;
    }

}
