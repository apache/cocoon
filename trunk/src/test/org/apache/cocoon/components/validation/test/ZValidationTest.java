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
package org.apache.cocoon.components.validation.test;

import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaFactory;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.Violation;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Test class for the Validation API 
 *
 * Takes 2 command line arguments.
 *
 * First is the location of a Schematron Schema file
 * Second is the validation phase to use
 *
 */
public class ZValidationTest {

	/**
	 * Method main
	 */
	public static void main(String args[]) throws Exception {
		System.out.println("\n=== Java based Schematron validation ===");

		if (args.length < 1) {
			System.err.println(
				"Usage: java Schematron <schema.xml> " + "[phase] ");
			return;
		}

		// use custom schema
		File file = new File(args[0]);
		if (!file.exists())
			throw new Exception("Error: schema file not found !");
		InputStream istrm = new FileInputStream(file);
		InputSource is = new InputSource(istrm);
		SchemaFactory schf =
			SchemaFactory.lookup(SchemaFactory.NAMESPACE_SCHEMATRON);
		Schema sch = schf.compileSchema(is);
		Validator validator = sch.newValidator();

		// set preprocessor parameters 
		if (args.length > 1)
			validator.setProperty("phase", new String(args[1]));

		ZTestBean tbean = new ZTestBean();

		// measure validation speed
		long time = System.currentTimeMillis();
		int i = 0;
		List violations = null;
		for (; i < 100; i++) {
			// perform validation
			violations = validator.validate(tbean);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("\nValidation performance:");
		System.out.println(
			" validate() executed "
				+ i
				+ " times for a total of "
				+ time
				+ " ms");
		System.out.println("Avarage validation time: " + (time / i) + " ms ");

		// everything ok?
		if (violations == null) {
			System.out.println("\nValidation ok, no messages generated");
		} else {
			System.out.println("Validation encountered errors. Messages :");
			Iterator viter = violations.iterator();
			while (viter.hasNext()) {
				Violation v = (Violation) viter.next();
				System.out.println(
					"Validity violation path: "
						+ v.getPath()
						+ ", message: "
						+ v.getMessage());
			}
		}

		System.out.println("\n=== Schematron validation done ===");
	}

}
