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
package org.apache.cocoon.environment.commandline.test;
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.cocoon.environment.commandline.CommandlineContext;
import org.apache.log.Priority;

/**
 * A simple test case for CommandLineContext.
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: CommandLineContextTestCase.java,v 1.3 2003/03/16 18:03:56 vgritsenko Exp $
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

    private String commandlineContextDir;
    private CommandlineContext commandlineContext;


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
        commandlineContextDir = System.getProperty("java.io.tmpdir", "/tmp");
        commandlineContext = new CommandlineContext(commandlineContextDir);
        commandlineContext.enableLogging( new ConsoleLogger() );
    }


    /**
     *The teardown method for JUnit
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void tearDown() throws Exception { }


    /**
     * A unit test for <code>getResource()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetResource() throws Exception {

        Object[] test_values = {
                new String[]{"", commandlineContextDir},
                new String[]{"/", commandlineContextDir},
                new String[]{"foo", commandlineContextDir + File.separator + "foo"},
                new String[]{"foo/bar", commandlineContextDir + File.separator + "foo/bar"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            File expected_file = new File(tests[1]);
            URL expected = expected_file.toURL();

            URL result = commandlineContext.getResource(test);
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
            File expected_file = new File(commandlineContextDir, tests[1]);
            String expected = expected_file.getAbsolutePath();

            String result = commandlineContext.getRealPath(test);
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

            commandlineContext.setAttribute(name, expected);

            String result = (String) commandlineContext.getAttribute(name);
            assertEquals("Test " + "'" + "n" + "'", expected, result);

            commandlineContext.removeAttribute(name);
            result = (String) commandlineContext.getAttribute(name);
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

