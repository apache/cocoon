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
package org.apache.cocoon.environment.commandline.test;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.environment.commandline.CommandLineContext;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.io.File;
import java.net.URL;

/**
 * A simple test case for CommandLineContext.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id$
 */
public final class CommandLineContextTestCase extends TestCase {

    private String commandLineContextDir;
    private CommandLineContext commandLineContext;


    /**
     * Constructor for the CommandLineContextTestCase object
     */
    public CommandLineContextTestCase() {
        this("CommandLineContextTestCase");
    }

    /**
     * Constructor for the CommandLineContextTestCase object
     */
    public CommandLineContextTestCase(String name) {
        super(name);
    }

    /**
     * The main program for the CommandLineContextTestCase class
     *
     * @param  args           The command line arguments
     */
    public static void main(final String[] args) throws Exception {
        final String[] testCaseName = {CommandLineContextTestCase.class.getName()};
        TestRunner.main(testCaseName);
    }


    /**
     * The JUnit setup method
     */
    public void setUp() throws Exception {
        super.setUp();
        commandLineContextDir = System.getProperty("java.io.tmpdir", "/tmp");
        new File(commandLineContextDir, "foo" + File.separator + "bar").mkdirs();

        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_DEBUG);
        Logger logger = new ConsoleLogger(Integer.parseInt(level));

        commandLineContext = new CommandLineContext(commandLineContextDir);
        commandLineContext.enableLogging(logger);
    }

    /**
     * The teardown method for JUnit
     */
    public void tearDown() throws Exception {
        super.tearDown();
        new File(commandLineContextDir, "foo" + File.separator + "bar").delete();
        new File(commandLineContextDir, "foo").delete();
    }

    /**
     * A unit test for <code>getResource()</code>
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
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }

    /**
     * A unit test for <code>getRealPath()</code>
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

            commandLineContext.setAttribute(name, expected);

            String result = (String) commandLineContext.getAttribute(name);
            assertEquals("Test " + "'" + "n" + "'", expected, result);

            commandLineContext.removeAttribute(name);
            result = (String) commandLineContext.getAttribute(name);
            assertEquals("Test " + "'" + "<null>" + "'", null, result);
        }
    }
}
