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
package org.apache.cocoon.components.language.programming.java;

import net.sf.pizzacompiler.compiler.ClassReader;
import net.sf.pizzacompiler.compiler.FileCompilerOutput;
import net.sf.pizzacompiler.compiler.FileSourceReader;
import net.sf.pizzacompiler.compiler.Main;
import org.apache.cocoon.util.ClassUtils;
import org.apache.log.Hierarchy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This class wraps the Pizza Java Compiler.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: Pizza.java,v 1.2 2003/11/15 04:21:29 joerg Exp $
 */
public class Pizza extends Javac {

    public final static String PIZZA_CLASS = "net.sf.pizzacompiler.compiler.Main";

    public Pizza() {
        super(true);

        try {
            ClassUtils.loadClass(PIZZA_CLASS);
        } catch (ClassNotFoundException e) {
            Hierarchy.getDefaultHierarchy().getLoggerFor("cocoon").error("No Pizza Java compiler found in your classpath. Make sure you added 'pizza.jar'", e);
            throw new RuntimeException("No Pizza Java compiler found in your classpath. Make sure you added 'pizza.jar'");
        }

        net.sf.pizzacompiler.compiler.Main.init();
    }

    /**
     * Compile a source file yielding a loadable class file.
     *
     * @exception IOException
     */
    public boolean compile() throws IOException {

        ByteArrayOutputStream err = new ByteArrayOutputStream();

        Main.init();
        Main.setClassReader(new ClassReader(this.classpath, null));
        Main.argument("-java");
        Main.argument("-O");
        Main.argument("-nowarn");
        Main.compile(new String[]{file},
                new FileSourceReader(),
                new FileCompilerOutput(new File(destDir)),
                new PrintStream(err));

        this.errors = new ByteArrayInputStream(err.toByteArray());
        return err.size() == 0;
    }

    public String toString() {
        return "Pizza Java Compiler";
    }
}
