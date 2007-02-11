/*
 * Copyright 2005 The Apache Software Foundation.
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
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Ant task to add block dependencies to xconf configuration
 *
 * @version $Id$
 */
public final class BlockConfigTask extends Task {

    private File file;
    private String depends;

    /**
     * Set file, which should be patched.
     *
     * @param file File, which should be patched.
     */
    public void setFile(File file) {
        this.file = file;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    /**
     * Execute task.
     */
    public void execute() throws BuildException {
        if (this.file == null) {
            throw new BuildException("file attribute is required", this.getLocation());
        }
        if ( this.depends == null ) {
            throw new BuildException("depends attribute is required", this.getLocation());            
        }
        final List dependencies = new ArrayList();
        final StringTokenizer st = new StringTokenizer(this.depends, ",");
        while ( st.hasMoreTokens() ) {
            String token = st.nextToken();
            dependencies.add(token);
        }
        
        if ( dependencies.size() > 0 ) {
            try {
                Document doc = DocumentCache.getDocument(this.file, this);
                Element elem = doc.getDocumentElement();
                Node firstChild = elem.getFirstChild();
                for(int i=0; i<dependencies.size();i++) {
                    final String token = (String)dependencies.get(i);
                    Node n;
                    n = doc.createTextNode("  ");
                    elem.insertBefore(n, firstChild);

                    n = doc.createComment("Include dependencies");
                    elem.insertBefore(n, firstChild);

                    n = doc.createTextNode("\n  ");
                    elem.insertBefore(n, firstChild);

                    n = doc.createElement("include");
                    ((Element)n).setAttribute("src", "context://WEB-INF/xconf/" + token + ".xconf");
                    elem.insertBefore(n, firstChild);

                    n = doc.createTextNode("\n");
                    elem.insertBefore(n, firstChild);
                }
                DocumentCache.writeDocument(this.file, doc, this);
                DocumentCache.storeDocument(this.file, doc, this);
            } catch (SAXException e) {
                throw new BuildException("SAXException:" +e);           
            } catch (IOException ioe) {
                throw new BuildException("IOException: "+ioe);
            }
        }
    }

 
}
