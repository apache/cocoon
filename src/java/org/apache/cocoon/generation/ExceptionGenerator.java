/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.ExceptionUtils;
import org.apache.cocoon.util.location.LocatableException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.MultiLocatable;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.lang.SystemUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ExceptionGenerator extends AbstractGenerator {
    
    private Throwable thr;
    
    public static String EXCEPTION_NS = "http://apache.org/cocoon/exception/1.0";

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
        Location loc = ExceptionUtils.getLocation(root);
        if (loc != null) {
            attr.addCDATAAttribute("uri", loc.getURI());
            attr.addCDATAAttribute("line", Integer.toString(loc.getLine()));
            attr.addCDATAAttribute("column", Integer.toString(loc.getColumn()));
        }
        handler.startElement(EXCEPTION_NS, "exception", "ex:exception", attr);

        // Root exception message
        attr.clear();
        simpleElement("message", attr, root.getMessage(), handler);
        
        // Locations
        handler.startElement(EXCEPTION_NS, "locations", "ex:locations", attr);
        Throwable current = thr;
        while (current != null) {
            if (current instanceof MultiLocatable) {
                // Get raw message for LocatableExceptions, message otherwise
                String message = current instanceof LocatableException ?
                        ((LocatableException)current).getRawMessage() :
                        current.getMessage();

                attr.clear();
                handler.startElement(EXCEPTION_NS, "location-list", "ex:location-list", attr);
                simpleElement("description", attr, message, handler);
                List locations = ((MultiLocatable)current).getLocations();
                for (int i = 0; i < locations.size(); i++) {
                    attr.clear();
                    dumpLocation((Location)locations.get(i), attr, null, handler);
                }
                handler.endElement(EXCEPTION_NS, "location-list", "ex:location-list");

            } else {
                // Not a MultiLocatable
                loc = ExceptionUtils.getLocation(current);
                if (loc != null) {
                    // Get raw message for LocatableExceptions, message otherwise
                    String message = current instanceof LocatableException ?
                            ((LocatableException)current).getRawMessage() :
                            current.getMessage();
    
                    attr.clear();                
                    dumpLocation(loc, attr, message, handler);
                    
                    // Dump additional locations (explains "1" below) if it's a MultiLocatable (e.g. ProcessingException)
                    if (current instanceof MultiLocatable) {
                        List locations = ((MultiLocatable)current).getLocations();
                        for (int i = 1; i < locations.size(); i++) {
                            attr.clear();
                            dumpLocation((Location)locations.get(i), attr, null, handler);
                        }
                    }
                }
            }
            
            // Dump parent location
            current = ExceptionUtils.getCause(current);
        }
        
        handler.endElement(EXCEPTION_NS, "locations", "ex:locations");
        
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
        
        handler.endElement(EXCEPTION_NS, "exception", "ex:exception");
        handler.endPrefixMapping("ex");
    }
    
    private static void dumpLocation(Location loc, AttributesImpl attr, String message, ContentHandler handler) throws SAXException {
        attr.addCDATAAttribute("uri", loc.getURI());
        attr.addCDATAAttribute("line", Integer.toString(loc.getLine()));
        attr.addCDATAAttribute("column", Integer.toString(loc.getColumn()));
        
        simpleElement("location", attr, message, handler);
    }

    private static void simpleElement(String name, Attributes attr, String value, ContentHandler handler) throws SAXException {
        handler.startElement(EXCEPTION_NS, name, "ex:" + name, attr);
        if (value != null && value.length() > 0) {
            handler.characters(value.toCharArray(), 0, value.length());
        }
        handler.endElement(EXCEPTION_NS, name, "ex:" + name);
    }
}
