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
package org.apache.cocoon.kernel.startup;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xml.sax.SAXException;

/**
 * <p>The {@link ConsoleLogger} is a simple {@link Logger} implementation
 * writing to {@link System#err} or a specified {@link PrintStream}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public class ConsoleLogger extends AbstractLogger {

    /** The {@link PrintStream} used for output */
    private PrintStream out = null;
    
    /* ====================================================================== */

    /**
     * <p>Create a new {@link ConsoleLogger} logging everything on
     * {@link System#err}.</p>
     */
    public ConsoleLogger() {
        this(null, DEBUG, true, true, null);
    }

    /**
     * <p>Create a new {@link ConsoleLogger} logging everything on a specified
     * {@link OutputStream}.</p>
     */
    public ConsoleLogger(OutputStream out) {
        this(null, DEBUG, true, true, out);
    }

    /**
     * <p>Create a new {@link ConsoleLogger} logging everything on
     * {@link System#err}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     */
    public ConsoleLogger(String name) {
        this(name, DEBUG, true, true, null);
    }
    
    /**
     * <p>Create a new {@link ConsoleLogger} logging everything on a specified
     * {@link OutputStream}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     */
    public ConsoleLogger(String name, OutputStream out) {
        this(name, DEBUG, true, true, out);
    }
    
    /**
     * <p>Create a new {@link ConsoleLogger} specifying its logging
     * level logging on {@link System#err}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     * @param level the level of output.
     */
    public ConsoleLogger(String name, int level) {
        this(name, level, true, true, null);
    }

    /**
     * <p>Create a new {@link ConsoleLogger} specifying its logging
     * level logging on a specified {@link OutputStream}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     * @param level the level of output.
     */
    public ConsoleLogger(String name, int level, OutputStream out) {
        this(name, level, true, true, out);
    }

    /**
     * <p>Create a new {@link ConsoleLogger} specifying wether it should
     * output a time stamp and the logger name on {@link System#err}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     * @param level the level of output.
     * @param time if <b>true</b> timestamps will be included in the output.
     * @param trace if <b>true</b> exception stack traces will be produced.
     */
    public ConsoleLogger(String name, int level, boolean time, boolean trace) {
        this(name, level, time, trace, null);
    }

    /**
     * <p>Create a new {@link ConsoleLogger} specifying wether it should
     * output a time stamp and the logger name on a specified
     * {@link PrintStream}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     * @param level the level of output.
     * @param time if <b>true</b> timestamps will be included in the output.
     * @param trace if <b>true</b> exception stack traces will be produced.
     * @param output the {@link PrintStream} used for output.
     */
    public ConsoleLogger(String name, int level, boolean time, boolean trace,
                         OutputStream output) {
        super(name, level, time, trace);

        if (output == null) {
            this.out = System.err;
        } else if (output instanceof PrintStream) {
            this.out = (PrintStream)output;
        } else {
            this.out = new PrintStream(output);
        }
    }
    
    /* ====================================================================== */

    /**
     * <p>Write a line to the output.</p>
     *
     * @param line the line to write.
     */
    public void output(String line) {
        this.out.println(line);
    }
}