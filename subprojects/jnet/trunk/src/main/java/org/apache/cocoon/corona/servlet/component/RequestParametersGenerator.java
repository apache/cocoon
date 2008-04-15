/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.servlet.component;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.corona.pipeline.component.AbstractXMLProducer;
import org.apache.cocoon.corona.pipeline.component.Starter;
import org.apache.cocoon.corona.servlet.util.HttpContextHelper;
import org.apache.cocoon.corona.sitemap.InvocationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class RequestParametersGenerator extends AbstractXMLProducer implements Starter {

    private Map<String, Object> parameters;

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.corona.pipeline.component.Starter#execute()
     */
    @SuppressWarnings("unchecked")
    public void execute() {
        HttpServletRequest request = HttpContextHelper.getRequest(this.parameters);
        Enumeration<String> parameterNames = request.getParameterNames();

        try {
            this.getXMLConsumer().startDocument();
            this.getXMLConsumer().startElement("", "request-paramters", "request-paramters", new AttributesImpl());

            while (parameterNames.hasMoreElements()) {
                String name = parameterNames.nextElement();
                String value = request.getParameter(name);
                this.getXMLConsumer().startElement("", name, name, new AttributesImpl());
                this.getXMLConsumer().characters(value.toCharArray(), 0, value.length());
                this.getXMLConsumer().endElement("", name, name);
            }

            this.getXMLConsumer().endElement("", "request-paramters", "request-paramters");
            this.getXMLConsumer().endDocument();
        } catch (SAXException e) {
            throw new InvocationException(e);
        }
    }

    @Override
    public void setInputParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
