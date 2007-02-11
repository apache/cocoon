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
import org.apache.cocoon.components.source.impl.MultiSourceValidity;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.TraversableSource;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TimeZone;

/**
 * Generates an XML source hierarchy listing from a Traversable Source.
 * <p>
 * The root node of the generated document will normally be a
 * <code>collection</code> node and a collection node can contain zero or more
 * <code>resource</code> or collection nodes. A resource node has no children.
 * Each node will contain the following attributes:
 * <blockquote>
 *  <dl>
 *   <dt> name
 *   <dd> the name of the source
 *   <dt> lastModified
 *   <dd> the time the source was last modified, measured as the number of
 *        milliseconds since the epoch (as in java.io.File.lastModified)
 *   <dt> size
 *   <dd> the source size, in bytes (as in java.io.File.length)
 *   <dt> date (optional)
 *   <dd> the time the source was last modified in human-readable form
 *  </dl>
 * </blockquote>
 * <p>
 *  <b>Configuration options:</b>
 *  <dl>
 *   <dt> <i>depth</i> (optional)
 *   <dd> Sets how deep TraversableGenerator should delve into the
 *        source hierarchy. If set to 1 (the default), only the starting
 *        collection's immediate contents will be returned.
 *   <dt> <i>sort</i> (optional)
 *   <dd> Sort order in which the nodes are returned. Possible values are
 *        name, size, time, collection. collection is the same as name,
 *        except that the collection entries are listed first. System order is
 *        default.
 *   <dt> <i>reverse</i> (optional)
 *   <dd> Reverse the order of the sort
 *   <dt> <i>dateFormat</i> (optional)
 *   <dd> Sets the format for the date attribute of each node, as
 *        described in java.text.SimpleDateFormat. If unset, the default
 *        format for the current locale will be used.
 *   <dt> <i>timeZone</i> (optional)
 *   <dd> Sets the time zone offset ID for the date attribute, as 
 *        described in java.util.TimeZone. If unset, the default
 *        system time zone will be used.
 *   <dt> <i>refreshDelay</i> (optional)
 *   <dd> Sets the delay (in seconds) between checks on the source hierarchy
 *        for changed content. Defaults to 1 second.
 *  </dl>
 * </p>
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:conny@smb-tec.com">Conny Krappatsch</a>
 *         (SMB GmbH) for Virbus AG
 * @author <a href="d.madama@pro-netics.com">Daniele Madama</a>
 * @author <a href="gianugo@apache.org">Gianugo Rabellino</a>
 * @version CVS $Id: TraversableGenerator.java,v 1.8 2004/02/13 11:50:49 gianugo Exp $
 */
public class TraversableGenerator extends ServiceableGenerator implements CacheableProcessingComponent {

    /** The URI of the namespace of this generator. */
    protected static final String URI = "http://apache.org/cocoon/collection/1.0";

    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "collection";

    /* Node and attribute names */
    protected static final String COL_NODE_NAME = "collection";
    protected static final String RESOURCE_NODE_NAME = "resource";

    protected static final String RES_NAME_ATTR_NAME = "name";
    protected static final String URI_ATTR_NAME = "uri";
    protected static final String LASTMOD_ATTR_NAME = "lastModified";
    protected static final String DATE_ATTR_NAME = "date";
    protected static final String SIZE_ATTR_NAME = "size";

    /** The validity that is being built */
    protected MultiSourceValidity validity;
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

    /** The depth parameter determines how deep the TraversableGenerator should delve. */
    protected int depth;
    /**
     * The dateFormatter determines into which date format the lastModified
     * time should be converted.
     * FIXME: SimpleDateFormat is not supported by all locales!
     */
    protected SimpleDateFormat dateFormatter;
    /** The delay between checks on updates to the source hierarchy. */
    protected long refreshDelay;
    /**
     * The sort parameter determines by which attribute the content of one
     * collection should be sorted. Possible values are "name", "size", "time"
     * and "collection", where "collection" is the same as "name", except that
     * collection entries are listed first.
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
     * This is only set to true for the requested source specified by the
     * <code>src</code> attribute on the generator's configuration.
     */
    protected boolean isRequestedSource;

    /**
     * Set the request parameters. Must be called before the generate method.
     *
     * @param resolver     the SourceResolver object
     * @param objectModel  a <code>Map</code> containing model object
     * @param src          the Traversable Source to be XMLized specified as
     *                     <code>src</code> attribute on &lt;map:generate/>
     * @param par          configuration parameters
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        if (src == null) {
            throw new ProcessingException("No src attribute pointing to a traversable source to be XMLized specified.");
        }
        super.setup(resolver, objectModel, src, par);

        this.cacheKeyParList = new ArrayList();
        this.cacheKeyParList.add(src);

        this.depth = par.getParameterAsInteger("depth", 1);
        this.cacheKeyParList.add(String.valueOf(this.depth));

        String dateFormatString = par.getParameter("dateFormat", null);
        this.cacheKeyParList.add(dateFormatString);
        if (dateFormatString != null) {
            this.dateFormatter = new SimpleDateFormat(dateFormatString);
        } else {
            this.dateFormatter = new SimpleDateFormat();
        }
        
        String timeZone = par.getParameter("timeZone",null);
        if (timeZone != null) {
            this.dateFormatter.setTimeZone(TimeZone.getTimeZone(timeZone));
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
            this.getLogger().debug("timeZone: " + timeZone);
            this.getLogger().debug("sort: " + this.sort);
            this.getLogger().debug("reverse: " + this.reverse);
            this.getLogger().debug("refreshDelay: " + this.refreshDelay);
        }

        String rePattern = null;
        try {
            rePattern = par.getParameter("root", null);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("root pattern: " + rePattern);
            }
            this.cacheKeyParList.add(rePattern);
            this.rootRE = (rePattern == null) ? null : new RE(rePattern);

            rePattern = par.getParameter("include", null);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("include pattern: " + rePattern);
            }
            this.cacheKeyParList.add(rePattern);
            this.includeRE = (rePattern == null) ? null : new RE(rePattern);

            rePattern = par.getParameter("exclude", null);
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("exclude pattern: " + rePattern);
            }
            this.cacheKeyParList.add(rePattern);
            this.excludeRE = (rePattern == null) ? null : new RE(rePattern);
            
        } catch (RESyntaxException rese) {
            throw new ProcessingException("Syntax error in regexp pattern '" 
            			                  + rePattern + "'", rese);
        }

        this.isRequestedSource = false;
        this.attributes = new AttributesImpl();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        StringBuffer buffer = new StringBuffer();
        int len = this.cacheKeyParList.size();
        for (int i = 0; i < len; i++) {
            buffer.append((String)this.cacheKeyParList.get(i) + ":");
        }
        return buffer.toString();
    }

    /**
     * Gets the source validity, using a deferred validity object. The validity
     * is initially empty since the resources that define it are not known
     * before generation has occured. So the returned object is kept by the
     * generator and filled with each of the resources that is traversed.
     * 
     * @see org.apache.cocoon.components.source.impl.MultiSourceValidity
     */
    public SourceValidity getValidity() {
        if (this.validity == null) {
            this.validity = new MultiSourceValidity(this.resolver, this.refreshDelay);
        }
        return this.validity;
    }

    /**
     * Generate XML data.
     *
     * @throws  SAXException if an error occurs while outputting the document
     * @throws  ProcessingException if something went wrong while traversing
     *                              the source hierarchy 
     */
    public void generate() throws SAXException, ProcessingException {
        TraversableSource inputSource = null;
        try {
            Source src = this.resolver.resolveURI(this.source);
            if (!(src instanceof TraversableSource)) {
                throw new SourceException(this.source + " is not a traversable source");
            }
            inputSource = (TraversableSource) this.resolver.resolveURI(this.source);

            if (!inputSource.exists()) {
                throw new ResourceNotFoundException(this.source + " does not exist.");
            }

            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping(PREFIX, URI);

            Stack ancestors = getAncestors(inputSource);
            addAncestorPath(inputSource, ancestors);

            this.contentHandler.endPrefixMapping(PREFIX);
            this.contentHandler.endDocument();
            if (this.validity != null) {
                this.validity.close();
            }
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (IOException ioe) {
            throw new ResourceNotFoundException("Could not read collection "
                                                + this.source, ioe);
        } finally {
            this.resolver.release(inputSource);
        }
    }

    /**
     * Creates a stack containing the ancestors of a traversable source up to
     * specific parent matching the root pattern.
     * 
     * @param source the traversable source whose ancestors shall be retrieved
     * @return a Stack containing the ancestors.
     */
    protected Stack getAncestors(TraversableSource source) throws IOException {
        TraversableSource parent = source;
        Stack ancestors = new Stack();

        while ((parent != null) && !isRoot(parent)) {
            parent = (TraversableSource) parent.getParent();
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
     * Adds recursively the path from the source matched by the root pattern
     * down to the requested source.
     * 
     * @param source       the requested source.
     * @param ancestors  the stack of the ancestors.
     * @throws SAXException
     * @throws ProcessingException
     */
    protected void addAncestorPath(TraversableSource source, Stack ancestors)
            throws SAXException, ProcessingException {
        if (ancestors.empty()) {
            this.isRequestedSource = true;
            addPath(source, depth);
        } else {
            startNode(COL_NODE_NAME, (TraversableSource) ancestors.pop());
            addAncestorPath(source, ancestors);
            endNode(COL_NODE_NAME);
        }
    }

    /**
     * Adds a single node to the generated document. If the path is a
     * collection and depth is greater than zero, then recursive calls
     * are made to add nodes for the collection's children.
     *
     * @param source  the resource/collection to process
     * @param depth   how deep to scan the collection hierarchy
     *
     * @throws SAXException  if an error occurs while constructing nodes
     * @throws ProcessingException  if a problem occurs with the source
     */
    protected void addPath(TraversableSource source, int depth)
            throws SAXException, ProcessingException {
        if (source.isCollection()) {
            startNode(COL_NODE_NAME, source);
            addContent(source);
            if (depth > 0) {

                Collection contents = null;
                try {
                    contents = source.getChildren();
                } catch (SourceException e) {
                    throw new ProcessingException("Error adding paths", e);
                }

                if (sort.equals("name")) {
                    Arrays.sort(contents.toArray(), new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return ((TraversableSource) o2).getName().compareTo(((TraversableSource) o1).getName());
                            }
                            return ((TraversableSource) o1).getName().compareTo(((TraversableSource) o2).getName());
                        }
                    });
                } else if (sort.equals("size")) {
                    Arrays.sort(contents.toArray(), new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return new Long(((TraversableSource) o2).getContentLength()).compareTo(new Long(((TraversableSource) o1).getContentLength()));
                            }
                            return new Long(((TraversableSource) o1).getContentLength()).compareTo(new Long(((TraversableSource) o2).getContentLength()));
                        }
                    });
                } else if (sort.equals("lastmodified")) {
                    Arrays.sort(contents.toArray(), new Comparator() {
                        public int compare(Object o1, Object o2) {
                            if (reverse) {
                                return new Long(((TraversableSource) o2).getLastModified()).compareTo(new Long(((TraversableSource) o1).getLastModified()));
                            }
                            return new Long(((TraversableSource) o1).getLastModified()).compareTo(new Long(((TraversableSource) o2).getLastModified()));
                        }
                    });
                } else if (sort.equals("collection")) {
                    Arrays.sort(contents.toArray(), new Comparator() {
                        public int compare(Object o1, Object o2) {
                            TraversableSource ts1 = (TraversableSource) o1;
                            TraversableSource ts2 = (TraversableSource) o2;

                            if (reverse) {
                                if (ts2.isCollection() && !ts1.isCollection())
                                    return -1;
                                if (!ts2.isCollection() && ts1.isCollection())
                                    return 1;
                                return ts2.getName().compareTo(ts1.getName());
                            }
                            if (ts2.isCollection() && !ts1.isCollection())
                                return 1;
                            if (!ts2.isCollection() && ts1.isCollection())
                                return -1;
                            return ts1.getName().compareTo(ts2.getName());
                        }
                    });
                }
                
                for (int i = 0; i < contents.size(); i++) {
                    if (isIncluded((TraversableSource) contents.toArray()[i]) && !isExcluded((TraversableSource) contents.toArray()[i])) {
                        addPath((TraversableSource) contents.toArray()[i], depth - 1);
                    }
                }
            }
            endNode(COL_NODE_NAME);
        } else {
            if (isIncluded(source) && !isExcluded(source)) {
                startNode(RESOURCE_NODE_NAME, source);
                addContent(source);
                endNode(RESOURCE_NODE_NAME);
            }
        }
    }
    
    /**
     * Allow subclasses a chance to generate additional elements within collection and resource
     * elements.
     * 
     * @param source  the source to generate additional data for.
     */
    protected void addContent(TraversableSource source) throws SAXException, ProcessingException {
    }
    
    /**
     * Begins a named node and calls setNodeAttributes to set its attributes.
     *
     * @param nodeName  the name of the new node
     * @param source    the source a node with its attributes is added for
     * 
     * @throws SAXException  if an error occurs while creating the node
     */
    protected void startNode(String nodeName, TraversableSource source)
            throws SAXException, ProcessingException {
        if (this.validity != null) {
            this.validity.addSource(source);
        }
        setNodeAttributes(source);
        super.contentHandler.startElement(URI, nodeName, PREFIX + ':' + nodeName, attributes);
    }

    /**
     * Sets the attributes for a given source. For example attributes for the
     * name, the size and the last modification date of the source are added.
     *
     * @param source  the source attributes are added for
     */
    protected void setNodeAttributes(TraversableSource source) throws SAXException, ProcessingException {
        long lastModified = source.getLastModified();
        attributes.clear();
        attributes.addAttribute("", RES_NAME_ATTR_NAME,RES_NAME_ATTR_NAME,
                                "CDATA", source.getName());
        attributes.addAttribute("", URI_ATTR_NAME,URI_ATTR_NAME,
                                "CDATA", source.getURI());
        attributes.addAttribute("", LASTMOD_ATTR_NAME, LASTMOD_ATTR_NAME,
                                "CDATA", Long.toString(source.getLastModified()));
        attributes.addAttribute("", DATE_ATTR_NAME, DATE_ATTR_NAME,
                                "CDATA", dateFormatter.format(new Date(lastModified)));
        attributes.addAttribute("", SIZE_ATTR_NAME, SIZE_ATTR_NAME,
                                "CDATA", Long.toString(source.getContentLength()));
        if (this.isRequestedSource) {
            attributes.addAttribute("", "sort", "sort", "CDATA", this.sort);
            attributes.addAttribute("", "reverse", "reverse", "CDATA",
                                    String.valueOf(this.reverse));
            attributes.addAttribute("", "requested", "requested", "CDATA", "true");
            this.isRequestedSource = false;
        }
    }

    /**
     * Ends the named node.
     *
     * @param nodeName  the name of the new node
     *
     * @throws SAXException  if an error occurs while closing the node
     */
    protected void endNode(String nodeName) throws SAXException {
        super.contentHandler.endElement(URI, nodeName, PREFIX + ':' + nodeName);
    }

    /**
     * Determines if a given source is the defined root.
     *
     * @param source  the source to check
     *
     * @return true if the source is the root or the root pattern is not set,
     *         false otherwise.
     */
    protected boolean isRoot(TraversableSource source) {
        return (this.rootRE == null) ? true : this.rootRE.match(source.getName());
    }

    /**
     * Determines if a given source shall be visible.
     *
     * @param source  the source to check
     *
     * @return true if the source shall be visible or the include Pattern is not set,
     *         false otherwise.
     */
    protected boolean isIncluded(TraversableSource source) {
        return (this.includeRE == null) ? true : this.includeRE.match(source.getName());
    }

    /**
     * Determines if a given source shall be excluded from viewing.
     *
     * @param source  the source to check
     *
     * @return false if the given source shall not be excluded or the exclude Pattern is not set,
     *         true otherwise.
     */
    protected boolean isExcluded(TraversableSource source) {
        return (this.excludeRE == null) ? false : this.excludeRE.match(source.getName());
    }

    /**
     * Recycle resources
     */
    public void recycle() {
        this.cacheKeyParList = null;
        this.attributes = null;
        this.dateFormatter = null;
        this.rootRE = null;
        this.includeRE = null;
        this.excludeRE = null;
        this.validity = null;
        super.recycle();
    }
}
