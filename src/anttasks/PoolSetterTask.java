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
import java.io.IOException;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  Set the special attributes for the pooling
 *
 * @since 2.1.5
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/05/01 16:12:05 $
 */
public final class PoolSetterTask extends Task {

    private File file;
    private String element;
    private boolean isSitemap = true;
    private String poolMax = "32";
    private String poolMin = "16";
    private String poolGrow = "4";

    public void setFile(File file) {
        this.file = file;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public void setIsSitemap(boolean value) {
        this.isSitemap = value;
    }

    public void setPoolMax(int value) {
        this.poolMax = String.valueOf(value);
    }

    public void setPoolMin(int value) {
        this.poolMin = String.valueOf(value);
    }

    public void setPoolGrow(int value) {
        this.poolGrow = String.valueOf(value);
    }

    public void execute() throws BuildException {

        if (this.file == null) {
            throw new BuildException("file attribute is required", this.getLocation());
        }
        if (this.element == null) {
            throw new BuildException("element attribute is required", this.getLocation());
        }

        try {
            // load xml
            final Document configuration = DocumentCache.getDocument(this.file, this);

            // process recursive
            boolean changed = false;
            StringTokenizer st = new StringTokenizer(this.element);
            while ( st.hasMoreTokens() ) {
                String componentName = st.nextToken();
                NodeList nodes;
                if (this.isSitemap) {
                    int pos = componentName.indexOf(":");
                    nodes = XPathAPI.selectNodeList(configuration, "*/*[local-name()='components']/*/*[local-name()='"+componentName.substring(0,pos)+"' and @name='"+componentName.substring(pos+1)+"']");
                } else {
                    nodes = XPathAPI.selectNodeList(configuration, "*/"+componentName);
                }
                if (nodes != null && nodes.getLength() > 0) {
                    for(int i=0; i < nodes.getLength(); i++) {
                        final Element e = (Element)nodes.item(i);
                        e.setAttributeNS(null, "pool-max", this.poolMax);
                        e.setAttributeNS(null, "pool-min", this.poolMin);
                        e.setAttributeNS(null, "pool-grow", this.poolGrow);
                        changed = true;
                    }
                } else {
                    System.out.println("Component not found: " + componentName);
                }
            }

            if ( changed ) {
                // save xml
                DocumentCache.writeDocument(this.file, configuration, this);
            }
            DocumentCache.storeDocument(this.file, configuration, this);
       } catch (TransformerException e) {
            throw new BuildException("TransformerException: " + e);
        } catch (SAXException e) {
            throw new BuildException("SAXException: " + e);
        } catch (IOException ioe) {
            throw new BuildException("IOException: " + ioe);
        }
    }

}
