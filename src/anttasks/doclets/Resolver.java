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
import java.io.IOException;
import java.util.Map;

/**
 * Resolving output files, classes, packages 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class Resolver {
    
    private File destDir;
    private Map javaClasses;
    private Map javaPackages;
    
    public Resolver(File destDir, Map javaClasses, Map javaPackages) {
        this.destDir = destDir;
        this.javaClasses = javaClasses;
        this.javaPackages = javaPackages;
    }
    
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public Map getJavaClasses() {
        return this.javaClasses;
    }

    public Map getJavaPackages() {
        return this.javaPackages;
    }

    public File getOutputFileForClass(String classname) throws IOException {
        String filename = classname.replace( '.', File.separatorChar ) + ".xml";
        return new File( this.destDir, filename ).getCanonicalFile();
    }

    public File getOutputFileForPackage(String packagename) throws IOException {
        String filename = packagename.replace( '.', File.separatorChar ) + File.separatorChar + "package-frame.xml";
        return new File( this.destDir, filename ).getCanonicalFile();
    }

    public File getOutputFileForPackageOverview() throws IOException {
        return new File( this.destDir, "overview-frame.xml").getCanonicalFile();
    }

    public File getOutputFileForClassOverview() throws IOException {
        return new File( this.destDir, "allclasses-frame.xml").getCanonicalFile();
    }

}
