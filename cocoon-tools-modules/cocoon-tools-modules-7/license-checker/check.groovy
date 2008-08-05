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

new File("../../tools").eachFileRecurse({file->
    if(file.name == "pom.xml") {
        println "checking module: $file.parentFile.canonicalPath"
        File lf = new File(file.parent, "LICENSE.txt")
        if(!lf.exists()) {
            FileUtils.copyFile(new File("LICENSE.txt"), lf);
            addToSVN(lf)
        }
        
        File nf = new File(file.parent, "NOTICE.txt")        
        if(!nf.exists()) {
            FileUtils.copyFile(new File("NOTICE.txt"), nf)    
            addToSVN(nf)   
        }   
        
        println "----------------------------------------------------"            
    }
})

def addToSVN(file) {
    String cmd = "svn add $file.canonicalPath"
    Process p = "cmd /c $cmd".execute()
    p.err.eachLine { line -> println line }   
}