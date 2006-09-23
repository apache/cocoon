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
package org.apache.cocoon.profiler.debugging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/**
 * This is a simple cli client for the {@link Debugger}.
 * @since 2.2
 * @version $Id$
 */
public class Client implements Runnable {

    protected static final String HOST = "localhost";
    protected static final int PORT = 4444;

    protected PrintWriter  writer;
    protected Reader       reader;
    protected ServerSocket server;
    protected Socket       socket;

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            this.server = new ServerSocket(PORT);
            
            this.socket = this.server.accept();
            if ( this.socket != null ) {
                this.writer = new PrintWriter(this.socket.getOutputStream());
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                while ( this.isSocketReaderReady() ) {
                    int read;
                    final StringBuffer response = new StringBuffer();
                    do {
                        try {
                            read = this.reader.read();
                            if (read > 0) {
                                response.append((char)read);
                            }
                        } catch (IOException ioe) {
                            // ignore
                            read = 0;
                        }
                    } while (read > 0);
                    System.out.println("Response");
                    System.out.println(response.toString());
                    System.out.println();
                    this.writer.write("<message><status>0</status></message>");
                    this.writer.write(0);
                    this.writer.flush();
                }
                this.writer.close();
                this.reader.close();
            }
            
        } catch (Exception ignore) {
            ignore.printStackTrace();
        } finally {
            this.close();
        }
    }

    /**
     * Close the connection
     */
    protected void close() {
        if (this.socket != null) {
            try {
                this.writer.close();
            } catch (Exception ignore) {}
            try {
                this.reader.close();
            } catch (Exception ignore) {}
            try {
                this.socket.close();
            } catch (Exception ignore) {}
            this.socket = null;
            this.reader = null;
            this.writer = null;
        }
        if ( this.server != null ) {
            try {
                this.server.close();
            } catch (Exception ignore) {}            
        }
    }

    protected boolean isSocketReaderReady() {
        try {
            while (!this.reader.ready() /*&& !this.socket.isInputShutdown()*/) {
                try {
                    java.lang.Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            return this.reader.ready();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * The <code>main</code> method.
     *
     * @param args a <code>String[]</code> of arguments
     * @exception Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        // the first argument is the full uri
        if ( args.length == 0 ) {
            printUsage();
        }
        // start thread
        Thread t = new Thread(new Client());
        t.start();
        Thread.yield();
        System.out.println("Invoking.");
        StringBuffer buffer = new StringBuffer(args[0]);
        if ( buffer.indexOf("?") != -1 ) {
            buffer.append('&');
        } else {
            buffer.append('?');
        }
        buffer.append(Debugger.REQUEST_PARAMETER);
        buffer.append('=');
        buffer.append(HOST);
        buffer.append(':');
        buffer.append(PORT);

        URL url = new URL(buffer.toString());
        InputStream is = url.openConnection().getInputStream();
        byte[] b = new byte[4096];
        while (is.read(b) > -1) {
            // ignore content
        }
        is.close();
    }

    protected static void printUsage() {
        System.out.println("Usage: client [uri]");
    }
}
