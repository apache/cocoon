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
package org.apache.cocoon.samples.errorhandling;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * Exception generator. Throws different kinds of exception depending on
 * value of src attribute.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class ExceptionGenerator extends AbstractGenerator {

    private String exception;
    private int code;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);

        this.exception = parameters.getParameter("exception", super.source);
        this.code = Integer.parseInt(parameters.getParameter("code", "0"));

        // Throw exception in the setup phase?
        if (parameters.getParameterAsBoolean("setup", false)) {
            ExceptionAction.exception(this.exception, this.code);
        }
    }

    /**
     * Overridden from superclass.
     */
    public void generate()
    throws ProcessingException , SAXException, IOException {
        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "html", "html", XMLUtils.EMPTY_ATTRIBUTES);
        this.contentHandler.startElement("", "body", "body", XMLUtils.EMPTY_ATTRIBUTES);
        this.contentHandler.startElement("", "p", "p", XMLUtils.EMPTY_ATTRIBUTES);

        String text = ExceptionAction.exception(this.exception, this.code);
        this.contentHandler.characters(text.toCharArray(), 0, text.length());

        this.contentHandler.endElement("", "p", "p");
        this.contentHandler.endElement("", "body", "body");
        this.contentHandler.endElement("", "html", "html");
        this.contentHandler.endDocument();
    }
}
