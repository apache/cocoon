/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.generation;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
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
 * @version CVS $Id: ProfilerGenerator.java,v 1.1 2003/03/09 00:05:52 pier Exp $
 */
public class ProfilerGenerator extends ComposerGenerator {

    /**
     * The XML PROFILER_NS for the output document.
     */
    protected static final String PROFILER_NS = "http://apache.org/cocoon/profiler/1.0";

    protected static final String PROFILERINFO_ELEMENT = "profilerinfo";
    protected static final String RESULTS_ELEMENT = "pipeline";
    protected static final String RESULT_ELEMENT = "result";
    protected static final String AVERAGERESULT_ELEMENT = "average";
    protected static final String ENVIROMENTINFO_ELEMENT = "environmentinfo";
    protected static final String REQUESTPARAMETERS_ELEMENT = "request-parameters";
    protected static final String REQUESTPARAMETER_ELEMENT = "parameter";
    protected static final String SESSIONATTRIBUTES_ELEMENT = "session-attributes";
    protected static final String SESSIONATTRIBUTE_ELEMENT = "attribute";
    protected static final String COMPONENT_ELEMENT = "component";
    protected static final String FRAGMENT_ELEMENT = "fragment";
    

    private Profiler profiler;

    // the key identifying the ProfilerResult
    protected Long key = null;

    // Index of the result of latest results 
    protected int resultIndex = -1;

    // Index of the componen of the latest results
    protected int componentIndex = -1;

    /**
     * Composable
     */
    public void compose(ComponentManager manager)
    throws ComponentException {
        super.compose(manager);
        this.profiler = (Profiler)super.manager.lookup(Profiler.ROLE);
    }

    public void setup(SourceResolver resolver, Map objectModel, String soure, Parameters parameters) 
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, source, parameters);
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (request.getParameter("key")!=null)
            this.key = new Long(Long.parseLong(request.getParameter("key")));
        else
            this.key = null;

        if ((request.getParameter("result")!=null) && (this.key!=null))
            this.resultIndex = Integer.parseInt(request.getParameter("result"));
        else
            this.resultIndex = -1;

        if ((request.getParameter("component")!=null) && (this.resultIndex!=-1))
            this.componentIndex = Integer.parseInt(request.getParameter("component"));
        else
            this.componentIndex = -1;
    }

    /**
     * Disposable
     */
    public void dispose() {
        if (this.profiler != null){
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

        // Start the document and set the PROFILER_NS.
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping("", PROFILER_NS);

        generateProfilerInfo();

        // End the document.
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    /** Generate the main status document. */
    private void generateProfilerInfo() throws SAXException {
        // Root element.

        // The current date and time.
        String dateTime = DateFormat.getDateTimeInstance().format(new Date());

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(PROFILER_NS, "date", "date", "CDATA", dateTime);
        this.contentHandler.startElement(PROFILER_NS, PROFILERINFO_ELEMENT, PROFILERINFO_ELEMENT, atts);

        Collection resultsKeys = profiler.getResultKeys();
        for(Iterator i = resultsKeys.iterator(); i.hasNext();) {
            Long key = (Long)i.next();
            if ((this.key==null) || (this.key.equals(key)))
                generateResults(key, profiler.getResult(key));
        }

        // End root element.
        this.contentHandler.endElement(PROFILER_NS, PROFILERINFO_ELEMENT, PROFILERINFO_ELEMENT);
    }

    private void generateResults(Long key, ProfilerResult result) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        int count = result.getCount();
        String[] roles = result.getRoles(); // Roles of the components
        String[] sources = result.getSources(); // Source of the components
        
        EnvironmentInfo[] environmentInfos = result.getLatestEnvironmentInfos();
        long[] totalTime = result.getTotalTime(); // Total time of the requests
        long[][] timeOfComponents = result.getLastTimes(); // Time of each component
        Object[][] fragments = result.getLatestSAXFragments(); // SAX Fragments of each component

        // Total time of all requests
        long totalTimeSum = 0; 
        for(int i=0; i < count; i++)
            totalTimeSum += totalTime[i];

        atts.addAttribute(PROFILER_NS, "uri", "uri", "CDATA", result.getURI());
        atts.addAttribute(PROFILER_NS, "count", "count", "CDATA", Integer.toString(result.getCount()));
        atts.addAttribute(PROFILER_NS, "time", "time", "CDATA", Long.toString(totalTimeSum));
        atts.addAttribute(PROFILER_NS, "key", "key", "CDATA", key.toString());
        this.contentHandler.startElement(PROFILER_NS, RESULTS_ELEMENT, RESULTS_ELEMENT, atts);
        atts.clear();

        // Generate average result 
        if ((count > 0) && (this.resultIndex==-1)) {
            atts.addAttribute(PROFILER_NS, "time", "time", "CDATA", Long.toString(totalTimeSum / count));
            this.contentHandler.startElement(PROFILER_NS, AVERAGERESULT_ELEMENT, AVERAGERESULT_ELEMENT, atts);
            atts.clear();

            // Total time of each component for all requests
            long[] totalTimeOfComponents = new long[roles.length]; 
          
            for(int i=0; i<roles.length; i++) {
                totalTimeOfComponents[i] = 0;
                for(int j=0; j<count; j++) {
                    totalTimeOfComponents[i] += timeOfComponents[j][i];
                }
            }

            for(int i=0; i<roles.length; i++){
                atts.addAttribute(PROFILER_NS, "offset", "offset", "CDATA", String.valueOf(i));

                if(roles[i] != null)
                    atts.addAttribute(PROFILER_NS, "role", "role", "CDATA", roles[i]);

                if(sources[i] != null)
                    atts.addAttribute(PROFILER_NS, "source", "source", "CDATA", sources[i]);

                atts.addAttribute(PROFILER_NS, "time", "time", "CDATA", 
                                  Long.toString(totalTimeOfComponents[i] / count));

                this.contentHandler.startElement(PROFILER_NS, COMPONENT_ELEMENT, COMPONENT_ELEMENT, atts);
                atts.clear();
                this.contentHandler.endElement(PROFILER_NS, COMPONENT_ELEMENT, COMPONENT_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS, AVERAGERESULT_ELEMENT, AVERAGERESULT_ELEMENT);
        }

        for(int j=0; j<count; j++) {
            if ((this.resultIndex==-1) || (this.resultIndex==j))
                generateResult(j, roles, sources, environmentInfos[j], 
                               totalTime[j], timeOfComponents[j], fragments[j]);
        }

        this.contentHandler.endElement(PROFILER_NS, RESULTS_ELEMENT, RESULTS_ELEMENT);
    }

    private void generateResult(int resultIndex, String[] roles, String[] sources, 
                                EnvironmentInfo environmentInfo,
                                long totaltime, long[] times, Object[] fragments) throws SAXException  {
 
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(PROFILER_NS, "time", "time", "CDATA", Long.toString(totaltime));
        atts.addAttribute(PROFILER_NS, "index", "index", "CDATA", String.valueOf(resultIndex));
        this.contentHandler.startElement(PROFILER_NS, RESULT_ELEMENT, RESULT_ELEMENT, atts);
        atts.clear();

        if (this.resultIndex!=-1)
            generateEnvironmentInfo(environmentInfo);

        for(int i=0; i<roles.length; i++) {
            generateComponent(i, roles[i], sources[i], times[i], fragments[i]);
        }
        this.contentHandler.endElement(PROFILER_NS, RESULT_ELEMENT, RESULT_ELEMENT);
    }

    private void generateComponent(int componentIndex, String role, String source, long time, Object fragment) 
        throws SAXException  {

        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(PROFILER_NS, "index", "index", "CDATA", String.valueOf(componentIndex));

        if (role != null)
            atts.addAttribute(PROFILER_NS, "role", "role", "CDATA", role);

        if (source != null)
            atts.addAttribute(PROFILER_NS, "source", "source", "CDATA", source);

        atts.addAttribute(PROFILER_NS, "time", "time", "CDATA", Long.toString(time));

        this.contentHandler.startElement(PROFILER_NS, COMPONENT_ELEMENT, COMPONENT_ELEMENT, atts);
        atts.clear();

        if (this.componentIndex==componentIndex)
            generateSAXFragment(fragment);

        this.contentHandler.endElement(PROFILER_NS, COMPONENT_ELEMENT, COMPONENT_ELEMENT);
    }

    private void generateEnvironmentInfo(EnvironmentInfo environmentInfo) throws SAXException  {
        this.contentHandler.startElement(PROFILER_NS, ENVIROMENTINFO_ELEMENT, ENVIROMENTINFO_ELEMENT, 
                                         new AttributesImpl());

        if (environmentInfo != null) {
            // Generate SAX events for the request parameters
            this.contentHandler.startElement(PROFILER_NS, REQUESTPARAMETERS_ELEMENT, REQUESTPARAMETERS_ELEMENT, 
                                        new AttributesImpl());

            Map requestParameters = environmentInfo.getRequestParameters();
            Set requestParamEntries = requestParameters.entrySet();
            Iterator requestParamEntriesIt = requestParamEntries.iterator();
            while (requestParamEntriesIt.hasNext()) {
                AttributesImpl atts = new AttributesImpl();
                Map.Entry entry = (Map.Entry)requestParamEntriesIt.next();
                atts.addAttribute(PROFILER_NS, "name", "name", "CDATA", (String)entry.getKey());
                atts.addAttribute(PROFILER_NS, "value", "value", "CDATA", (String)entry.getValue());
                this.contentHandler.startElement(PROFILER_NS, REQUESTPARAMETER_ELEMENT, 
                                                 REQUESTPARAMETER_ELEMENT, atts);
                this.contentHandler.endElement(PROFILER_NS, REQUESTPARAMETER_ELEMENT, 
                                               REQUESTPARAMETER_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS, REQUESTPARAMETERS_ELEMENT, REQUESTPARAMETERS_ELEMENT);

            // Generate SAX events for the session attributes
            this.contentHandler.startElement(PROFILER_NS, SESSIONATTRIBUTES_ELEMENT, SESSIONATTRIBUTES_ELEMENT, 
                                        new AttributesImpl());

            Map sessionAttributes = environmentInfo.getSessionAttributes();
            Set sessionAttrEntries = sessionAttributes.entrySet();
            Iterator sessionAttrEntriesIt = sessionAttrEntries.iterator();
            while (sessionAttrEntriesIt.hasNext()) {
                AttributesImpl atts = new AttributesImpl();
                Map.Entry entry = (Map.Entry)sessionAttrEntriesIt.next();
                atts.addAttribute(PROFILER_NS, "name", "name", "CDATA", (String)entry.getKey());
                atts.addAttribute(PROFILER_NS, "value", "value", "CDATA", (String)entry.getValue());
                this.contentHandler.startElement(PROFILER_NS, SESSIONATTRIBUTE_ELEMENT, 
                                                 SESSIONATTRIBUTE_ELEMENT, atts);
                this.contentHandler.endElement(PROFILER_NS, SESSIONATTRIBUTE_ELEMENT, 
                                               SESSIONATTRIBUTE_ELEMENT);
            }
            this.contentHandler.endElement(PROFILER_NS, SESSIONATTRIBUTES_ELEMENT, SESSIONATTRIBUTES_ELEMENT);

            // And the rest
            this.contentHandler.startElement(PROFILER_NS, "uri", "uri", new AttributesImpl());
            this.contentHandler.characters(environmentInfo.getURI().toCharArray(), 0, 
                                           environmentInfo.getURI().length());
            this.contentHandler.endElement(PROFILER_NS, "uri", "uri");
        }

        this.contentHandler.endElement(PROFILER_NS, ENVIROMENTINFO_ELEMENT, ENVIROMENTINFO_ELEMENT);
    }

    public void generateSAXFragment(Object fragment) throws SAXException {

        if (fragment!=null) {

            this.contentHandler.startElement(PROFILER_NS, FRAGMENT_ELEMENT, FRAGMENT_ELEMENT, 
                                             new AttributesImpl());

            XMLDeserializer deserializer = null;
            try {
                deserializer = (XMLDeserializer)this.manager.lookup(XMLDeserializer.ROLE);
                deserializer.setConsumer(new IncludeXMLConsumer(this.xmlConsumer));
                deserializer.deserialize(fragment);
            } catch (ComponentException ce) {
                getLogger().debug("Could not retrieve XMLDeserializer component", ce);
                throw new SAXException("Could not retrieve XMLDeserializer component", ce);
            } catch (Exception e) {
                getLogger().debug("Could not serialize SAX fragment", e);
                throw new SAXException("Could not serialize SAX fragment", e);
            } finally { 
                if (deserializer!=null)
                    this.manager.release(deserializer);
            }

            this.contentHandler.endElement(PROFILER_NS, FRAGMENT_ELEMENT, FRAGMENT_ELEMENT);
        }
    }
}

