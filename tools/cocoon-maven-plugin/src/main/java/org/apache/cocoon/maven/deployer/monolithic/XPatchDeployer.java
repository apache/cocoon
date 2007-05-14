/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.maven.deployer.monolithic;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cocoon.maven.deployer.utils.CopyUtils;
import org.apache.cocoon.maven.deployer.utils.FileUtils;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO should this extend SingleFileDeployer at all?
// TODO decouple patch collecting from logic that applies patches
public class XPatchDeployer extends SingleFileDeployer {
    final List patches = new Vector();

    public XPatchDeployer(String outputDir) {
        super(outputDir, false);
    }

    public OutputStream writeResource(String documentName) throws IOException {
        getLogger().debug("catching patch: " + documentName);
        PatchCachingOutputStream out = new PatchCachingOutputStream(documentName);
        patches.add(out);
        return out;
    }

    public void addPatch(File file) throws IOException {
        getLogger().debug("catching patch: " + file.getAbsolutePath());
        PatchCachingOutputStream pcis = new PatchCachingOutputStream(file.getCanonicalPath());
        CopyUtils.copy(new FileInputStream(file), pcis);
        patches.add(pcis);
    }

    public void applyPatches(InputStream source, final String fileName) {
        try {
            if (patches.size() == 0) {
                getLogger().info("No patches to aplly");
            } else {
                getLogger().info("Applying patches to: " + fileName);
            }
            Document original = XMLUtils.parseXml(source);
            File outFile = FileUtils.createPath(new File(getBasedir(), fileName));

            Iterator it = patches.iterator();
            while (it.hasNext()) {
                PatchCachingOutputStream pcis = (PatchCachingOutputStream) it.next();
                Document component = (pcis).getPatch();

                Element elem = component.getDocumentElement();
                if (!elem.getTagName().equals("xweb")) {
                    throw new DeploymentException("not a valid xweb patch file: " + pcis.getDocumentName());
                }

                String xpath = elem.getAttribute("xpath");
                if (xpath == null) {
                    throw new DeploymentException("no xpath parameter in patch file: " + pcis.getDocumentName());
                }

                NodeList nodes = XPathAPI.selectNodeList(original, xpath);

                if (nodes.getLength() == 0) {
                    throw new DeploymentException("no matches for xpath: [" + xpath + "] in patch file: "
                            + pcis.getDocumentName());
                }
                if (nodes.getLength() > 1) {
                    throw new DeploymentException("multiple matches for xpath: [" + xpath + "] in patch file: "
                            + pcis.getDocumentName());
                }

                Node root = nodes.item(0);
                // Test that 'root' node satisfies 'component'
                // insertion criteria
                String testPath = elem.getAttribute("unless-path");
                if (testPath == null || testPath.length() == 0) {
                    // only look for old "unless" attr if
                    // unless-path is not present
                    testPath = elem.getAttribute("unless");
                }

                if (testPath != null && testPath.length() > 0 && XPathAPI.eval(root, testPath).bool()) {
                    // no test path or 'unless' condition is satisfied
                    getLogger().debug("skipping application of patch file: " + pcis.getDocumentName());
                } else {
                    // Test if component wants us to remove
                    // a list of nodes first
                    xpath = elem.getAttribute("remove");
                    if (xpath != null && xpath.length() > 0) {
                        nodes = XPathAPI.selectNodeList(original, xpath);
                        for (int i = 0, length = nodes.getLength(); i < length; i++) {
                            Node node = nodes.item(i);
                            Node parent = node.getParentNode();
                            parent.removeChild(node);
                        }
                    }
                    // Test for an attribute that needs to be
                    // added to an element
                    String name = elem.getAttribute("add-attribute");
                    String value = elem.getAttribute("value");

                    if (name != null && name.length() > 0 && value != null && root instanceof Element) {
                        ((Element) root).setAttribute(name, value);
                    }

                    // Allow multiple attributes to be added or
                    // modified
                    if (root instanceof Element) {
                        NamedNodeMap attrMap = elem.getAttributes();
                        for (int i = 0; i < attrMap.getLength(); ++i) {
                            Attr attr = (Attr) attrMap.item(i);
                            final String addAttr = "add-attribute-";
                            if (attr.getName().startsWith(addAttr)) {
                                String key = attr.getName().substring(addAttr.length());
                                ((Element) root).setAttribute(key, attr.getValue());
                            }
                        }
                    }

                    // Test if 'component' provides desired
                    // insertion point
                    xpath = elem.getAttribute("insert-before");
                    Node before = null;

                    if (xpath != null && xpath.length() > 0) {
                        nodes = XPathAPI.selectNodeList(root, xpath);
                        if (nodes.getLength() != 0) {
                            before = nodes.item(0);
                        }
                    } else {
                        xpath = elem.getAttribute("insert-after");
                        if (xpath != null && xpath.length() > 0) {
                            nodes = XPathAPI.selectNodeList(root, xpath);
                            if (nodes.getLength() != 0) {
                                before = nodes.item(nodes.getLength() - 1).getNextSibling();
                            }
                        }
                    }

                    NodeList componentNodes = component.getDocumentElement().getChildNodes();
                    for (int i = 0; i < componentNodes.getLength(); i++) {
                        Node node = original.importNode(componentNodes.item(i), true);

                        if (before == null) {
                            root.appendChild(node);
                        } else {
                            root.insertBefore(node, before);
                        }
                    }
                }
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));
            try {
                getLogger().debug("Deploying resource file to " + fileName);
                transformer.transform(new DOMSource(original), new StreamResult(os));
            } finally {
                IOUtils.closeQuietly(os);
            }
        } catch (FileNotFoundException e) {
            throw new DeploymentException("Can't write to nonexistant file " + fileName, e);
        } catch (IOException e) {
            throw new DeploymentException("Can't write to " + fileName, e);
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Unable to configure parser " + fileName, e);
        } catch (SAXException e) {
            throw new DeploymentException("Unable to parse XML " + fileName, e);
        } catch (TransformerConfigurationException e) {
            throw new DeploymentException("Unable to configure transformer " + fileName, e);
        } catch (TransformerException e) {
            throw new DeploymentException("Unable to transform XML " + fileName, e);
        }
    }

    private class PatchCachingOutputStream extends ByteArrayOutputStream {
        private String documentName;

        public String getDocumentName() {
            return documentName;
        }

        public PatchCachingOutputStream(String documentName) {
            this.documentName = documentName;
        }

        public Document getPatch() throws SAXException, IOException, ParserConfigurationException {
            return XMLUtils.parseXml(new ByteArrayInputStream(this.buf, 0, this.count));
        }
    }
}
