/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.programming.java;

import java.io.*;
import java.util.*;
import org.apache.cocoon.components.language.programming.*;

/**
 * This class wraps IBM's <i>Jikes</i> Java compiler
 * NOTE: inspired by the Apache Jasper implementation.
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.10 $ $Date: 2001-04-23 17:52:30 $
 * @since 2.0
 */

public class Jikes extends AbstractJavaCompiler {

    static final int OUTPUT_BUFFER_SIZE = 1024;
    static final int BUFFER_SIZE = 512;

    private class StreamPumper extends Thread {

        private BufferedInputStream stream;
        private boolean endOfStream = false;
        private boolean stopSignal  = false;
        private int SLEEP_TIME = 5;
        private OutputStream out;

        public StreamPumper(BufferedInputStream is, OutputStream out) {
            this.stream = is;
            this.out = out;
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
                    sleep(SLEEP_TIME);
                }
            } catch (Exception e) {
               getLogger().warn("Jikes.run()", e);
            }
        }
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

            StreamPumper errPumper = new StreamPumper(compilerErr, tmpErr);

            errPumper.start();

            p.waitFor();
            exitValue = p.exitValue();

            // Wait until the complete error stream has been read
            errPumper.join();
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
     * @param errors The error stream
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
                if ((line == null) || (line.charAt(0) != ' ')) break;
                buffer.append(line);
                buffer.append('\n');
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
        int startline = Integer.parseInt(tokens.nextToken());
        int startcolumn = Integer.parseInt(tokens.nextToken());
        int endline = Integer.parseInt(tokens.nextToken());
        int endcolumn = Integer.parseInt(tokens.nextToken());
        String type = tokens.nextToken().trim().toLowerCase();
        String message = tokens.nextToken().trim();

        return new CompilerError(file, type.equals("error"), startline, startcolumn, endline, endcolumn, message);
    }

    public String toString() {
        return "IBM Jikes Compiler";
    }
}
