/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.io.File;
import java.io.IOException;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:34 $
 */
public class Main {
    public static void main(String argv[]) {
        // Check wether we have the right number of parameters
        if (argv.length<1) {
            System.out.println("Usage: java org.apache.cocoon.Main [config]");
            System.exit(1);
        }
        Configurations conf=new Configurations();
        conf.setParameter("configurationFile",argv[0]);
        Cocoon cocoon=new Cocoon();
        try {
            cocoon.configure(conf);
        } catch (ConfigurationException e) {
            System.out.println(e.getSource()+" reported:");
            System.out.println("  "+e.getMessage());

            Exception e2=e.getException();
            System.out.println();
            if (e2!=null) {
                System.out.println("The nested exception is: "+e2.getClass());
                System.out.println("-> "+e2.getMessage());
            }
        }        
    }
}
