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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Generate documentation for sitemap components based on javadoc tags
 * in the source of the component.
 * 
 * This is the first, experimental version - the code is a little bit
 * hacky but straight forward :)
 * 
 * @since 2.1.5
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.16 $ $Date: 2004/05/26 13:12:40 $
 */
public final class SitemapTask extends AbstractQdoxTask {

    /** The name of the component in the sitemap (required) */    
    public static final String NAME_TAG   = "cocoon.sitemap.component.name";
    /** The logger category (optional) */
    public static final String LOGGER_TAG = "cocoon.sitemap.component.logger";
    /** The label for views (optional) */
    public static final String LABEL_TAG  = "cocoon.sitemap.component.label";
    /** The mime type for serializers and readers (optional) */
    public static final String MIMETYPE_TAG  = "cocoon.sitemap.component.mimetype";
    /** If this tag is specified, the component is not added to the sitemap (optional) */
    public static final String HIDDEN_TAG = "cocoon.sitemap.component.hide";
    /** If this tag is specified no documentation is generated (optional) */
    public static final String NO_DOC_TAG = "cocoon.sitemap.component.documentation.disabled";
    /** The documentation (optional) */
    public static final String DOC_TAG    = "cocoon.sitemap.component.documentation";
    /** Configuration (optional) */
    public static final String CONF_TAG   = "cocoon.sitemap.component.configuration";
    /** Caching info (optional) */
    public static final String CACHING_INFO_TAG = "cocoon.sitemap.component.documentation.caching";

    /** Pooling min (optional) */
    public static final String POOL_MIN_TAG = "cocoon.sitemap.component.pooling.min";
    /** Pooling max (optional) */
    public static final String POOL_MAX_TAG = "cocoon.sitemap.component.pooling.max";
    /** Pooling grow (optional) */
    public static final String POOL_GROW_TAG = "cocoon.sitemap.component.pooling.grow";
    
    private static final String LINE_SEPARATOR = "\n";
    
    /** The sitemap */
    private File sitemap;
    
    /** The doc dir */
    private File docDir;
    
    /** Cache for classes */
    private static Map cache = new HashMap();
    
    /** The directory containing the sources*/
    private String directory;
    
    /** The name of the block */
    protected String blockName;
    
    /** Is this block deprecated? */
    protected boolean deprecated = false;
    
    /** Is this block stable? */
    protected boolean stable = true;
    
    /**
     * Set the directory containg the source files.
     * Only .java files will be scannend
     */
    public void setSource(File dir) {
        try {
            this.directory = dir.toURL().toExternalForm();
            if ( !dir.isDirectory() ) {
                throw new BuildException("Source is not a directory.");
            }
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
        FileSet set = new FileSet();
        set.setDir(dir);
        set.setIncludes("**/*.java");
        super.addFileset(set);
    }
    
    /**
     * The location of the sitemap
     */
    public void setSitemap( final File sitemap ) {
        this.sitemap = sitemap;
    }

    /**
     * The location of the documentation files
     */
    public void setDocDir( final File dir ) {
        this.docDir = dir;        
    }
    
    /**
     * Set the block name
     */
    public void setBlock(String value) {
        this.blockName = value;
    }
    
    /**
     * Is the block deprecated?
     */
    public void setDeprecated(boolean value) {
        this.deprecated = value;
    }
    
    /**
     * Is the block stable?
     */
    public void setStable(boolean value) {
        this.stable = value;
    }
    
    /**
     * Execute generator task.
     *
     * @throws BuildException if there was a problem collecting the info
     */
    public void execute()
    throws BuildException {
        validate();

        List components = (List)cache.get(this.directory);
        if ( components == null ) {
            // this does the hard work :)
            super.execute();
            components = this.collectInfo();
            cache.put(this.directory, components);
        }

        try {
            
            if ( this.sitemap != null ) {
                this.processSitemap(components);
            }
            if ( this.docDir != null ) {
                this.processDocDir(components);
            }
            
        } catch ( final BuildException e ) {
            throw e;
        } catch ( final Exception e ) {
            throw new BuildException( e.toString(), e );
        }
    }

    /**
     * Validate that the parameters are valid.
     */
    private void validate() {
        if ( this.directory == null ) {
            throw new BuildException("Source is not specified.");
        }
        if ( this.sitemap == null && this.docDir == null ) {
            throw new BuildException("Sitemap or DocDir is not specified.");
        }
        
        if ( this.sitemap != null && this.sitemap.isDirectory() ) {
            throw new BuildException( "Sitemap (" + this.sitemap + ") is not a file." );
        }
        if ( this.docDir != null && !this.docDir.isDirectory() ) {
            throw new BuildException( "DocDir (" + this.docDir + ") is not a directory." );            
        }
    }

    /**
     * Collect the component infos
     */
    private List collectInfo() {
        log("Collection sitemap components info");
        final List components = new ArrayList();
        
        final Iterator it = super.allClasses.iterator();
        
        while ( it.hasNext() ) {
            final JavaClass javaClass = (JavaClass) it.next();

            final DocletTag tag = javaClass.getTagByName( NAME_TAG );

            if ( null != tag ) {
                final SitemapComponent comp = new SitemapComponent( javaClass );

                log("Found component: " + comp, Project.MSG_DEBUG);
                components.add(comp);
            }
        }
        return components;
    }

    /**
     * Add components to sitemap
    */
    private void processSitemap(List components) 
    throws Exception {
        log("Adding sitemap components");
        Document document;
        
        document = DocumentCache.getDocument(this.sitemap, this);
        
        boolean changed = false;

        Iterator iter = components.iterator();
        while ( iter.hasNext() ) {
            SitemapComponent component = (SitemapComponent)iter.next();
            final String type = component.getType();
            final String section = type + 's';
            
            NodeList nodes = XPathAPI.selectNodeList(document, "/sitemap/components/" + section);

            if (nodes.getLength() != 1 ) {
                throw new BuildException("Unable to find section for component type " + type);
            }
            // remove old node!
            NodeList oldNodes = XPathAPI.selectNodeList(document, 
                    "/sitemap/components/" + section + '/' + type + "[@name='" + component.getName() + "']");
            for(int i=0; i < oldNodes.getLength(); i++ ) {
                final Node node = oldNodes.item(i);
                node.getParentNode().removeChild(node);
            }
            
            // and add it again
            if (component.append(nodes.item(0)) ) {
                changed = true;
            }
            
        }
        
        if ( changed ) {
            DocumentCache.writeDocument(this.sitemap, document, this);
        }
        DocumentCache.storeDocument(this.sitemap, document, this);
    }
    
    /**
     * Add components to sitemap
    */
    private void processDocDir(List components) 
    throws Exception {
        log("Generating documentation");

        Iterator iter = components.iterator();
        while ( iter.hasNext() ) {
            final SitemapComponent component = (SitemapComponent)iter.next();
            
            // Read template
            final File templateFile = this.getProject().resolveFile("src/documentation/templates/sitemap-component.xml");
            Document template = DocumentCache.getDocument(templateFile, this);
            
            // create directory - if required
            final File componentsDir = new File(this.docDir, component.getType()+'s');
            componentsDir.mkdir();
            
            // get file name
            String fileName = component.getName() + "-" + component.getType() + ".xml";
            if ( this.blockName != null ) {
                fileName = this.blockName + "-" + fileName;
            }
            final File docFile = new File(componentsDir, fileName);
            
            // generate the doc
            component.generateDocs(template, docFile, this.getProject());
            
            // generate the index
            final File indexFile = new File(componentsDir, "book.xml");
            final Document indexDoc = DocumentCache.getDocument(indexFile, this);
            final String section;
            if ( this.blockName == null ) {
                section = "Core";
            } else {
                section = this.blockName + " Block";
            }
            Node sectionNode = XPathAPI.selectSingleNode(indexDoc, "/book/menu[@label='"+section+"']");
            if ( sectionNode == null ) {
                sectionNode = indexDoc.createElement("menu");
                ((Element)sectionNode).setAttribute("label", section);
                indexDoc.getDocumentElement().appendChild(sectionNode);
            }
            final String htmlName = docFile.getName().substring(0, docFile.getName().length()-3) + "html";
            Node oldEntry = XPathAPI.selectSingleNode(sectionNode, "menu-item[@href='"+htmlName+"']");
            Node newEntry = indexDoc.createElement("menu-item");
            ((Element)newEntry).setAttribute("href", htmlName);  
            final String label = capitalize(component.getName()) + " " + capitalize(component.getType());
            ((Element)newEntry).setAttribute("label", label);
            if ( oldEntry != null ) {
                oldEntry.getParentNode().replaceChild(newEntry, oldEntry);
            } else {
                Node nextLabel = null;
                final NodeList childs = sectionNode.getChildNodes();
                int i = 0;
                while ( nextLabel == null && i < childs.getLength() ) {
                    final Node current = childs.item(i);
                    if ( current instanceof Element ) {
                        final String currentLabel = ((Element)current).getAttribute("label");
                        if ( label.compareTo(currentLabel) < 0 ) {
                            nextLabel = current;
                        }
                    }
                    i++;
                }
                if ( nextLabel == null ) {
                    sectionNode.appendChild(newEntry);
                } else {
                    sectionNode.insertBefore(newEntry, nextLabel);
                }
            }
            DocumentCache.writeDocument(indexFile, indexDoc, this);
        }
        
    }

    /**
     * Helper method to capitalize a string.
     * This is taken from commons-lang, but we don't want the dependency 
     * right now!
     */
    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }

    final class SitemapComponent {
        
        final protected JavaClass javaClass;
        final String    name;
        final String    type;
        
        public SitemapComponent(JavaClass javaClass) {
            this.javaClass = javaClass;
            
            this.name = javaClass.getTagByName( NAME_TAG ).getValue();            
            this.type = getType(this.javaClass);
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "Sitemap component: " + this.javaClass.getName();
        }
        
        public String getType() {
            return this.type;
        }
        
        public String getName() {
            return this.name;
        }
        
        public boolean append(Node parent) {
            if ( this.getTagValue(HIDDEN_TAG, null) != null ) {
                return false;
            }
            Document doc = parent.getOwnerDocument();
            Node node;
            StringBuffer buffer = new StringBuffer();
            
            // first check: deprecated?
            if ( SitemapTask.this.deprecated || this.getTagValue("deprecated", null) != null ) {
                indent(parent, 3);
                buffer.append("The ")
                .append(this.type)
                .append(" ")
                .append(this.name)
                .append(" is deprecated");
                node = doc.createComment(buffer.toString());
                parent.appendChild(node);
                newLine(parent);
                buffer = new StringBuffer();
            }
            // unstable block?
            if ( !SitemapTask.this.stable ) {
                indent(parent, 3);
                buffer.append("The ")
                .append(this.type)
                .append(" ")
                .append(this.name)
                .append(" is in an unstable block");
                node = doc.createComment(buffer.toString());
                parent.appendChild(node);
                newLine(parent);
                buffer = new StringBuffer();                
            }
            
            indent(parent, 3);
            node = doc.createElement("map:" + this.type);
            ((Element)node).setAttribute("name", this.name);
            ((Element)node).setAttribute("src", this.javaClass.getFullyQualifiedName());
            
            // test for logger
            // TODO Default logger?
            if ( this.javaClass.isA(LOG_ENABLED) ) {
                this.addAttribute(node, LOGGER_TAG, "logger", null);
            }
            
            // test for label
            this.addAttribute(node, LABEL_TAG, "label", null);

            // pooling?
            if ( this.javaClass.isA(POOLABLE) ) {
                // TODO - Think about default values
                this.addAttribute(node, POOL_MIN_TAG, "pool-min", null);
                this.addAttribute(node, POOL_MAX_TAG, "pool-max", null);
                this.addAttribute(node, POOL_GROW_TAG, "pool-grow", null);
            }
            
            // mime-type
            if ( this.javaClass.isA(OUTPUT_COMPONENT) ) {
                this.addAttribute(node, MIMETYPE_TAG, "mime-type", null);
            }
            
            // append node
            parent.appendChild(node);
            newLine(parent);
            
            // add configuration
            String configuration = this.getTagValue(CONF_TAG, null);
            if ( configuration != null ) {
                configuration = "<root>" + configuration + "</root>";
                final Document confDoc = DocumentCache.getDocument(configuration);
                setValue(node, null, confDoc.getDocumentElement().getChildNodes());
            }

            return true;
        }
        
        private void addAttribute(Node node, String tag, String attributeName, String defaultValue) {
            final String tagValue = this.getTagValue(tag, defaultValue);
            if ( tagValue != null ) {
                ((Element)node).setAttribute(attributeName, tagValue);
            }
        }
        
        private void newLine(Node node) {
            final Node n = node.getOwnerDocument().createTextNode(LINE_SEPARATOR);
            node.appendChild(n);
        }
        
        private void indent(Node node, int depth) {
            final StringBuffer buffer = new StringBuffer();
            for(int i=0; i < depth*2; i++ ) {
                buffer.append(' ');
            }
            final Node n = node.getOwnerDocument().createTextNode(buffer.toString());
            node.appendChild(n);
        }
        
        public void generateDocs(Document template, File docFile, Project project) 
        throws TransformerException {

            final String doc = this.getDocumentation();
            if ( doc == null ) {
                if ( docFile.exists() ) {
                    docFile.delete();
                }
                return;
            }
            // get body from template
            final Node body = XPathAPI.selectSingleNode(template, "/document/body");
            
            // append root element and surrounding paragraph
            final String description = "<root><p>" + doc + "</p></root>";
            final Document descriptionDoc = DocumentCache.getDocument(description);
            
            // Title
            setValue(template, "/document/header/title", 
                     "Description of the " + this.name + " " + this.type);
            
            // Version
            setValue(template, "/document/header/version",
                     project.getProperty("version"));
            
            // Description
            setValue(body, "s1[@title='Description']", 
                     descriptionDoc.getDocumentElement().getChildNodes());
            
            // check: deprecated?
            if ( SitemapTask.this.deprecated || this.getTagValue("deprecated", null) != null ) {
                Node node = XPathAPI.selectSingleNode(body, "s1[@title='Description']");
                // node is never null - this is ensured by the test above
                Element e = node.getOwnerDocument().createElement("note");
                node.appendChild(e);
                e.appendChild(node.getOwnerDocument().createTextNode("This component is deprecated."));
                final String info = this.getTagValue("deprecated", null);
                if ( info != null ) {
                    e.appendChild(node.getOwnerDocument().createTextNode(info));
                }
            }
            // check: stable?
            if ( !SitemapTask.this.stable ) {
                Node node = XPathAPI.selectSingleNode(body, "s1[@title='Description']");
                // node is never null - this is ensured by the test above
                Element e = node.getOwnerDocument().createElement("note");
                node.appendChild(e);
                e.appendChild(node.getOwnerDocument().createTextNode("This component is in an unstable block."));
            }
            
            // Info Table
            final Node tableNode = XPathAPI.selectSingleNode(body, "s1[@title='Info']/table");
            
            // Info - Name
            this.addRow(tableNode, "Name", this.name);

            // Info - Block
            if ( SitemapTask.this.blockName != null ) {
                this.addRow(tableNode, "Block", SitemapTask.this.blockName);
            }
            
            // Info - Cacheable
            if ( this.javaClass.isA(GENERATOR)
                 || this.javaClass.isA(TRANSFORMER)
                 || this.javaClass.isA(SERIALIZER)
                 || this.javaClass.isA(READER)) {
                
                String cacheInfo;
                if ( this.javaClass.isA(CACHEABLE) ) {
                    cacheInfo = this.getTagValue(CACHING_INFO_TAG, null);
                    if ( cacheInfo != null ) {
                        cacheInfo = "Yes - " + cacheInfo;
                    } else {
                        cacheInfo = "Yes";
                    }
                } else if ( this.javaClass.isA(DEPRECATED_CACHEABLE) ) {
                    cacheInfo = this.getTagValue(CACHING_INFO_TAG, null);
                    if ( cacheInfo != null ) {
                        cacheInfo = "Yes (2.0 Caching) - " + cacheInfo;
                    } else {
                        cacheInfo = "Yes (2.0 Caching)";
                    }
                } else {
                    cacheInfo = "No";
                }
                this.addRow(tableNode, "Cacheable", cacheInfo);
            }
            
            // Info - mime-type
            if ( this.javaClass.isA(OUTPUT_COMPONENT) ) {
                final String value = this.getTagValue(MIMETYPE_TAG, "-");
                this.addRow(tableNode, "Mime-Type", value);
            }
            
            // Info - Class
            this.addRow(tableNode, "Class", this.javaClass.getFullyQualifiedName());

            // merge with old doc
            this.merge(body, docFile);
            
            // finally write the doc
            DocumentCache.writeDocument(docFile, template, SitemapTask.this);            
        }
        
        /**
         * Merge the sections of the old document with the new generated one.
         * All sections (s1) of the old document are added to the generated one
         * if not a section with the same title exists.
         */
        private void merge(Node body, File docFile) throws TransformerException {            
            final Document mergeDocument;
            try {
                mergeDocument = DocumentCache.getDocument(docFile, SitemapTask.this);
            } catch (Exception ignore) {
                return;
            }
            NodeList sections = XPathAPI.selectNodeList(mergeDocument, "/document/body/s1");
            if ( sections != null ) {
                for(int i=0; i<sections.getLength(); i++) {
                    final Element current = (Element)sections.item(i);
                    final String title = current.getAttribute("title");

                    // is this section not in the template?
                    if (XPathAPI.selectSingleNode(body, "s1[@title='"+title+"']") == null ) {
                        body.appendChild(body.getOwnerDocument().importNode(current, true));
                    }
                }
            }
        }
        
        /**
         * Return the documentation or null
         * @return
         */
        private String getDocumentation() {
            if ( this.getTagValue(NO_DOC_TAG, null) != null ) {
                return null;
            }
            return this.getTagValue(DOC_TAG, null);
        }
        
        private String getTagValue(String tagName, String defaultValue) {
            final DocletTag tag = this.javaClass.getTagByName( tagName );
            if ( tag != null ) {
                return tag.getValue();
            }
            return defaultValue;
        }
        
        private void addRow(Node table, String title, String value) {
            final Element row = table.getOwnerDocument().createElement("tr");
            final Element firstColumn = table.getOwnerDocument().createElement("td");
            firstColumn.appendChild(table.getOwnerDocument().createTextNode(title));
            final Element secondColumn = table.getOwnerDocument().createElement("td");
            secondColumn.appendChild(table.getOwnerDocument().createTextNode(value));
            row.appendChild(firstColumn);
            row.appendChild(secondColumn);
            table.appendChild(row);
        }
        
        private String getType(JavaClass clazz) {
            if ( clazz.isA(GENERATOR) ) {
                return "generator";
            } else if ( clazz.isA(TRANSFORMER) ) {
                return "transformer";
            } else if ( clazz.isA(READER) ) {
                return "reader";
            } else if ( clazz.isA(SERIALIZER) ) {
                return "serializer";
            } else if ( clazz.isA(ACTION) ) {
                return "action";
            } else if ( clazz.isA(MATCHER) ) {
                return "matcher";
            } else if ( clazz.isA(SELECTOR) ) {
                return "selector";
            } else if ( clazz.isA(PIPELINE) ) {
                return "pipe";
            } else {
                throw new BuildException("Sitemap component " + clazz.getFullyQualifiedName() + " does not implement a sitemap component interface.");
            }            
        }
        
        private void setValue(Node node, String xpath, String value) {
            try {
                final Node insertNode = (xpath == null ? node : XPathAPI.selectSingleNode(node, xpath));
                if ( insertNode == null ) {
                    throw new BuildException("Node (" + xpath + ") not found.");
                }
                Node text = insertNode.getOwnerDocument().createTextNode(value);
                while (insertNode.hasChildNodes() ) {
                    insertNode.removeChild(insertNode.getFirstChild());
                }
                insertNode.appendChild(text);
            } catch (TransformerException e) {
                throw new BuildException(e);
            }
        }

        private void setValue(Node node, String xpath, NodeList nodes) {
            try {
                final Node insertNode = (xpath == null ? node : XPathAPI.selectSingleNode(node, xpath));
                if ( insertNode == null ) {
                    throw new BuildException("Node (" + xpath + ") not found.");
                }
                while (insertNode.hasChildNodes() ) {
                    insertNode.removeChild(insertNode.getFirstChild());
                }
                for(int i=0; i<nodes.getLength(); i++) {
                    final Node current = nodes.item(i);
                    insertNode.appendChild(insertNode.getOwnerDocument().importNode(current, true));
                }
            } catch (TransformerException e) {
                throw new BuildException(e);
            }
        }
    
}
    
    // Class Constants
    private static final String LOG_ENABLED = "org.apache.avalon.framework.logger.LogEnabled";
    private static final String POOLABLE = "org.apache.avalon.excalibur.pool.Poolable";

    private static final String CACHEABLE = "org.apache.cocoon.caching.CacheableProcessingComponent";
    private static final String DEPRECATED_CACHEABLE = "org.apache.cocoon.caching.Cacheable";
    
    private static final String OUTPUT_COMPONENT = "org.apache.cocoon.sitemap.SitemapOutputComponent";

    private static final String GENERATOR = "org.apache.cocoon.generation.Generator";
    private static final String TRANSFORMER = "org.apache.cocoon.transformation.Transformer";
    private static final String SERIALIZER = "org.apache.cocoon.serialization.Serializer";
    private static final String READER = "org.apache.cocoon.reading.Reader";
    private static final String MATCHER = "org.apache.cocoon.matching.Matcher";
    private static final String SELECTOR = "org.apache.cocoon.selection.Selector";
    private static final String ACTION = "org.apache.cocoon.acting.Action";
    private static final String PIPELINE = "org.apache.cocoon.components.pipeline.ProcessingPipeline";
}
