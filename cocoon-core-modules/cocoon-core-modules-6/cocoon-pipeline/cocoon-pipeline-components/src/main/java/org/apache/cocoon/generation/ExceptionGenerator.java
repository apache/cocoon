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
package org.apache.cocoon.generation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.location.LocatableException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationUtils;
import org.apache.cocoon.util.location.MultiLocatable;
import org.apache.cocoon.xml.AttributesImpl;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.xml.sax.XMLizable;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A generator that dumps an XML representation of the exception raised during
 * pipeline execution.
 *
 * <p>
 * XML representation includes root cause exception message, XML content for all
 * {@link XMLizable} exceptions in the chain, the Cocoon stack trace reflecting all
 * locations the original exception went through, the root cause exception stacktrace
 * and the full exception stacktrace.
 *
 * <p>
 * The XML representation of the exception has following structure:
 *
 * <pre>
 *   &lt;ex:exception-report xmlns:ex="http://apache.org/cocoon/exception/1.0"
 *                           class="<i>root.cause.class.Name</i>"&gt;
 *     &lt;ex:location uri="..." line="..." column="..."&gt;
 *       ...
 *     &lt;/ex:location&gt;
 *     &lt;ex:message&gt;
 *       <i>root cause message</i>
 *     &lt;/ex:message&gt;
 *     <i>&lt;!-- for each XMLizable exception in the cause chain: --&gt;</i>
 *     &lt;ex:content&gt;
 *       <i>&lt;-- output of XMLizable exception --&gt;</i>
 *     &lt;/ex:content&gt;
 *     &lt;ex:cocoon-stacktrace&gt;
 *       <i>&lt;!-- for each exception in the cause chain --&gt;</i>
 *       &lt;ex:exception&gt;
 *         &lt;ex:message&gt;...&lt;/ex:message&gt;
 *         &lt;ex:locations&gt;...&lt;/ex:locations&gt;
 *       &lt;/ex:exception&gt;
 *     &lt;/ex:cocoon-stacktrace&gt;
 *     &lt;ex:stacktrace&gt;
 *       <i>&lt;-- root cause stacktrace --&gt;</i>
 *     &lt;/ex:stacktrace&gt;
 *     &lt;ex:full-stacktrace&gt;
 *       <i>&lt;-- full cause chain stacktrace --&gt;</i>
 *     &lt;/ex:full-stacktrace&gt;
 *   &lt;/ex:exception-report&gt;
 * </pre>
 *
 * @cocoon.sitemap.component.documentation
 * A generator that dumps an XML representation of the exception raised during
 * pipeline execution.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class ExceptionGenerator extends AbstractGenerator {

    public static final String EXCEPTION_NS = "http://apache.org/cocoon/exception/1.0";

    private Throwable thr;


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        thr = ObjectModelHelper.getThrowable(objectModel);
        if (thr == null) {
            throw new ProcessingException("ExceptionGenerator should be used in <map:handle-errors>");
        }
    }

    public void generate() throws IOException, SAXException, ProcessingException {
        this.contentHandler.startDocument();
        toSAX(thr, this.contentHandler);
        this.contentHandler.endDocument();
    }

    public static void toSAX(Throwable thr, ContentHandler handler) throws SAXException {
        Throwable cause = ExceptionUtils.getRootCause(thr);
        if (cause == null) {
            cause = thr;
        }

        AttributesImpl attr = new AttributesImpl();
        handler.startPrefixMapping("ex", EXCEPTION_NS);
        attr.addCDATAAttribute("class", cause.getClass().getName());
        handler.startElement(EXCEPTION_NS, "exception-report", "ex:exception-report", attr);

        // Root cause exception location
        Location loc = LocationUtils.getLocation(cause);
        if (LocationUtils.isKnown(loc)) {
            attr.clear();
            dumpLocation(loc, attr, handler);
        }

        // Root cause exception message
        attr.clear();
        String message = cause instanceof LocatableException ? ((LocatableException) cause).getRawMessage() : cause.getMessage();
        simpleElement("message", attr, message, handler);

        // Exception XML content: dump all XMLizable exceptions in the exception cause chain
        for (Throwable current = thr; current != null; current = ExceptionUtils.getCause(current)) {
            handler.startElement(EXCEPTION_NS, "content", "ex:content", attr);
            if (current instanceof XMLizable) {
                ((XMLizable) current).toSAX(handler);
            }
            handler.endElement(EXCEPTION_NS, "content", "ex:content");
        }

        // Cocoon stacktrace: dump all located exceptions in the exception cause chain
        handler.startElement(EXCEPTION_NS, "cocoon-stacktrace", "ex:cocoon-stacktrace", attr);
        for (Throwable current = thr; current != null; current = ExceptionUtils.getCause(current)) {
            loc = LocationUtils.getLocation(current);
            if (LocationUtils.isKnown(loc)) {
                // One or more locations: dump it
                handler.startElement(EXCEPTION_NS, "exception", "ex:exception", attr);

                message = current instanceof LocatableException ? ((LocatableException)current).getRawMessage() : current.getMessage();
                simpleElement("message", attr, message, handler);

                attr.clear();
                handler.startElement(EXCEPTION_NS, "locations", "ex:locations", attr);
                dumpLocation(loc, attr, handler);

                if (current instanceof MultiLocatable) {
                    List locations = ((MultiLocatable) current).getLocations();
                    for (int i = 1; i < locations.size(); i++) { // start at 1 because we already dumped the first one
                        attr.clear();
                        dumpLocation((Location) locations.get(i), attr, handler);
                    }
                }
                handler.endElement(EXCEPTION_NS, "locations", "ex:locations");
                handler.endElement(EXCEPTION_NS, "exception", "ex:exception");
            }
        }
        handler.endElement(EXCEPTION_NS, "cocoon-stacktrace", "ex:cocoon-stacktrace");

        // Root cause exception stacktrace
        attr.clear();
        simpleElement("stacktrace", attr, ExceptionUtils.getStackTrace(cause), handler);

        // Full stack trace (if exception is chained)
        if (thr != cause) {
            String trace = SystemUtils.isJavaVersionAtLeast(140) ?
                    ExceptionUtils.getStackTrace(thr) :
                    ExceptionUtils.getFullStackTrace(thr);

            simpleElement("full-stacktrace", attr, trace, handler);
        }

        handler.endElement(EXCEPTION_NS, "exception-report", "ex:exception-report");
        handler.endPrefixMapping("ex");
    }

    private static void dumpLocation(Location loc, AttributesImpl attr, ContentHandler handler) throws SAXException {
        attr.addCDATAAttribute("uri", loc.getURI());
        attr.addCDATAAttribute("line", Integer.toString(loc.getLineNumber()));
        attr.addCDATAAttribute("column", Integer.toString(loc.getColumnNumber()));
        simpleElement("location", attr, loc.getDescription(), handler);
    }

    private static void simpleElement(String name, Attributes attr, String value, ContentHandler handler) throws SAXException {
        handler.startElement(EXCEPTION_NS, name, "ex:" + name, attr);
        if (value != null && value.length() > 0) {
            handler.characters(value.toCharArray(), 0, value.length());
        }
        handler.endElement(EXCEPTION_NS, name, "ex:" + name);
    }
}
