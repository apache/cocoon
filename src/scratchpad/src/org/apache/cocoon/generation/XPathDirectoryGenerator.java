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
package org.apache.cocoon.generation;

import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Generates an XML directory listing performing XPath queries
 * on XML files. It can be used both as a plain DirectoryGenerator
 * or, using an "xpointerinsh" syntax it will perform an XPath
 * query on every XML resource.
 *
 * Sample usage:
 *
 * Sitemap:
 * &lt;map:match pattern="documents/**"&gt;
 *   &lt;map:generate type="xpathdirectory"
 *     src="docs/{1}#/article/title|/article/abstract" /&gt;
 *   &lt;map:serialize type="xml" /&gt;
 * &lt;/map:match&gt;
 *
 * Request:
 *   http://www.some.host/documents/test
 * Result:
 * &lt;dir:directory
 *   name="test" lastModified="1010400942000"
 *   date="1/7/02 11:55 AM" requested="true"
 *   xmlns:dir="http://apache.org/cocoon/directory/2.0"&gt;
 *   &lt;dir:directory name="subdirectory" lastModified="1010400942000" date="1/7/02 11:55 AM" /&gt;
 *   &lt;dir:file name="test.xml" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 *     &lt;dir:xpath docid="test.xml" query="/article/title"&gt;
 *       &lt;title&gt;This is a test document&lt;/title&gt;
 *       &lt;abstract&gt;
 *         &lt;para&gt;Abstract of my test article&lt;/para&gt;
 *       &lt;/abstract&gt;
 *     &lt;/dir:xpath&gt;
 *   &lt;/dir:file&gt;
 *   &lt;dir:file name="test.gif" lastModified="1011011579000" date="1/14/02 1:32 PM"&gt;
 * &lt;/dir:directory&gt;
 *
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version CVS $Id: XPathDirectoryGenerator.java,v 1.1 2003/03/09 00:10:11 pier Exp $
 */
public class XPathDirectoryGenerator extends DirectoryGenerator {

    /** Element &lt;result&gt; */
    protected static final String RESULT = "xpath";
    protected static final String QRESULT = PREFIX + ":" + RESULT;
    protected static final String RESULT_DOCID_ATTR = "docid";
    protected static final String QUERY_ATTR = "query";

    protected static final String CDATA  = "CDATA";
    protected String XPathQuery = null;
    protected XPathProcessor processor = null;
    protected DOMParser parser;
    protected Document doc;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        // See if an XPath was specified
        int pointer;
        if ((pointer = this.source.indexOf("#")) != -1) {
          this.XPathQuery = source.substring(pointer + 1);
          this.source = source.substring(0, pointer);
          if (this.getLogger().isDebugEnabled())
            this.getLogger().debug("Applying XPath: " + XPathQuery
              + " to directory " + source);
        }
    }

    public void compose(ComponentManager manager) {
      try {
        super.compose(manager);
        processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
        parser = (DOMParser)manager.lookup(DOMParser.ROLE);
      } catch (Exception e) {
        this.getLogger().error("Could not obtain a required component", e);
      }
    }

    /**
     * Adds a single node to the generated document. If the path is a
     * directory, and depth is greater than zero, then recursive calls
     * are made to add nodes for the directory's children. Moreover,
     * if the file is an XML file (ends with .xml), the XPath query
     * is performed and results returned.
     *
     * @param   path
     *      the file/directory to process
     * @param   depth
     *      how deep to scan the directory
     *
     * @throws  SAXException
     *      if an error occurs while constructing nodes
     */
    protected void addPath(File path, int depth)
    throws SAXException {
        if (path.isDirectory()) {
            startNode(DIR_NODE_NAME, path);
            if (depth>0) {
                File contents[] = path.listFiles();
                for (int i=0; i<contents.length; i++) {
                    if (isIncluded(contents[i]) && !isExcluded(contents[i])) {
                        addPath(contents[i], depth-1);
                    }
                }
            }
            endNode(DIR_NODE_NAME);
        } else {
            if (isIncluded(path) && !isExcluded(path)) {
                startNode(FILE_NODE_NAME, path);
                if (path.getName().endsWith(".xml") && XPathQuery != null)
                  performXPathQuery(path);
                endNode(FILE_NODE_NAME);
            }
        }
    }

    protected void performXPathQuery(File in)
      throws SAXException {
      doc = null;
      try {
        doc = parser.parseDocument(
          SourceUtil.getInputSource(resolver.resolveURI(in.toURL().toExternalForm())));
      } catch (SAXException se) {
         this.getLogger().error("Warning:" + in.getName()
          + " is not a valid XML file. Ignoring");
      } catch (Exception e) {
         this.getLogger().error("Unable to resolve and parse file" + e);
       }
       if (doc != null) {
         NodeList nl = processor.selectNodeList(doc.getDocumentElement(), XPathQuery);
         final String id = in.getName();
         AttributesImpl attributes = new AttributesImpl();
         attributes.addAttribute("", RESULT_DOCID_ATTR, RESULT_DOCID_ATTR,
           CDATA, id);
         attributes.addAttribute("", QUERY_ATTR, QUERY_ATTR, CDATA,
           XPathQuery);
         super.contentHandler.startElement(URI, RESULT, QRESULT, attributes);
         DOMStreamer ds = new DOMStreamer(super.xmlConsumer);
         for (int i = 0; i < nl.getLength(); i++)
           ds.stream(nl.item(i));
         super.contentHandler.endElement(URI, RESULT, QRESULT);
      }
    }

    /**
     * Recycle resources
     *
     */
   public void recycle() {
      super.recycle();
      this.XPathQuery = null;
      this.attributes = null;
      this.doc = null;
    }
}

