package org.apache.cocoon.compiler;

import java.io.*;
import java.util.*;
import org.apache.cocoon.framework.*;

/**
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
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