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
import org.apache.cocoon.Job;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:42 $
 */
public class Partition {
    /** This Partition array of Processor. */
    protected Processor processors[]=null;

    /** This Partition name */
    private String name=null;
    /** The Sitemap containing this Partition */
    private Sitemap sitemap=null;
    /** Deny empty construction */
    private Partition() {}

    /**
     * Create a new Processor instance.
     */
    public Partition(Sitemap smap, String name) {
        super();
        this.sitemap=smap;
    }

    public boolean handle(Job job, OutputStream out)
    throws IOException, SAXException {
        for(int x=0; x<processors.length; x++)
            if (processors[x].handle(job,out)) return(true);
        return(false);
    }
}
