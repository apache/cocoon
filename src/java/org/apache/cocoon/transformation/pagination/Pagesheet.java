/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.transformation.pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.Modifiable;
import org.apache.cocoon.util.ResizableContainer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Interprets the pagesheet rules to perform pagination.
 *
 * <pre>
 * FIXME (SM): this code sucks! It was done to show the concept of
 *             rule driven pagination (which I find very nice) but
 *             it needs major refactoring in order to be sufficiently
 *             stable to allow any input to enter without breaking
 *             SAX well-formness. I currently don't have the time to make
 *             it any better (along with implementing the char-based rule
 *             that is mostly useful for text documents) but if you want
 *             to blast the code and rewrite it better, you'll make me happy :)
 * </pre>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:bhtek@yahoo.com">Boon Hian Tek</a>
 * @version CVS $Id: Pagesheet.java,v 1.4 2004/05/17 09:32:04 unico Exp $
 */

/*

This is an example pagesheet to show the power of this:

  <?xml version="1.0"?>
  <pagesheet xmlns="http://apache.org/cocoon/paginate/1.0">
   <items>
    <group name="pictures" element="file" namespace="http://apache.org/cocoon/directory/2.0"/>
   </items>
   <rules page="1">
    <count type="element" name="file" namespace="http://apache.org/cocoon/directory/2.0" num="16"/>
     <link type="unit" num="2"/>
     <link type="range" value="10"/>
   </rules>
   <rules>
    <count type="element" name="file" namespace="http://apache.org/cocoon/directory/2.0" num="16"/>
     <link type="unit" num="5"/>
     <link type="range" value="20"/>
   </rules>
   <rules>
     <count type="element" name="file" namespace="http://apache.org/cocoon/directory/2.0" num="16"/>
     <link type="unit" num="5"/>
     <link type="range" value="2"/>
     <link type="range" value="5"/>
     <link type="range" value="10"/>
     <link type="range" value="20"/>
     <link type="range" value="100"/>
   </rules>
  </pagesheet>

which indicates that:

 1) there is one item group called "picture" and each item is given by the
    element "file" of the namespace "http://apache.org/cocoon/directory/2.0".

 2) for the first page, the pagination rules indicate that there are two unit
    links (two above and two below, so linking to page -2 -1 0 +1 +2) and
    range links have value 10 (so they link to page -10 and +10).

 3) for the rest of the pages, there are three unit links (-3 -2 -1 0 +1 +2 +3)
    and range goes 20 (so +20 and -20).

 4) if more than one ranges are defined, range links will be created in sequence

 5) range links will be from big to small (eg. 20, 10, then 5) for backward links,
    range links will be from small to big (eg. 5, 10, then 20) for forward links

 6) range link(s) will have an attribute 'range' to indicate the range size

*/
public class Pagesheet extends DefaultHandler
  implements Cloneable, Modifiable, Serializable {

    // Used only during parsing of pagesheet document
    private int level = 0;
    private int pg = 0;
    private long lastModified;
    private PageRules rules;

    // Loaded pagesheet information
    ResizableContainer pageRules;

    Map itemGroupsPerName;
    Map itemGroupsPerElement;
    Map itemListsPerName;
    Map itemListsPerElement;

    // Runtime information
    private ResizableContainer pages;
    private Page currentPage = null;
    private int pageCounter = 1;
    private int elementCounter = 0;
    private int descendant = 0;

    private static class Page {

        public int elementStart;
        public int elementEnd;
        public int characters;

        public Page(PageRules rules, int elementStart) {
            this.elementStart = elementStart;

            if (rules.elementCount>0) {
                this.elementEnd = this.elementStart+rules.elementCount-1;
            } else {
                this.elementEnd = this.elementStart+1;
            }
        }

        public boolean validInPage(int elementCounter) {
            return (this.elementStart<=elementCounter) &&
                   (elementCounter<=this.elementEnd);
        }
    }

    private static class ItemList extends ArrayList {

        public ItemList(int capacity) {
            super(capacity);
        }

        public void addItem(int page) {
            this.add(new Integer(page));
        }

        public int getPageForItem(int item) {
            Integer i = (Integer) this.get(item-1);

            return (i==null) ? 0 : i.intValue();
        }

        public boolean valid(int item) {
            return (item==this.size());
        }
    }

    public Pagesheet() {
        this.pages = new ResizableContainer(2);
    }

    private Pagesheet(ResizableContainer rules, Map itemGroupsPerName,
                      Map itemGroupsPerElement) {
        this.pageRules = rules;
        this.itemGroupsPerName = itemGroupsPerName;
        this.itemGroupsPerElement = itemGroupsPerElement;

        this.pages = new ResizableContainer(5);

        if ((this.itemGroupsPerName!=null) &&
            (this.itemGroupsPerElement!=null)) {
            this.itemListsPerName = new HashMap(itemGroupsPerName.size());
            this.itemListsPerElement = new HashMap(itemGroupsPerName.size());

            Iterator iter = itemGroupsPerName.values().iterator();

            for (; iter.hasNext(); ) {
                ItemGroup group = (ItemGroup) iter.next();
                ItemList list = new ItemList(10);

                this.itemListsPerName.put(group.getName(), list);
                this.itemListsPerElement.put(group.getElementURI()+
                                             group.getElementName(), list);
            }
        }
    }

    // --------------- interprets the pagesheet document ----------------

    public void startPrefixMapping(String prefix,
                                   String uri) throws SAXException {
        if ( !uri.equals(Paginator.PAGINATE_URI)) {
            throw new SAXException("The pagesheet's namespace is not supported.");
        }
    }

    public void startElement(String uri, String loc, String raw,
                             Attributes a) throws SAXException {
        level++;
        switch (level) {
            case 1 :
                if (loc.equals("pagesheet")) {
                    // This object represents pagesheet
                    return;
                }
                break;

            case 2 :
                if (loc.equals("rules")) {
                    if (this.pageRules==null) {
                        this.pageRules = new ResizableContainer(2);
                    }
                    String key = a.getValue("page");

                    if (key!=null) {
                        try {
                            pg = Integer.parseInt(key);
                        } catch (NumberFormatException e) {
                            throw new SAXException("Syntax error: the attribute 'rules/@page' must contain a number");
                        }
                    } else {
                        pg = 0;
                    }
                    rules = new PageRules();
                    return;
                } else if (loc.equals("items")) {
                    if (this.itemGroupsPerName==null) {
                        this.itemGroupsPerName = new HashMap(2);
                    }
                    if (this.itemGroupsPerElement==null) {
                        this.itemGroupsPerElement = new HashMap(2);
                    }
                    return;
                }
                break;

            case 3 :
                if (loc.equals("count")) {
                    rules.elementName = a.getValue("name");
                    rules.elementURI = a.getValue("namespace");

                    if (a.getValue("type").equals("element")) {
                        try {
                            rules.elementCount = Integer.parseInt(a.getValue("num"));
                        } catch (NumberFormatException e) {
                            throw new SAXException("Syntax error: the attribute 'count/@num' must contain a number");
                        }
                    } else if (a.getValue("type").equals("chars")) {
                        try {
                            rules.charCount = Integer.parseInt(a.getValue("num"));
                        } catch (NumberFormatException e) {
                            throw new SAXException("Syntax error: the attribute 'count/@num' must contain a number.");
                        }
                    } else {
                        throw new SAXException("Syntax error: count type not supported.");
                    }
                    return;
                } else if (loc.equals("link")) {
                    if (a.getValue("type").equals("unit")) {
                        try {
                            rules.unitLinks = Integer.parseInt(a.getValue("num"));
                        } catch (NumberFormatException e) {
                            throw new SAXException("Syntax error: the attribute 'link/@num' must contain a number.");
                        }
                    } else if (a.getValue("type").equals("range")) {
                        try {
                            rules.addRangeLink(a.getValue("value"));
                        } catch (NumberFormatException e) {
                            throw new SAXException("Syntax error: the attribute 'link/@value' must contain a number.");
                        }
                    } else {
                        throw new SAXException("Syntax error: link type not supported.");
                    }
                    return;
                } else if (loc.equals("group")) {
                    String name = a.getValue("name");

                    if (name==null) {
                        throw new SAXException("Syntax error: the attribute 'group/@name' must be present.");
                    }
                    String elementName = a.getValue("element");

                    if (elementName==null) {
                        throw new SAXException("Syntax error: the attribute 'group/@element' must be present.");
                    }
                    String elementURI = a.getValue("namespace");
                    ItemGroup group = new ItemGroup(name, elementURI,
                                                    elementName);

                    this.itemGroupsPerName.put(name, group);
                    this.itemGroupsPerElement.put(elementURI+elementName,
                                                  group);
                    return;
                }
        }
        throw new SAXException("Syntax error: element "+raw+
                               " is not recognized or is misplaced.");
    }

    public void endElement(String uri, String loc,
                           String raw) throws SAXException {
        level--;
        if (loc.equals("rules")) {
            pageRules.set(pg, rules);
        }
    }

    public void endDocument() throws SAXException {
        if (pageRules.size()==0) {
            throw new SAXException("Pagesheet must contain at least a set of pagination rules.");
        }
        if (pageRules.get(0)==null) {
            throw new SAXException("Pagesheet must contain the global pagination rules.");
        }
    }

    // --------------- process the received element events ----------------

    public void processStartElement(String uri, String name) {
        PageRules rules = getPageRules(pageCounter);

        if (rules.match(name, uri)) {
            elementCounter++;
            descendant++;

            if (currentPage==null) {
                currentPage = new Page(rules, 1);
            }

            if (elementCounter>currentPage.elementEnd) {
                /*System.out.println(">>>> "+pageCounter+
                                   ": Starting new page!!! >>> "+
                                   elementCounter);*/
                pageCounter++;
                currentPage = new Page(rules, currentPage.elementEnd+1);
            }

            pages.set(pageCounter, currentPage);
        }

        if (itemGroupsPerElement!=null) {
            String qname = uri+name;
            ItemGroup group = (ItemGroup) this.itemGroupsPerElement.get(qname);

            if ((group!=null) && (group.match(uri))) {
                ItemList list = (ItemList) this.itemListsPerElement.get(qname);

                if (list!=null) {
                    list.addItem(pageCounter);
                }
            }
        }
    }

    public void processEndElement(String uri, String name) {
        PageRules rules = getPageRules(pageCounter);

        if (rules.match(name, uri)) {
            descendant--;

            if ((rules.charCount>0) &&
                (currentPage.characters>rules.charCount)) {
                // We are over character limit. Flip the page.
                // System.out.println(">>>> " + pageCounter + ": Flipping page!!!");
                currentPage.elementEnd = elementCounter;
            } else if (rules.elementCount==0) {
                // No limit on elements is specified, and limit on characters is not reached yet.
                currentPage.elementEnd++;
            }
        }
    }

    public void processCharacters(char[] ch, int index, int len) {
        if (descendant>0) {
            // Count amount of characters in the currect page.
            // System.out.println(">>>> " + pageCounter + ": " + new String(ch, index, len) + " (" + len + " bytes)");
            currentPage.characters += len;
        }
    }

    // --------------- return the pagination information ----------------

    public boolean isInPage(int page, int item, String itemGroup) {
        return ((descendant==0) || valid(page, item, itemGroup));
    }

    public int getTotalPages() {
        return pageCounter;
    }

    public int getTotalItems(String itemGroup) {
        if (this.itemListsPerName==null) {
            return 0;
        }
        ItemList list = (ItemList) this.itemListsPerName.get(itemGroup);

        return (list==null) ? 0 : list.size();
    }

    public int getPageForItem(int item, String itemGroup) {
        if (this.itemListsPerName==null) {
            return 0;
        }
        ItemList list = (ItemList) this.itemListsPerName.get(itemGroup);

        return (list==null) ? 0 : list.getPageForItem(item);
    }

    public int itemCount(String elementURI, String elementName) {
        if (this.itemListsPerElement==null) {
            return 0;
        }
        ItemList list = (ItemList) this.itemListsPerElement.get(elementURI+
                            elementName);

        return (list==null) ? 0 : list.size();
    }

    public String getItemGroupName(String elementURI, String elementName) {
        if (this.itemListsPerElement==null) {
            return null;
        }
        return ((ItemGroup) this.itemGroupsPerElement.get(elementURI+
            elementName)).getName();
    }

    // ---------------- miscellaneous methods ----------------------------

    private boolean valid(int page, int item, String itemGroup) {
        if (item==0) {
            Page p = (Page) pages.get(page);

            return (p!=null) && (p.validInPage(elementCounter));
        } else {
            if (this.itemListsPerElement==null) {
                return false;
            }
            ItemList list = (ItemList) this.itemListsPerName.get(itemGroup);

            return (list!=null) && (list.valid(item));
        }
    }

    public PageRules getPageRules(int page) {
        PageRules p = (PageRules) pageRules.get(page);

        return (p!=null) ? p : (PageRules) pageRules.get(0);
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean modifiedSince(long date) {
        return (this.lastModified == 0 || date!=this.lastModified);
    }

    public Object clone() {
        return new Pagesheet(pageRules, itemGroupsPerName,
                             itemGroupsPerElement);
    }
}
