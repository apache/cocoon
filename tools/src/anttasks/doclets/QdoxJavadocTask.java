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
package doclets;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Customized javadoc task genrates API docs only for marked classes
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class QdoxJavadocTask extends AbstractQdoxTask {
    
    public static final String DEFAULT_USAGE_TAG = "cocoon.usage";
    public static final String DEFAULT_TAG_VALUE = "published";

    private File destDir;
    private String usageTag = DEFAULT_USAGE_TAG;
    private String tagValue = DEFAULT_TAG_VALUE;
    private Resolver resolver;
    private Map publishedClasses = new TreeMap();
    private Map publishedPackages = new TreeMap();

    
    /**
     * the destination directory to generate output files to.
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * the tag name to consider for doc generation.
     */
    public void setUsageTag(String tagName) {
        this.usageTag = tagName;
    }

    /**
     * the tag value to consider for doc generation.
     */
    public void setTagValue(String value) {
        this.tagValue = value;
    }

    /**
     * Execute the doc generation.
     */
    public void execute() {

        try {
            super.execute();
            this.collectPublished();
            this.resolver = new Resolver(this.destDir,
                                         Collections.unmodifiableMap(this.publishedClasses),
                                         Collections.unmodifiableMap(this.publishedPackages));
            this.writeClasses();
            this.writePackages();

        }catch (Exception e) {
            throw new BuildException("Build Error: " + e.getMessage(), e);
        }
    }

    private void collectPublished() {

        for (int i=0; i<this.allClasses.size(); i++) {

            JavaClass clazz = (JavaClass)this.allClasses.get(i);

            DocletTag[] tags = clazz.getTagsByName(this.usageTag);
            for (int j=0; j<tags.length; j++) {
                if ((tags[j].getValue().trim()).equals(this.tagValue)) {
                    this.publishedClasses.put(clazz.getFullyQualifiedName(), clazz);
    
                    if (this.publishedPackages.containsKey(clazz.getPackage())) {
                        ((Set)this.publishedPackages.get(clazz.getPackage())).add(clazz.getName());
    
                    } else {
                        Set set = new HashSet();
                        set.add(clazz.getName());
                        this.publishedPackages.put(clazz.getPackage(), set);
                    }
                }
            }
        }
    }

    private void writeClasses()throws Exception {

        new ClassOverviewXMLWriter(this.resolver).writeClassOverview();

        ClassXMLWriter classWriter = new ClassXMLWriter(this.resolver); 

        Iterator classIter = this.publishedClasses.keySet().iterator();
        while (classIter.hasNext()) {
            JavaClass clazz = (JavaClass)this.publishedClasses.get((String)classIter.next());
            this.log("generating docs for " + clazz.getFullyQualifiedName() + " ...");
            classWriter.writeClass(clazz);
        }
    }

    private void writePackages() throws Exception {

        new PackageOverviewXMLWriter(this.resolver).writePackageOverview();

        PackageXMLWriter packageWriter = new PackageXMLWriter(this.resolver); 
        
        Iterator packageIter = this.publishedPackages.keySet().iterator();
        while (packageIter.hasNext()) {
            String javaPackage = (String)packageIter.next();
            packageWriter.writePackage(javaPackage);
        }
    }
}
