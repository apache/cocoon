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
package org.apache.cocoon.transformation.pagination;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.acting.AbstractTransformer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * A paginating transformer.
 *
 * @author     <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author     <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bhtek@yahoo.com">Boon Hian Tek</a>
 * @version    CVS $Id: Paginator.java,v 1.6 2003/12/08 10:17:01 cziegeler Exp $
 */
public class Paginator extends AbstractTransformer
  implements Composable, Disposable, CacheableProcessingComponent {

    public static final String PAGINATE_URI = "http://apache.org/cocoon/paginate/1.0";
    public static final String PAGINATE_PREFIX = "page";
    public static final String PAGINATE_PREFIX_TOKEN = PAGINATE_PREFIX+":";

    private ComponentManager manager;
    private SAXParser parser;
    private Store store;
    private SourceResolver resolver;
    private Source inputSource;
    private int page;
    private int item;
    private String itemGroup;
    private String requestURI;
    private Request request;
    private Pagesheet pagesheet;
    private int level;
    private boolean prefixMapping;

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     *
     * @param  manager  Description of the Parameter
     */
    public void compose(ComponentManager manager) {
        try {
            this.manager = manager;
            getLogger().debug("Looking up "+SAXParser.ROLE);
            this.parser = (SAXParser) manager.lookup(SAXParser.ROLE);
            getLogger().debug("Looking up "+Store.ROLE+"/TransientStore");
            this.store = (Store) manager.lookup(Store.ROLE+"/TransientStore");
        } catch (Exception e) {
            getLogger().error("Could not find component", e);
        }
    }

    /**
     * Dispose this component.
     */
    public void dispose() {
        if (this.parser!=null) {
            this.manager.release((Component) this.parser);
        } else {
            this.parser = null;
        }
        if (this.store!=null) {
            this.manager.release(this.store);
        } else {
            this.store = null;
        }
    }

    /**
     * Setup the transformer.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par)
                        throws ProcessingException, SAXException,
                               IOException {

        if (src == null) {
            throw new ProcessingException("I need the paginate instructions (pagesheet) to continue. Set the 'src' attribute.");
        }

        try {
            this.level = 0;
            this.prefixMapping = false;
            this.resolver = resolver;
            this.inputSource = resolver.resolveURI(src);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Using pagesheet: '"+
                                  this.inputSource.getURI()+"' in "+this+
                                  ", last modified: "+
                                  this.inputSource.getLastModified());
            }
            this.page = par.getParameterAsInteger("page", 1);
            this.item = par.getParameterAsInteger("item", 0);
            this.itemGroup = par.getParameter("item-group", "");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Paginating with [page = "+this.page+
                                  ", item = "+this.item+", item-group = "+
                                  this.itemGroup+"]");
            }

            this.request = ObjectModelHelper.getRequest(objectModel);
            this.requestURI = request.getRequestURI();

            // Get the pagesheet factory from the Store if available,
            // otherwise load it and put it into the store for further request
            if (store!=null) {
                pagesheet = (Pagesheet) store.get(src);
            }

            // If not in the store or if pagesheet has changed, loads and stores it
            if ((pagesheet==null) ||
                pagesheet.modifiedSince(inputSource.getLastModified())) {
                pagesheet = new Pagesheet();
                pagesheet.setLastModified(inputSource.getLastModified());
                parser.parse(new InputSource(inputSource.getInputStream()),
                             pagesheet);
                if (store!=null) {
                    store.store(src, pagesheet);
                }
            }

            // Clone it in order to avoid concurrency collisions since the
            // implementation is not reentrant.
            this.pagesheet = (Pagesheet) this.pagesheet.clone();
        } catch (SourceException se) {
            throw new ProcessingException("Could not retrieve source '" +
                                          src + "'", se);
        }
    }

    public void recycle() {
        if (null != this.inputSource) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        this.resolver = null;
        super.recycle();
    }

    /**
     * Generate the unique key. This key must be unique inside the space of
     * this component. This method must be invoked before the
     * generateValidity() method.
     *
     * @return The generated key or <code>null</code> if the component is
     *         currently not cacheable.
     */
    public Serializable getKey() {
        if (this.inputSource.getLastModified()!=0) {
            return this.inputSource.getURI()+page;
        } else {
            return null;
        }
    }

    /**
     * Generate the validity object. Before this method can be invoked the
     * generateKey() method must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        if (this.inputSource.getLastModified()!=0) {
            AggregatedValidity validity = new AggregatedValidity();

            validity.add(new TimeStampValidity(page));
            validity.add(this.inputSource.getValidity());
            return validity;
        } else {
            return null;
        }
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the
     *            element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param loc The local name (without prefix), or the empty
     *            string if Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty
     *            string if raw names are not available.
     * @param a The attributes attached to the element. If there
     *          are no attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw,
                             Attributes a) throws SAXException {
        if ( !prefixMapping) {
            super.startPrefixMapping(PAGINATE_PREFIX, PAGINATE_URI);
            this.prefixMapping = true;
        }
        level++;
        pagesheet.processStartElement(uri, loc);
        if (pagesheet.isInPage(page, item, itemGroup)) {
            int itemCount = pagesheet.itemCount(uri, loc);

            if (itemCount>0) {
                String itemGroup = pagesheet.getItemGroupName(uri, loc);
                AttributesImpl atts = new AttributesImpl(a);

                atts.addAttribute(PAGINATE_URI, "item",
                                  PAGINATE_PREFIX_TOKEN+"item", "CDATA",
                                  String.valueOf(itemCount));
                atts.addAttribute(PAGINATE_URI, "item-group",
                                  PAGINATE_PREFIX_TOKEN+"item-group",
                                  "CDATA", itemGroup);
                super.startElement(uri, loc, raw, atts);
            } else {
                super.startElement(uri, loc, raw, a);
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the
     *            element has no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param loc The local name (without prefix), or the empty
     *            string if Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty
     *            string if raw names are not available.
     */
    public void endElement(String uri, String loc,
                           String raw) throws SAXException {
        level--;

        // Prevent infinite recursive loop.
        if (PAGINATE_URI.equals(uri)) {
            super.endElement(uri, loc, raw);
            return;
        }

        if (pagesheet.isInPage(page, item, itemGroup)) {
            if (level==0) {
                if (item==0) {
                    int totalPages = pagesheet.getTotalPages();
                    PageRules rules = pagesheet.getPageRules(page);

                    Integer[] rangeLinks = rules.getRangeLinks();
                    int unitLinks = rules.unitLinks;
                    int currentPage = page;

                    // call add paginate
                    addPaginateTags(rangeLinks, unitLinks, currentPage,
                                    totalPages, requestURI, this);

                } else {
                    int totalItems = pagesheet.getTotalItems(itemGroup);
                    AttributesImpl atts = new AttributesImpl();

                    atts.addAttribute("", "current", "current", "CDATA",
                                      String.valueOf(item));
                    atts.addAttribute("", "total", "total", "CDATA",
                                      String.valueOf(totalItems));
                    atts.addAttribute("", "current-uri", "current-uri",
                                      "CDATA", requestURI);
                    atts.addAttribute("", "clean-uri", "clean-uri",
                                      "CDATA", cleanURI(requestURI, item));
                    atts.addAttribute("", "page", "page", "CDATA",
                                      String.valueOf(pagesheet.getPageForItem(item,
                                          itemGroup)));
                    super.startElement(PAGINATE_URI, "item",
                                       PAGINATE_PREFIX_TOKEN+"item", atts);
                    if (item>1) {
                        atts.clear();
                        atts.addAttribute("", "type", "type", "CDATA",
                                          "prev");
                        atts.addAttribute("", "uri", "uri", "CDATA",
                                          encodeURI(requestURI, item,
                                                    item-1));
                        super.startElement(PAGINATE_URI, "link",
                                           PAGINATE_PREFIX_TOKEN+"link",
                                           atts);
                        super.endElement(PAGINATE_URI, "link",
                                         PAGINATE_PREFIX_TOKEN+"link");
                    }
                    if (item<=totalItems) {
                        atts.clear();
                        atts.addAttribute("", "type", "type", "CDATA",
                                          "next");
                        atts.addAttribute("", "uri", "uri", "CDATA",
                                          encodeURI(requestURI, item,
                                                    item+1));
                        super.startElement(PAGINATE_URI, "link",
                                           PAGINATE_PREFIX_TOKEN+"link",
                                           atts);
                        super.endElement(PAGINATE_URI, "link",
                                         PAGINATE_PREFIX_TOKEN+"link");
                    }
                    super.endElement(PAGINATE_URI, "item",
                                     PAGINATE_PREFIX_TOKEN+"item");
                }

                super.endPrefixMapping(PAGINATE_PREFIX);
            }

            super.endElement(uri, loc, raw);
        }

        pagesheet.processEndElement(uri, loc);
    }

    public static void addPaginateTags(Integer[] rangeLinks, int unitLinks,
                                       int currentPage, int totalPages,
                                       String requestURI,
                                       AbstractTransformer saxTransformer)
                                         throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute("", "current", "current", "CDATA",
                          String.valueOf(currentPage));
        atts.addAttribute("", "total", "total", "CDATA",
                          String.valueOf(totalPages));
        atts.addAttribute("", "current-uri", "current-uri", "CDATA",
                          requestURI);
        atts.addAttribute("", "clean-uri", "clean-uri", "CDATA",
                          Paginator.cleanURI(requestURI, currentPage));
        saxTransformer.startElement(Paginator.PAGINATE_URI, "page",
                                    Paginator.PAGINATE_PREFIX_TOKEN+"page",
                                    atts);

        for (int i = rangeLinks.length-1; i>-1; i--) {
            int rangeLink = rangeLinks[i].intValue();

            if ((rangeLink>0) && (currentPage-rangeLink>=1)) {
                atts.clear();
                atts.addAttribute("", "type", "type", "CDATA", "prev");
                atts.addAttribute("", "range", "range", "CDATA",
                                  rangeLinks[i].toString());
                atts.addAttribute("", "uri", "uri", "CDATA",
                                  Paginator.encodeURI(requestURI,
                                                      currentPage,
                                                      currentPage-rangeLink));
                atts.addAttribute("", "page", "page", "CDATA",
                                  String.valueOf(currentPage-rangeLink));
                saxTransformer.startElement(Paginator.PAGINATE_URI,
                                            "range-link",
                                            Paginator.PAGINATE_PREFIX_TOKEN+
                                            "range-link", atts);
                saxTransformer.endElement(Paginator.PAGINATE_URI,
                                          "range-link",
                                          Paginator.PAGINATE_PREFIX_TOKEN+
                                          "range-link");
            }
        }

        for (int i = currentPage-unitLinks; i<currentPage; i++) {
            if (i>0) {
                atts.clear();
                atts.addAttribute("", "type", "type", "CDATA", "prev");
                atts.addAttribute("", "uri", "uri", "CDATA",
                                  Paginator.encodeURI(requestURI,
                                                      currentPage, i));
                atts.addAttribute("", "page", "page", "CDATA",
                                  String.valueOf(i));
                saxTransformer.startElement(Paginator.PAGINATE_URI, "link",
                                            Paginator.PAGINATE_PREFIX_TOKEN+
                                            "link", atts);
                saxTransformer.endElement(Paginator.PAGINATE_URI, "link",
                                          Paginator.PAGINATE_PREFIX_TOKEN+
                                          "link");
            }
        }
        for (int i = currentPage+1; i<=currentPage+unitLinks; i++) {
            if (i<=totalPages) {
                atts.clear();
                atts.addAttribute("", "type", "type", "CDATA", "next");
                atts.addAttribute("", "uri", "uri", "CDATA",
                                  Paginator.encodeURI(requestURI,
                                                      currentPage, i));
                atts.addAttribute("", "page", "page", "CDATA",
                                  String.valueOf(i));
                saxTransformer.startElement(Paginator.PAGINATE_URI, "link",
                                            Paginator.PAGINATE_PREFIX_TOKEN+
                                            "link", atts);
                saxTransformer.endElement(Paginator.PAGINATE_URI, "link",
                                          Paginator.PAGINATE_PREFIX_TOKEN+
                                          "link");
            }
        }

        for (int i = 0; i<rangeLinks.length; i++) {
            int rangeLink = rangeLinks[i].intValue();

            if ((rangeLink>0) && (currentPage+rangeLink<=totalPages)) {
                atts.clear();
                atts.addAttribute("", "type", "type", "CDATA", "next");
                atts.addAttribute("", "range", "range", "CDATA",
                                  rangeLinks[i].toString());
                atts.addAttribute("", "uri", "uri", "CDATA",
                                  Paginator.encodeURI(requestURI,
                                                      currentPage,
                                                      currentPage+rangeLink));
                atts.addAttribute("", "page", "page", "CDATA",
                                  String.valueOf(currentPage+rangeLink));
                saxTransformer.startElement(Paginator.PAGINATE_URI,
                                            "range-link",
                                            Paginator.PAGINATE_PREFIX_TOKEN+
                                            "range-link", atts);
                saxTransformer.endElement(Paginator.PAGINATE_URI,
                                          "range-link",
                                          Paginator.PAGINATE_PREFIX_TOKEN+
                                          "range-link");
            }
        }

        saxTransformer.endElement(Paginator.PAGINATE_URI, "page",
                                  Paginator.PAGINATE_PREFIX_TOKEN+"page");
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len) throws SAXException {
        pagesheet.processCharacters(c, start, len);
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.characters(c, start, len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start,
                                    int len) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.ignorableWhitespace(c, start, len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none
     *             was supplied.
     */
    public void processingInstruction(String target,
                                      String data) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.processingInstruction(target, data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity. If it is a
     *             parameter entity, the name will begin with '%'.
     */
    public void skippedEntity(String name) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external
     *                 DTD subset, or null if none was declared.
     * @param systemId The declared system identifier for the external
     *                 DTD subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId,
                         String systemId) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.startDTD(name, publicId, systemId);
        } else {
            throw new SAXException("Recieved startDTD not in page.");
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD() throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.endDTD();
        } else {
            throw new SAXException("Recieved endDTD not in page.");
        }
    }

    /**
     * Report the beginning of an entity.
     *
     *@param name The name of the entity. If it is a parameter
     *            entity, the name will begin with '%'.
     */
    public void startEntity(String name) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA() throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len) throws SAXException {
        if (pagesheet.isInPage(page, item, itemGroup)) {
            super.comment(ch, start, len);
        }
    }

    /**
     * Removes the pagination encoding from the URI by removing the page number
     * and the previous and next character.
     */
    public static String cleanURI(String uri, int current) {
        String currentS = String.valueOf(current);
        int index = uri.lastIndexOf(currentS);

        if (index==-1) {
            return uri;
        } else {
            return uri.substring(0, index-1)+
                   uri.substring(index+currentS.length()+1);
        }
    }

    /**
     * Encode the next page in the given URI. First tries to use the existing
     * encoding by replacing the current page number, but if the current
     * encoding is not found it appends "(xx)" to the filename (before the file
     * extention, if any) where "xx" is the next page value.
     */
    public static String encodeURI(String uri, int current, int next) {
        String currentS = String.valueOf(current);
        String nextS = String.valueOf(next);
        int index = uri.lastIndexOf(currentS);

        if (index==-1) {
            index = uri.lastIndexOf('.');
            if (index==-1) {
                return uri+"("+nextS+")";
            } else {
                return uri.substring(0, index)+"("+nextS+")."+
                       uri.substring(index+1);
            }
        } else {
            return uri.substring(0, index)+nextS+
                   uri.substring(index+currentS.length());
        }
    }
}
