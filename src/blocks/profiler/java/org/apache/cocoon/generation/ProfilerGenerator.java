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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.profiler.EnvironmentInfo;
import org.apache.cocoon.components.profiler.Profiler;
import org.apache.cocoon.components.profiler.ProfilerResult;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generates an XML representation of the current status of Profiler.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ProfilerGenerator.java,v 1.6 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class ProfilerGenerator extends ServiceableGenerator {

    /**
     * The XML PROFILER_NS for the output document.
     */
    private static final String PROFILER_NS = "http://apache.org/cocoon/profiler/1.0";

    private static final String PROFILERINFO_ELEMENT = "profilerinfo";
    private static final String RESULTS_ELEMENT = "pipeline";
    private static final String RESULT_ELEMENT = "result";
    private static final String AVERAGERESULT_ELEMENT = "average";
    private static final String ENVIROMENTINFO_ELEMENT = "environmentinfo";
    private static final String REQUESTPARAMETERS_ELEMENT = "request-parameters";
    private static final String REQUESTPARAMETER_ELEMENT = "parameter";
    private static final String SESSIONATTRIBUTES_ELEMENT = "session-attributes";
    private static final String SESSIONATTRIBUTE_ELEMENT = "attribute";
    private static final String COMPONENT_ELEMENT = "component";
    private static final String FRAGMENT_ELEMENT = "fragment";
    private static final String PREFIX = "profiler";
    private static final String PREFIX_COLON = "profiler:";

    private Profiler profiler;

    // the key identifying the ProfilerResult
    private Long key = null;

    // Index of the result of latest results
    private int resultIndex = -1;

    // Index of the componen of the latest results
    private int componentIndex = -1;

    // Indicates if the fragment only, and not the profiler metadata around it, should be generated
    private boolean fragmentOnly;

    /**
     * Serviceable
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.profiler = (Profiler) super.manager.lookup(Profiler.ROLE);
    }

    /**
     * Setup of the profiler generator.
     */
    public void setup(SourceResolver resolver, Map objectModel, String soure,
                      Parameters parameters)
                        throws ProcessingException, SAXException,
                               IOException {

        super.setup(resolver, objectModel, source, parameters);
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (request.getParameter("key")!=null) {
            this.key = new Long(Long.parseLong(request.getParameter("key")));
        } else {
            this.key = null;
        }

        if ((request.getParameter("result")!=null) && (this.key!=null)) {
            this.resultIndex = Integer.parseInt(request.getParameter("result"));
        } else {
            this.resultIndex = -1;
        }

        if ((request.getParameter("component")!=null) &&
            (this.resultIndex!=-1)) {
            this.componentIndex = Integer.parseInt(request.getParameter("component"));
        } else {
            this.componentIndex = -1;
        }

        if (request.getParameter("fragmentonly") != null && request.getParameter("fragmentonly").equals("true")) {
            fragmentOnly = true;
        } else {
            fragmentOnly = false;
        }
    }

    /**
     * Disposable
     */
    public void dispose() {
        if (this.profiler!=null) {
            super.manager.release(this.profiler);
            this.profiler = null;
        }
        super.dispose();
    }

    /**
     * Generate the status information in XML format.
     * @throws SAXException
     *         when there is a problem creating the output SAX events.
     */
    public void generate() throws SAXException {
        // check if only the stored XML data is requested
        if (fragmentOnly && key != null && resultIndex != -1 && componentIndex != -1) {
            // find the fragment
            Object fragment = null;
            try {
                ProfilerResult result = profiler.getResult(key);
                fragment = result.getSAXFragments()[resultIndex][componentIndex];
            } catch (Exception e) {
                // fragment will be null
            }
            if (fragment != null) {
                generateSAXFragment(fragment, false);
            } else {
                this.contentHandler.startDocument();
                this.contentHandler.startPrefixMapping(PREFIX, PROFILER_NS);
                this.contentHandler.startElement(PROFILER_NS, "fragment-error", PREFIX_COLON + "fragment-error", new AttributesImpl());
                char[] message = "Fragment is not available.".toCharArray();
                this.contentHandler.characters(message, 0, message.length);
                this.contentHandler.endElement(PROFILER_NS, "fragment-error", PREFIX_COLON + "fragment-error");
                this.contentHandler.endPrefixMapping(PREFIX);
                this.contentHandler.endDocument();
            }
        } else {
            // Start the document and set the PROFILER_NS.
            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX, PROFILER_NS);

            generateProfilerInfo();

            // End the document.
            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();
        }
    }

    /**
     * Generate the main status document.
     */
    private void generateProfilerInfo() throws SAXException {
        // Root element.

        // The current date and processingTime.
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "date", "date", "CDATA", dateTime);
        this.contentHandler.startElement(PROFILER_NS, PROFILERINFO_ELEMENT,
                                         PREFIX_COLON + PROFILERINFO_ELEMENT, atts);

        Collection resultsKeys = profiler.getResultKeys();

        for (Iterator i = resultsKeys.iterator(); i.hasNext(); ) {
            Long key = (Long) i.next();

            if ((this.key==null) || (this.key.equals(key))) {
                generateResults(key, profiler.getResult(key));
            }
        }

        // End root element.
        this.contentHandler.endElement(PROFILER_NS, PROFILERINFO_ELEMENT,
                                       PREFIX_COLON + PROFILERINFO_ELEMENT);
    }

    /**
     *
     *
     * @param key        
     * @param result     
     */
    private void generateResults(Long key,
                                 ProfilerResult result) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        int count = result.getCount();
        String[] roles = result.getRoles();                     // Roles of the components
        String[] sources = result.getSources();                 // Source of the components

        EnvironmentInfo[] environmentInfos = result.getLatestEnvironmentInfos();
        long[] totalTime = result.getTotalTime();               // Total time of the requests
        long[][] setupTimes = result.getSetupTimes();           // Setup time of each component
        long[][] processingTimes = result.getProcessingTimes(); // Processing time of each component
        Object[][] fragments = result.getSAXFragments();        // SAX Fragments of each component

        // Total time of all requests
        long totalTimeSum = 0;

        for (int i = 0; i<count; i++)
            totalTimeSum += totalTime[i];

        atts.addAttribute("", "uri", "uri", "CDATA", result.getURI());
        atts.addAttribute("", "count", "count", "CDATA",
                          Integer.toString(result.getCount()));
        atts.addAttribute("", "processingTime", "processingTime", "CDATA",
                          Long.toString(totalTimeSum));
        atts.addAttribute("", "key", "key", "CDATA", key.toString());
        this.contentHandler.startElement(PROFILER_NS, RESULTS_ELEMENT,
                                         PREFIX_COLON + RESULTS_ELEMENT, atts);
        atts.clear();

        // Generate average result
        if ((count>0) && (this.resultIndex==-1)) {
            atts.addAttribute("", "time", "time", "CDATA",
                              Long.toString(totalTimeSum/count));
            this.contentHandler.startElement(PROFILER_NS,
                                             AVERAGERESULT_ELEMENT,
                                             PREFIX_COLON + AVERAGERESULT_ELEMENT, atts);
            atts.clear();

            // Total time of each component for all requests
            long[] totalTimeOfComponents = new long[roles.length];

            for (int i = 0; i<roles.length; i++) {
                totalTimeOfComponents[i] = 0;
                for (int j = 0; j<count; j++) {
                    totalTimeOfComponents[i] += setupTimes[j][i]+
                                                processingTimes[j][i];
                }
            }

            for (int i = 0; i<roles.length; i++) {
                atts.addAttribute("", "offset", "offset", "CDATA",
                                  String.valueOf(i));

                if (roles[i]!=null) {
                    atts.addAttribute("", "role", "role", "CDATA", roles[i]);
                }

                if (sources[i]!=null) {
                    atts.addAttribute("", "source", "source", "CDATA",
                                      sources[i]);
                }

                atts.addAttribute("", "time", "time", "CDATA",
                                  Long.toString(totalTimeOfComponents[i]/
                                                count));

                this.contentHandler.startElement(PROFILER_NS,
                                                 COMPONENT_ELEMENT,
                                                 PREFIX_COLON + COMPONENT_ELEMENT, atts);
                atts.clear();
                this.contentHandler.endElement(PROFILER_NS,
                                               COMPONENT_ELEMENT,
                                               PREFIX_COLON + COMPONENT_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS,
                                           AVERAGERESULT_ELEMENT,
                                           PREFIX_COLON + AVERAGERESULT_ELEMENT);
        }

        for (int j = 0; j<count; j++) {
            if ((this.resultIndex==-1) || (this.resultIndex==j)) {
                generateResult(j, roles, sources, environmentInfos[j],
                               totalTime[j], setupTimes[j],
                               processingTimes[j], fragments[j]);
            }
        }

        this.contentHandler.endElement(PROFILER_NS, RESULTS_ELEMENT,
                                       PREFIX_COLON + RESULTS_ELEMENT);
    }

    private void generateResult(int resultIndex, String[] roles,
                                String[] sources,
                                EnvironmentInfo environmentInfo,
                                long totalTime, long[] setupTimes,
                                long[] processingTimes,
                                Object[] fragments) throws SAXException {

        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "time", "time", "CDATA",
                          Long.toString(totalTime));
        atts.addAttribute("", "index", "index", "CDATA",
                          String.valueOf(resultIndex));
        this.contentHandler.startElement(PROFILER_NS, RESULT_ELEMENT,
                                         PREFIX_COLON + RESULT_ELEMENT, atts);
        atts.clear();

        if (this.resultIndex!=-1) {
            generateEnvironmentInfo(environmentInfo);
        }

        for (int i = 0; i<roles.length; i++) {
            generateComponent(i, roles[i], sources[i], setupTimes[i],
                              processingTimes[i], fragments[i]);
        }
        this.contentHandler.endElement(PROFILER_NS, RESULT_ELEMENT,
                                       PREFIX_COLON + RESULT_ELEMENT);
    }

    private void generateComponent(int componentIndex, String role,
                                   String source, long setupTime,
                                   long processingTime,
                                   Object fragment) throws SAXException {

        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "index", "index", "CDATA",
                          String.valueOf(componentIndex));

        if (role!=null) {
            atts.addAttribute("", "role", "role", "CDATA", role);
        }

        if (source!=null) {
            atts.addAttribute("", "source", "source", "CDATA", source);
        }

        atts.addAttribute("", "setup", "setup", "CDATA",
                          Long.toString(setupTime));

        atts.addAttribute("", "processing", "processing", "CDATA",
                          Long.toString(processingTime));

        atts.addAttribute("", "time", "time", "CDATA",
                          Long.toString(setupTime+processingTime));

        this.contentHandler.startElement(PROFILER_NS, COMPONENT_ELEMENT,
                                         PREFIX_COLON + COMPONENT_ELEMENT, atts);
        atts.clear();

        if (this.componentIndex==componentIndex) {
            this.contentHandler.startElement(PROFILER_NS, FRAGMENT_ELEMENT,
                                             PREFIX_COLON + FRAGMENT_ELEMENT,
                                             new AttributesImpl());
            generateSAXFragment(fragment, true);
            this.contentHandler.endElement(PROFILER_NS, FRAGMENT_ELEMENT,
                                           PREFIX_COLON + FRAGMENT_ELEMENT);
        }

        this.contentHandler.endElement(PROFILER_NS, COMPONENT_ELEMENT,
                                       PREFIX_COLON + COMPONENT_ELEMENT);
    }

    private void generateEnvironmentInfo(EnvironmentInfo environmentInfo)
      throws SAXException {
        this.contentHandler.startElement(PROFILER_NS, ENVIROMENTINFO_ELEMENT,
                                         PREFIX_COLON + ENVIROMENTINFO_ELEMENT,
                                         new AttributesImpl());

        if (environmentInfo!=null) {
            // Generate SAX events for the request parameters
            this.contentHandler.startElement(PROFILER_NS,
                                             REQUESTPARAMETERS_ELEMENT,
                                             PREFIX_COLON + REQUESTPARAMETERS_ELEMENT,
                                             new AttributesImpl());

            Map requestParameters = environmentInfo.getRequestParameters();
            Set requestParamEntries = requestParameters.entrySet();
            Iterator requestParamEntriesIt = requestParamEntries.iterator();

            while (requestParamEntriesIt.hasNext()) {
                AttributesImpl atts = new AttributesImpl();
                Map.Entry entry = (Map.Entry) requestParamEntriesIt.next();

                atts.addAttribute("", "name", "name", "CDATA",
                                  (String) entry.getKey());
                atts.addAttribute("", "value", "value", "CDATA",
                                  (String) entry.getValue());
                this.contentHandler.startElement(PROFILER_NS,
                                                 REQUESTPARAMETER_ELEMENT,
                                                 PREFIX_COLON + REQUESTPARAMETER_ELEMENT,
                                                 atts);
                this.contentHandler.endElement(PROFILER_NS,
                                               REQUESTPARAMETER_ELEMENT,
                                               PREFIX_COLON + REQUESTPARAMETER_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS,
                                           REQUESTPARAMETERS_ELEMENT,
                                           PREFIX_COLON + REQUESTPARAMETERS_ELEMENT);

            // Generate SAX events for the session attributes
            this.contentHandler.startElement(PROFILER_NS,
                                             SESSIONATTRIBUTES_ELEMENT,
                                             PREFIX_COLON + SESSIONATTRIBUTES_ELEMENT,
                                             new AttributesImpl());

            Map sessionAttributes = environmentInfo.getSessionAttributes();
            Set sessionAttrEntries = sessionAttributes.entrySet();
            Iterator sessionAttrEntriesIt = sessionAttrEntries.iterator();

            while (sessionAttrEntriesIt.hasNext()) {
                AttributesImpl atts = new AttributesImpl();
                Map.Entry entry = (Map.Entry) sessionAttrEntriesIt.next();

                atts.addAttribute("", "name", "name", "CDATA",
                                  (String) entry.getKey());
                atts.addAttribute("", "value", "value", "CDATA",
                                  (String) entry.getValue());
                this.contentHandler.startElement(PROFILER_NS,
                                                 SESSIONATTRIBUTE_ELEMENT,
                                                 PREFIX_COLON + SESSIONATTRIBUTE_ELEMENT,
                                                 atts);
                this.contentHandler.endElement(PROFILER_NS,
                                               SESSIONATTRIBUTE_ELEMENT,
                                               PREFIX_COLON + SESSIONATTRIBUTE_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS,
                                           SESSIONATTRIBUTES_ELEMENT,
                                           PREFIX_COLON + SESSIONATTRIBUTES_ELEMENT);

            // And the rest
            this.contentHandler.startElement(PROFILER_NS, "uri", PREFIX_COLON + "uri",
                                             new AttributesImpl());
            this.contentHandler.characters(environmentInfo.getURI().toCharArray(),
                                           0, environmentInfo.getURI().length());
            this.contentHandler.endElement(PROFILER_NS, "uri", PREFIX_COLON + "uri");
        }

        this.contentHandler.endElement(PROFILER_NS, ENVIROMENTINFO_ELEMENT,
                                       PREFIX_COLON + ENVIROMENTINFO_ELEMENT);
    }

    public void generateSAXFragment(Object fragment, boolean embed) throws SAXException {

        if (fragment!=null) {
            XMLDeserializer deserializer = null;

            try {
                deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
                if (embed)
                    deserializer.setConsumer(new IncludeXMLConsumer(this.xmlConsumer));
                else
                    deserializer.setConsumer(this.xmlConsumer);
                deserializer.deserialize(fragment);
            } catch (ServiceException ce) {
                getLogger().debug("Could not retrieve XMLDeserializer component",
                                  ce);
                throw new SAXException("Could not retrieve XMLDeserializer component",
                                       ce);
            } catch (Exception e) {
                getLogger().debug("Could not serialize SAX fragment", e);
                throw new SAXException("Could not serialize SAX fragment", e);
            } finally {
                if (deserializer!=null) {
                    this.manager.release(deserializer);
                }
            }
        }
    }
}

