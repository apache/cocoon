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
package org.apache.cocoon.components.xpath;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * This class defines the implementation of the {@link XPathProcessor}
 * component.
 *
 * To configure it, add the following lines in the
 * <file>cocoon.xconf</file> file:
 *
 * <pre>
 * &lt;xslt-processor class="org.apache.cocoon.components.xpath.JaxenProcessorImpl"&gt;
 * &lt;/xslt-processor&gt;
 * </pre>
 * @deprecated Use the org.apache.excalibur.xml.xpath.JaxenProcessorImpl instead.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JaxenProcessorImpl.java,v 1.1 2003/03/09 00:07:11 pier Exp $
 */
public class JaxenProcessorImpl
  extends AbstractLogEnabled
  implements XPathProcessor, ThreadSafe
{
    /**
     * Use an XPath string to select a single node. XPath namespace
     * prefixes are resolved from the context node, which may not
     * be what you want (see the next method).
     *
     * @param contextNode The node to start searching from.
     * @param str A valid XPath string.
     * @return The first node found that matches the XPath, or null.
     */
    public Node selectSingleNode(Node contextNode, String str)
    {
        try {
            DOMXPath path = new DOMXPath(str);
            return (Node)path.selectSingleNode((Object)contextNode);
        } catch (Exception e){
            // ignore it
        }
        return null;
    }

      /**
       *  Use an XPath string to select a nodelist.
       *  XPath namespace prefixes are resolved from the contextNode.
       *
       *  @param contextNode The node to start searching from.
       *  @param str A valid XPath string.
       *  @return A NodeList, should never be null.
       */
    public NodeList selectNodeList(Node contextNode, String str)
    {
        try {
            DOMXPath path = new DOMXPath(str);
            List list = path.selectNodes((Object)contextNode);
            return new NodeListEx(list);
        } catch (Exception e){
            // ignore it
        }
        return new NodeListEx();
    }

    class NodeListEx implements NodeList{
        List list = null;
        NodeListEx(){
        }
        NodeListEx(List l){
            list = l;
        }
        public Node item(int index) {
            if(list==null)
                return null;
            return (Node)list.get(index);
        }
        public int getLength(){
            if(list==null)
                return 0;
            return list.size();
        }
    }
}
