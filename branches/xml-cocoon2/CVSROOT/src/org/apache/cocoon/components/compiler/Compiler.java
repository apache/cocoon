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
import org.apache.arch.*;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:50 $
 * @since 2.0
 */

public interface Compiler extends Component {

    void setFile(String file);
    
    void setSource(String srcDir);
    
    void setDestination(String destDir);
    
    void setClasspath(String classpath);
    
    void setEncoding(String encoding);
    
    boolean compile() throws IOException;
    
    Vector getErrors() throws IOException;
}