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
 * @version CVS $Id: WriteDOMSessionTransformer.java,v 1.4 2003/12/06 21:22:07 cziegeler Exp $
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
