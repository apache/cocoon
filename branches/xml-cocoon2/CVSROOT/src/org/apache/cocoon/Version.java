/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.net.*;

/*
 * This utility class is used thruout the package to obtain naming and 
 * versioning information. It is also used by automatic build scripts 
 * to get a versioned file name for the distribution package 
 * (a sort of command line driven auto-introspection).
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 */
 
public class Version implements org.apache.arch.Version {
    
    public static final String version = "2.0-predev";
    public static final String name = "Cocoon";
    
    public static String getName() {
        return name;
    }

    public static String getVersion() {
        return version;
    }
    
    public static final void main(String[] ignoreArgs) {
        System.out(getName() + "-" + getVersion());
    }
}
