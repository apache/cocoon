/*-- $Id: Javac.java,v 1.3 1999-11-09 02:29:35 dirkx Exp $ -- 

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
 * @version $Revision: 1.3 $ $Date: 1999-11-09 02:29:35 $
 */

public class Javac extends AbstractCompiler {
    
    public boolean compile() throws IOException {
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        in.connect(out);
        errors = in;
    	sun.tools.javac.Main compiler = new sun.tools.javac.Main(out, "Cocoon Javac");
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
        return "Sun JavaC";
    }
}