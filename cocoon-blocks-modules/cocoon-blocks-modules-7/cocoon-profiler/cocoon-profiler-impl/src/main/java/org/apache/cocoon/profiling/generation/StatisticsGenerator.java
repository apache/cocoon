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
package org.apache.cocoon.profiling.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.profiling.statistics.Collector;
import org.apache.cocoon.profiling.statistics.PageReport;
import org.apache.cocoon.profiling.statistics.Report;
import org.apache.cocoon.profiling.statistics.Statistics;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * Statistic-Generator.
 *
 * @version $Id$
 * @since 2.1.10
 */
public class StatisticsGenerator
    extends ServiceableGenerator {

    protected Collector collector;

    /**
     * @see org.apache.cocoon.generation.AbstractGenerator#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        final Request req = ObjectModelHelper.getRequest(objectModel);
        if ( req.getParameter("clear") != null ) {
            this.collector.clear();
        }
        if ( req.getParameter("enable") != null ) {
            this.collector.setCollectingStatistics(true);
        }
        if ( req.getParameter("disable") != null ) {
            this.collector.setCollectingStatistics(false);
        }
    }

    /**
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        this.xmlConsumer.startDocument();
        XMLUtils.startElement(this.xmlConsumer, "statistics");

        if ( this.collector.isCollectingStatistics() ) {
            XMLUtils.startElement(this.xmlConsumer, "reports");
            boolean found = false;
            List entries = new ArrayList(this.collector.getStatistics());
            Collections.sort(entries, new ReportComparator());
            final Iterator i = entries.iterator();
            while ( i.hasNext() ) {
                found = true;
                final Report report = (Report)i.next();
                final AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("name", report.getCategory());
                XMLUtils.startElement(this.xmlConsumer, "report", attrs);
                XMLUtils.createElement(this.xmlConsumer, "count", String.valueOf(report.getCount()));
                XMLUtils.createElement(this.xmlConsumer, "min", this.getTime(report.getMin()));
                XMLUtils.createElement(this.xmlConsumer, "max", this.getTime(report.getMax()));
                XMLUtils.createElement(this.xmlConsumer, "last", this.getTime(report.getLast()));
                XMLUtils.createElement(this.xmlConsumer, "all", report.getAll());
                XMLUtils.createElement(this.xmlConsumer, "average", this.getTime(report.getAverage()));
                XMLUtils.endElement(this.xmlConsumer, "report");
            }
            if ( !found ) {
                XMLUtils.data(this.xmlConsumer, "No reports");                
            }
            XMLUtils.endElement(this.xmlConsumer, "reports");
            if ( found ) {
                XMLUtils.startElement(this.xmlConsumer, "pages");
                entries = new ArrayList(this.collector.getPageReports());
                Collections.sort(entries, new PageReportComparator());
                final Iterator pi = entries.iterator();
                while ( pi.hasNext() ) {
                    final PageReport report = (PageReport)pi.next();
                    final AttributesImpl attrs = new AttributesImpl();
                    attrs.addCDATAAttribute("id", report.getId());
                    attrs.addCDATAAttribute("date", report.getDate().toString());
                    XMLUtils.startElement(this.xmlConsumer, "report", attrs);
                    final Iterator si = report.getStatistics().iterator();
                    while ( si.hasNext() ) {
                        final Statistics stats = (Statistics)si.next();
                        attrs.clear();
                        attrs.addCDATAAttribute("name", stats.getCategory());
                        attrs.addCDATAAttribute("duraration", String.valueOf(stats.getDuration()));
                        XMLUtils.createElement(this.xmlConsumer, "component", attrs);
                    }
                    XMLUtils.endElement(this.xmlConsumer, "report");
                }
                XMLUtils.endElement(this.xmlConsumer, "pages");                
            }
        } else {
            XMLUtils.data(this.xmlConsumer, "Turned off");
        }
        XMLUtils.endElement(this.xmlConsumer, "statistics");
        this.xmlConsumer.endDocument();
    }

    protected String getTime(long msecs) {
        long secs = msecs / 1000;
        StringBuffer buffer = new StringBuffer();
        buffer.append(secs);
        buffer.append('.');
        long rest = (msecs - secs * 1000);
        if ( rest < 100 ) {
            buffer.append('0');
        }
        if ( rest < 10 ) {
            buffer.append('0');
        }
        buffer.append(rest);
        buffer.append('s');
        return buffer.toString();
    }

    /**
     * @see org.apache.cocoon.generation.ServiceableGenerator#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.collector);
            this.collector = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.generation.ServiceableGenerator#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        super.service(aManager);
        this.collector = (Collector)this.manager.lookup(Collector.class.getName());
    }

    public static final class ReportComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            Report r1 = (Report)o1;
            Report r2 = (Report)o2;
            return r1.getCategory().compareTo(r2.getCategory());
        }
    }

    public static final class PageReportComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            PageReport r1 = (PageReport)o1;
            PageReport r2 = (PageReport)o2;
            return r1.getId().compareTo(r2.getId());
        }
    }
}
