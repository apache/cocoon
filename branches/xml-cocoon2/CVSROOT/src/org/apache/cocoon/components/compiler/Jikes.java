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

public class Jikes extends AbstractCompiler {
    
    public boolean compile() throws IOException {
        
        Vector args = new Vector(12);
        // command line name
        args.add("jikes");
        // indicate Emacs output mode must be used
        args.add("+D");

        Process p = Runtime.getRuntime().exec(toStringArray(fillArguments(args)));

        errors = p.getInputStream();
        
        try {
            p.waitFor();
            return (p.exitValue() == 0);
        } catch(InterruptedException somethingHappened) {
            return false;
        }
    }
    
    protected Vector parseStream(BufferedReader input) throws IOException {
        Vector errors = null;
        String line = null;
        StringBuffer buffer = new StringBuffer();

        while (true) {
            // cleanup the buffer
            buffer.delete(0, buffer.length());

            // first line is not space-starting            
            if (line == null) line = input.readLine();
            if (line == null) return errors;
            buffer.append(line);

            // all other space-starting lines are one error
            while (true) {            
                line = input.readLine();
                if ((line == null) || (line.charAt(0) != ' ')) break;
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
        int startline = Integer.parseInt(tokens.nextToken());
        int startcolumn = Integer.parseInt(tokens.nextToken());
        int endline = Integer.parseInt(tokens.nextToken());
        int endcolumn = Integer.parseInt(tokens.nextToken());
        String type = tokens.nextToken().trim().toLowerCase();
        String message = tokens.nextToken().trim();
        
        return new Error(file, type.equals("error"), startline, startcolumn, endline, endcolumn, message);
    }
    
    public String getStatus() {
        return "IBM Jikes";
    }
}