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
package org.apache.cocoon.components.modules.input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.cocoon.xml.dom.DocumentWrapper;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** 
 * Meta module that obtains values from other module and returns all
 * parameters as XML.
 * 
 * <p>Config</p>
 * <pre>
 *   &lt;!-- in cocoon.xconf --&gt;
 *   &lt;ignore&gt;do-&lt;/ignore&gt;
 *   &lt;strip&gt;user.&lt;/strip&gt;
 *   &lt;input-module name="request-param"/&gt;
 *
 *   &lt;!-- e.g. in database.xml --&gt;
 *   &lt;mode type="all" name="xmlmeta"/&gt;
 *      &lt;ignore&gt;foo.&lt;/ignore&gt;
 *      &lt;strip&gt;f&lt;/strip&gt;
 *      &lt;use&gt;foo&lt;/use&gt;
 *      &lt;root&gt;my-root&lt;/root&gt;
 *      &lt;input-module name="request-param"/&gt;
 *   &lt;/mode&gt;
 * </pre>
 *
 * <p>If present, "ignore" gives a prefix of parameters to ignore,
 * ignore has precedence over the "use" attribute, "strip" a prefix
 * that will be removed from the final parameter names in the produced
 * XML, "use" is a prefix for parameters to include in the XML, and
 * "root" is the name of the root element in the created XML.</p>
 *
 * <p>Input</p>
 * <pre>
 *    foo.one = ['abc']
 *    foo.two = ['def']
 *    foo1 = ['bar']
 *    foo2 = ['one','two','three']
 *    bar = ['rubber duck']
 * </pre>
 *
 * <p>Output</p>
 * <pre> 
 *   &lt;my-root&gt;
 *     &lt;item name="oo1"&gt;bar&lt;/item&gt;
 *     &lt;item name="oo2"&gt;
 *        &lt;value&gt;one&lt;/value&gt;
 *        &lt;value&gt;two&lt;/value&gt;
 *        &lt;value&gt;three&lt;/value&gt;
 *     &lt;/item&gt;
 *   &lt;/my-root&gt;
 * </pre>
 *
 * <p>Produces Objects of type {@link org.apache.cocoon.xml.dom.DocumentWrapper DocumentWrapper}</p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: XMLMetaModule.java,v 1.3 2004/02/15 21:30:00 haul Exp $
 */
public class XMLMetaModule extends AbstractMetaModule implements ThreadSafe {

    protected String rootName = "root";
    protected String ignore;
    protected String use;
    protected String strip;
    protected Object config;
    protected XPathProcessor xpathProcessor;
    
    protected static final String CACHE_OBJECT_NAME = "org.apache.cocoon.component.modules.input.XMLMetaModule";

    final static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("XML");
        returnNames = tmp;
    }


    
    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name", this.defaultInput);
        this.rootName = this.inputConf.getAttribute("root",this.rootName);
        this.ignore = this.inputConf.getAttribute("ignore",this.ignore);
        this.use = this.inputConf.getAttribute("use",this.use);
        this.strip = this.inputConf.getAttribute("strip",this.strip);
        this.config = config;

        // preferred
        this.rootName = config.getChild("root").getValue(this.rootName);
        this.ignore = config.getChild("ignore").getValue(this.ignore);
        this.use = config.getChild("use").getValue(this.use);
        this.strip = config.getChild("strip").getValue(this.strip);
    }




    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }


        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = null;
        String inputName=null;
        String rootName = this.rootName;
        String ignore  = this.ignore;
        String use  = this.use;
        String strip  = this.strip;
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            rootName = modeConf.getAttribute("root",this.rootName);
            ignore  = modeConf.getAttribute("ignore" ,this.ignore );
            use  = modeConf.getAttribute("use" ,this.use );
            strip  = modeConf.getAttribute("strip" ,this.strip );

            // preferred
            rootName = modeConf.getChild("root").getValue(rootName);
            ignore  = modeConf.getChild("ignore").getValue(ignore );
            use  = modeConf.getChild("use").getValue(use );
            strip  = modeConf.getChild("strip").getValue(strip );
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        // see whether the Document is already stored as request
        // attribute and return that
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map cache = (Map) request.getAttribute(CACHE_OBJECT_NAME);
        Object key = (modeConf != null ? modeConf : this.config);
        Document doc = null;

        if (cache != null && cache.containsKey(key)) {
            doc = (Document) cache.get(key);
            if (getLogger().isDebugEnabled())
                getLogger().debug("using cached copy "+doc);
            return doc;
        }
        if (getLogger().isDebugEnabled())
            getLogger().debug("no cached copy "+cache+" / "+key);


        // get InputModule and all attribute names
        InputModule input = null;
        if (inputName != null) input = obtainModule(inputName);

        Iterator names = getNames(objectModel, 
                                  this.input, this.defaultInput, this.inputConf,
                                  input, inputName, inputConfig);

        // first, sort all attribute names that the DOM can be created in one go
        // while doing so, remove unwanted attributes
        SortedSet set = new TreeSet();
        String aName = null;
        while (names.hasNext()){
            aName = (String) names.next();
            if ((use == null || aName.startsWith(use)) &&
                (ignore == null || !aName.startsWith(ignore))) {
                set.add(aName);
            }
        }

        try {
            names = set.iterator();
            
            // create new document and append root node
            doc = DOMUtil.createDocument();
            Element elem = doc.createElement(rootName);
            doc.appendChild(elem);

            while (names.hasNext()){
                aName = (String) names.next();
                // obtain values from input module
                Object[] value = getValues(aName, objectModel,
                                           this.input, this.defaultInput, this.inputConf,
                                           input, inputName, inputConfig);

                // strip unwanted prefix from attribute name if present
                if (strip != null && aName.startsWith(strip)) 
                    aName = aName.substring(strip.length());

                if (value.length > 0) {
                    // add new node from xpath 
                    // (since the names are in a set, the node cannot exist already)
                    Node node = DOMUtil.selectSingleNode(doc.getDocumentElement(), aName, this.xpathProcessor);
                    node.appendChild( node.getOwnerDocument().createTextNode(value[0].toString()));

                    if (value.length > 1) {
                        // if more than one value was obtained, append
                        // further nodes (same name)

                        // isolate node name, selection expressions
                        // "[...]" may not be part of it
                        int endPos = aName.length() - (aName.endsWith("/") ? 1 : 0);
                        int startPos = aName.lastIndexOf("/", endPos) +1;
                        String nodeName = aName.substring(startPos, endPos);
                        if (nodeName.indexOf("[") != -1) {
                            endPos = nodeName.lastIndexOf("]");
                            startPos = nodeName.indexOf("[") +1;
                            nodeName = nodeName.substring(startPos, endPos);
                        }

                        // append more nodes
                        Node parent = node.getParentNode();
                        for (int i = 1; i < value.length; i++) {
                            Node newNode = parent.getOwnerDocument().createElementNS(null, nodeName);
                            parent.appendChild( newNode );
                            newNode.appendChild( newNode.getOwnerDocument().createTextNode(value[i].toString()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }

        if (input != null) releaseModule(input);

        // create a wrapped instance that is XMLizable
        doc = new DocumentWrapper(doc);

        // store Document as request attribute
        if (cache == null)
            cache = new HashMap();
        if (getLogger().isDebugEnabled())
            getLogger().debug("no cached copy "+cache+" / "+key);
        cache.put(key, doc);
        request.setAttribute(CACHE_OBJECT_NAME,cache);
        
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("returning "+doc.toString());
        return doc;
    }





    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

         if (!this.initialized) {
             this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        return XMLMetaModule.returnNames.iterator();
   }




    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
        throws ConfigurationException {

        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }
        
        Object[] values = new Object[1];
        values[0] = this.getAttribute(name, modeConf, objectModel);
        return values;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        super.compose(manager);
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release((Component)this.xpathProcessor);
            this.xpathProcessor = null;
        }
        super.dispose();
    }

}
