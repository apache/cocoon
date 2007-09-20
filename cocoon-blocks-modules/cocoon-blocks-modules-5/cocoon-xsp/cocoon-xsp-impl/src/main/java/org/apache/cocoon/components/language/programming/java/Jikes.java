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
package org.apache.cocoon.components.language.programming.java;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.language.programming.CompilerError;
import org.apache.cocoon.thread.RunnableManager;
import EDU.oswego.cs.dl.util.concurrent.CountDown;

/**
 * This class wraps IBM's <i>Jikes</i> Java compiler
 * NOTE: inspired by the Apache Jasper implementation.
 * @version $Id$
 * @since 2.0
 */

public class Jikes extends AbstractJavaCompiler implements Serviceable {

    static final int OUTPUT_BUFFER_SIZE = 1024;
    static final int BUFFER_SIZE = 512;

    private ServiceManager m_serviceManager;

    private class StreamPumper implements Runnable {

        private BufferedInputStream stream;
        private boolean endOfStream = false;
        private int SLEEP_TIME = 5;
        private OutputStream out;
        private CountDown m_done;

        public StreamPumper(BufferedInputStream is, OutputStream out, CountDown done) {
            this.stream = is;
            this.out = out;
            m_done = done;
        }

        public void pumpStream() throws IOException {
            byte[] buf = new byte[BUFFER_SIZE];
            if (!endOfStream) {
                int bytesRead = stream.read(buf, 0, BUFFER_SIZE);

                if (bytesRead > 0) {
                    out.write(buf, 0, bytesRead);
                } else if (bytesRead == -1) {
                    endOfStream = true;
                }
            }
        }

        public void run() {
            try {
                while (!endOfStream) {
                    pumpStream();
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (Exception e) {
               // getLogger().warn("Jikes.run()", e);
            }
            m_done.release(); // signal 'we are finished'
        }
    }

    /**
     * Set the {@link ServiceManager}
     */
    public void service( ServiceManager serviceManager )
    throws ServiceException
    {
        m_serviceManager = serviceManager;
    }

    /**
     * Copy arguments to a string array
     *
     * @param arguments The compiler arguments
     * @return A string array containing compilation arguments
     */
    protected String[] toStringArray(List arguments) {
      int i;

      for (i = 0; i < arguments.size(); i++) {
        String arg = (String) arguments.get(i);
        if (arg.equals("-sourcepath")) {
          // Remove -sourcepath option. Jikes does not understand that.
          arguments.remove(i);
          arguments.remove(i);
          break;
        }
      }

      String[] args = new String[arguments.size() + 1];
      for (i = 0; i < arguments.size(); i++) {
        args[i] = (String) arguments.get(i);
      }

      args[i] = file;

      return args;
    }

    /**
     * Execute the compiler
     */
    public boolean compile() throws IOException {

        List args = new ArrayList();
        // command line name
        args.add("jikes");
        // indicate Emacs output mode must be used
        args.add("+E");
        // avoid warnings
        // Option nowarn with one hyphen only
        args.add("-nowarn");

        int exitValue;
        ByteArrayOutputStream tmpErr = new ByteArrayOutputStream(OUTPUT_BUFFER_SIZE);

        try {
            Process p = Runtime.getRuntime().exec(toStringArray(fillArguments(args)));

            BufferedInputStream compilerErr = new BufferedInputStream(p.getErrorStream());

            RunnableManager runnableManager = null;
            try
            {
                runnableManager = (RunnableManager)m_serviceManager.lookup( RunnableManager.ROLE );
            }
            catch( final ServiceException se )
            {
                getLogger().error( "Cannot get RunnableManager", se );
                throw new IOException( "Cannot get RunnableManager" );
            }

            final CountDown done = new CountDown( 1 );
            StreamPumper errPumper = new StreamPumper(compilerErr, tmpErr, done);
            runnableManager.execute( errPumper );
            m_serviceManager.release( runnableManager );

            p.waitFor();
            exitValue = p.exitValue();

            done.acquire(); // Wait for StreadmPumper to finish
            compilerErr.close();

            p.destroy();

            tmpErr.close();
            this.errors = new ByteArrayInputStream(tmpErr.toByteArray());

        } catch (InterruptedException somethingHappened) {
            getLogger().debug("Jikes.compile():SomethingHappened", somethingHappened);
            return false;
        }

        // Jikes returns 0 even when there are some types of errors.
        // Check if any error output as well
        // Return should be OK when both exitValue and
        // tmpErr.size() are 0 ?!
        return ((exitValue == 0) && (tmpErr.size() == 0));
    }

    /**
     * Parse the compiler error stream to produce a list of
     * <code>CompilerError</code>s
     *
     * @param input The error stream
     * @return The list of compiler error messages
     * @exception IOException If an error occurs during message collection
     */
    protected List parseStream(BufferedReader input) throws IOException {
        List errors = null;
        String line = null;
        StringBuffer buffer = null;

        while (true) {
            // cleanup the buffer
            buffer = new StringBuffer(); // this is faster than clearing it

            // first line is not space-starting
            if (line == null) line = input.readLine();
            if (line == null) return errors;
            buffer.append(line);

            // all other space-starting lines are one error
            while (true) {
                line = input.readLine();
                // EOF
                if (line == null)
                    break;
                // Continuation of previous error starts with ' '
                if (line.length() > 0 && line.charAt(0) != ' ')
                    break;
                buffer.append('\n');
                buffer.append(line);
            }

            // if error is found create the vector
            if (errors == null) errors = new ArrayList();

            // add the error bean
            errors.add(parseError(buffer.toString()));
        }
    }

    /**
     * Parse an individual compiler error message
     *
     * @param error The error text
     * @return A mssaged <code>CompilerError</code>
     */
    private CompilerError parseError(String error) {
        StringTokenizer tokens = new StringTokenizer(error, ":");
        String file = tokens.nextToken();
        if (file.length() == 1) file = new StringBuffer(file).append(":").append(tokens.nextToken()).toString();
        StringBuffer message = new StringBuffer();
        String type = "";
        int startline = 0;
        int startcolumn = 0;
        int endline = 0;
        int endcolumn = 0;

        try {
            startline = Integer.parseInt(tokens.nextToken());
            startcolumn = Integer.parseInt(tokens.nextToken());
            endline = Integer.parseInt(tokens.nextToken());
            endcolumn = Integer.parseInt(tokens.nextToken());
        } catch (Exception e) {
            // FIXME: VG: This is not needed anymore?
            message.append("Please ensure that you have your JDK's rt.jar listed in your classpath. Jikes needs it to operate.");
            type="error";
            getLogger().error(message.toString(), e);
        }

        if ("".equals(message.toString())) {
            type = tokens.nextToken().trim().toLowerCase();
            message.append(tokens.nextToken("\n").substring(1).trim());

            while (tokens.hasMoreTokens())
                message.append("\n").append(tokens.nextToken());
        }

        return new CompilerError(file, type.equals("error"), startline, startcolumn, endline, endcolumn, message.toString());
    }

    public String toString() {
        return "IBM Jikes Compiler";
    }
}
