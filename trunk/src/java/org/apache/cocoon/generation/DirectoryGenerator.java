/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;
import java.util.Comparator;

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
 *   <dt> size
 *   <dd> the file size, in bytes (as in java.io.File.length)
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
 * <dt> <i>sort</i> (optional)
 * <dd> Sort order in which the nodes are returned. Possible values are
 * name, size, time, directory. directory is the same as name,
 * except that the directory entries are listed first. System order is default.
 * <dt> <i>reverse</i> (optional)
 * <dd>	Reverse the order of the sort
 * <dt> <i>dateFormat</i> (optional)
 * <dd> Sets the format for the date attribute of each node, as
 * described in java.text.SimpleDateFormat. If unset, the default
 * format for the current locale will be used.
 * <dt> <i>refreshDelay</i> (optional)
 * <dd> Sets the delay (in seconds) between checks on the filesystem for changed content.
 * Defaults to 1 second.
 * </dl>
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:conny@smb-tec.com">Conny Krappatsch</a>
 *         (SMB GmbH) for Virbus AG
 * @version CVS $Id: DirectoryGenerator.java,v 1.3 2003/05/13 22:14:51 vgritsenko Exp $
 */
public class DirectoryGenerator extends ComposerGenerator implements CacheableProcessingComponent  {
  private static final String FILE = "file:";

    /** The URI of the namespace of this generator. */
    protected static final String URI =
    "http://apache.org/cocoon/directory/2.0";

    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "dir";

    /* Node and attribute names */
    protected static final String DIR_NODE_NAME         = "directory";
    protected static final String FILE_NODE_NAME        = "file";

    protected static final String FILENAME_ATTR_NAME    = "name";
    protected static final String LASTMOD_ATTR_NAME     = "lastModified";
    protected static final String DATE_ATTR_NAME        = "date";
    protected static final String SIZE_ATTR_NAME        = "size";

    /*
     * Variables set per-request
     *
     * FIXME: SimpleDateFormat is not supported by all locales!
     */
    protected int depth;
    protected AttributesImpl attributes = new AttributesImpl();
    protected SimpleDateFormat dateFormatter;
    protected String sort;
    protected boolean reverse;

    protected RE rootRE;
    protected RE includeRE;
    protected RE excludeRE;

    protected boolean isRequestedDirectory;
    
    /** The validity that is being built */
    protected DirValidity validity;
    
    /** The delay between checks to the filesystem */
    protected long refreshDelay;


    /**
     * Set the request parameters. Must be called before the generate
     * method.
     *
     * @param   resolver
     *      the SourceResolver object
     * @param   objectModel
     *      a <code>Map</code> containing model object
     * @param   src
     *      the URI for this request (?)
     * @param   par
     *      configuration parameters
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        String dateFormatString = par.getParameter("dateFormat", null);
        if (dateFormatString != null) {
            this.dateFormatter = new SimpleDateFormat(dateFormatString);
        } else {
            this.dateFormatter = new SimpleDateFormat();
        }

        this.depth = par.getParameterAsInteger("depth", 1);

        this.sort = par.getParameter("sort", "name");

        this.reverse = par.getParameterAsBoolean("reverse", false);

        String rePattern = par.getParameter("root", null);
        
        this.refreshDelay = par.getParameterAsLong("refreshDelay", 1L) * 1000L;
        
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("depth: " + this.depth);
            this.getLogger().debug("sort: " + this.sort);
            this.getLogger().debug("reverse: " + this.reverse);
            this.getLogger().debug("refreshDelay: " + this.refreshDelay);
        }
        try {
            this.rootRE = (rePattern == null)?null:new RE(rePattern);

            rePattern = par.getParameter("include", null);
            this.includeRE = (rePattern == null)?null:new RE(rePattern);

            rePattern = par.getParameter("exclude", null);
            this.excludeRE = (rePattern == null)?null:new RE(rePattern);

            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("root pattern: " + rePattern);
                this.getLogger().debug("include pattern: " + rePattern);
                this.getLogger().debug("exclude pattern: " + rePattern);
            }
        } catch (RESyntaxException rese) {
            throw new ProcessingException("Syntax error in regexp pattern '"
                + rePattern + "'", rese);
        }

        this.isRequestedDirectory = false;

        /* Create a reusable attributes for creating nodes */
        this.attributes = new AttributesImpl();
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
	 */
	public Serializable getKey()
	{
		return super.source + this.sort + this.depth + this.excludeRE + this.includeRE + this.reverse;
	}


	/**
	 * Gets the source validity, using a deferred validity object. The validity is initially empty since
	 * the files that define it are not known before generation has occured. So the returned object is
	 * kept by the generator and filled with each of the files that are traversed.
	 * @see DirectoryGenerator.DirValidity
	 */
	public SourceValidity getValidity()
	{
		this.validity = new DirValidity(this.refreshDelay);
		return this.validity;
	}

    /**
     * Generate XML data.
     *
     * @throws  SAXException
     *      if an error occurs while outputting the document
     * @throws  ProcessingException
     *      if the requsted URI isn't a directory on the local
     *      filesystem
     */
    public void generate()
    throws SAXException, ProcessingException {
        String directory = super.source;
        Source inputSource = null;
        try {
            inputSource = this.resolver.resolveURI(directory);
            String systemId = inputSource.getURI();
            if (!systemId.startsWith(FILE)) {
              throw new ResourceNotFoundException(systemId + " does not denote a directory");
            }
            // This relies on systemId being of the form "file://..."
            File directoryFile = new File(new URL(systemId).getFile());
            if (!directoryFile.isDirectory()) {
                throw new ResourceNotFoundException(directory + " is not a directory.");
            }

            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX,URI);

            Stack ancestors = getAncestors(directoryFile);
            addPathWithAncestors(directoryFile, ancestors);

            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (IOException ioe) {
            throw new ResourceNotFoundException("Could not read directory "
                + directory, ioe);
        } finally {
            this.resolver.release( inputSource );
        }
    }

    /**
     * Creates a stack containing the ancestors of File up to specified
     * directory.
     * @param path the File whose ancestors shall be retrieved
     *
     * @return a Stack containing the ancestors.
     */
    protected Stack getAncestors(File path) {
        File parent = path;
        Stack ancestors = new Stack();

        while ((parent != null) && !isRoot(parent)) {
            parent = parent.getParentFile();
            if (parent != null) {
                ancestors.push(parent);
            }
        }

        return ancestors;
    }


    protected void addPathWithAncestors(File path, Stack ancestors)
            throws SAXException {

        if (ancestors.empty()) {
            this.isRequestedDirectory = true;
            addPath(path, depth);
        } else {
            startNode(DIR_NODE_NAME, (File)ancestors.pop());
            addPathWithAncestors(path, ancestors);
            endNode(DIR_NODE_NAME);
        }
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

                if(sort.equals("name")) {
                    Arrays.sort(contents,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                if(reverse) {
                                    return ((File) o2).getName()
                                        .compareTo(((File) o1).getName());
                                }
                                return ((File) o1).getName()
                                    .compareTo(((File) o2).getName());
                            }
                        });
                } else if(sort.equals("size")) {
                    Arrays.sort(contents,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                if(reverse) {
                                    return new Long(((File) o2).length())
                                        .compareTo(new Long(((File) o1).length()));
                                }
                                return new Long(((File) o1).length())
                                    .compareTo(new Long(((File) o2).length()));
                            }
                        });
                } else if(sort.equals("lastmodified")) {
                    Arrays.sort(contents,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                if(reverse) {
                                    return new Long(((File) o2).lastModified())
                                        .compareTo(new Long(((File) o1).lastModified()));
                                }
                                return new Long(((File) o1).lastModified())
                                    .compareTo(new Long(((File) o2).lastModified()));
                            }
                        });
                } else if(sort.equals("directory")) {
                    Arrays.sort(contents,
                        new Comparator() {
                            public int compare(Object o1, Object o2) {
                                File f1 = (File) o1;
                                File f2 = (File) o2;

                                if(reverse) {
                                    if(f2.isDirectory() && f1.isFile())
                                        return -1;
                                    if(f2.isFile() && f1.isDirectory())
                                        return 1;
                                    return f2.getName().compareTo(f1.getName());
                                }
                                if(f2.isDirectory() && f1.isFile())
                                    return 1;
                                if(f2.isFile() && f1.isDirectory())
                                    return -1;
                                return f1.getName().compareTo(f2.getName());
                            }
                        });
                }

                for (int i=0; i<contents.length; i++) {
                    if (isIncluded(contents[i]) && !isExcluded(contents[i])) {
                        addPath(contents[i], depth-1);
                    }
                }
            }
            endNode(DIR_NODE_NAME);
        } else {
            if (isIncluded(path) && !isExcluded(path)) {
                startNode(FILE_NODE_NAME, path);
                endNode(FILE_NODE_NAME);
            }
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
		if (this.validity != null) {
			this.validity.addFile(path);
		}

        setNodeAttributes(path);
        super.contentHandler.startElement(URI, nodeName, PREFIX+':'+nodeName, attributes);
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
        attributes.addAttribute("", DATE_ATTR_NAME,
                    DATE_ATTR_NAME, "CDATA",
                    dateFormatter.format(new Date(lastModified)));
        attributes.addAttribute("", SIZE_ATTR_NAME,
                    SIZE_ATTR_NAME, "CDATA",
                    Long.toString(path.length()));

        if (this.isRequestedDirectory) {
            attributes.addAttribute("", "sort", "sort", "CDATA", this.sort);
            attributes.addAttribute("", "reverse", "reverse", "CDATA",
                String.valueOf(this.reverse));
            attributes.addAttribute("", "requested", "requested", "CDATA",
                "true");
            this.isRequestedDirectory = false;
        }
    }


    /**
     * Ends the named node.
     *
     * @param   nodeName
     *      the name of the new node
     *
     * @throws  SAXException
     *      if an error occurs while closing the node
     */
    protected void endNode(String nodeName)
    throws SAXException {
        super.contentHandler.endElement(URI, nodeName, PREFIX+':'+nodeName);
    }


    /**
     * Determines if a given File is the defined root.
     *
     * @param path the File to check
     *
     * @return true if the File is the root or the root pattern is not set,
     *      false otherwise.
     */
    protected boolean isRoot(File path) {

        return (this.rootRE == null)
                ? true
                : this.rootRE.match(path.getName());
    }


    /**
     * Determines if a given File shall be visible.
     *
     * @param path the File to check
     *
     * @return true if the File shall be visible or the include Pattern is
            <code>null</code>, false otherwise.
     */
    protected boolean isIncluded(File path) {

        return (this.includeRE == null)
                ? true
                : this.includeRE.match(path.getName());
    }


    /**
     * Determines if a given File shall be excluded from viewing.
     *
     * @param path the File to check
     *
     * @return false if the given File shall not be excluded or the
     * exclude Pattern is <code>null</code>, true otherwise.
     */
    protected boolean isExcluded(File path) {

        return (this.excludeRE == null)
                ? false
                : this.excludeRE.match(path.getName());
    }

    /**
     * Recycle resources
     *
     */

    public void recycle() {
       super.recycle();
       this.attributes = null;
         this.dateFormatter = null;
       this.rootRE = null;
       this.includeRE = null;
       this.excludeRE = null;
       this.validity = null;

    }

    /** Specific validity class, that holds all files that have been generated */
	public static class DirValidity implements SourceValidity {
		private long expiry;
		private long delay;
		List files = new ArrayList();
		List fileDates = new ArrayList();

		public DirValidity(long delay) {
			expiry = System.currentTimeMillis() + delay;
			this.delay = delay;
		}

		public int isValid() {
			if (System.currentTimeMillis() <= expiry)
				return 1;

			//            System.out.println("Regenerating cache validity");
			expiry = System.currentTimeMillis() + delay;
			int len = files.size();
			for (int i = 0; i < len; i++) {
				File f = (File) files.get(i);
				if (!f.exists())
					return -1; // File was removed

				long oldDate = ((Long) fileDates.get(i)).longValue();
				long newDate = f.lastModified();

				if (oldDate != newDate)
					return -1;
			}

			// All content is up to date : update the expiry date
			expiry = System.currentTimeMillis() + delay;
			return 1;
		}
		
		public int isValid(SourceValidity newValidity) {
			return isValid();
		}

		public void addFile(File f) {
			files.add(f);
			fileDates.add(new Long(f.lastModified()));
		}
	}
}
