/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.serialization;

import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.butterfly.xml.AbstractXMLPipe;
import org.apache.butterfly.xml.xslt.TraxTransformerFactory;


/**
 * Description of XMLSerializer.
 * 
 * @version CVS $Id$
 */
public class XMLSerializer extends AbstractXMLPipe implements Serializer {

    protected OutputStream output;
    private Map objectModel;
    protected TraxTransformerFactory transformerFactory;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();
    
    /**
     * 
     */
    public XMLSerializer() {
        this.format.put(OutputKeys.METHOD, "xml");
    }

    /**
     * @param transformerFactory The transformerFactory to set.
     */
    public void setTraxTransformerFactory(TraxTransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public void setCdataSectionElements(String cdataSectionElements) {
        format.put(OutputKeys.CDATA_SECTION_ELEMENTS, cdataSectionElements);
    }
    
    public void setDoctypePublic(String dtPublic) {
        format.put(OutputKeys.DOCTYPE_PUBLIC, dtPublic);
    }
    
    public void setDocTypeSystem(String dtSystem) {
        format.put(OutputKeys.DOCTYPE_SYSTEM, dtSystem);
    }
    
    public void setEncoding(String encoding) {
        format.put(OutputKeys.ENCODING, encoding);
    }
    
    public void setIndent(String indent) {
        format.put(OutputKeys.INDENT, indent);
    }
    
    public void setMediaType(String mediaType) {
        format.put(OutputKeys.MEDIA_TYPE, mediaType);
    }
    
    public void setMethod(String method) {
        format.put(OutputKeys.METHOD, method);
    }
    
    public void setOmitXMLDeclaration(String omitXMLDeclaration) {
        format.put(OutputKeys.OMIT_XML_DECLARATION, omitXMLDeclaration);
    }
    
    public void setStandAlone(String standAlone) {
        format.put(OutputKeys.STANDALONE, standAlone);
    }
    
    public void setVersion(String version) {
        format.put(OutputKeys.VERSION, version);
    }
    
    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream output) {
        this.output = output;
        TransformerHandler handler = this.transformerFactory.getTransformerHandler();
        handler.getTransformer().setOutputProperties(this.format);
        handler.setResult(new StreamResult(this.output));
        this.setContentHandler(handler);
        this.setLexicalHandler(handler);
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#getMimeType()
     */
    public String getMimeType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#shouldSetContentLength()
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#setObjectModel(java.util.Map)
     */
    public void setObjectModel(Map objectModel) {
        this.objectModel = objectModel;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#getEncoding()
     */
    public String getEncoding() {
        return (String) format.get(OutputKeys.ENCODING);
    }
}
