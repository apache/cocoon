/*>$File$ -- $Id: Jikes.java,v 1.2 1999-11-09 02:20:56 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.compiler;

import java.io.*;
import java.util.*;

/**
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 1999-11-09 02:20:56 $
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