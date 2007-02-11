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
package org.apache.cocoon.bean.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.helpers.OutputStreamListener;
import org.apache.commons.lang.BooleanUtils;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static class for configuring a CocoonBean from a DOM Document object
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: BeanConfigurator.java,v 1.8 2004/03/28 20:51:24 antonio Exp $
 */
public class BeanConfigurator {

    private static final String NODE_ROOT = "cocoon";
    private static final String ATTR_VERBOSE = "verbose";

    private static final String NODE_LOGGING = "logging";
    private static final String ATTR_LOG_KIT = "log-kit";
    private static final String ATTR_LOG_LEVEL = "level";
    private static final String ATTR_LOGGER = "logger";

    private static final String NODE_CONTEXT_DIR = "context-dir";
    private static final String NODE_DEST_DIR = "dest-dir";
    private static final String NODE_WORK_DIR = "work-dir";
    private static final String NODE_CONFIG_FILE = "config-file";
    private static final String NODE_URI_FILE = "uri-file";
    private static final String NODE_CHECKSUMS_URI = "checksums-uri";
 
    private static final String ATTR_CONTEXT_DIR = "context-dir";
    private static final String ATTR_DEST_DIR = "dest-dir";
    private static final String ATTR_WORK_DIR = "work-dir";
    private static final String ATTR_CONFIG_FILE = "config-file";
    private static final String ATTR_URI_FILE = "uri-file";
    private static final String ATTR_CHECKSUMS_URI = "checksums-uri";
    private static final String ATTR_AGENT = "user-agent";
    private static final String ATTR_ACCEPT = "accept";
    private static final String ATTR_DEFAULT_FILENAME = "default-filename";
     
    private static final String NODE_BROKEN_LINKS = "broken-links";
    private static final String ATTR_BROKEN_LINK_REPORT_TYPE = "type";
    private static final String ATTR_BROKEN_LINK_REPORT_FILE = "file";
    private static final String ATTR_BROKEN_LINK_GENERATE = "generate";
    private static final String ATTR_BROKEN_LINK_EXTENSION = "extension";

    private static final String NODE_AGENT = "user-agent";
    private static final String NODE_ACCEPT = "accept";

    private static final String ATTR_FOLLOW_LINKS = "follow-links";
    private static final String ATTR_PRECOMPILE_ONLY = "precompile-only";
    private static final String ATTR_CONFIRM_EXTENSIONS = "confirm-extensions";
    private static final String NODE_LOAD_CLASS = "load-class";
    private static final String NODE_DEFAULT_FILENAME = "default-filename";

    private static final String NODE_INCLUDE = "include";
    private static final String NODE_EXCLUDE = "exclude";
    private static final String ATTR_INCLUDE_EXCLUDE_PATTERN = "pattern";
    
    private static final String NODE_INCLUDE_LINKS = "include-links";
    private static final String ATTR_LINK_EXTENSION = "extension";
    
    private static final String NODE_URI = "uri";
    private static final String ATTR_URI_TYPE = "type";
    private static final String ATTR_URI_SOURCEPREFIX = "src-prefix";
    private static final String ATTR_URI_SOURCEURI = "src";
    private static final String ATTR_URI_DESTURI = "dest";

    private static final String NODE_URIS = "uris";
    private static final String ATTR_NAME = "name";
    
    public static String configure(Document xconf, CocoonBean cocoon, String destDir, String uriGroup, OutputStreamListener listener) 
        throws IllegalArgumentException {

        Node root = xconf.getDocumentElement();
        if (!NODE_ROOT.equals(root.getNodeName())) {
            throw new IllegalArgumentException("Expected root node of "+ NODE_ROOT);
        }

        if (hasAttribute(root, ATTR_VERBOSE)) {
            cocoon.setVerbose(getBooleanAttributeValue(root, ATTR_VERBOSE));
        }
        if (hasAttribute(root, ATTR_FOLLOW_LINKS)) {
            cocoon.setFollowLinks(getBooleanAttributeValue(root, ATTR_FOLLOW_LINKS));
        }
        if (hasAttribute(root, ATTR_PRECOMPILE_ONLY)) {
            cocoon.setPrecompileOnly(getBooleanAttributeValue(root, ATTR_PRECOMPILE_ONLY));
        }
        if (hasAttribute(root, ATTR_CONFIRM_EXTENSIONS)) {
            cocoon.setConfirmExtensions(getBooleanAttributeValue(root, ATTR_CONFIRM_EXTENSIONS));
        }
        if (hasAttribute(root, ATTR_CONTEXT_DIR)) {
            cocoon.setContextDir(getAttributeValue(root, ATTR_CONTEXT_DIR));
        }
        if (hasAttribute(root, ATTR_DEST_DIR)) {
            destDir = getAttributeValue(root, ATTR_DEST_DIR);
        }
        if (hasAttribute(root, ATTR_WORK_DIR)) {
            cocoon.setWorkDir(getAttributeValue(root, ATTR_WORK_DIR));
        }
        if (hasAttribute(root, ATTR_CONFIG_FILE)) {
            cocoon.setConfigFile(getAttributeValue(root, ATTR_CONFIG_FILE));
        }
        if (hasAttribute(root, ATTR_URI_FILE)) {
            cocoon.addTargets(processURIFile(getAttributeValue(root, ATTR_URI_FILE)), destDir);
        }
        if (hasAttribute(root, ATTR_CHECKSUMS_URI)) {
            cocoon.setChecksumURI(getAttributeValue(root, ATTR_CHECKSUMS_URI));
        }
        if (hasAttribute(root, ATTR_AGENT)) {
            cocoon.setAgentOptions(getAttributeValue(root, ATTR_AGENT));
        }
        if (hasAttribute(root, ATTR_ACCEPT)) {
            cocoon.setAcceptOptions(getAttributeValue(root, ATTR_ACCEPT));
        }
        if (hasAttribute(root, ATTR_DEFAULT_FILENAME)) {
            cocoon.setDefaultFilename(getAttributeValue(root, ATTR_DEFAULT_FILENAME));
        }

        if (destDir == null || destDir.length() == 0) {
            destDir = getNodeValue(root, NODE_DEST_DIR);
        }
 
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()== Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                if (nodeName.equals(NODE_BROKEN_LINKS)) {
                    parseBrokenLinkNode(cocoon, node, listener);

                } else if (nodeName.equals(NODE_LOAD_CLASS)) {
                    cocoon.addLoadedClass(getNodeValue(node));

                } else if (nodeName.equals(NODE_LOGGING)) {
                    parseLoggingNode(cocoon, node);

                } else if (nodeName.equals(NODE_CONTEXT_DIR)) {
                    if (hasAttribute(root, ATTR_CONTEXT_DIR)) {
                        throw new IllegalArgumentException("Cannot have "+NODE_CONTEXT_DIR+" as both element and attribute");
                    } else {
                        cocoon.setContextDir(getNodeValue(node));
                    }

                } else if (nodeName.equals(NODE_CONFIG_FILE)) {
                    if (hasAttribute(root, ATTR_CONFIG_FILE)) {
                        throw new IllegalArgumentException("Cannot have "+NODE_CONFIG_FILE+" as both element and attribute");
                    } else {
                        cocoon.setConfigFile(getNodeValue(node));
                    }
                } else if (nodeName.equals(NODE_DEST_DIR)) {
                    // Ignore

                } else if (nodeName.equals(NODE_WORK_DIR)) {
                    if (hasAttribute(root, ATTR_WORK_DIR)) {
                        throw new IllegalArgumentException("Cannot have "+NODE_WORK_DIR+" as both element and attribute");
                    } else {
                        cocoon.setWorkDir(getNodeValue(node));
                    }
                } else if (nodeName.equals(NODE_CHECKSUMS_URI)) {
                    if (hasAttribute(root, ATTR_CHECKSUMS_URI)) {
                        throw new IllegalArgumentException("Cannot have "+NODE_CHECKSUMS_URI+" as both element and attribute");
                    } else {
                        cocoon.setChecksumURI(getNodeValue(node));
                    }                
                } else if (nodeName.equals(NODE_AGENT)) {
                    cocoon.setAgentOptions(getNodeValue(node));

                } else if (nodeName.equals(NODE_ACCEPT)) {
                    cocoon.setAcceptOptions(getNodeValue(node));

                } else if (nodeName.equals(NODE_DEFAULT_FILENAME)) {
                    cocoon.setDefaultFilename(getNodeValue(node));

                } else if (nodeName.equals(NODE_INCLUDE)) {
                    String pattern = parseIncludeExcludeNode(cocoon, node, NODE_INCLUDE);
                    cocoon.addIncludePattern(pattern);

                } else if (nodeName.equals(NODE_EXCLUDE)) {
                    String pattern = parseIncludeExcludeNode(cocoon, node, NODE_EXCLUDE);
                    cocoon.addExcludePattern(pattern);

                } else if (nodeName.equals(NODE_INCLUDE_LINKS)) {
                    parseIncludeLinksNode(cocoon, node);

                } else if (nodeName.equals(NODE_URI)) {
                    parseURINode(cocoon, node, destDir);

                } else if (nodeName.equals(NODE_URIS)) {
                    parseURIsNode(cocoon, node, destDir, uriGroup);

                } else if (nodeName.equals(NODE_URI_FILE)) {
                    if (hasAttribute(root, ATTR_URI_FILE)) {
                        throw new IllegalArgumentException("Cannot have "+NODE_URI_FILE+" as both element and attribute");
                    } else {
                        cocoon.addTargets(processURIFile(getNodeValue(node)), destDir);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown element: <" + nodeName + ">");
                }
            }
        }
        return destDir;
    }

    private static void parseLoggingNode(CocoonBean cocoon, Node node) throws IllegalArgumentException {
        if (hasAttribute(node, ATTR_LOG_KIT)) {
            cocoon.setLogKit(getAttributeValue(node, ATTR_LOG_KIT));
        }
        if (hasAttribute(node, ATTR_LOGGER)) {
            cocoon.setLogger(getAttributeValue(node, ATTR_LOGGER));
        }
        if (hasAttribute(node, ATTR_LOG_LEVEL)) {
            cocoon.setLogLevel(getAttributeValue(node, ATTR_LOG_LEVEL));
        }
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength()!=0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_LOGGING + "> node");
        }
    }

    private static void parseIncludeLinksNode(CocoonBean cocoon, Node node) throws IllegalArgumentException {
        if (hasAttribute(node, ATTR_LINK_EXTENSION)) {
            cocoon.addIncludeLinkExtension(getAttributeValue(node, ATTR_LINK_EXTENSION));
        }
    }

    private static void parseBrokenLinkNode(CocoonBean cocoon, Node node, OutputStreamListener listener) throws IllegalArgumentException {
        if (hasAttribute(node, ATTR_BROKEN_LINK_REPORT_FILE)) {
            listener.setReportFile(getAttributeValue(node, ATTR_BROKEN_LINK_REPORT_FILE));
        }
        if (hasAttribute(node, ATTR_BROKEN_LINK_REPORT_TYPE)) {
            listener.setReportType(getAttributeValue(node, ATTR_BROKEN_LINK_REPORT_TYPE));
        }
        if (hasAttribute(node, ATTR_BROKEN_LINK_GENERATE)) {
        cocoon.setBrokenLinkGenerate(getBooleanAttributeValue(node, ATTR_BROKEN_LINK_GENERATE));
        }
        if (hasAttribute(node, ATTR_BROKEN_LINK_EXTENSION)) {
        cocoon.setBrokenLinkExtension(getAttributeValue(node, ATTR_BROKEN_LINK_EXTENSION));
        }
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength()!=0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_BROKEN_LINKS + "> node");
        }
    }

    private static String parseIncludeExcludeNode(CocoonBean cocoon, Node node, final String NODE_TYPE) throws IllegalArgumentException {
        NodeList nodes = node.getChildNodes();
        if (nodes.getLength() != 0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_INCLUDE + "> node");
        }

        if (hasAttribute(node, ATTR_INCLUDE_EXCLUDE_PATTERN)) {
            return getAttributeValue(node, ATTR_INCLUDE_EXCLUDE_PATTERN);
        } else {
            throw new IllegalArgumentException("Expected a "+ATTR_INCLUDE_EXCLUDE_PATTERN+" attribute for <"+NODE_TYPE+"> node");
        }
    }

    private static void parseURIsNode(CocoonBean cocoon, Node node, String destDir, String uriGroup) throws IllegalArgumentException {

        boolean followLinks = cocoon.followLinks();
        boolean confirmExtensions = cocoon.confirmExtensions();
        String logger = cocoon.getLoggerName();
        String destURI = destDir;
        String root = null;
        String type = null;
        String name = null;
        
        if (hasAttribute(node, ATTR_FOLLOW_LINKS)) {
            followLinks = getBooleanAttributeValue(node, ATTR_FOLLOW_LINKS);
        }
        if (hasAttribute(node, ATTR_CONFIRM_EXTENSIONS)) {
            confirmExtensions = getBooleanAttributeValue(node, ATTR_CONFIRM_EXTENSIONS);
        }
        if (hasAttribute(node, ATTR_URI_TYPE)) {
            type = getAttributeValue(node, ATTR_URI_TYPE);
        }
        if (hasAttribute(node, ATTR_URI_SOURCEPREFIX)) {
            root = getAttributeValue(node, ATTR_URI_SOURCEPREFIX);
        }
        if (hasAttribute(node, ATTR_URI_DESTURI)) {
            destURI = getAttributeValue(node, ATTR_URI_DESTURI);
        }
        if (hasAttribute(node, ATTR_LOGGER)) {
            logger = getAttributeValue(node, ATTR_LOGGER);
        }
        if (hasAttribute(node, ATTR_NAME)) {
            name = getAttributeValue(node, ATTR_NAME);
            if (name != null && uriGroup != null && !name.equals(uriGroup)) {
                // The user has not selected this URI group, so ignore it.
                return;
            }
        }
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            if (child.getNodeType()== Node.ELEMENT_NODE) {
                String childName = child.getNodeName();
                if (childName.equals(NODE_URI)) {
                    String _sourceURI = null;
                    String _destURI = destURI;
                    String _root = root;
                    String _type = type;
                    
                    if (child.getAttributes().getLength() == 0) {
                        _sourceURI = getNodeValue(child);
                        //destURI is inherited 
                    } else {
                        _sourceURI = getAttributeValue(child, ATTR_URI_SOURCEURI);

                        if (hasAttribute(child, ATTR_URI_TYPE)) {
                            _type = getAttributeValue(child, ATTR_URI_TYPE);
                        }
                        if (hasAttribute(child, ATTR_URI_SOURCEPREFIX)) {
                            _root = getAttributeValue(child, ATTR_URI_SOURCEPREFIX);
                        }
                        if (hasAttribute(child, ATTR_URI_DESTURI)) {
                            _destURI = getAttributeValue(child, ATTR_URI_DESTURI);
                        }
                    }
                    cocoon.addTarget(_type, _root, _sourceURI, _destURI, followLinks, confirmExtensions, logger);
                } else {
                    throw new IllegalArgumentException("Unknown element: <" + childName + ">");
                }
            }
        }
    }
        
    private static void parseURINode(CocoonBean cocoon, Node node, String destDir) throws IllegalArgumentException {
        NodeList nodes = node.getChildNodes();

        if (node.getAttributes().getLength() == 0 && nodes.getLength() != 0) {
            cocoon.addTarget(getNodeValue(node), destDir);
        } else if (node.getAttributes().getLength() !=0 && nodes.getLength() ==0){

            String src = getAttributeValue(node, ATTR_URI_SOURCEURI);

            String type = null;
            if (hasAttribute(node, ATTR_URI_TYPE)) {
                type = getAttributeValue(node, ATTR_URI_TYPE);
            }
            String root = null;
            if (hasAttribute(node, ATTR_URI_SOURCEPREFIX)) {
                root = getAttributeValue(node, ATTR_URI_SOURCEPREFIX);
            }
            String dest = null;
            if (hasAttribute(node, ATTR_URI_DESTURI)) {
                dest = getAttributeValue(node, ATTR_URI_DESTURI);
            }

            if (root != null && type != null & dest != null) {
                cocoon.addTarget(type, root, src, dest);
            } else if (root != null & dest != null) {
                cocoon.addTarget(root, src, dest);
            } else if (dest != null) {
                cocoon.addTarget(src, dest);
            } else {
                cocoon.addTarget(src, destDir);
            }
        } else if (node.getAttributes().getLength() !=0 && nodes.getLength() != 0) {
            throw new IllegalArgumentException("Unexpected children of <" + NODE_URI + "> node");
        } else {
            throw new IllegalArgumentException("Not enough information for <"+ NODE_URI + "> node");
        }
    }

    private static String getNodeValue(Node root, String name) throws IllegalArgumentException {
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()== Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                if (nodeName.equals(name)) {
                    return getNodeValue(node);
                }
            }
        }
        return null;
    }

    private static String getNodeValue(Node node) throws IllegalArgumentException {
        StringBuffer s = new StringBuffer();
        NodeList children = node.getChildNodes();
        boolean found = false;

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                s.append(child.getNodeValue());
                found = true;
            } else {
                throw new IllegalArgumentException("Unexpected node:" + child.getLocalName());
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Expected value for " + node.getLocalName() + " node");
        }
        return s.toString();
    }

    private static String getAttributeValue(Node node, String attr) throws IllegalArgumentException {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            if (attribute != null && attribute.getNodeValue() != null) {
                return attribute.getNodeValue();
            }
        }
        throw new IllegalArgumentException("Missing " + attr + " attribute on <" + node.getNodeName() + "> node");
    }

    private static boolean hasAttribute(Node node, String attr) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);
            return (attribute != null);
        }
        return false;
    }

    private static boolean getBooleanAttributeValue(Node node, String attr) {
        NamedNodeMap nodes = node.getAttributes();
        if (nodes != null) {
            Node attribute = nodes.getNamedItem(attr);

            if (attribute != null) {
                String value = attribute.getNodeValue();
                return BooleanUtils.toBoolean(value);
            }
        }
        return false;
    }

    /**
     * <code>processURIFile</code> method.
     *
     * @param filename a <code>String</code> value
     * @return uris a <code>List</code> of URIs
     */
    public static List processURIFile(String filename) {
        List uris = new ArrayList();
        try {
            BufferedReader uriFile = new BufferedReader(new FileReader(filename));

            while (true) {
                String uri = uriFile.readLine();

                if (null == uri) {
                    break;
                }
                
                uri = uri.trim();
                if (!uri.equals("") && !uri.startsWith("#")){
                    uris.add(uri.trim());
                }
            }

            uriFile.close();
        } catch (Exception e) {
            // ignore errors.
        }
        return uris;
    }

}
