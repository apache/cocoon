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
package org.apache.cocoon.it;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.SAXException;

public class RequestParametersGenerator extends AbstractGenerator {

    public void generate() throws IOException, SAXException, ProcessingException {
        Request request = ObjectModelHelper.getRequest(this.objectModel);

        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "request-paramters", "request-paramters", new AttributesImpl());

        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            String value = (String) request.getParameter(name);
            this.contentHandler.startElement("", name, name, new AttributesImpl());
            this.contentHandler.characters(value.toCharArray(), 0, value.length());
            this.contentHandler.endElement("", name, name);
        }

        this.contentHandler.endElement("", "request-paramters", "request-paramters");
        this.contentHandler.endDocument();
    }

}
