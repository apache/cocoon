/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.compiler;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:51 $
 * @since 2.0
 */

public class Javac extends AbstractCompiler {
    
    public boolean compile() throws IOException {
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        in.connect(out);
        errors = in;
    	sun.tools.javac.Main compiler = new sun.tools.javac.Main(out, "javac");
        return compiler.compile(toStringArray(fillArguments(new Vector(10))));
    }
    
    protected Vector parseStream(BufferedReader input) throws IOException {
        Vector errors = null;
        String line = null;
        StringBuffer buffer = new StringBuffer();

        while (true) {
            // cleanup the buffer
            buffer.delete(0, buffer.length());

            // each error has 3 lines
            for (int i = 0; i < 3 ; i++) {
                if ((line = input.readLine()) == null) return errors;
                buffer.append(line);
            }

            // if error is found create the vector
            if (errors == null) errors = new Vector(10);
            
            // add the error bean
            errors.addElement(parseError(buffer.toString()));
        }
    }
    
    private Error parseError(String error) {
        StringTokenizer tokens = new StringTokenizer(error, ":");
        String file = tokens.nextToken();
        int line = Integer.parseInt(tokens.nextToken());
        
        tokens = new StringTokenizer(tokens.nextToken().trim(), "\n");
        String message = tokens.nextToken();
        String context = tokens.nextToken();
        String pointer = tokens.nextToken();
        int startcolumn = pointer.indexOf("^");
        int endcolumn = context.indexOf(" ", startcolumn);
        if (endcolumn == -1) endcolumn = context.length();
        
        String type = "error";
        
        return new Error(srcDir + File.separator + file, type.equals("error"), line, startcolumn, line, endcolumn, message);
    }
    
    public String getStatus() {
        return "Sun Classic JavaC";
    }
}