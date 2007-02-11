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
package org.apache.cocoon.precept.stores.dom.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.cocoon.precept.Context;
import org.apache.cocoon.precept.InvalidXPathSyntaxException;
import org.apache.cocoon.precept.NoSuchNodeException;
import org.apache.cocoon.precept.Preceptor;
import org.apache.cocoon.precept.PreceptorViolationException;
import org.apache.cocoon.precept.stores.AbstractInstance;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 14, 2002
 * @version CVS $Id: InstanceImpl.java,v 1.2 2003/03/16 17:49:05 vgritsenko Exp $
 */
public class InstanceImpl extends AbstractInstance {

    private HashMap index = new HashMap();
    private Preceptor preceptor;
    private ElementNode root;

    public void setPreceptor(Preceptor preceptor) {
        this.preceptor = preceptor;
        preceptor.buildInstance(this);
    }

    private Node createNode(String xpath) throws InvalidXPathSyntaxException {
        try {
            StringBuffer currentPath = new StringBuffer();
            StringTokenizer tok = new StringTokenizer(xpath, "/", false);
            Node currentParent = root;
            boolean first = true;
            while (tok.hasMoreTokens()) {
                String level = tok.nextToken();
                if (!first) {
                    currentPath.append("/");
                }
                else {
                    first = false;
                }
                if (level.endsWith("[1]")) {
                    level = level.substring(0, level.length() - 3);
                }
                currentPath.append(level);
                Node node = (Node) index.get(currentPath.toString());
                if (node != null) {
                    getLogger().debug("found node [" + String.valueOf(currentPath) + "] in index");
                    currentParent = node;
                }
                else {
                    if (currentParent != null) {
                        if (level.startsWith("@")) {
                            if (level.indexOf("[") >= 0 || level.indexOf("]") >= 0) {
                                throw new InvalidXPathSyntaxException(level);
                            }
                            if (preceptor != null) {
                                node = new AttributeNode(level.substring(1), preceptor.getConstraintsFor(currentPath.toString()));
                            }
                            else {
                                node = new AttributeNode(level.substring(1), null);
                            }
                            getLogger().debug("creating attribute [" + String.valueOf(currentPath) + "]");
                            ((ElementNode) currentParent).addAttribute(node);
                            index.put(currentPath.toString(), node);
                            return (node);
                        }
                        else {
                            if (preceptor != null) {
                                node = new ElementNode(level, preceptor.getConstraintsFor(currentPath.toString()));
                            }
                            else {
                                node = new ElementNode(level, null);
                            }
                            getLogger().debug("creating node [" + String.valueOf(currentPath) + "]");
                            ((ElementNode) currentParent).addChild(node);
                            index.put(currentPath.toString(), node);
                        }
                    }
                    else {
                        getLogger().debug("creating root node [" + String.valueOf(currentPath) + "]");
                        if (preceptor != null) {
                            node = root = new ElementNode(level, preceptor.getConstraintsFor(currentPath.toString()));
                        }
                        else {
                            node = root = new ElementNode(level, null);
                        }
                        index.put(currentPath.toString(), node);
                    }
                }
                currentParent = node;
            }
            return (currentParent);
        }
        catch (NoSuchNodeException e) {
            getLogger().error("hm.. this should not happen!");
            return (null);
        }
    }

    private Node lookupNode(String xpath) {
        Node node = (Node) index.get(xpath);
        if (node == null) {
            node = (Node) index.get(xpath + "[1]");
        }
        return (node);
    }

    public void setValue(String xpath, Object value) throws PreceptorViolationException, InvalidXPathSyntaxException {
        setValue(xpath, value, null);
    }

    public void setValue(String xpath, Object value, Context context) throws PreceptorViolationException, InvalidXPathSyntaxException {
        Node node = lookupNode(xpath);

        if (node != null) {
            node.setValue((String) value);
        }
        else {
            if (preceptor != null) {
                getLogger().debug("checking preceptor for [" + String.valueOf(xpath) + "]");
                if (preceptor.isValidNode(xpath)) {
                    node = createNode(xpath);
                    node.setValue((String) value);
                }
                else {
                    throw new PreceptorViolationException("[" + String.valueOf(xpath) + "] is prohibited by preceptor");
                }
            }
            else {
                getLogger().debug("no preceptor");
                node = createNode(xpath);
                node.setValue((String) value);
            }
        }
    }

    public Object getValue(String xpath) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Node node = lookupNode(xpath);
        if (node != null) {
            return (node.getValue());
        }
        else {
            throw new NoSuchNodeException(xpath);
        }
    }

    public Preceptor getPreceptor() {
        return (preceptor);
    }

    public long getLastModified() {
        //NYI
        return 0;
    }

    public void toSAX(ContentHandler handler, boolean constraints) throws SAXException {
        if (root != null) {
            root.toSAX(handler, root, constraints);
        }
    }

    public String toString() {
        if (root != null) {
            StringBuffer sb = new StringBuffer();
            root.toStringBuffer(sb, root, 0);
            return (sb.toString());
        }
        else {
            return ("");
        }
    }

    public Collection getNodePaths() {
        return (Collections.unmodifiableCollection(index.keySet()));
    }
}
