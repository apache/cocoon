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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Ant task to patch xmlfiles.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:crafterm@fztig938.bank.dresdner.net">Marcus Crafter</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Revision: 1.10 $ $Date: 2003/11/23 10:13:11 $
 */
public final class XConfToolTask extends MatchingTask {

    private static final String NL=System.getProperty("line.separator");
    private static final String FSEP=System.getProperty("file.separator");
    private File file;
    private File directory;
    private File srcdir;
    private boolean addComments;
    /** for resolving entities such as dtds */
    private XMLCatalog xmlCatalog = new XMLCatalog();

    /**
     * Set file, which should be patched.
     *
     * @param file File, which should be patched.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Set base directory for the patch files.
     *
     * @param srcdir Base directory for the patch files.
     */
    public void setSrcdir(File srcdir) {
        this.srcdir = srcdir;
    }

    /**
     * Add the catalog to our internal catalog
     *
     * @param xmlCatalog the XMLCatalog instance to use to look up DTDs
     */
    public void addConfiguredXMLCatalog(XMLCatalog xmlCatalog)
    {
      this.xmlCatalog.addConfiguredXMLCatalog(xmlCatalog);
    }

    /**
     * Whether to add a comment indicating where this block of code comes
     * from.
     */
    public void setAddComments(Boolean addComments) {
        this.addComments = addComments.booleanValue();
    }

    /**
     * Initialize internal instance of XMLCatalog
     */
    public void init() throws BuildException
    {
      super.init();
      xmlCatalog.setProject(project);
    }

    /**
     * Execute task.
     */
    public void execute() throws BuildException {

        if (this.file==null) {
            throw new BuildException("file attribute is required",
                                     location);
        }

        try {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setExpandEntityReferences(false);
            builderFactory.setNamespaceAware(false);
            builderFactory.setAttribute(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                new Boolean(false));
            final DocumentBuilder builder = builderFactory.newDocumentBuilder();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // load xml
            log("Reading: " + this.file, Project.MSG_DEBUG);
            final Document document = builder.parse(this.file.toURL().toExternalForm());

            if (this.srcdir==null)
                this.srcdir = project.resolveFile(".");

            DirectoryScanner scanner = getDirectoryScanner(this.srcdir);

            String[] list = scanner.getIncludedFiles();

            boolean hasChanged = false;
            // process recursive
            File patchfile;
            for (int i = 0; i<list.length; i++) {
                patchfile = new File(this.srcdir, list[i]);
                try {
                    // Adds configuration snippet from the file to the configuration
                    hasChanged |= patch(document,
                                        builder.parse(patchfile.toURL().toExternalForm()),
                                        patchfile.toString());
                } catch (SAXException e) {
                    log("Ignoring: "+patchfile+"\n(not a valid XML)");
                }
            }

            if (hasChanged) {
                log("Writing: "+this.file);
                // Set the DOCTYPE output option on the transformer 
                // if we have any DOCTYPE declaration in the input xml document
                final DocumentType doctype = document.getDoctype();
                if (null != doctype && null != doctype.getPublicId()) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
                }
                transformer.transform(new DOMSource(document),
                                      new StreamResult(this.file));
            } else {
                log("No Changes: " + this.file, Project.MSG_DEBUG);
            }
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: "+e);
        } catch (SAXException e) {
            throw new BuildException("SAXException: "+e);
        } catch (DOMException e) {
            throw new BuildException("DOMException:" +e);           
        } catch (ParserConfigurationException e) {
            throw new BuildException("ParserConfigurationException: "+e);
        } catch (UnknownHostException e) {
            throw new BuildException("UnknownHostException.  Probable cause: The parser is " +
                "trying to resolve a dtd from the internet and no connection exists.\n" +
                "You can either connect to the internet during the build, or patch \n" +
                "XConfToolTask.java to ignore DTD declarations when your parser is in use.");
        } catch (IOException ioe) {
            throw new BuildException("IOException: "+ioe);
        }
    }

    /**
     * Patch XML document with a given patch file.
     *
     * @param configuration Orginal document
     * @param component Patch document
     * @param file Patch file
     *
     * @return True, if the document was successfully patched
     */
    private boolean patch(final Document configuration,
                          final Document component,
                          String file)
                          throws TransformerException, IOException, DOMException {
        // Check to see if Document is an xconf-tool document
        Element elem = component.getDocumentElement();

        String extension = file.lastIndexOf(".")>0?file.substring(file.lastIndexOf(".")+1):"";
        String basename = basename(file);

        if ( !elem.getTagName().equals(extension)) {
            log("Skipping non xconf-tool file: "+file);
            return false;
        }

        String replacePropertiesStr = elem.getAttribute("replace-properties");

        boolean replaceProperties = !("no".equalsIgnoreCase(replacePropertiesStr) ||
                                      "false".equalsIgnoreCase(replacePropertiesStr));

        // Get 'root' node were 'component' will be inserted into
        String xpath = getAttribute(elem, "xpath", replaceProperties);

        NodeList nodes = XPathAPI.selectNodeList(configuration, xpath);

        if (nodes.getLength()!=1) {
            log("Error in: "+file);
            throw new IOException("XPath ("+xpath+
                                  ") returned not one node, but "+
                                  nodes.getLength()+" nodes");
        }
        Node root = nodes.item(0);

        // Test that 'root' node satisfies 'component' insertion criteria
        String testPath = getAttribute(elem, "unless-path", replaceProperties);
        if (testPath == null || testPath.length()==0) {
            // only look for old "unless" attr if unless-path is not present
            testPath = getAttribute(elem, "unless", replaceProperties);
        }
        // Is if-path needed?
        String ifProp = getAttribute(elem, "if-prop", replaceProperties);
        boolean ifValue = Boolean.valueOf(project.getProperty(ifProp)).booleanValue();

        if (ifProp != null && (ifProp.length()>0) && !ifValue ) {
            log("Skipping: " + file, Project.MSG_DEBUG);
            return false;
        } else if ((testPath!=null) && (testPath.length()>0) &&
            (XPathAPI.selectNodeList(root, testPath).getLength()!=0)) {
            log("Skipping: " + file, Project.MSG_DEBUG);
            return false;
        } else {
            // Test if component wants us to remove a list of nodes first
            xpath = getAttribute(elem, "remove", replaceProperties);

            Node remove = null;

            if ((xpath!=null) && (xpath.length()>0)) {
                nodes = XPathAPI.selectNodeList(configuration, xpath);

                for (int i = 0, length = nodes.getLength(); i<length; i++) {
                    Node node = nodes.item(i);
                    Node parent = node.getParentNode();

                    parent.removeChild(node);
                }
            }

            // Test for an attribute that needs to be added to an element
            String name = getAttribute(elem, "add-attribute", replaceProperties);
            String value = getAttribute(elem, "value", replaceProperties);

            if ((name!=null) && (name.length()>0)) {
                if (value==null) {
                    throw new IOException("No attribute value specified for 'add-attribute' "+
                                          xpath);
                }
                if (root instanceof Element) {
                    ((Element) root).setAttribute(name, value);
                }
            }

            // Test if 'component' provides desired insertion point
            xpath = getAttribute(elem, "insert-before", replaceProperties);
            Node before = null;

            if ((xpath!=null) && (xpath.length()>0)) {
                nodes = XPathAPI.selectNodeList(root, xpath);
                if (nodes.getLength()!=1) {
                    log("Error in: "+file);
                    throw new IOException("XPath ("+xpath+
                                          ") returned not one node, but "+
                                          nodes.getLength()+" nodes");
                }
                before = nodes.item(0);
            } else {
                xpath = getAttribute(elem, "insert-after", replaceProperties);
                if ((xpath!=null) && (xpath.length()>0)) {
                    nodes = XPathAPI.selectNodeList(root, xpath);
                    if (nodes.getLength()!=1) {
                        log("Error in: "+file);
                        throw new IOException("XPath ("+xpath+
                                              ") returned not one node, but "+
                                              nodes.getLength()+" nodes");
                    }
                    before = nodes.item(0).getNextSibling();
                }
            }

            // Add 'component' data into 'root' node
            log("Processing: "+file);
            NodeList componentNodes = component.getDocumentElement().getChildNodes();

            if (this.addComments) {
                root.appendChild(configuration.createComment("..... Start configuration from '"+basename+"' "));
                root.appendChild(configuration.createTextNode(NL));
            }
            for (int i = 0; i<componentNodes.getLength(); i++) {
                Node node = configuration.importNode(componentNodes.item(i),
                                                     true);

                if (replaceProperties) {
                    replaceProperties(node);
                }
                if (before==null) {
                    root.appendChild(node);
                } else {
                    root.insertBefore(node, before);
                }
            }
            if (this.addComments) {
                root.appendChild(configuration.createComment("..... End configuration from '"+basename+"' "));
                root.appendChild(configuration.createTextNode(NL));
            }
            return true;
        }
    }

    private String getAttribute(Element elem, String attrName, boolean replaceProperties) {
        String attr = elem.getAttribute(attrName);
        if (attr == null) {
            return null;
        } else if (replaceProperties) {
            return getProject().replaceProperties(attr);
        } else {
            return attr;
        }
    }

    private void replaceProperties(Node n) throws DOMException {

        NamedNodeMap attrs = n.getAttributes();
        if (attrs!=null) {
            for (int i = 0; i< attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                attr.setNodeValue(getProject().replaceProperties(attr.getNodeValue()));     
            } 
        }
        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE: {
                n.setNodeValue(getProject().replaceProperties(n.getNodeValue()));
                break;
            }
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.ELEMENT_NODE: {
                Node child = n.getFirstChild();
                while (child != null) {
                    replaceProperties(child);
                    child = child.getNextSibling();
                }
                break;
            }
            default: {
                // ignore all other node types
            }
        }
    }

    /** Returns the file name (excluding directories and extension). */
    private String basename(String file) {
        int start = file.lastIndexOf(FSEP)+1; // last '/'
        int end = file.lastIndexOf(".");  // last '.'

        if (end == 0) end = file.length();

        return file.substring(start, end);
    }
}
