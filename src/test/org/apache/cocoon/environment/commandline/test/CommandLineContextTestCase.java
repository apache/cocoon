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
package org.apache.cocoon.environment.commandline.test;
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.log.Priority;

/**
 * A simple test case for CommandLineContext.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: CommandLineContextTestCase.java,v 1.6 2004/03/08 14:04:20 cziegeler Exp $
 */
public final class CommandLineContextTestCase
         extends TestCase
{
    ///Format of default formatter
    /**
     *Description of the Field
     *
     * @since
     */
    protected final static String FORMAT =
            "%7.7{priority} %{time}   [%8.8{category}] (%{context}): %{message}\\n%{throwable}";
    /**
     *Description of the Field
     *
     * @since
     */
    protected Priority m_logPriority = Priority.INFO;

    private String commandLineContextDir;
    private CommandLineContext commandLineContext;


    /**
     *Constructor for the CommandLineContextTestCase object
     *
     * @since
     */
    public CommandLineContextTestCase() {
        this("CommandLineContextTestCase");
    }


    /**
     *Constructor for the CommandLineContextTestCase object
     *
     * @param  name  Description of Parameter
     * @since
     */
    public CommandLineContextTestCase(String name) {
        super(name);

    }


    /**
     *The main program for the CommandLineContextTestCase class
     *
     * @param  args           The command line arguments
     * @exception  Exception  Description of Exception
     * @since
     */
    public static void main(final String[] args) throws Exception {
        final String[] testCaseName = {CommandLineContextTestCase.class.getName()};
        TestRunner.main(testCaseName);
    }


    /**
     *The JUnit setup method
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void setUp() throws Exception {
        commandLineContextDir = System.getProperty("java.io.tmpdir", "/tmp");
        new File(commandLineContextDir, "foo"+File.separator+"bar").mkdirs();
        commandLineContext = new CommandLineContext(commandLineContextDir);
        commandLineContext.enableLogging( new ConsoleLogger() );
    }


    /**
     *The teardown method for JUnit
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void tearDown() throws Exception {
        new File(commandLineContextDir, "foo"+File.separator+"bar").delete();
        new File(commandLineContextDir, "foo").delete();
    }


    /**
     * A unit test for <code>getResource()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetResource() throws Exception {

        Object[] test_values = {
                new String[]{"", commandLineContextDir},
                new String[]{"/", commandLineContextDir},
                new String[]{"foo", commandLineContextDir + File.separator + "foo"},
                new String[]{"foo/bar", commandLineContextDir + File.separator + "foo/bar"},
                new String[]{"foo/bar/nonexistent", null}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            URL result = commandLineContext.getResource(test);
            URL expected = null;
            if (result != null) {
                expected = new File(tests[1]).toURL();
            }
            String message = "Test " +
                    "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>getRealPath()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetRealPath() throws Exception {

        Object[] test_values = {
                new String[]{"", ""},
                new String[]{"/", "/"},
                new String[]{"foo", "foo"},
                new String[]{"foo/bar", "foo/bar"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            File expected_file = new File(commandLineContextDir, tests[1]);
            String expected = expected_file.getAbsolutePath();

            String result = commandLineContext.getRealPath(test);
            String message = "Test " +
                    "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>getAttribute</code>,
     * <code>setAttribute</code>, and <code>removeAttribute()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testAttributes() throws Exception {
        Object[] test_values = {
                new String[]{"a", "b"},
                new String[]{"foo", "foo"},
                new String[]{"foo/bar", "foo/bar"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String name = tests[0];
            String expected = tests[1];

            /* NEVER USED!
            String message = "Test " +
                    "'" + name + "'" + ", " + "'" + expected + "'";
            */

            commandLineContext.setAttribute(name, expected);

            String result = (String) commandLineContext.getAttribute(name);
            assertEquals("Test " + "'" + "n" + "'", expected, result);

            commandLineContext.removeAttribute(name);
            result = (String) commandLineContext.getAttribute(name);
            assertEquals("Test " + "'" + "<null>" + "'", null, result);
        }
    }


    /**
     * Setup a <code>Logger</code>.
     * <p>
     *   Setup a logger needed by AbstractLogEnabled objects.
     * </p>
     *
     * @return    Logger for logging
     * @since
    private final Logger setupLogger() {
        //FIXME(GP): This method should setup a LogConfigurator and LogManager
        //           according to the configuration spec. not yet completed/implemented
        //           It will return a default logger for now.
        final org.apache.log.Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("Test");
        logger.setPriority(m_logPriority);

        final PatternFormatter formatter = new PatternFormatter(FORMAT);
        final StreamTarget target = new StreamTarget(System.out, formatter);
        logger.setLogTargets(new LogTarget[]{target});

        return logger;
    }
     */
}

