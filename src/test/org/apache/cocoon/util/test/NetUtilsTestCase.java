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
package org.apache.cocoon.util.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cocoon.util.NetUtils;

/**
 * Test Cases for the NetUtils class.
 * @see org.apache.cocoon.util.NetUtils
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @version CVS $Id: NetUtilsTestCase.java,v 1.4 2003/10/31 21:42:00 vgritsenko Exp $
 */
public class NetUtilsTestCase extends TestCase
{

    /**
     *Constructor for the IOUtilsTestCase object
     *
     * @param  name  Description of Parameter
     * @since
     */
    public NetUtilsTestCase(String name) {
        super(name);
    }


    /**
     *Description of the Method
     *
     * @param  args  Description of Parameter
     * @since
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(NetUtilsTestCase.class);
    }


    /**
     * A unit test for <code>NetUtils.getPath()</code>.
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetPath() throws Exception {
        Object[] test_values = {
                new String[]{"/", ""},
                new String[]{"/foo.bar", ""},
                new String[]{"foo/bar", "foo"},
                new String[]{"/foo/bar", "/foo"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            String expected = tests[1];

            String result = NetUtils.getPath(test);
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.getExtension()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testGetExtension() throws Exception {
        Object[] test_values = {
                new String[]{"/foo.bar", ".bar"},
                new String[]{"foo.bar#a", ".bar"},
                new String[]{"foo.bar?b=c", ".bar"},
                new String[]{"foo.bar#a?b=c", ".bar"},
                new String[]{"foo.bar", ".bar"},
                new String[]{"foo/bar", null},
                new String[]{"/x.html", ".html"},
                new String[]{"/foo.bar.org/x.y.z.html", ".html"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            String expected = tests[1];

            String result = NetUtils.getExtension(test);
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.absolutize()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testAbsolutize() throws Exception {

        Object[] test_values = {
            new String[]{"/base/path",  "foo.bar",  "/base/path/foo.bar"},
            new String[]{"/base/path/", "foo.bar",  "/base/path/foo.bar"},
            new String[]{"/base/path",  "/foo.bar", "/foo.bar"},
            
            new String[]{"/base/path", "",   "/base/path"},
            new String[]{"/base/path", null, "/base/path"},
            
            new String[]{"",   "foo.bar", "foo.bar"},
            new String[]{null, "foo.bar", "foo.bar"},
        };
        
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test_path = tests[0];
            String test_rel_resource = tests[1];
            String expected = tests[2];

            String result = NetUtils.absolutize(test_path, test_rel_resource);
            String message = "Test " +
                    " path " + "'" + test_path + "'" +
                    " relativeResource " + "'" + test_rel_resource;
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.testEncodePath()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testEncodePath() throws Exception {

        Object[] test_values = {
                new String[]{"abc def", "abc%20def"},
                new String[]{"foo/bar?n=v&N=V", "foo/bar%3Fn=v&N=V"}
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String original = tests[0];
            String expected = tests[1];

            String result = NetUtils.encodePath(original);
            String message = "Test " +
                    " original " + "'" + original + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.relativize()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testRelativize() throws Exception {

        Object[] test_values = {
                new String[]{"/xml.apache.org", "/xml.apache.org/foo.bar", "foo.bar"},
                new String[]{"/xml.apache.org", "/xml.apache.org/foo.bar", "foo.bar"},
                new String[]{"/xml.apache.org", "/xml.apache.org/foo.bar", "foo.bar"},
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test_path = tests[0];
            String test_abs_resource = tests[1];
            String expected = tests[2];

            String result = NetUtils.relativize(test_path, test_abs_resource);
            String message = "Test " +
                    " path " + "'" + test_path + "'" +
                    " absoluteResource " + "'" + test_abs_resource;
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.normalize()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testNormalize() throws Exception {
        Object[] test_values = {
                new String[]{"/foo/bar", "/foo/bar"},
                new String[]{"bar", "bar"},
                new String[]{"foo/../bar", "bar"},
                new String[]{"foo/./bar", "foo/bar"},
                new String[]{"foo/bar1/bar2/bar3/../../..", "foo"},
                };
        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            String expected = tests[1];

            String result = NetUtils.normalize(test);
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.deparameterize()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testDeparameterize() throws Exception {
        Map parameters = new HashMap();

        Object[] test_values = {
            new String[]{"/foo/bar", "/foo/bar"},
            new String[]{"bar?a=b&c=d", "bar"},
        };

        for (int i = 0; i < test_values.length; i++) {
            String tests[] = (String[]) test_values[i];
            String test = tests[0];
            String expected = tests[1];

            parameters.clear();
            String result = NetUtils.deparameterize(test, parameters);
            if (test.indexOf('?') > -1) {
                assertTrue(parameters.size() > 0);
            }
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }
    }


    /**
     * A unit test for <code>NetUtils.parameterize()</code>
     *
     * @exception  Exception  Description of Exception
     * @since
     */
    public void testParameterize() throws Exception {
        Map parameters1 = new HashMap();

        Object[] test_values = {
            new Object[]{"/foo/bar", parameters1, "/foo/bar"},
        };

        for (int i = 0; i < test_values.length; i++) {
            Object tests[] = (Object[]) test_values[i];
            String test = (String) tests[0];
            Map parameters = (Map) tests[1];
            String expected = (String) tests[2];

            String result = NetUtils.parameterize(test, parameters);
            String message = "Test " + "'" + test + "'";
            assertEquals(message, expected, result);
        }

        Map parameters2 = new HashMap();
        parameters2.put("a", "b");
        parameters2.put("c", "d");
        
        String test = "bar";
        String expected1 = "bar?a=b&c=d";
        String expected2 = "bar?c=d&a=b";
        
        String message = "Test " + "'" + test + "'";
                    
        String result = NetUtils.parameterize(test, parameters2);        

        if (expected1.equals(result)) {
          assertEquals(message, expected1, result);  
        } else {
          assertEquals(message, expected2, result);  
        }
    }
}
