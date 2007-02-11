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
 * @version CVS $Id: ZValidationTest.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
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
