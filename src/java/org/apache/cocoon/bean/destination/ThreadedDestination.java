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
package org.apache.cocoon.bean.destination;

import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <code>Threaded Destination</code>: A <code>Destination</code> for dispatching Cocoon content to
 * other Destination objects, in a multithreaded environment.
 *
 * The threaded destination fulfills the following goals:
 *	- to allow content to be dispatched to a remote server in as close as possible to real time,
 *    by having multiple threads simultaneously dispatching threads
 *	- to allow content to be dispatched to a range of destinations, depending upon the Cocoon
 *    URI of the Content.
 *
 * The first of these aims can be achieved with code such as:
 *
 *   <code>Integer maxThreads = 10;
 *   cocoon.process(targets, new ThreadedDestination(
 *                      new FTPDestination("ftp://user:pass@ftp.host.com")), maxThreads);</code>
 *
 * Thus the ThreadedDestination object is handed a single threaded destination object with
 * which to dispatch the content.
 *
 * (Note, this object may be cloned for use in a multithreaded environment. Therefore, in order
 * to be usable with the ThreadedDestination, a Destination object must implement the Cloneable
 *  Interface.)
 *
 * For the second scenario, the user builds up a DestinationMap object, which matches Cocoon URIs
 * to Destination objects.
 *
 * Sample code would be:
 *
 *   <code>Map map = new HashMap();
 *   HTTPDestination http1 = new HTTPDestination("http://192.168.1.100/foo.asp", "text/html");
 *   map.add("site/web.html", http1);
 *
 *   FTPDestination ftp1 = new FTPDestination("ftp://user:pass@ftp.host.com");
 *   map.add("site/page2.html", ftp1);
 *
 *   EmailDestination email1 = new EmailDestination("from", "to", "subject", "smtp.myisp.com");
 *   map.add("site/email.txt", email1);
 *
 *   FileDestination file1 = new FileDestination("path");
 *   map.add("site/file.html", file1);
 *
 *   Integer maxThreads = 10;
 *   cocoon.process(map.getTargets(), new ThreadedDestination(map, maxThreads));
 *   </code>
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: ThreadedDestination.java,v 1.1 2003/03/09 00:08:43 pier Exp $
 */
public class ThreadedDestination implements Destination {

    private List results = new LinkedList();
    private int maxThreads;
    private int currentThreads;
    private Destination destination = null;
    private Map destinations = null;

    public ThreadedDestination (Destination destination, int maxThreads) {
        this.destination = destination;
        this.maxThreads = maxThreads;
    }

    public ThreadedDestination (Map destinationMap, int maxThreads) {
        this.destinations = destinationMap;
        this.maxThreads = maxThreads;
    }

    public Object clone () {
        throw new IllegalArgumentException("Not Implemented");
    }

    public OutputStream getOutputStream (final String fileName) throws IOException {
        return new ByteArrayOutputStream() {
            public synchronized void close () throws IOException {
                super.close();
                onClose(fileName, this);
            }
        };
    }

    void onClose (String fileName, ByteArrayOutputStream stream) throws IOException {
        Destination d;
        if (destination != null){
            d = destination;
        } else {
            d = (Destination)destinations.get(fileName);
        }

        if (d == null){
            throw new DestinationNotFoundException(fileName);
        } else {
            d = (Destination) d.clone();
        }

        results.add(new ThreadedDispatchRequest(stream.toByteArray(), fileName, d));
        startThread();
    }

    private synchronized void startThread(){
        if (currentThreads < maxThreads){
            currentThreads++;
            new DestinationThread().start();
        }
    }

    synchronized void endThread(){
        currentThreads--;
    }

    synchronized ThreadedDispatchRequest getRequest() {
        if (results.size() > 0) {
            return (ThreadedDispatchRequest) results.remove(0);
        }
        return null;
    }

    private class DestinationThread extends Thread {
        public void run() {
            ThreadedDispatchRequest request;
            try {
                while ((request = getRequest()) != null) {
                    try {
                        request.process();
                    } catch (IOException e) {
                        // TODO: Handle Exception
                    }
                }
            } finally {
                endThread();
            }
        }
    }

    private class ThreadedDispatchRequest {
        private byte[] content;
        private String filename;
        private Destination destination;

        public ThreadedDispatchRequest (byte[] content, String filename, Destination destination) {
            this.content = content;
            this.filename = filename;
            this.destination = destination;
        }

        public void process() throws IOException {
            OutputStream out = destination.getOutputStream(filename);
            out.write(content);
            out.close();
        }
    }
}
