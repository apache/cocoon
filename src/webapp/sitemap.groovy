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
class Sitemap extends Pipeline {
  
    boolean setup(String requestPath) {
        if (requestPath =~ "index\.html") {
            // TODO: paths should be relative to the webapp context!
            generate "build/webapp/welcome.xml"
            transform "trax", "build/webapp/welcome.xslt" 
            serialize "xml"
        } else if (requestPath =~ "images/.*\.gif") {
            read "build/webapp/resources/images/powered.gif", "image/gif"
        } else if (requestPath =~ "styles/.*\.css") {
            read "build/webapp/resources/styles/main.css", "text/css"
        } else {
            return false;
        }
        return true;
    }
}