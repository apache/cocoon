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
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;

/**
 * The filter transformer can be used to let only an amount of elements through in
 * a given block.
 *
 * <p>Usage in the sitemap:
 *    &lt;map:transform type="filter"&gt;
 *     &lt;map:parameter name="element-name" value="row"/&gt;
 *     &lt;map:parameter name="count" value="5"/&gt;
 *     &lt;map:parameter name="blocknr" value="3"/&gt;
 *    &lt;/map:transform&gt;
 *
 * <p>Only the 3rd block will be shown, containing only 5 row elements.
 *
 * <p><b>Known limitation: behaviour of transformer when trigger elements are nested
 * is not predictable.</b>
 *
 * @author <a href="mailto:sven.beauprez@the-ecorp.com">Sven Beauprez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: FilterTransformer.java,v 1.5 2004/03/09 00:57:35 joerg Exp $
 */
public class FilterTransformer
extends AbstractTransformer
implements CacheableProcessingComponent {
    
    private static final String ELEMENT = "element-name";
    private static final String COUNT = "count";
    private static final String BLOCKNR = "blocknr";
    private static final String BLOCK = "block";
    private static final String BLOCKID = "id";
    private static final int DEFAULT_COUNT = 10;
    private static final int DEFAULT_BLOCK = 1;
    
    protected int counter;
    protected int count;
    protected int blocknr;
    protected int currentBlocknr;
    protected String elementName;
    protected String parentName;
    protected boolean skip;
    protected boolean foundIt;
    
    /** BEGIN SitemapComponent methods **/
    public void setup(SourceResolver resolver,
    Map objectModel,
    String source,
    Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.counter=0;
        this.currentBlocknr=0;
        this.skip=false;
        this.foundIt=false;
        this.parentName=null;
        this.elementName = parameters.getParameter(ELEMENT, "");
        this.count = parameters.getParameterAsInteger(COUNT, DEFAULT_COUNT);
        this.blocknr = parameters.getParameterAsInteger(BLOCKNR, DEFAULT_BLOCK);
        if (this.elementName == null || this.elementName.equals("") || this.count == 0)  {
            throw new ProcessingException("FilterTransformer: both "+ ELEMENT + " and " +
            COUNT + " parameters need to be specified");
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
    public java.io.Serializable getKey() {
        return this.elementName + '<' + this.count + '>' + this.blocknr;
    }
    
    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }
    
    /** BEGIN SAX ContentHandler handlers **/
    public void startElement(String uri, String name, String raw, Attributes attributes)
    throws SAXException {
        if (name.equalsIgnoreCase(elementName)) {
            this.foundIt = true;
            this.counter++;
            if (this.counter <= (this.count*(this.blocknr)) && this.counter > (this.count*(this.blocknr-1))) {
                this.skip = false;
            } else  {
                this.skip = true;
            }
            if (this.currentBlocknr != (int)Math.ceil((float)this.counter/this.count)) {
                this.currentBlocknr = (int)Math.ceil((float)this.counter/this.count);
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", BLOCKID, BLOCKID, "CDATA", String.valueOf(this.currentBlocknr));
                if (this.counter < this.count)  {
                    super.contentHandler.startElement("", BLOCK, BLOCK, attr);
                } else  {
                    // fix Bugzilla Bug 13904, check if counter == 1
                    // in this case there is no startElement("", BLOCK, BLOCK)
                    // written, yet
                    if (this.counter > 1) {
                        super.contentHandler.endElement("", BLOCK, BLOCK);
                    }
                    super.contentHandler.startElement("", BLOCK, BLOCK, attr);
                }
            }
        } else if (!this.foundIt)  {
            this.parentName = name;
        }
        if (!this.skip)  {
            super.contentHandler.startElement(uri,name,raw,attributes);
        }
    }
    
    public void endElement(String uri,String name,String raw)
    throws SAXException  {
        if (this.foundIt && name.equals(this.parentName)) {
            // FIXME: VG: This will fail on XML like:
            // <parent>
            //   <element>
            //     <parent>
            super.contentHandler.endElement("", BLOCK, BLOCK);
            super.contentHandler.endElement(uri, name, raw);
            this.foundIt = false;
            this.skip = false;
        } else if (!this.skip)  {
            super.contentHandler.endElement(uri,name,raw);
        }
    }
    
    public void characters(char c[], int start, int len)
    throws SAXException {
        if (!this.skip)  {
            super.contentHandler.characters(c,start,len);
        }
    }
    
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (!this.skip)  {
            super.contentHandler.processingInstruction(target, data);
        }
    }
    
    public void startEntity(String name)
    throws SAXException {
        if (!this.skip)  {
            super.lexicalHandler.startEntity(name);
        }
    }
    
    public void endEntity(String name)
    throws SAXException {
        if (!this.skip)  {
            super.lexicalHandler.endEntity( name);
        }
    }
    
    public void startCDATA()
    throws SAXException {
        if (!this.skip)  {
            super.lexicalHandler.startCDATA();
        }
    }
    
    public void endCDATA()
    throws SAXException {
        if (!this.skip)  {
            super.lexicalHandler.endCDATA();
        }
    }
    
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (!this.skip)  {
            super.lexicalHandler.comment(ch, start, len);
        }
    }
}
