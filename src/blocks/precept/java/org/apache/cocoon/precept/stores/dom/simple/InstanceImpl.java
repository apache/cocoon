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
 * @version CVS $Id: InstanceImpl.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
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
