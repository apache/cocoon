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
package org.apache.cocoon.components.validation;

import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaFactory;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.Violation;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

/**
 * Test class for the Validation API.
 *
 * <p>Uses file src/test/org/apache/cocoon/components/validation/test/zxmlform-sch-report-test.xml.
 *
 * @version CVS $Id: ZValidationTest.java,v 1.1 2003/04/26 12:09:44 stephan Exp $
 */
public class ZValidationTest extends TestCase {

    private static final int count = 100;

    public ZValidationTest(String name) {
        super(name);
    }

    private void testSchema(String schema, String phase, List violations) {
        // use custom schema
        InputStream in = getClass().getResourceAsStream(schema);
        if (in==null) {
            fail("Error: schema file "+schema+" not found");
        }

        try {
            InputSource is = new InputSource(in);
            SchemaFactory schf = SchemaFactory.lookup(SchemaFactory.NAMESPACE_SCHEMATRON);
            Schema sch = schf.compileSchema(is);
            Validator validator = sch.newValidator();

            // set preprocessor parameters
            if (phase!=null) {
                validator.setProperty(Validator.PROPERTY_PHASE, phase);
            }

            ZTestBean tbean = new ZTestBean();

            // measure validation speed
            long time = System.currentTimeMillis();
            List vs = null;

            for (int i = 0; i<count; i++) {
                // perform validation
                vs = validator.validate(tbean);
            }
            time = System.currentTimeMillis()-time;
            System.out.println("Validation performed "+count+
                               " times for a total of "+time+" ms");
            System.out.println("Avarage validation time is "+(time/count)+
                               " ms ");

            if (vs==null) {
                vs = new ArrayList();
            }

            // everything ok?
            assertEquals("Violations count does not match",
                         violations.size(), vs.size());
            for (Iterator i = violations.iterator(); i.hasNext(); ) {
                Violation v = (Violation) i.next();
                boolean gotit = false;

                for (Iterator j = vs.iterator(); j.hasNext(); ) {
                    Violation w = (Violation) j.next();

                    if (v.getPath().equals(w.getPath()) &&
                        v.getMessage().equals(w.getMessage())) {
                        gotit = true;
                        break;
                    }
                }
                assertTrue("Expected violation "+v.getPath()+" '"+
                           v.getMessage()+"' not found", gotit);
            }
            for (Iterator i = vs.iterator(); i.hasNext(); ) {
                Violation v = (Violation) i.next();
                boolean gotit = false;

                for (Iterator j = violations.iterator(); j.hasNext(); ) {
                    Violation w = (Violation) j.next();

                    if (v.getPath().equals(w.getPath()) &&
                        v.getMessage().equals(w.getMessage())) {
                        gotit = true;
                        break;
                    }
                }
                assertTrue("Unexpected violation "+v.getPath()+" '"+
                           v.getMessage()+"' found", gotit);
            }
        } catch (Exception e) {
            fail("Got an exception "+e);
        }
    }

    public void testSchema() {
        Violation violation;
        List violations = new ArrayList();

        violation = new Violation();
        violation.setPath("/scope");
        violation.setMessage("Scope should be request or session.");
        violations.add(violation);

        violation = new Violation();
        violation.setPath("/name");
        violation.setMessage("Animal name should be at least 4 characters.");
        violations.add(violation);

        violation = new Violation();
        violation.setPath("/count");
        violation.setMessage("The counter should be > 0.");
        violations.add(violation);

        testSchema("zxmlform-sch-report-test.xml", null, violations);
    }

    public void testSchema_PhaseNew() {
        Violation violation;
        List violations = new ArrayList();

        violation = new Violation();
        violation.setPath("/scope");
        violation.setMessage("Scope should be request or session.");
        violations.add(violation);

        violation = new Violation();
        violation.setPath("/name");
        violation.setMessage("Animal name should be at least 4 characters.");
        violations.add(violation);

        testSchema("zxmlform-sch-report-test.xml", "New", violations);
    }
}
