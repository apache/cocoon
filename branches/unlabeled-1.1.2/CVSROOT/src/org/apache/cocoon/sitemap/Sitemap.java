/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-11 13:14:50 $
 */
public class Sitemap {
    /** This sitemap Partition table */
    protected Hashtable partitions=null;
    /** This sitemap default Partition */
    protected Partition defaultPartition=null;

    public Sitemap() {
        super();
        this.partitions=new Hashtable();
    }
    
    public boolean handle(Request req, Response res, OutputStream out)
    throws IOException, SAXException {
        // Try to handle the request to the default partition.
        if(this.defaultPartition.handle(req,res,out)) return(true);
        // Iterate thru all partitions handling the request
        Enumeration enum=this.partitions.elements();
        while(enum.hasMoreElements()) {
            if (((Partition)enum.nextElement()).handle(req,res,out)) {
                return(true);
            }
        }
        return(false);
    }
}
