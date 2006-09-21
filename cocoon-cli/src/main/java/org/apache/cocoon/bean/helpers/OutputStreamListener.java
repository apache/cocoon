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
package org.apache.cocoon.bean.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.bean.BeanListener;
import org.apache.cocoon.bean.Target;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @version $Id$
 */
public class OutputStreamListener implements BeanListener {

    private final PrintWriter writer;
    private final Map brokenLinks = new HashMap();
    private final long startTimeMillis;
    private String reportFile = null;
    private String reportType = "text";
    private long siteSize = 0L;
    private int sitePages = 0;
    private boolean isShowingReferrers = false;
    
    public OutputStreamListener(OutputStream os) {
        writer = new PrintWriter(os);
        startTimeMillis = System.currentTimeMillis();
    }

    public void setReportFile(String filename) {
        reportFile = filename;
    }

    public void setReportType(String type) {
        reportType = type;
    }

    public void setIsShowingReferrers(boolean isShowingReferrers) {
        this.isShowingReferrers = isShowingReferrers;
    }
    
    public void pageGenerated(String sourceURI,
                              String destinationURI,
                              int pageSize,
                              int linksInPage,
                              int newLinksInPage,
                              int pagesRemaining,
                              int pagesComplete,
                              long timeTaken) {
        this.siteSize += pageSize;
        this.sitePages++;

        double time = (((double)timeTaken)/1000);

        String size;
        if (pageSize < 1024) {
            size = pageSize + "b";
        } else {
            size = ((float)((int)(pageSize/102.4)))/10 + "Kb";
        }

        if (linksInPage == -1) {
            this.print("* " + sourceURI);
        } else {
            this.print(pad(12, "* [" + pagesComplete + "/" + pagesRemaining + "] ") +
                       pad(10, "[" + newLinksInPage + "/" + linksInPage + "] ") +
                       pad(7,time + "s ") +
                       pad(7, size) + " " +
                       sourceURI);
        }

    }
    public void messageGenerated(String msg) {
        this.print(msg);
    }

    public void warningGenerated(String uri, String warning) {
        this.print("Warning: "+warning + " when generating " + uri);
    }

    /**
     * @deprecated use brokenLinkFound(String, List, String, Throwable) instead
     */
    public void brokenLinkFound(String uri, String parentURI, String message, Throwable t) {
        this.print(pad(42,"X [0] ")+uri+"\tBROKEN: "+message);
        brokenLinks.put(uri + "\t" + message, "");
    }

    public void brokenLinkFound(Target target, Throwable t) {
        this.print(pad(42,"X [0] ")+target.getSourceURI()+"\tBROKEN: "+t.getMessage());
        brokenLinks.put(target.getSourceURI() + "\t" + t.getMessage(), target);

    }

    public void pageSkipped(String uri, String message) {
        this.print(pad(37, "^ ") + uri);
    }

    public void complete() {
        outputBrokenLinks();

        long duration = System.currentTimeMillis() - startTimeMillis;
        DecimalFormat df = new DecimalFormat("###,###,##0");

        this.print("Total time: " +
                   (duration / 60000) + " minutes " +
                   (duration % 60000)/1000 + " seconds, " +
                   " Site size: " + df.format(this.siteSize) +
                   " Site pages: " + this.sitePages);
        this.close();
    }

    public boolean isSuccessful() {
        return brokenLinks.size() == 0;
    }

    private void outputBrokenLinks() {
        if (reportFile == null) {
            return;
        } else if ("text".equalsIgnoreCase(reportType)) {
            outputBrokenLinksAsText();
        } else if ("xml".equalsIgnoreCase(reportType)) {
            outputBrokenLinksAsXML();
        }
    }

    private void outputBrokenLinksAsText() {
        PrintWriter writer;
        try {
            writer =
                    new PrintWriter(
                            new FileWriter(new File(reportFile)),
                            true);
            for (Iterator i = brokenLinks.keySet().iterator(); i.hasNext();) {
                writer.println((String) i.next());
            }
            writer.close();
        } catch (IOException ioe) {
            this.print("Broken link file does not exist: " + reportFile);
        }
    }
    private void outputBrokenLinksAsXML() {
        PrintWriter writer;
        try {
            writer =
                    new PrintWriter(
                            new FileWriter(new File(reportFile)),
                            true);
            writer.println("<broken-links>");
            for (Iterator i = brokenLinks.keySet().iterator(); i.hasNext();) {
                String linkMsg = (String) i.next();
                String uri = linkMsg.substring(0,linkMsg.indexOf('\t'));
                String msg = linkMsg.substring(linkMsg.indexOf('\t')+1);
                if (!isShowingReferrers) {
                    writer.println("  <link message=\"" + msg + "\">" + uri + "</link>");
                } else {
                    writer.println("  <link message=\"" + msg + "\" uri=\"" + uri + "\">");
                    Object t = brokenLinks.get(linkMsg);
                    if (t instanceof Target) {
                        Target target = (Target)t;
                        for (Iterator j = target.getReferringURIs().iterator(); j.hasNext();) {
                            String referrer=(String) j.next();
                            writer.println("    <referrer uri=\"" + referrer + "\"/>");
                        }
                    }
                    writer.println("  </link>");
                }
            }
            writer.println("</broken-links>");
            writer.close();
        } catch (IOException ioe) {
            this.print("Could not create broken link file: " + reportFile);
        }
    }

    private String pad(int chars, String str) {
        int len = str.length();
        if (len < chars) {
            StringBuffer sb = new StringBuffer(chars > len ? chars+1 : len+1);
            sb.append(str);
            for (int i=len; i<chars; i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
        return str;
    }

    private void print(String message) {
        writer.println(message);
        writer.flush();
    }

    private void close() {
        // Flush the writer, but don't close the underlying stream.
        writer.flush();
    }
}
