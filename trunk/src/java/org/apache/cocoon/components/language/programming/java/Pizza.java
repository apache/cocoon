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
 * @version CVS $Id: Pizza.java,v 1.3 2004/03/08 13:58:32 cziegeler Exp $
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
