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
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * Exception action. Throws different kinds of exception depending on
 * value of src attribute.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class ExceptionAction extends AbstractAction {

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
    throws Exception {
        String exception = parameters.getParameter("exception", source);
        int code = parameters.getParameterAsInteger("code", 1);
        exception(exception, code);
        return null;
    }

    public static String exception(String exception, int code)
    throws ProcessingException , SAXException, IOException {
        if (exception == null) {
            return "No exception occured.";
        } else if (exception.equals("validation")) {
            throw new ProcessingException(new ValidationException("Validation Exception Message"));
        } else if (exception.equals("application")) {
            throw new ProcessingException(new ApplicationException(code, "Application Exception " + code + " Message"));
        } else if (exception.equals("processing")) {
            throw new ProcessingException("Processing Exception Message");
        } else if (exception.equals("notFound")) {
            throw new ResourceNotFoundException("Resource Not Found Exception Message");
        } else if (exception.equals("sax")) {
            throw new SAXException("SAX Exception Message");
        } else if (exception.equals("saxWrapped")) {
            throw new SAXException(new ProcessingException("Processing Exception Wrapped In SAX Exception Message"));
        } else if (exception.equals("nullPointer")) {
            throw new NullPointerException("Null Pointer Exception Message");
        } else if (exception.equals("io")) {
            throw new IOException("IO Exception Message");
        } else if (exception.equals("error")) {
            throw new Error("Error Message");
        } else {
            return "Unknown exception requested.";
        }
    }
}
