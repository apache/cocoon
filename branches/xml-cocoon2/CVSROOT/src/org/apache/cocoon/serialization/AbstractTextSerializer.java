/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.NOPCacheValidity;
import org.apache.cocoon.util.TraxErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @version CVS $Revision: 1.1.2.15 $ $Date: 2001-05-04 11:02:14 $
 */
public abstract class AbstractTextSerializer extends AbstractSerializer implements Configurable, Cacheable {

    /**
     * The trax <code>TransformerFactory</code> used by this serializer.
     */
    private SAXTransformerFactory tfactory = null;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();
    
    /**
     * The prefixes of startPreficMapping() declarations for the coming element.
     */
    private List prefixList = new ArrayList();
    
    /**
     * The URIs of startPrefixMapping() declarations for the coming element.
     */
    private List uriList = new ArrayList();
    
    /**
     * True if there has been some startPrefixMapping() for the coming element.
     */
    private boolean hasMappings = false;

    /**
     * Helper for TransformerFactory.
     */
    protected synchronized SAXTransformerFactory getTransformerFactory()
    {
        if(tfactory == null)  {
            tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            tfactory.setErrorListener(new TraxErrorHandler(getLogger()));
        }
        return tfactory;
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
      throws ConfigurationException {

        Configuration cdataSectionElements = conf.getChild("cdata-section-elements");
        Configuration dtPublic = conf.getChild("doctype-public");
        Configuration dtSystem = conf.getChild("doctype-system");
        Configuration encoding = conf.getChild("encoding");
        Configuration indent = conf.getChild("indent");
        Configuration mediaType = conf.getChild("media-type");
        Configuration method = conf.getChild("method");
        Configuration omitXMLDeclaration = conf.getChild("omit-xml-declaration");
        Configuration standAlone = conf.getChild("standalone");
        Configuration version = conf.getChild("version");

        if (! cdataSectionElements.getLocation().equals("-")) {
            format.put(OutputKeys.CDATA_SECTION_ELEMENTS,cdataSectionElements.getValue());
        }
        if (! dtPublic.getLocation().equals("-")) {
            format.put(OutputKeys.DOCTYPE_PUBLIC,dtPublic.getValue());
        }
        if (! dtSystem.getLocation().equals("-")) {
            format.put(OutputKeys.DOCTYPE_SYSTEM,dtSystem.getValue());
        }
        if (! encoding.getLocation().equals("-")) {
            format.put(OutputKeys.ENCODING,encoding.getValue());
        }
        if (! indent.getLocation().equals("-")) {
            format.put(OutputKeys.INDENT,indent.getValue());
        }
        if (! mediaType.getLocation().equals("-")) {
            format.put(OutputKeys.MEDIA_TYPE,mediaType.getValue());
        }
        if (! method.getLocation().equals("-")) {
            format.put(OutputKeys.METHOD,method.getValue());
        }
        if (! omitXMLDeclaration.getLocation().equals("-")) {
            format.put(OutputKeys.OMIT_XML_DECLARATION,omitXMLDeclaration.getValue());
        }
        if (! standAlone.getLocation().equals("-")) {
            format.put(OutputKeys.STANDALONE,standAlone.getValue());
        }
        if (! version.getLocation().equals("-")) {
            format.put(OutputKeys.VERSION,version.getValue());
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public long generateKey() {
        return 1;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        return new NOPCacheValidity();
    }

    /**
     * Recycle serializer by removing references
     */
    public void recycle() {
        clearMappings();
        super.recycle();
    }
    
    /**
     *
     */
    public void startDocument()
      throws SAXException {
        // Cleanup
        clearMappings();
        super.startDocument();
    }
    
    /**
     * Add tracking of mappings to be able to add <code>xmlns:</code> attributes
     * in <code>startElement()</code>.
     */
    public void startPrefixMapping(String prefix, String uri)
      throws SAXException {
        // Store the mappings to reconstitute xmlns:attributes
        this.hasMappings = true;
        this.prefixList.add(prefix);
        this.uriList.add(uri);
        
        super.startPrefixMapping(prefix, uri);
    }
    
    /**
     * Ensure all namespace declarations are present as <code>xmlns:</code> attributes
     * and add those needed before calling superclass. This is a workaround for a Xalan bug
     * (at least in version 2.0.1) : <code>org.apache.xalan.serialize.SerializerToXML</code>
     * ignores <code>start/endPrefixMapping()</code>.
     */
    public void startElement(String eltUri, String eltLocalName, String eltQName, Attributes attrs)
      throws SAXException {
      
        if (this.hasMappings) {
            // Add xmlns* attributes where needed
            
            // New Attributes if we have to add some.
            AttributesImpl newAttrs = null;
            
            int mappingCount = this.prefixList.size();
            int attrCount = attrs.getLength();
            
            for(int mapping = 0; mapping < mappingCount; mapping++) {
                
                // Build infos for this namespace
                String uri = (String)this.uriList.get(mapping);
                String prefix = (String)this.prefixList.get(mapping);
                String qName = prefix.equals("") ? "xmlns" : ("xmlns:" + prefix);

                // Search for the corresponding xmlns* attribute
                boolean found = false;
                find : for (int attr = 0; attr < attrCount; attr++) {
                    if (qName.equals(attrs.getQName(attr))) {
                        // Check if mapping and attribute URI match
                        if (! uri.equals(attrs.getValue(attr))) {
                            getLogger().error("AbstractTextSerializer:URI in prefix mapping and attribute do not match : '" + uri + "' - '" + attrs.getURI(attr) + "'");
                            throw new SAXException("URI in prefix mapping and attribute do not match");
                        }
                        found = true;
                        break find;
                    }
                }
                
                if (!found) {
                    // Need to add this namespace
                    if (newAttrs == null) {
                        // Need to test if attrs is empty or we go into an infinite loop...
                        // Well know SAX bug which I spent 3 hours to remind of :-(
                        if (attrCount == 0)
                            newAttrs = new AttributesImpl();
                        else
                            newAttrs = new AttributesImpl(attrs);
                    }
                    
                    if (prefix.equals("")) {
                        newAttrs.addAttribute(Constants.XML_NAMESPACE_URI, "xmlns", "xmlns", "CDATA", uri);
                    } else {
                        newAttrs.addAttribute(Constants.XML_NAMESPACE_URI, prefix, qName, "CDATA", uri);
                    }
                }
            } // end for mapping
            
            // Cleanup for the next element
            clearMappings();
            
            // Start element with new attributes, if any
            super.startElement(eltUri, eltLocalName, eltQName, newAttrs == null ? attrs : newAttrs);
        }
        else {
            // Normal job
            super.startElement(eltUri, eltLocalName, eltQName, attrs);
        }
    }
        
    private void clearMappings()
    {
        this.hasMappings = false;
        this.prefixList.clear();
        this.uriList.clear();
    }
}
