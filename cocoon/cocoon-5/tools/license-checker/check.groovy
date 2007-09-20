/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 /*
  * USAGE NOTES: This scripts needs command line SVN installed and commons-io.jar in
  * the Groovy classpath.
  */
import java.io.File
import org.apache.commons.io.FileUtils

new File("../../core").eachFileRecurse({file->
    if(file.name == "pom.xml") {
        println "checking module: $file.parentFile.canonicalPath"
        File lf = new File(file.parent, "src/main/resources/META-INF/license.txt")
        if(!lf.exists()) {
            lf.parentFile.mkdirs()
            FileUtils.copyFile(new File("license.txt"), lf);
            // add to SVN
            addToSVN(new File(file.parent, "src/main/resources"))
            addToSVN(new File(file.parent, "src/main/resources/META-INF"))
            addToSVN(lf)
        }
        
        File nf = new File(file.parent, "src/main/resources/META-INF/notice.txt")        
        if(!nf.exists()) {
            FileUtils.copyFile(new File("notice.txt"), nf)    
            addToSVN(nf)   
        }    
        
        File cf = new File(file.parent, "src/changes/changes.xml")        
        if(!cf.exists()) {
            cf.parentFile.mkdirs()        
            FileUtils.copyFile(new File("changes.xml"), cf)
            addToSVN(new File(file.parent, "src/changes"))   
            addToSVN(cf)   
        }          
    }
})

def addToSVN(file) {
    String cmd = "svn add $file.canonicalPath"
    Process p = "cmd /c $cmd".execute()
    p.err.eachLine { line -> println line }   
    println "----------------------------------------------------"
}