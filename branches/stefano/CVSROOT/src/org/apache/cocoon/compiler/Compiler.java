package org.apache.cocoon.compiler;

import java.io.*;
import java.util.*;

/**
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:15 $
 */

public interface Compiler {

    void setFile(String file);    
    void setSource(String srcDir);    
    void setDestination(String destDir);    
    void setClasspath(String classpath);    
    void setEncoding(String encoding);    
    boolean compile() throws IOException;
    Vector getErrors() throws IOException;
}