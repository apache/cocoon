/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.cocoon.xml.util;

import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.apache.xalan.xpath.XPathSupport;
import org.apache.xalan.xpath.XPath;
import org.apache.xalan.xpath.XPathProcessorImpl;
import org.apache.xalan.xpath.xml.XMLParserLiaisonDefault;
import org.apache.xalan.xpath.xml.PrefixResolverDefault;
import org.apache.xalan.xpath.XObject;

/**
 * The methods in this class are convenience methods into the
 * low-level XPath API.  We would like to eventually move these
 * methods into the XPath core, but would like to do some peer
 * review first to make sure we have it right.
 * Please note that these methods execute pure XPaths. They do not
 * implement those parts of XPath extended by XSLT, such as the
 * document() function).  If you want to install XSLT functions, you
 * have to use the low-level API.
 * These functions tend to be a little slow, since a number of objects must be
 * created for each evaluation.  A faster way is to precompile the
 * XPaths using the low-level API, and then just use the XPaths
 * over and over.
 * @see http://www.w3.org/TR/xpath
 */
public class XPathAPI
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
  public static Node selectSingleNode(Node contextNode, String str)
    throws SAXException
  {
    return selectSingleNode(contextNode, str, contextNode);
  }

  /**
   * Use an XPath string to select a single node.
   * XPath namespace prefixes are resolved from the namespaceNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return The first node found that matches the XPath, or null.
   */
  public static Node selectSingleNode(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Have the XObject return its result as a NodeSet.
    NodeList nl = selectNodeList(contextNode, str, namespaceNode);

    // Return the first node, or null
    return (nl.getLength() > 0) ? nl.item(0) : null;
  }

 /**
   * Use an XPath string to select a nodelist.
   * XPath namespace prefixes are resolved from the contextNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @return A nodelist, should never be null.
   */
  public static NodeList selectNodeList(Node contextNode, String str)
    throws SAXException
  {
    return selectNodeList(contextNode, str, contextNode);
  }

 /**
   * Use an XPath string to select a nodelist.
   * XPath namespace prefixes are resolved from the namespaceNode.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return A nodelist, should never be null.
   */
  public static NodeList selectNodeList(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Execute the XPath, and have it return the result
    XObject list = eval(contextNode, str, namespaceNode);

    // Have the XObject return its result as a NodeSet.
    return list.nodeset();

  }

 /**
   * Evaluate XPath string to an XObject.  Using this method,
   * XPath namespace prefixes will be resolved from the namespaceNode.
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return An XObject, which can be used to obtain a string, number, nodelist, etc, should never be null.
   * @see org.apache.xalan.xpath.XObject
   * @see org.apache.xalan.xpath.XNull
   * @see org.apache.xalan.xpath.XBoolean
   * @see org.apache.xalan.xpath.XNumber
   * @see org.apache.xalan.xpath.XString
   * @see org.apache.xalan.xpath.XRTreeFrag
   */
  public static XObject eval(Node contextNode, String str)
    throws SAXException
  {
    return eval(contextNode, str, contextNode);
  }

 /**
   * Evaluate XPath string to an XObject.
   * XPath namespace prefixes are resolved from the namespaceNode.
   * The implementation of this is a little slow, since it creates
   * a number of objects each time it is called.  This could be optimized
   * to keep the same objects around, but then thread-safety issues would arise.
   *
   * @param contextNode The node to start searching from.
   * @param str A valid XPath string.
   * @param namespaceNode The node from which prefixes in the XPath will be resolved to namespaces.
   * @return An XObject, which can be used to obtain a string, number, nodelist, etc, should never be null.
   * @see org.apache.xalan.xpath.XObject
   * @see org.apache.xalan.xpath.XNull
   * @see org.apache.xalan.xpath.XBoolean
   * @see org.apache.xalan.xpath.XNumber
   * @see org.apache.xalan.xpath.XString
   * @see org.apache.xalan.xpath.XRTreeFrag
   */
  public static XObject eval(Node contextNode, String str, Node namespaceNode)
    throws SAXException
  {
    // Since we don't have a XML Parser involved here, install some default support
    // for things like namespaces, etc.
    // (Changed from: XPathSupportDefault xpathSupport = new XPathSupportDefault();
    //    because XPathSupportDefault is weak in a number of areas... perhaps
    //    XPathSupportDefault should be done away with.)
    XPathSupport xpathSupport = new XMLParserLiaisonDefault();

    // Create an object to resolve namespace prefixes.
    // XPath namespaces are resolved from the input context node's document element
    // if it is a root node, or else the current context node (for lack of a better
    // resolution space, given the simplicity of this sample code).
    PrefixResolverDefault prefixResolver = new PrefixResolverDefault((contextNode.getNodeType() == Node.DOCUMENT_NODE)
                                                         ? ((Document)contextNode).getDocumentElement() :
                                                           contextNode);

    // Create the XPath object.
    XPath xpath = new XPath();

    // Create a XPath parser.
    XPathProcessorImpl parser = new XPathProcessorImpl(xpathSupport);
    parser.initXPath(xpath, str, prefixResolver);

    // Execute the XPath, and have it return the result
    return xpath.execute(xpathSupport, contextNode, prefixResolver);
  }
}
