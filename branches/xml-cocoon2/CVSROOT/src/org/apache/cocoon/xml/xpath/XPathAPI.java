/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.xml.xpath;

import org.xml.sax.SAXException;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.xpath.objects.XObject;

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
 
public class XPathAPI {
    
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
    return org.apache.xpath.XPathAPI.selectSingleNode(contextNode, str);
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
    return org.apache.xpath.XPathAPI.selectSingleNode(contextNode, str, namespaceNode);
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
    return org.apache.xpath.XPathAPI.selectNodeList(contextNode, str);
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
    return org.apache.xpath.XPathAPI.selectNodeList(contextNode, str, namespaceNode);
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
    return org.apache.xpath.XPathAPI.eval(contextNode, str);
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
    return org.apache.xpath.XPathAPI.eval(contextNode, str, namespaceNode);
  }
}
