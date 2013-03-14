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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.location.LocatableException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationUtils;
import org.apache.cocoon.util.location.MultiLocatable;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A generator that dumps an XML representation of the exception raised during a pipeline execution.
 * <p>
 * The Cocoon stack trace is produced, reflecting all locations the original exception went through,
 * along with the root exception stacktrace and the full exception stacktrace.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class ExceptionGenerator extends AbstractGenerator {
    
    private Throwable thr;
    
    public static final String EXCEPTION_NS = "http://apache.org/cocoon/exception/1.0";

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        thr = (Throwable)objectModel.get(ObjectModelHelper.THROWABLE_OBJECT);
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
        Throwable root = ExceptionUtils.getRootCause(thr);
        if (root == null) root = thr;

        AttributesImpl attr = new AttributesImpl();
        handler.startPrefixMapping("ex", EXCEPTION_NS);
        attr.addCDATAAttribute("class", root.getClass().getName());
        handler.startElement(EXCEPTION_NS, "exception-report", "ex:exception-report", attr);
        
        // Root exception location
        Location loc = LocationUtils.getLocation(root);        
        if (LocationUtils.isKnown(loc)) {
            attr.clear();
            dumpLocation(loc, attr, handler);
        }

        // Root exception message
        attr.clear();
        String message = root instanceof LocatableException ? ((LocatableException)root).getRawMessage() : root.getMessage();
        simpleElement("message", attr, message, handler);
        
        // Cocoon stacktrace: dump all located exceptions in the exception stack
        handler.startElement(EXCEPTION_NS, "cocoon-stacktrace", "ex:cocoon-stacktrace", attr);
        Throwable current = thr;
        while (current != null) {
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
                    List locations = ((MultiLocatable)current).getLocations();
                    for (int i = 1; i < locations.size(); i++) { // start at 1 because we already dumped the first one
                        attr.clear();
                        dumpLocation((Location)locations.get(i), attr, handler);
                    }
                }
                handler.endElement(EXCEPTION_NS, "locations", "ex:locations");
                handler.endElement(EXCEPTION_NS, "exception", "ex:exception");
            }
            
            
            // Dump parent location
            current = ExceptionUtils.getCause(current);
        }
        
        handler.endElement(EXCEPTION_NS, "cocoon-stacktrace", "ex:cocoon-stacktrace");
        
        // Root exception stacktrace
        attr.clear();
        simpleElement("stacktrace", attr, ExceptionUtils.getStackTrace(root), handler);
        
        // Full stack trace (if exception is chained)
        if (thr != root) {
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
