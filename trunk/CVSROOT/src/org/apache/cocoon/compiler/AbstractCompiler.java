/*>$File$ -- $Id: AbstractCompiler.java,v 1.2 1999-11-09 02:20:53 dirkx Exp $ -- 

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
import org.apache.cocoon.framework.*;

/**
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 1999-11-09 02:20:53 $
 */

public abstract class AbstractCompiler implements org.apache.cocoon.compiler.Compiler, Status {

    protected String file;
    protected String srcDir;
    protected String destDir;
    protected String classpath;
    protected String encoding = null;
    protected InputStream errors;

    public void setFile(String file) {
        this.file = file;
    }
    
    public void setSource(String srcDir) {
        this.srcDir = srcDir;
    }
    
    public void setDestination(String destDir) {
        this.destDir = destDir;
    }
    
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public Vector getErrors() throws IOException {
        return parseStream(new BufferedReader(new InputStreamReader(errors)));
    }
    
    protected abstract Vector parseStream(BufferedReader errors) throws IOException;
    
    protected Vector fillArguments(Vector arguments) {
        // destination directory
    	arguments.addElement("-d");
    	arguments.addElement(destDir);
    	
    	// classpath
    	arguments.addElement("-classpath");
    	if (System.getProperty("java.version").startsWith("1.1")) {
    	    arguments.addElement(classpath + File.pathSeparator + srcDir);
    	} else {
    	    arguments.addElement(classpath);
    	    arguments.addElement("-sourcepath");
    	    arguments.addElement(srcDir);
    	}

        // add optimization (for what is worth)
	    arguments.addElement("-O");
	    
	    // add encoding if set
	    if (encoding != null) {
	        arguments.addElement("-encoding");
	        arguments.addElement(encoding);
	    }
	    
	    return arguments;
    }

    protected String[] toStringArray(Vector arguments) {    
        int i;
	    String[] args = new String[arguments.size() + 1];
	
	    for (i = 0; i < arguments.size(); i++) {
    	    args[i] = (String) arguments.elementAt(i);
    	}

        args[i] = file;
            	
    	return args;
	}
}