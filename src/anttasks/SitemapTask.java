/* 
 * Copyright 2003-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/04/30 07:20:34 $
 */
public final class SitemapTask extends AbstractQdoxTask {

    /** The name of the component in the sitemap (required) */    
    public static final String NAME_TAG   = "cocoon.sitemap.component.name";
    /** The logger category (optional) */
    public static final String LOGGER_TAG = "cocoon.sitemap.component.logger";
    /** The label for views (optional) */
    public static final String LABEL_TAG  = "cocoon.sitemap.component.label";
    /** If this tag is specified, the component is not added to the sitemap (optional) */
    public static final String HIDDEN_TAG = "cocoon.sitemap.component.hide";
    /** If this tag is specified no documentation is generated (optional) */
    public static final String NO_DOC_TAG = "cocoon.sitemap.component.documentation.disabled";
    /** The documentation (optional) */
    public static final String DOC_TAG    = "cocoon.sitemap.component.documentation";
    
    /** Pooling min (optional) */
    public static final String POOL_MIN_TAG = "cocoon.sitemap.component.pooling.min";
    /** Pooling max (optional) */
    public static final String POOL_MAX_TAG = "cocoon.sitemap.component.pooling.max";
    /** Pooling grow (optional) */
    public static final String POOL_GROW_TAG = "cocoon.sitemap.component.pooling.grow";
    
    private static final String LINE_SEPARATOR = "\n";//System.getProperty("line.separator");
    
    /** The sitemap namespace. TODO - this has to be configurable for newer versions! */
    private static final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
    
    /** The sitemap */
    private File sitemap;
    
    /** The doc dir */
    private File docDir;
    
    /** The components */
    private List components = new ArrayList();
    
    public void setSitemap( final File sitemap ) {
        this.sitemap = sitemap;
    }

    public void setDocDir( final File dir ) {
        this.docDir = dir;        
    }
    
    /**
     * Execute generator task.
     *
     * @throws BuildException if there was a problem collecting the info
     */
    public void execute()
    throws BuildException {

        validate();

        // this does the hard work :)
        super.execute();

        try {
            
            this.collectInfo();
            if ( this.sitemap != null ) {
                this.processSitemap();
            }
            if ( this.docDir != null ) {
                this.processDocDir();
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
    private void collectInfo() {
        log("Collection sitemap components info");
        final Iterator it = super.allClasses.iterator();
        
        while ( it.hasNext() ) {
            final JavaClass javaClass = (JavaClass) it.next();

            final DocletTag tag = javaClass.getTagByName( NAME_TAG );

            if ( null != tag ) {
                final SitemapComponent comp = new SitemapComponent( javaClass );

                log("Found component: " + comp, Project.MSG_DEBUG);
                this.components.add(comp);
            }
        }
    }

    /**
     * Add components to sitemap
    */
    private void processSitemap() 
    throws Exception {
        log("Adding sitemap components");
        final String fileName = this.sitemap.toURL().toExternalForm();
        Document document;
        
        document = DocumentCache.getDocument(fileName, this);
        
        boolean changed = false;

        Iterator iter = this.components.iterator();
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
        DocumentCache.storeDocument(fileName, document, this);
    }
    
    /**
     * Add components to sitemap
    */
    private void processDocDir() 
    throws Exception {
        log("Generating documentation");

        Iterator iter = this.components.iterator();
        while ( iter.hasNext() ) {
            final SitemapComponent component = (SitemapComponent)iter.next();
            
            component.generateDocs(this.docDir);
        }
        
    }

    static final class SitemapComponent {
        
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
            if ( this.getTagValue("deprecated", null) != null ) {
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
            indent(parent, 3);
            node = doc.createElementNS(SITEMAP_NS, "map:" + this.type);
            ((Element)node).setAttribute("name", this.name);
            ((Element)node).setAttribute("src", this.javaClass.getFullyQualifiedName());
            
            // test for logger
            // TODO Default logger?
            if ( this.javaClass.isA("org.apache.avalon.framework.logger.LogEnabled") ) {
                this.addAttribute(node, LOGGER_TAG, "logger", null);
            }
            
            // test for label
            this.addAttribute(node, LABEL_TAG, "label", null);

            if ( this.javaClass.isA("org.apache.avalon.excalibur.pool.Poolable") ) {
                // TODO - Think about default values
                this.addAttribute(node, POOL_MIN_TAG, "pool-min", null);
                this.addAttribute(node, POOL_MAX_TAG, "pool-max", null);
                this.addAttribute(node, POOL_GROW_TAG, "pool-grow", null);
            }
            parent.appendChild(node);
            newLine(parent);
            // TODO Add configuration

            return true;
        }
        
        private void addAttribute(Node node, String tag, String attributeName, String defaultValue) {
            final String tagValue = this.getTagValue(tag, defaultValue);
            if ( tagValue != null ) {
                ((Element)node).setAttribute(attributeName, tagValue);
            }
        }
        
        private static void newLine(Node node) {
            final Node n = node.getOwnerDocument().createTextNode(LINE_SEPARATOR);
            node.appendChild(n);
        }
        
        private static void indent(Node node, int depth) {
            final StringBuffer buffer = new StringBuffer();
            for(int i=0; i < depth*2; i++ ) {
                buffer.append(' ');
            }
            final Node n = node.getOwnerDocument().createTextNode(buffer.toString());
            node.appendChild(n);
        }
        
        public void generateDocs(File parentDir) {
            final String doc = this.getDocumentation();
            if ( doc == null ) {
                return;
            }
            try {
                final File componentsDir = new File(parentDir, this.type+'s');
                componentsDir.mkdir();
                
                final File docFile = new File(componentsDir, this.name + ".txt");
                docFile.createNewFile();
                
                final FileWriter writer = new FileWriter(docFile);
                writer.write(doc);
                writer.close();
            } catch (IOException ioe) {
                throw new BuildException("Error writing doc.", ioe);
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
            final DocletTag tag = javaClass.getTagByName( tagName );
            if ( tag != null ) {
                return tag.getValue();
            }
            return defaultValue;
        }
        
        private static String getType(JavaClass clazz) {
            if ( clazz.isA("org.apache.cocoon.generation.Generator") ) {
                return "generator";
            } else if ( clazz.isA("org.apache.cocoon.transformation.Transformer") ) {
                return "transformer";
            } else if ( clazz.isA("org.apache.cocoon.reading.Reader") ) {
                return "reader";
            } else if ( clazz.isA("org.apache.cocoon.serialization.Serializer") ) {
                return "serializer";
            } else if ( clazz.isA("org.apache.cocoon.acting.Action") ) {
                return "action";
            } else if ( clazz.isA("org.apache.cocoon.matching.Matcher") ) {
                return "matcher";
            } else if ( clazz.isA("org.apache.cocoon.selection.Selector") ) {
                return "selector";
            } else if ( clazz.isA("org.apache.cocoon.components.pipeline.ProcessingPipeline") ) {
                return "pipe";
            } else {
                throw new BuildException("Sitemap component " + clazz.getName() + " does not implement a sitemap component interface.");
            }            
        }
    }
}
