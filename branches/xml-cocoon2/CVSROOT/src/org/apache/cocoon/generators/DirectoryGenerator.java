/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generators;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-03-19 01:08:47 $
 */
public class DirectoryGenerator extends ComposerGenerator {

    /** The URI of the namespace of this generator. */
    private String URI="http://xml.apache.org/cocoon/2.0/DirectoryGenerator";

    /**
     * Generate XML data.
     */
    public void generate()
    throws SAXException, IOException {
        EntityResolver r=(EntityResolver)super.manager.getComponent("cocoon");
        InputSource i=r.resolveEntity(null,super.source);
        URL u=new URL(i.getSystemId());
        if (!u.getProtocol().equals("file"))
            throw new IOException("Cannot read directory from "+u.toString());

        File d=new File(u.getFile()).getCanonicalFile();
        if (!d.isDirectory())
            throw new IOException("Cannot find directory \""+d.getPath()+"\"");

        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping("",URI);
        AttributesImpl attr=new AttributesImpl();

        this.attribute(attr,"name",d.getName()+"/");
        this.start("directory",attr);
        this.data("\n");

        String c[]=d.list();
        for (int x=0; x<c.length; x++) {
            File f=new File(d,c[x]);
            this.data("  ");
            if (f.isDirectory()) {
                this.attribute(attr,"name",f.getName()+"/");
                this.start("directory",attr);
                this.data(f.getName());
                this.end("directory");
            } else {
                this.attribute(attr,"name",f.getName());
                this.start("file",attr);
                this.data(f.getName());
                this.end("file");
            }
            this.data("\n");
        }

        this.end("directory");

        // Finish
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("",name,name,"CDATA",value);
    }

    private void start(String name, AttributesImpl attr)
    throws SAXException {
        super.contentHandler.startElement(URI,name,name,attr);
        attr.clear();
    }

    private void end(String name)
    throws SAXException {
        super.contentHandler.endElement(URI,name,name);
    }

    private void data(String data)
    throws SAXException {
        super.contentHandler.characters(data.toCharArray(),0,data.length());
    }
}
