/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Add components to the cocoon.xconf.
 * This is an ugly second shot.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:crafterm@fztig938.bank.dresdner.net">Marcus Crafter</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Revision: 1.2 $ $Date: 2003/03/11 15:29:13 $
 */

public final class XConfToolTask extends Task {

    private String configuration;
    private String directory;
    private String extension;

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void execute() throws BuildException {

        if (this.configuration == null) {
            throw new BuildException("configuration attribute is required", location);
        }
        if (this.extension == null) {
            throw new BuildException("extension attribute is required", location);
        }
        if (this.directory == null) {
            throw new BuildException("directory attribute is required", location);
        }

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final String file = this.project.resolveFile(this.configuration).getCanonicalPath();

            // load xml
            // System.out.println("Reading: " + file);
            final Document configuration = builder.parse((new File(file)).toURL().toExternalForm());

            // process recursive
            if (process(builder, configuration, this.project.resolveFile(this.directory), this.extension)) {
                // save xml
                System.out.println("Writing: " + file);
                transformer.transform(new DOMSource(configuration), new StreamResult(file));
            } else {
                // System.out.println("No Changes: " + file);
            }
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: " + e);
        } catch (SAXException e) {
            throw new BuildException("SAXException: " + e);
        } catch (ParserConfigurationException e) {
            throw new BuildException("ParserConfigurationException: " + e);
        } catch (IOException ioe) {
            throw new BuildException("IOException: " + ioe);
        }
    }

    /**
     * Scan recursive
     */
    private boolean process(final DocumentBuilder builder,
                         final Document configuration,
                         final File   directoryFile,
                         final String ext)
    throws IOException, BuildException, ParserConfigurationException, TransformerException, SAXException {

        boolean hasChanged = false;
        final File[] files = directoryFile.listFiles();
        if (files != null) {
            for(int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    hasChanged |= process(builder, configuration, files[i], ext);
                } else if (files[i].getName().endsWith("." + ext)) {
                    String file = files[i].getCanonicalPath();
                    try {
                        // Adds configuration snippet from the file to the configuration
                        hasChanged |= add(configuration, builder.parse((new File(file)).toURL().toExternalForm()), file);
                    } catch (SAXException e) {
                        System.out.println("Ignoring: " + file + "\n(not a valid XML)");
                    }
                }
            }
        }

        return hasChanged;
    }

    /**
     * Add entry to cocoon.xconf
     */
    private boolean add(final Document configuration,
                        final Document component,
                        String file)
    throws TransformerException, IOException {
        // Check to see if Document is an xconf-tool document
        Element elem = component.getDocumentElement();
        if (!elem.getTagName().equals(extension)) {
            System.out.println("Skipping non xconf-tool file: " + file);
            return false;
        }

        // Get 'root' node were 'component' will be inserted into
        String xpath = elem.getAttribute("xpath");

        NodeList nodes = XPathAPI.selectNodeList(configuration, xpath);
        if (nodes.getLength() != 1) {
            System.out.println("Error in: " + file);
            throw new IOException("XPath (" + xpath + ") returned not one node, but "
                    + nodes.getLength() + " nodes");
        }
        Node root = nodes.item(0);

        // Test that 'root' node satisfies 'component' insertion criteria
        String test = component.getDocumentElement().getAttribute("unless");
        if (test != null && test.length() > 0 &&
                XPathAPI.selectNodeList(root, test).getLength() != 0) {
            // System.out.println("Skipping: " + file);
            return false;
        } else {
            // Test if component wants us to remove a list of nodes first
            xpath = component.getDocumentElement().getAttribute("remove");

            Node remove = null;
            if (xpath != null && xpath.length() > 0) {
                nodes = XPathAPI.selectNodeList(configuration, xpath);

                for (int i = 0, length = nodes.getLength(); i < length; i++) {
                    Node node = nodes.item(i);
                    Node parent = node.getParentNode();
                    parent.removeChild(node);
                }
            }

            // Test for an attribute that needs to be added to an element
            String name = component.getDocumentElement().getAttribute("add-attribute");
            String value = component.getDocumentElement().getAttribute("value");
            if (name != null && name.length() > 0) {
                if (value == null)
                    throw new IOException("No attribute value specified for 'add-attribute' " + xpath);
                if (root instanceof Element)
                    ((Element)root).setAttribute(name, value);
            }


            // Test if 'component' provides desired insertion point
            xpath = component.getDocumentElement().getAttribute("insert-before");
            Node before = null;
            if (xpath != null && xpath.length() > 0) {
                nodes = XPathAPI.selectNodeList(root, xpath);
                if (nodes.getLength() != 1) {
                    System.out.println("Error in: " + file);
                    throw new IOException("XPath (" + xpath + ") returned not one node, but "
                            + nodes.getLength() + " nodes");
                }
                before = nodes.item(0);
            } else {
                xpath = component.getDocumentElement().getAttribute("insert-after");
                if (xpath != null && xpath.length() > 0) {
                    nodes = XPathAPI.selectNodeList(root, xpath);
                    if (nodes.getLength() != 1) {
                        System.out.println("Error in: " + file);
                        throw new IOException("XPath (" + xpath + ") returned not one node, but "
                                + nodes.getLength() + " nodes");
                    }
                    before = nodes.item(0).getNextSibling();
                }
            }

            // Add 'component' data into 'root' node
            System.out.println("Processing: " + file);
            NodeList componentNodes = component.getDocumentElement().getChildNodes();
            for (int i = 0; i < componentNodes.getLength(); i++ ){
                Node node = configuration.importNode(componentNodes.item(i), true);
                if (before == null) {
                    root.appendChild(node);
                } else {
                    root.insertBefore(node, before);
                }
            }
            return true;
        }
    }
}
