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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xml.sax.SAXException;

/**
 * <p>The {@link AbstractLogger} is a simple abstract {@link Logger} formatting
 * log entries and outputting them.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.2 $)
 */
public abstract class AbstractLogger extends Logger {

    /** <p>Output debug, info, warn, error and fatal messages.</p> */
    public static final int DEBUG = 0;
    
    /** <p>Output info, warn, error and fatal messages.</p> */
    public static final int INFO = 1;
    
    /** <p>Output warn, error and fatal messages.</p> */
    public static final int WARN = 2;
    
    /** <p>Output error and fatal messages.</p> */
    public static final int ERROR = 3;
    
    /** <p>Output only fatal error messages.</p> */
    public static final int FATAL = 4;

    /* ====================================================================== */

    /** <p>The logging level.</p> */
    private int level = DEBUG;

    /** <p>The name of this logger.</p> */
    private String name = null;
    
    /** <p>The timestamp formatter (if any).</p> */
    private SimpleDateFormat time = null;

    /** <p>Whether we should produce exception stack traces or not.</p> */
    private boolean trace = true;
    
    /* ====================================================================== */
        
    /**
     * <p>Create a new {@link AbstractLogger}.</p>
     *
     * @param name if <b>not null</b> the name will be included in the output.
     * @param level the minimum level of logging messages to output.
     * @param time if <b>true</b> timestamps will be included in the output.
     * @param trace if <b>true</b> exception stack traces will be produced.
     */
    public AbstractLogger(String name, int level, boolean time, boolean trace) {
        /* Set up name */
        this.name = name;

        /* Check level */
        if (level < DEBUG) this.level = DEBUG;
        else if (level > FATAL) this.level = FATAL;
        else this.level = level;

        /* Produce a timestamp? */
        if (time) this.time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        
        /* Trace exceptions? */
        this.trace = trace;
    }
    
    /* ====================================================================== */

    /**
     * <p>Log a debug message.</p>
     *
     * @param message the message.
     */
    public void debug(String message) {
        this.out(DEBUG, message, null);
    }
    
    /**
     * <p>Log a debug message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void debug(String message, Throwable throwable) {
        this.out(DEBUG, message, throwable);
    }
    
    /**
     * <p>Log a info message.</p>
     *
     * @param message the message.
     */
    public void info(String message) {
        this.out(INFO, message, null);
    }
    
    /**
     * <p>Log a info message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void info(String message, Throwable throwable) {
        this.out(INFO, message, throwable);
    }
   
    /**
     * <p>Log a warn message.</p>
     *
     * @param message the message.
     */
    public void warn(String message) {
        this.out(WARN, message, null);
    }
    
    /**
     * <p>Log a warn message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void warn(String message, Throwable throwable) {
        this.out(WARN, message, throwable);
    }
    
    /**
     * <p>Log an error message.</p>
     *
     * @param message the message.
     */
    public void error(String message) {
        this.out(ERROR, message, null);
    }
    
    /**
     * <p>Log a error message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void error(String message, Throwable throwable) {
        this.out(ERROR, message, throwable);
    }
    
    /**
     * <p>Log a fatal error message.</p>
     *
     * @param message the message.
     */
    public void fatal(String message) {
        this.out(FATAL, message, null);
    }
    
    /**
     * <p>Log a fatal error message.</p>
     *
     * @param message the message.
     * @param throwable the throwable.
     */
    public void fatal(String message, Throwable throwable) {
        this.out(FATAL, message, throwable);
    }
    
    /* ====================================================================== */

    /**
     * <p>Write a line to the output.</p>
     *
     * @param line the line to write.
     */
    public abstract void output(String line);
    
    /* ====================================================================== */

    /**
     * <p>Generate output from this logger.</p>
     *
     * @param level the level of the log entry.
     * @param message the message to log (if any).
     * @param throwable the {@link Throwable} to log (if any).
     */
    private void out(int level, String message, Throwable throwable) {
        if (level < this.level) return;

        /* Prepare the header for output in a new buffer */
        StringBuffer buffer = new StringBuffer(64);
        
        /* If we got a time formatter, append the time */
        if (this.time != null) buffer.append(this.time.format(new Date()));
        
        /* Dump out the log level */
        if (buffer.length() > 0) buffer.append(" ");
        switch (level) {
            case DEBUG: buffer.append("[DEBUG] "); break;
            case INFO:  buffer.append("[INFO ] "); break;
            case WARN:  buffer.append("[WARN ] "); break;
            case ERROR: buffer.append("[ERROR] "); break;
            case FATAL: buffer.append("[FATAL] "); break;
            default:    buffer.append("[?*?*?] "); break;
        }
        
        /* If we have a name, we dump it out */
        if (this.name != null) {
            buffer.append("(");
            buffer.append(this.name);
            buffer.append(") ");
        }
        
        /* If both message and throwable were null, whopsie */
        if ((message == null) && (throwable == null)) {
            this.output(buffer.append("null logging entry?").toString());
            return;
        }

        /* If we want to re-use the header for the throwable, lets save it*/
        String header = (throwable != null ? buffer.toString() : null);
        
        /* If we have a message, we want to output it */
        if (message != null) this.output(buffer.append(message).toString());

        /* If we have a throwable, we output it with the saved header */
        if (throwable != null) this.out(header, throwable, false);
    }

    /**
     * <p>Generate output from this logger for a {@link Throwable}.</p>
     *
     * @param header the header to use.
     * @param throwable the {@link Throwable} to log.
     * @param cause if the {@link Throwable} is the cause of another.
     */
    private void out(String header, Throwable throwable, boolean cause) {
        StringBuffer buffer = new StringBuffer(200);

        /* Check if this throwable has a cause */
        Throwable causedby = null;
        if (throwable instanceof SAXException) {
            causedby = ((SAXException)throwable).getException();
        } else causedby = throwable.getCause();
        
        /* Print the logging header and an explaination */
        buffer.append(header);
        buffer.append(cause ? "+ Caused by " : "Exception ");

        /* Print the Throwable class name */
        buffer.append(throwable.getClass().getName());
        buffer = this.out(buffer);

        /* Print the Throwable message */
        String message = throwable.getMessage();
        if (message != null) {
            /* Print the header for the message */
            buffer.append(header);
            
            /* Widgeting, many checks, one buffer append call */
            if (!cause) buffer.append("+--- ");
            else buffer.append(causedby == null ? "  +--- "  : "| +--- ");

            /* Print out the message */
            buffer = this.out(buffer.append(message));
        }

        /* Analyze every single stack trace element */
        StackTraceElement trace[] = throwable.getStackTrace();
        if (this.trace) for (int x = 0; x < trace.length; x++) {

            /* Per each stack trace element print the header */
            buffer.append(header);

            /* Widgeting, many checks, one buffer append call */
            if ((x + 1) == trace.length) {
                /* What widgets to print if this the last trace element */
                if (causedby == null)
                     buffer.append(cause ? "  + at " : "+ at ");
                else buffer.append(cause ? "| + at " : "| at ");
            } else {
                /* What widgets to print if this the last trace element */
                if (cause) 
                     buffer.append(causedby == null ? "  | at " : "| | at ");
                else buffer.append("| at ");
            }

            /* And finally print the trace */
            buffer = this.out(buffer.append(trace[x].toString()));
        }

        /* Recursively loop through the causes of the exception */
        if (causedby != null) {
            buffer.append(header);
            this.out(buffer.append("|"));
            this.out(header, causedby, true);
        } else this.out(buffer.append(header));
    }

    /**
     * <p>Output a StringBuffer and return a new one.</p>
     *
     * @param buffer the buffer to output.
     * @return a new buffer.
     */
    private StringBuffer out(StringBuffer buffer) {
        if (buffer != null) this.output(buffer.toString());
        return(new StringBuffer(200));
    }
}
