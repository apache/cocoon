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
package org.apache.cocoon.bean.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.cocoon.bean.BeanListener;

/**
 * Command line entry point. Parses command line, create Cocoon bean and invokes it
 * with file destination.
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: OutputStreamListener.java,v 1.4 2003/09/27 09:50:29 upayavira Exp $
 */
public class OutputStreamListener implements BeanListener {
    
    private final PrintWriter writer;
    private final List brokenLinks = new ArrayList();
    private final long startTimeMillis;    
    private String reportFile = null;
    private String reportType = "text";
   
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
    
    public void pageGenerated(String sourceURI,
                              String destinationURI, 
                              int pageSize,
                              int linksInPage, 
                              int newLinksInPage, 
                              int pagesRemaining, 
                              int pagesComplete, 
                              long timeTaken) {
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
            this.print(pad(8, "* ["+linksInPage + "] ") +
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

    public void brokenLinkFound(String uri, String parentURI, String message, Throwable t) {
        this.print(pad(28,"X [0] ")+uri+"\tBROKEN: "+message);
        brokenLinks.add(uri + "\t" + message);
        
//            StringWriter sw = new StringWriter();
//            t.printStackTrace(new PrintWriter(sw));
//            System.out.println(sw.toString());

    }

    public void pageSkipped(String uri, String message) {
        this.print(pad(23, "^ ") + uri);
    }
    
    public void complete() {
        outputBrokenLinks();

        long duration = System.currentTimeMillis() - startTimeMillis;
        this.print("Total time: " + (duration / 60000) + " minutes " + (duration % 60000)/1000 + " seconds");
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
            for (Iterator i = brokenLinks.iterator(); i.hasNext();) {
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
            for (Iterator i = brokenLinks.iterator(); i.hasNext();) {
                String linkMsg = (String) i.next();
                String uri = linkMsg.substring(0,linkMsg.indexOf('\t'));
                String msg = linkMsg.substring(linkMsg.indexOf('\t')+1);
                writer.println("  <link message=\"" + msg + "\">" + uri + "</link>");
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
        writer.close();
    }
}
