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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.OutputKeys;
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
 * Ant task to patch xmlfiles.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:crafterm@fztig938.bank.dresdner.net">Marcus Crafter</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Revision: 1.4 $ $Date: 2003/05/16 07:06:10 $
 */
public final class XConfToolTask extends MatchingTask {

    private File file;
    private File directory;
    private File srcdir;

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
     * Execute task.
     */
    public void execute() throws BuildException {

        if (this.file==null) {
            throw new BuildException("file attribute is required",
                                     location);
        }

        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
        } catch (ParserConfigurationException e) {
            throw new BuildException("ParserConfigurationException: "+e);
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
                          throws TransformerException, IOException {
        // Check to see if Document is an xconf-tool document
        Element elem = component.getDocumentElement();

        String extension = file.lastIndexOf(".")>0?file.substring(file.lastIndexOf(".")+1):"";

        if ( !elem.getTagName().equals(extension)) {
            log("Skipping non xconf-tool file: "+file);
            return false;
        }

        // Get 'root' node were 'component' will be inserted into
        String xpath = elem.getAttribute("xpath");

        NodeList nodes = XPathAPI.selectNodeList(configuration, xpath);

        if (nodes.getLength()!=1) {
            log("Error in: "+file);
            throw new IOException("XPath ("+xpath+
                                  ") returned not one node, but "+
                                  nodes.getLength()+" nodes");
        }
        Node root = nodes.item(0);

        // Test that 'root' node satisfies 'component' insertion criteria
        String test = component.getDocumentElement().getAttribute("unless");

        if ((test!=null) && (test.length()>0) &&
            (XPathAPI.selectNodeList(root, test).getLength()!=0)) {
            log("Skipping: " + file, Project.MSG_DEBUG);
            return false;
        } else {
            // Test if component wants us to remove a list of nodes first
            xpath = component.getDocumentElement().getAttribute("remove");

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
            String name = component.getDocumentElement().getAttribute("add-attribute");
            String value = component.getDocumentElement().getAttribute("value");

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
            xpath = component.getDocumentElement().getAttribute("insert-before");
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
                xpath = component.getDocumentElement().getAttribute("insert-after");
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

            for (int i = 0; i<componentNodes.getLength(); i++) {
                Node node = configuration.importNode(componentNodes.item(i),
                                                     true);

                if (before==null) {
                    root.appendChild(node);
                } else {
                    root.insertBefore(node, before);
                }
            }
            return true;
        }
    }
}
