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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @cocoon.sitemap.component.documentation
 * Make a DOM object from SAX events and write it to the session.
 * 
 * @cocoon.sitemap.component.name   writeDOMsession
 * @cocoon.sitemap.component.logger sitemap.transformer.writeDOMsession
 * 
 * 
 * Make a DOM object from SAX events and write it to the session.
 *
 * Usage in sitemap:
 *    &lt;map:transform type="writeDOMsession"&gt;
 *      &lt;map:parameter name="dom-name" value="content"/&gt;
 *      &lt;map:parameter name="dom-root-element" value="companies"/&gt;
 *    &lt;/map:transform&gt;
 *
 * Where:
 *   dom-name is the name for the DOM object in the session
 *   dom-root-element is the trigger that will be the root element of the DOM
 *
 * @author <a href="mailto:sven.beauprez@the-ecorp.com">Sven Beauprez</a>
 * @version CVS $Id: WriteDOMSessionTransformer.java,v 1.4 2004/06/17 14:55:24 cziegeler Exp $
 */

public class WriteDOMSessionTransformer
  extends AbstractTransformer {

    public static final String DOM_NAME = "dom-name";
    public static final String DOM_ROOT_ELEMENT = "dom-root-element";

    private boolean buildDom = false;
    private boolean sessionAvailable = false;

    private Session session;
    private DOMBuilder builder;

    private String DOMName;
    private String rootElement;
    private Map storedPrefixMap;

    /**
     * Recyclable
     */
    public void recycle() {
        super.recycle();
        this.session = null;
        this.builder = null;
        this.buildDom = false;
        this.sessionAvailable = false;
    }

    /** BEGIN SitemapComponent methods **/

    public void setup(SourceResolver resolver, Map objectModel,
                      String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
      getLogger().debug("WriteSessionTransformer: setup");
      Request request = ObjectModelHelper.getRequest(objectModel);
      session = request.getSession(false);
      if (session != null) {
        DOMName = parameters.getParameter(WriteDOMSessionTransformer.DOM_NAME,null);
        rootElement = parameters.getParameter(WriteDOMSessionTransformer.DOM_ROOT_ELEMENT,null);
        if (DOMName!=null && rootElement!=null)  {
          //only now we know it is usefull to store something in the session
          getLogger().debug("WriteSessionTransformer: "+WriteDOMSessionTransformer.DOM_NAME + "=" +
                            DOMName + "; " + WriteDOMSessionTransformer.DOM_ROOT_ELEMENT + "=" +
                            rootElement);
          sessionAvailable = true;
                    storedPrefixMap = new HashMap();
        } else  {
          getLogger().error("WriteSessionTransformer: need " + WriteDOMSessionTransformer.DOM_NAME +
                            " and " + WriteDOMSessionTransformer.DOM_ROOT_ELEMENT + " parameters");
        }
      } else  {
        getLogger().error("WriteSessionTransformer: no session object");
      }
    }

    /** END SitemapComponent methods **/

    /** BEGIN SAX ContentHandler handlers **/

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
      super.startPrefixMapping(prefix,uri);
      if (buildDom)  {
        builder.startPrefixMapping(prefix,uri);
      } else {
                storePrefixMapping(prefix,uri);
            }
    }

    public void startElement(String uri, String name, String raw, Attributes attributes)
    throws SAXException {
        //only build the DOM tree if session is available
        if (name.equalsIgnoreCase(rootElement) && sessionAvailable)  {
          getLogger().debug("WriteSessionTransformer: start building DOM tree");
          buildDom = true;
          builder = new DOMBuilder();
          builder.startDocument();
                    launchStoredMappings();
          builder.startElement(uri,name,raw,attributes);
        } else if (buildDom)  {
          builder.startElement(uri,name,raw,attributes);
        }
        super.contentHandler.startElement(uri,name,raw,attributes);
    }

    public void endElement(String uri, String name, String raw)
    throws SAXException {
        if (name.equalsIgnoreCase(rootElement) && sessionAvailable) {
          buildDom = false;
          builder.endElement(uri,name,raw);
          builder.endDocument();
          getLogger().debug("WriteSessionTransformer: putting DOM tree in session object");
          session.setAttribute(DOMName,builder.getDocument().getFirstChild());
          getLogger().debug("WriteSessionTransformer: DOM tree is in session object");
        } else if (buildDom)  {
          builder.endElement(uri,name,raw);
        }
        super.contentHandler.endElement(uri,name,raw);
    }


    public void characters(char c[], int start, int len)
    throws SAXException {
        if (buildDom)  {
          builder.characters(c,start,len);
        }
        super.contentHandler.characters(c,start,len);
    }

    public void startCDATA()
    throws SAXException  {
      if (buildDom)  {
        builder.startCDATA();
      }
      super.lexicalHandler.startCDATA();
    }

    public void endCDATA()
    throws SAXException {
      if (buildDom)  {
        builder.endCDATA();
      }
      super.lexicalHandler.endCDATA();
    }

    /** END SAX ContentHandler handlers **/

      protected void storePrefixMapping(String prefix, String uri) {
           storedPrefixMap.put(prefix,uri);
    }

      protected void launchStoredMappings()
        throws SAXException {
            Iterator it = storedPrefixMap.keySet().iterator();
                while(it.hasNext()) {
                    String pre = (String)it.next();
                    String uri = (String)storedPrefixMap.get(pre);
                    getLogger().debug("WriteSessionTransformer: launching prefix mapping[ pre: "+pre+" uri: "+uri+" ]");
                    builder.startPrefixMapping(pre,uri);
                }
        }



}
