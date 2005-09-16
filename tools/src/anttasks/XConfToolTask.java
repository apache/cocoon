/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Ant task to patch xmlfiles.
 *
 * 
 * replace-properties no|false,anything else
 * xpath: xpath expression for context node
 * unless-path: xpath expression that must return empty node set
 * unless: (deprecated) xpath expression that must return empty node set
 * if-prop: use path file only when project property is set
 * remove: xpath expression to remove before adding nodes
 * add-comments: if specified, overrides the ant task value
 * add-attribute: name of attribute to add to context node (requires value)
 * add-attribute-<i>name</i>: add attribute <i>name</i> with the specified value
 * value: value of attribute to add to context node (requires add-attribute)
 * insert-before: xpath expression, add new nodes before
 * insert-after: xpath expression, add new nodes after
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:crafterm@fztig938.bank.dresdner.net">Marcus Crafter</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id$
 */
public final class XConfToolTask extends MatchingTask {

    private static final String NL=System.getProperty("line.separator");
    private static final String FSEP=System.getProperty("file.separator");
    
    private File file;
    //private File directory;
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
    public void addConfiguredXMLCatalog(XMLCatalog newXMLCatalog) {
        this.xmlCatalog.addConfiguredXMLCatalog(newXMLCatalog);
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
    public void init() throws BuildException {
        super.init();
        xmlCatalog.setProject(this.getProject());
    }

    /**
     * Execute task.
     */
    public void execute() throws BuildException {
        if (this.file == null) {
            throw new BuildException("file attribute is required", this.getLocation());
        }
        try {
            Document document = DocumentCache.getDocument(this.file, this);
            
            if (this.srcdir == null) {
                this.srcdir = this.getProject().resolveFile(".");
            }

            DirectoryScanner scanner = getDirectoryScanner(this.srcdir);
            String[] list = scanner.getIncludedFiles();
            boolean modified = false;
            // process recursive
            File patchfile;
            ArrayList suspended = new ArrayList();
            boolean hasChanged = false;
            for (int i = 0; i < list.length; i++) {
                patchfile = new File(this.srcdir, list[i]);
                try {
                    // Adds configuration snippet from the file to the configuration
                    boolean changed = patch(document, patchfile);
                    hasChanged |= changed;
                    if (!changed) {
                        suspended.add(patchfile);
                    }
                } catch (SAXException e) {
                    log("Ignoring: "+patchfile+"\n(not a valid XML)");
                }
            }
            modified = hasChanged;

            if (hasChanged && !suspended.isEmpty()) {
                log("Try to apply suspended patch files", Project.MSG_DEBUG);
            }

            ArrayList newSuspended = new ArrayList();
            while (hasChanged && !suspended.isEmpty()) {
                hasChanged = false;
                for(Iterator i=suspended.iterator(); i.hasNext();) {
                    patchfile = (File)i.next();
                    try {
                         // Adds configuration snippet from the file to the configuration
                        boolean changed = patch(document, patchfile);
                        hasChanged |= changed;
                        if (!changed) {
                            newSuspended.add(patchfile);
                        }
                    } catch (SAXException e) {
                        log("Ignoring: "+patchfile+"\n(not a valid XML)");
                    }
                }
                suspended = newSuspended;
                newSuspended = new ArrayList();
            }

            if (!suspended.isEmpty()) {
                for(Iterator i=suspended.iterator(); i.hasNext();) {
                    patchfile = (File)i.next();
                    log("Dismiss: "+patchfile.toString(), Project.MSG_DEBUG);
                }
            }

            if (modified) {
                DocumentCache.writeDocument(this.file, document, this);
            } else {
                log("No Changes: " + this.file, Project.MSG_DEBUG);
            }
            DocumentCache.storeDocument(this.file, document, this);
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: "+e);
        } catch (SAXException e) {
            throw new BuildException("SAXException:" +e);           
        } catch (DOMException e) {
            throw new BuildException("DOMException:" +e);           
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
     * @param patchFile Patch file
     *
     * @return True, if the document was successfully patched
     */
    private boolean patch(final Document configuration,
                           final File patchFile)
                           throws TransformerException, IOException, DOMException, SAXException {

        Document component = DocumentCache.getDocument(patchFile, this);
        String filename = patchFile.toString();
                            
        // Check to see if Document is an xconf-tool document
        Element elem = component.getDocumentElement();

        String extension = filename.lastIndexOf(".")>0?filename.substring(filename.lastIndexOf(".")+1):"";
        String basename = basename(filename);

        if (!elem.getTagName().equals(extension)) {
            throw new BuildException("Not a valid xpatch file: "+filename);
        }

        String replacePropertiesStr = elem.getAttribute("replace-properties");

        boolean replaceProperties = !("no".equalsIgnoreCase(replacePropertiesStr) ||
                                      "false".equalsIgnoreCase(replacePropertiesStr));

        // Get 'root' node were 'component' will be inserted into
        String xpath = getAttribute(elem, "xpath", replaceProperties);
        if ( xpath == null ) {
            throw new IOException("Attribute 'xpath' is required.");    
        }
        NodeList nodes = XPathAPI.selectNodeList(configuration, xpath);

        // Suspend, because the xpath returned not one node
        if (nodes.getLength() !=1 ) {
            log("Suspending: "+filename, Project.MSG_DEBUG);
            return false;
        }
        Node root = nodes.item(0);

        // Test that 'root' node satisfies 'component' insertion criteria
        String testPath = getAttribute(elem, "unless-path", replaceProperties);
        if (testPath == null || testPath.length() == 0) {
            // only look for old "unless" attr if unless-path is not present
            testPath = getAttribute(elem, "unless", replaceProperties);
        }
        // Is if-path needed?
        String ifProp = getAttribute(elem, "if-prop", replaceProperties);
        boolean ifValue = false;
        if (ifProp != null && !ifProp.equals("")) {
            ifValue = Boolean.valueOf(this.getProject().getProperty(ifProp)).booleanValue();
        }

        if (ifProp != null && ifProp.length() > 0 && !ifValue ) {
            log("Skipping: " + filename, Project.MSG_DEBUG);
            return false;
        } else if (testPath != null && testPath.length() > 0 &&
            XPathAPI.eval(root, testPath).bool()) {
            log("Skipping: " + filename, Project.MSG_DEBUG);
            return false;
        } else {
            // Test if component wants us to remove a list of nodes first
            xpath = getAttribute(elem, "remove", replaceProperties);

            if (xpath != null && xpath.length() > 0) {
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

            if (name != null && name.length() > 0) {
                if (value == null) {
                    throw new IOException("No attribute value specified for 'add-attribute' "+
                                          xpath);
                }
                if (root instanceof Element) {
                    ((Element) root).setAttribute(name, value);
                }
            }
 
            // Override addComments from ant task if specified as an attribute
            String addCommentsAttr = getAttribute(elem, "add-comments", replaceProperties);
            if ((addCommentsAttr!=null) && (addCommentsAttr.length()>0)) {
                setAddComments(Boolean.valueOf(addCommentsAttr));
            }

            // Allow multiple attributes to be added or modified
            if (root instanceof Element) {
                NamedNodeMap attrMap = elem.getAttributes();
                for (int i=0; i<attrMap.getLength(); ++i){
                    Attr attr = (Attr)attrMap.item(i);
                    final String addAttr = "add-attribute-";
                    if (attr.getName().startsWith(addAttr)) {
                        String key = attr.getName().substring(addAttr.length());
                        ((Element) root).setAttribute(key, attr.getValue());
                    }
                }
            }

            // Test if 'component' provides desired insertion point
            xpath = getAttribute(elem, "insert-before", replaceProperties);
            Node before = null;

            if (xpath != null && xpath.length() > 0) {
                nodes = XPathAPI.selectNodeList(root, xpath);
                if (nodes.getLength() == 0) {
                    log("Error in: "+filename);
                    throw new IOException("XPath ("+xpath+") returned zero nodes");
                }
                before = nodes.item(0);
            } else {
                xpath = getAttribute(elem, "insert-after", replaceProperties);
                if (xpath != null && xpath.length() > 0) {
                    nodes = XPathAPI.selectNodeList(root, xpath);
                    if (nodes.getLength() == 0) {
                        log("Error in: "+filename);
                        throw new IOException("XPath ("+xpath+") zero nodes.");
                    }
                    before = nodes.item(nodes.getLength()-1).getNextSibling();
                }
            }

            // Add 'component' data into 'root' node
            log("Processing: "+filename);
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
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
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
    private String basename(String fileName) {
        int start = fileName.lastIndexOf(FSEP)+1; // last '/'
        int end = fileName.lastIndexOf(".");  // last '.'

        if (end == 0) {
            end = fileName.length();
        }
        return fileName.substring(start, end);
    }
}
