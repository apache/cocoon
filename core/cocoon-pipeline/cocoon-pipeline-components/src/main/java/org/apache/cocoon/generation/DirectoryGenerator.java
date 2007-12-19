/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.generation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML directory listing.
 * A more general approach is implemented by the {@link TraversableGenerator}.
 *
 * @cocoon.sitemap.component.documentation
 * Generates an XML directory listing.
 * A more general approach is implemented by the {@link TraversableGenerator}.
 * @cocoon.sitemap.component.name   directory
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Uses the last modification date of the directory and the contained files.
 * @cocoon.sitemap.component.pooling.max  16
 *
 * @version $Id$
 */
public class DirectoryGenerator extends ServiceableGenerator
                                implements CacheableProcessingComponent {

    /** Constant for the file protocol. */
    private static final String FILE = "file:";

    /** The URI of the namespace of this generator. */
    protected static final String URI = "http://apache.org/cocoon/directory/2.0";

    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "dir";

    /* Node and attribute names */
    protected static final String DIR_NODE_NAME = "directory";
    protected static final String FILE_NODE_NAME = "file";

    protected static final String FILENAME_ATTR_NAME = "name";
    protected static final String LASTMOD_ATTR_NAME = "lastModified";
    protected static final String DATE_ATTR_NAME = "date";
    protected static final String SIZE_ATTR_NAME = "size";

    /** The validity that is being built */
    protected DirValidity validity;

    /** Convenience object, so we don't need to create an AttributesImpl for every element. */
    protected AttributesImpl attributes;

    /**
     * The cache key needs to be generated for the configuration of this
     * generator, so storing the parameters for generateKey().
     * Using the member variables after setup() would not work I guess. I don't
     * know a way from the regular expressions back to the pattern or at least
     * a useful string.
     */
    protected List cacheKeyParList;

    /** The depth parameter determines how deep the DirectoryGenerator should delve. */
    protected int depth;

    /**
     * The dateFormatter determines into which date format the lastModified
     * time should be converted.
     * FIXME: SimpleDateFormat is not supported by all locales!
     */
    protected SimpleDateFormat dateFormatter;

    /** The delay between checks on updates to the filesystem. */
    protected long refreshDelay;

    /**
     * The sort parameter determines by which attribute the content of one
     * directory should be sorted. Possible values are "name", "size", "lastmodified"
     * and "directory", where "directory" is the same as "name", except that
     * directory entries are listed first.
     */
    protected String sort;

    /** The reverse parameter reverses the sort order. <code>false</code> is default. */
    protected boolean reverse;

    /** The regular expression for the root pattern. */
    protected RE rootRE;

    /** The regular expression for the include pattern. */
    protected RE includeRE;

    /** The regular expression for the exclude pattern. */
    protected RE excludeRE;

    /**
     * This is only set to true for the requested directory specified by the
     * <code>src</code> attribute on the generator's configuration.
     */
    protected boolean isRequestedDirectory;

    /** The source object for the directory. */
    protected Source directorySource;


    /**
     * Set the request parameters. Must be called before the generate method.
     *
     * @param resolver     the SourceResolver object
     * @param objectModel  a <code>Map</code> containing model object
     * @param src          the directory to be XMLized specified as src attribute on &lt;map:generate/>
     * @param par          configuration parameters
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        if (src == null) {
            throw new ProcessingException("No src attribute pointing to a directory to be XMLized specified.");
        }
        super.setup(resolver, objectModel, src, par);

        try {
            this.directorySource = this.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        }

        this.cacheKeyParList = new ArrayList();
        this.cacheKeyParList.add(this.directorySource.getURI());

        this.depth = par.getParameterAsInteger("depth", 1);
        this.cacheKeyParList.add(String.valueOf(this.depth));

        String dateFormatString = par.getParameter("dateFormat", null);
        this.cacheKeyParList.add(dateFormatString);
        if (dateFormatString != null) {
            this.dateFormatter = new SimpleDateFormat(dateFormatString);
        } else {
            this.dateFormatter = new SimpleDateFormat();
        }

        this.sort = par.getParameter("sort", "name");
        this.cacheKeyParList.add(this.sort);

        this.reverse = par.getParameterAsBoolean("reverse", false);
        this.cacheKeyParList.add(String.valueOf(this.reverse));

        this.refreshDelay = par.getParameterAsLong("refreshDelay", 1L) * 1000L;
        this.cacheKeyParList.add(String.valueOf(this.refreshDelay));

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("depth: " + this.depth);
            this.getLogger().debug("dateFormat: " + this.dateFormatter.toPattern());
            this.getLogger().debug("sort: " + this.sort);
            this.getLogger().debug("reverse: " + this.reverse);
            this.getLogger().debug("refreshDelay: " + this.refreshDelay);
        }

        String rePattern = null;
        try {
            rePattern = par.getParameter("root", null);
            this.cacheKeyParList.add(rePattern);
            this.rootRE = (rePattern == null) ? null : new RE(rePattern);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("root pattern: " + rePattern);
            }

            rePattern = par.getParameter("include", null);
            this.cacheKeyParList.add(rePattern);
            this.includeRE = (rePattern == null) ? null : new RE(rePattern);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("include pattern: " + rePattern);
            }

            rePattern = par.getParameter("exclude", null);
            this.cacheKeyParList.add(rePattern);
            this.excludeRE = (rePattern == null) ? null : new RE(rePattern);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("exclude pattern: " + rePattern);
            }
        } catch (RESyntaxException rese) {
            throw new ProcessingException("Syntax error in regexp pattern '"
                                          + rePattern + "'", rese);
        }

        this.isRequestedDirectory = false;
        this.attributes = new AttributesImpl();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        StringBuffer buffer = new StringBuffer();
        int len = this.cacheKeyParList.size();
        for (int i = 0; i < len; i++) {
            buffer.append(this.cacheKeyParList.get(i)).append(":");
        }
        return buffer.toString();
    }

    /**
     * Gets the source validity, using a deferred validity object. The validity
     * is initially empty since the files that define it are not known before
     * generation has occured. So the returned object is kept by the generator
     * and filled with each of the files that are traversed.
     * 
     * @see DirectoryGenerator.DirValidity
     */
    public SourceValidity getValidity() {
        if (this.validity == null) {
            this.validity = new DirValidity(this.refreshDelay);
        }
        return this.validity;
    }

    /**
     * Generate XML data.
     * 
     * @throws SAXException  if an error occurs while outputting the document
     * @throws ProcessingException  if the requsted URI isn't a directory on the local filesystem
     */
    public void generate() throws SAXException, ProcessingException {
        try {
            String systemId = this.directorySource.getURI();
            if (!systemId.startsWith(FILE)) {
                throw new ResourceNotFoundException(systemId + " does not denote a directory");
            }
            // This relies on systemId being of the form "file://..."
            File directoryFile = new File(new URL(systemId).getFile());
            if (!directoryFile.isDirectory()) {
                throw new ResourceNotFoundException(super.source + " is not a directory.");
            }

            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX, URI);

            Stack ancestors = getAncestors(directoryFile);
            addAncestorPath(directoryFile, ancestors);

            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();
        } catch (IOException ioe) {
            throw new ResourceNotFoundException("Could not read directory " + super.source, ioe);
        }
    }

    /**
     * Creates a stack containing the ancestors of File up to specified directory.
     * 
     * @param path the File whose ancestors shall be retrieved
     * @return a Stack containing the ancestors.
     */
    protected Stack getAncestors(File path) {
        File parent = path;
        Stack ancestors = new Stack();

        while ((parent != null) && !isRoot(parent)) {
            parent = parent.getParentFile();
            if (parent != null) {
                ancestors.push(parent);
            } else {
                // no ancestor matched the root pattern
                ancestors.clear();
            }
        }

        return ancestors;
    }

    /**
     * Adds recursively the path from the directory matched by the root pattern
     * down to the requested directory.
     * 
     * @param path       the requested directory.
     * @param ancestors  the stack of the ancestors.
     * @throws SAXException
     */
    protected void addAncestorPath(File path, Stack ancestors) throws SAXException {
        if (ancestors.empty()) {
            this.isRequestedDirectory = true;
            addPath(path, depth);
        } else {
            startNode(DIR_NODE_NAME, (File)ancestors.pop());
            addAncestorPath(path, ancestors);
            endNode(DIR_NODE_NAME);
        }
    }

    /**
     * Adds a single node to the generated document. If the path is a
     * directory, and depth is greater than zero, then recursive calls
     * are made to add nodes for the directory's children.
     * 
     * @param path   the file/directory to process
     * @param depth  how deep to scan the directory
     * @throws SAXException  if an error occurs while constructing nodes
     */
    protected void addPath(File path, int depth) throws SAXException {
        if (path.isDirectory()) {
            startNode(DIR_NODE_NAME, path);
            if (depth > 0) {
                File contents[] = path.listFiles();

                if (sort.equals("name")) {
                    Arrays.sort(contents, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return ((File)o2).getName().compareTo(((File)o1).getName());
                            }
                            return ((File)o1).getName().compareTo(((File)o2).getName());
                        }
                    });
                } else if (sort.equals("size")) {
                    Arrays.sort(contents, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return new Long(((File)o2).length()).compareTo(
                                    new Long(((File)o1).length()));
                            }
                            return new Long(((File)o1).length()).compareTo(
                                new Long(((File)o2).length()));
                        }
                    });
                } else if (sort.equals("lastmodified")) {
                    Arrays.sort(contents, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return new Long(((File)o2).lastModified()).compareTo(
                                    new Long(((File)o1).lastModified()));
                            }
                            return new Long(((File)o1).lastModified()).compareTo(
                                new Long(((File)o2).lastModified()));
                        }
                    });
                } else if (sort.equals("directory")) {
                    Arrays.sort(contents, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            File f1 = (File)o1;
                            File f2 = (File)o2;

                            if (reverse) {
                                if (f2.isDirectory() && f1.isFile())
                                    return -1;
                                if (f2.isFile() && f1.isDirectory())
                                    return 1;
                                return f2.getName().compareTo(f1.getName());
                            }
                            if (f2.isDirectory() && f1.isFile())
                                return 1;
                            if (f2.isFile() && f1.isDirectory())
                                return -1;
                            return f1.getName().compareTo(f2.getName());
                        }
                    });
                }

                for (int i = 0; i < contents.length; i++) {
                    if (isIncluded(contents[i]) && !isExcluded(contents[i])) {
                        addPath(contents[i], depth - 1);
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
     * Begins a named node and calls setNodeAttributes to set its attributes.
     * 
     * @param nodeName  the name of the new node
     * @param path      the file/directory to use when setting attributes
     * @throws SAXException  if an error occurs while creating the node
     */
    protected void startNode(String nodeName, File path) throws SAXException {
        if (this.validity != null) {
            this.validity.addFile(path);
        }
        setNodeAttributes(path);
        super.contentHandler.startElement(URI, nodeName, PREFIX + ':' + nodeName, attributes);
    }

    /**
     * Sets the attributes for a given path. The default method sets attributes
     * for the name of thefile/directory and for the last modification time
     * of the path.
     * 
     * @param path  the file/directory to use when setting attributes
     * @throws SAXException  if an error occurs while setting the attributes
     */
    protected void setNodeAttributes(File path) throws SAXException {
        long lastModified = path.lastModified();
        attributes.clear();
        attributes.addAttribute("", FILENAME_ATTR_NAME, FILENAME_ATTR_NAME,
                                "CDATA", path.getName());
        attributes.addAttribute("", LASTMOD_ATTR_NAME, LASTMOD_ATTR_NAME,
                                "CDATA", Long.toString(path.lastModified()));
        attributes.addAttribute("", DATE_ATTR_NAME, DATE_ATTR_NAME,
                                "CDATA", dateFormatter.format(new Date(lastModified)));
        attributes.addAttribute("", SIZE_ATTR_NAME, SIZE_ATTR_NAME,
                                "CDATA", Long.toString(path.length()));
        if (this.isRequestedDirectory) {
            attributes.addAttribute("", "sort", "sort", "CDATA", this.sort);
            attributes.addAttribute("", "reverse", "reverse", "CDATA",
                                    String.valueOf(this.reverse));
            attributes.addAttribute("", "requested", "requested", "CDATA", "true");
            this.isRequestedDirectory = false;
        }
    }

    /**
     * Ends the named node.
     * 
     * @param nodeName  the name of the new node
     * @throws SAXException  if an error occurs while closing the node
     */
    protected void endNode(String nodeName) throws SAXException {
        super.contentHandler.endElement(URI, nodeName, PREFIX + ':' + nodeName);
    }

    /**
     * Determines if a given File is the defined root.
     * 
     * @param path  the File to check
     * @return true if the File is the root or the root pattern is not set,
     *         false otherwise.
     */
    protected boolean isRoot(File path) {
        return this.rootRE == null || this.rootRE.match(path.getName());
    }

    /**
     * Determines if a given File shall be visible.
     * 
     * @param path  the File to check
     * @return true if the File shall be visible or the include Pattern is <code>null</code>,
     *         false otherwise.
     */
    protected boolean isIncluded(File path) {
        return this.includeRE == null || this.includeRE.match(path.getName());
    }

    /**
     * Determines if a given File shall be excluded from viewing.
     * 
     * @param path  the File to check
     * @return false if the given File shall not be excluded or the exclude Pattern is <code>null</code>,
     *         true otherwise.
     */
    protected boolean isExcluded(File path) {
        return this.excludeRE != null && this.excludeRE.match(path.getName());
    }

    /**
     * Recycle resources
     */
    public void recycle() {
        if (this.resolver != null) {
            this.resolver.release(this.directorySource);
            this.directorySource = null;
        }
        this.cacheKeyParList = null;
        this.attributes = null;
        this.dateFormatter = null;
        this.rootRE = null;
        this.includeRE = null;
        this.excludeRE = null;
        this.validity = null;
        super.recycle();
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
            if (System.currentTimeMillis() <= expiry) {
                return SourceValidity.VALID;
            }

            int len = files.size();
            for (int i = 0; i < len; i++) {
                File f = (File)files.get(i);
                if (!f.exists()) {
                    return SourceValidity.INVALID; // File was removed
                }

                long oldDate = ((Long)fileDates.get(i)).longValue();
                long newDate = f.lastModified();

                if (oldDate != newDate) {
                    // File's last modified date has changed since last check
                    // NOTE: this occurs on directories as well when a file is added
                    return SourceValidity.INVALID;
                }
            }

            // all content is up to date: update the expiry date
            expiry = System.currentTimeMillis() + delay;
            return SourceValidity.VALID;
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
