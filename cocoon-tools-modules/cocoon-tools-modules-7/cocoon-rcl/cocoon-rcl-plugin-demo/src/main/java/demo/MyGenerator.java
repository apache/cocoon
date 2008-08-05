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
package demo;


import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.SAXException;

public final class MyGenerator extends AbstractGenerator {

    public void generate() throws IOException, SAXException, ProcessingException {
        contentHandler.startDocument();
        contentHandler.startElement("","html","html", new AttributesImpl());
        contentHandler.startElement("","body","body", new AttributesImpl());
        
        //Change message to see the Reloading classloader working
        final char[] message = "generator!!!".toCharArray();
        contentHandler.characters(message, 0, message.length);
        
        contentHandler.endElement("","body","body");
        contentHandler.endElement("","html","html");
        contentHandler.endDocument();
    }
    
}
