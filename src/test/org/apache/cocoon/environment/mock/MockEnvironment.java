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
package org.apache.cocoon.environment.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.apache.excalibur.source.SourceResolver;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MockEnvironment implements Environment {

    private SourceResolver resolver;

    private String uri;
    private String uriprefix;
    private URL rootcontext;
    private URL context;
    private String view;
    private String action;
    private String contenttype;
    private int contentlength;
    private int status;
    private ByteArrayOutputStream outputstream;
    private HashMap objectmodel;
    private Hashtable attributes = new Hashtable();

    public MockEnvironment(SourceResolver resolver) {
        this.resolver = resolver;
    }

    public String getURI() {
        return uri;
    }

    public String getURIPrefix() {
        return uriprefix;
    }

    public URL getRootContext() {
        return rootcontext;
    }

    public URL getContext() {
        return context;
    }

    public String getView() {
        return view;
    }

    public String getAction() {
        return action;
    }

    public void setContext(String prefix, String uri) {
        throw new AssertionFailedError("Not implemented");
    }

    public void changeContext(String uriprefix, String context) throws Exception {
        throw new AssertionFailedError("Not implemented");
    }

    public void redirect(boolean sessionmode, String url) throws IOException {
        throw new AssertionFailedError("Use Redirector.redirect instead!");
    }

    public void setContentType(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getContentType() {
        return contenttype;
    }

    public void setContentLength(int length) {
        this.contentlength = contentlength;
    }

    public int getContentLength() {
        return contentlength;
    }

    public void setStatus(int statusCode) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public OutputStream getOutputStream() throws IOException {
        outputstream = new ByteArrayOutputStream();
        return outputstream;
    }

    public OutputStream getOutputStream(int bufferSize) throws IOException {
        outputstream = new ByteArrayOutputStream();
        return outputstream;
    }

    public byte[] getOutput() {
        return outputstream.toByteArray();
    }

    public Map getObjectModel() {
        return objectmodel;
    }

    public boolean isResponseModified(long lastModified) {
        throw new AssertionFailedError("Not implemented");
    }

    public void setResponseIsNotModified() {
        throw new AssertionFailedError("Not implemented");
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public boolean tryResetResponse() throws IOException {
        throw new AssertionFailedError("Not implemented");
    }

    public void commitResponse() throws IOException {
        throw new AssertionFailedError("Not implemented");
    }
    
    public void startingProcessing() {
        throw new AssertionFailedError("Not implemented");
    }
    
    public void finishingProcessing() {
        throw new AssertionFailedError("Not implemented");
    }


    public Source resolve(String systemID)
      throws ProcessingException, SAXException, IOException {
  
        throw new AssertionFailedError("Not not use deprecated methods!");
    }

    public void toSAX(org.apache.excalibur.source.Source source,
                ContentHandler handler)
      throws SAXException, IOException, ProcessingException {

        throw new AssertionFailedError("Not not use deprecated methods!");
    }

    public void toSAX(org.apache.excalibur.source.Source source,
               String         mimeTypeHint,
               ContentHandler handler)
      throws SAXException, IOException, ProcessingException {

        throw new AssertionFailedError("Not not use deprecated methods!");
    }

    public org.apache.excalibur.source.Source resolveURI(String location)
        throws MalformedURLException, IOException, org.apache.excalibur.source.SourceException {

        return resolver.resolveURI(location);
    }

    public org.apache.excalibur.source.Source resolveURI(String location,
                                                         String base,
                                                         Map parameters)
        throws MalformedURLException, IOException, org.apache.excalibur.source.SourceException {

        return resolver.resolveURI(location, base, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release(org.apache.excalibur.source.Source source) {
        resolver.release(source);
    }
}

