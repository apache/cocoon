/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.generation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.avalon.utils.Parameters;

/**
 * Generates an XML directory listing.
 * <p>
 * The root node of the generated document will normally be a
 * <code>directory</code> node, and a directory node can contain zero
 * or more <code>file</code> or directory nodes. A file node has no
 * children. Each node will contain the following attributes:
 * <blockquote>
 *   <dl>
 *   <dt> name
 *   <dd> the name of the file or directory
 *   <dt> lastModified
 *   <dd> the time the file was last modified, measured as the number of
 *   milliseconds since the epoch (as in java.io.File.lastModified)
 *   <dt> date (optional)
 *   <dd> the time the file was last modified in human-readable form
 *   </dl>
 * </blockquote>
 * <p>
 * <b>Configuration options:</b>
 * <dl>
 * <dt> <i>depth</i> (optional)
 * <dd> Sets how deep DirectoryGenerator should delve into the
 * directory structure. If set to 1 (the default), only the starting
 * directory's immediate contents will be returned.
 * <dt> <i>dateFormat</i> (optional)
 * <dd> Sets the format for the date attribute of each node, as
 * described in java.text.SimpleDateFormat. If unset, the default
 * format for the current locale will be used.
 * </dl>
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-08-21 17:37:47 $ */
 
public class DirectoryGenerator extends ComposerGenerator {

    /** The URI of the namespace of this generator. */
    protected static final String URI =
    "http://apache.org/cocoon/2.0/directory";

    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "dir";

    /* Node and attribute names */
    protected static final String DIR_NODE_NAME         = "directory";
    protected static final String FILE_NODE_NAME        = "file";

    protected static final String FILENAME_ATTR_NAME    = "name";
    protected static final String LASTMOD_ATTR_NAME     = "lastModified";
    protected static final String DATE_ATTR_NAME        = "date";

    /*
     * Variables set per-request
     * 
     * FIXME: SimpleDateFormat is not supported by all locales!
     */
    protected int depth;
    protected AttributesImpl attributes = new AttributesImpl();
    protected SimpleDateFormat dateFormatter;
    protected EntityResolver resolver;

    /**
     * Set the request parameters. Must be called before the generate
     * method.
     *
     * @param   resolver
     *      the EntityResolver object
     * @param   objectModel
     *      a <code>Dictionary</code> containing model object
     * @param   src
     *      the URI for this request (?)
     * @param   par
     *      configuration parameters
     */
    public void setup(EntityResolver resolver, Dictionary objectModel, String src, Parameters par) {
        super.setup(resolver, objectModel, src, par);
    
        String dateFormatString = par.getParameter("dateFormat", null);
    
        if (dateFormatString != null) {
            this.dateFormatter = new SimpleDateFormat(dateFormatString);
        } else {
            this.dateFormatter = new SimpleDateFormat();
        }
    
        this.depth = par.getParameterAsInteger("depth", 1);
    
        /* Create a reusable attributes for creating nodes */
        AttributesImpl attributes = new AttributesImpl();
    }

    /**
     * Generate XML data.
     * 
     * @throws  SAXException
     *      if an error occurs while outputting the document
     * @throws  IOException
     *      if the requsted URI isn't a directory on the local
     *      filesystem
     */
    public void generate()
    throws SAXException, IOException {

        InputSource input;
        URL url;
        File path;
    
        input = resolver.resolveEntity(null,super.source);
            url = new URL(input.getSystemId());
            path = new File(url.getFile());
    
            if (!path.isDirectory()) {
                throw new IOException("Cannot read directory from "
                      + url.toString() + "\"");
        }

        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX,URI);
        addPath(path, depth);
        this.contentHandler.endPrefixMapping(PREFIX);
        this.contentHandler.endDocument();

    }

    /**
     * Adds a single node to the generated document. If the path is a
     * directory, and depth is greater than zero, then recursive calls
     * are made to add nodes for the directory's children.
     *
     * @param   path
     *      the file/directory to process
     * @param   depth
     *      how deep to scan the directory
     *
     * @throws  SAXException
     *      if an error occurs while constructing nodes
     */
    protected void addPath(File path, int depth)
    throws SAXException {
        if (path.isDirectory()) {
            startNode(DIR_NODE_NAME, path);
            if (depth>0) {
                File contents[] = path.listFiles();
                for (int i=0; i<contents.length; i++) {
                    addPath(contents[i], depth-1);
                }
            }
            endNode(DIR_NODE_NAME);
        } else {
            startNode(FILE_NODE_NAME, path);
            endNode(FILE_NODE_NAME);
        }
    }

    /**
     * Begins a named node, and calls setNodeAttributes to set its
     * attributes.
     *
     * @param   nodeName
     *      the name of the new node
     * @param   path
     *      the file/directory to use when setting attributes
     * 
     * @throws  SAXException
     *      if an error occurs while creating the node
     */
    protected void startNode(String nodeName, File path)
    throws SAXException {
        setNodeAttributes(path);
        super.contentHandler.startElement(URI, nodeName, nodeName, attributes);
    }

    /**
     * Sets the attributes for a given path. The default method sets attributes 
     * for the name of thefile/directory and for the last modification time 
     * of the path.
     *
     * @param path
     *        the file/directory to use when setting attributes
     *
     * @throws SAXException
     *         if an error occurs while setting the attributes
     */
    protected void setNodeAttributes(File path) throws SAXException {
        long lastModified = path.lastModified();
        attributes.clear();
        attributes.addAttribute("", FILENAME_ATTR_NAME,
                    FILENAME_ATTR_NAME, "CDATA",
                    path.getName());
        attributes.addAttribute("", LASTMOD_ATTR_NAME,
                    LASTMOD_ATTR_NAME, "CDATA",
                    Long.toString(path.lastModified()));
        attributes.addAttribute("", LASTMOD_ATTR_NAME,
                    LASTMOD_ATTR_NAME, "CDATA",
                    Long.toString(lastModified));
        attributes.addAttribute("", DATE_ATTR_NAME,
                    DATE_ATTR_NAME, "CDATA",
                    dateFormatter.format(new Date(lastModified)));
    }

    /**
     * Ends the named node.
     *
     * @param   nodeName
     *      the name of the new node
     * @param   path
     *      the file/directory to use when setting attributes
     * 
     * @throws  SAXException
     *      if an error occurs while closing the node
     */
    protected void endNode(String nodeName)
    throws SAXException {
        super.contentHandler.endElement(URI, nodeName, nodeName);
    }
}
