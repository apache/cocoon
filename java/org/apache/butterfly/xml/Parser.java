/*
 * Copyright 2004, Ugo Cei.
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
package org.apache.butterfly.xml;

import java.io.IOException;

import org.apache.butterfly.source.Source;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Wraps a {@link org.xml.sax.XMLReader}.
 * 
 * TODO: wrap all exceptions in runtime exceptions.
 * 
 * @version CVS $Id: Parser.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class Parser {
    private XMLReader xmlReader;
    private String saxDriver;
    
    protected static final Log logger = LogFactory.getLog(Parser.class);
    
    /**
     * @param saxDriver The saxDriver to set.
     */
    public void setSaxDriver(String saxDriver) {
        this.saxDriver = saxDriver;
    }
    
    public void initialize() {
        try {
            xmlReader = XMLReaderFactory.createXMLReader(saxDriver);
        } catch (SAXException e) {
            throw new XMLException("Cannot initialize XML parser", e);
        }
    }
    
    public void parse(Source source) {
        try {
            xmlReader.parse(new InputSource(source.getInputStream()));
        } catch (IOException e) {
            // TODO log
            throw new XMLException("I/O error while reading '" + source + "'", e);
        } catch (SAXException e) {
            // TODO log
            throw new XMLException("SAX error while parsing '" + source + "'", e);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return xmlReader.equals(arg0);
    }
    
    /**
     * @return
     */
    public ContentHandler getContentHandler() {
        return xmlReader.getContentHandler();
    }
    
    /**
     * @return
     */
    public DTDHandler getDTDHandler() {
        return xmlReader.getDTDHandler();
    }
    
    /**
     * @return
     */
    public EntityResolver getEntityResolver() {
        return xmlReader.getEntityResolver();
    }
    
    /**
     * @return
     */
    public ErrorHandler getErrorHandler() {
        return xmlReader.getErrorHandler();
    }
    
    /**
     * @param arg0
     * @return
     * @throws org.xml.sax.SAXNotRecognizedException
     * @throws org.xml.sax.SAXNotSupportedException
     */
    public boolean getFeature(String arg0) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return xmlReader.getFeature(arg0);
    }
    
    /**
     * @param arg0
     * @return
     * @throws org.xml.sax.SAXNotRecognizedException
     * @throws org.xml.sax.SAXNotSupportedException
     */
    public Object getProperty(String arg0) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return xmlReader.getProperty(arg0);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return xmlReader.hashCode();
    }
    
    /**
     * @param arg0
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void parse(String arg0) throws IOException, SAXException {
        xmlReader.parse(arg0);
    }
    
    /**
     * @param arg0
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void parse(InputSource arg0) throws IOException, SAXException {
        xmlReader.parse(arg0);
    }
    
    /**
     * @param arg0
     */
    public void setContentHandler(ContentHandler arg0) {
        xmlReader.setContentHandler(arg0);
    }
    
    /**
     * @param arg0
     */
    public void setDTDHandler(DTDHandler arg0) {
        xmlReader.setDTDHandler(arg0);
    }
    
    /**
     * @param arg0
     */
    public void setEntityResolver(EntityResolver arg0) {
        xmlReader.setEntityResolver(arg0);
    }
    
    /**
     * @param arg0
     */
    public void setErrorHandler(ErrorHandler arg0) {
        xmlReader.setErrorHandler(arg0);
    }
    
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXNotRecognizedException
     * @throws org.xml.sax.SAXNotSupportedException
     */
    public void setFeature(String arg0, boolean arg1)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        xmlReader.setFeature(arg0, arg1);
    }
    
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXNotRecognizedException
     * @throws org.xml.sax.SAXNotSupportedException
     */
    public void setProperty(String arg0, Object arg1)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        xmlReader.setProperty(arg0, arg1);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return xmlReader.toString();
    }
}
