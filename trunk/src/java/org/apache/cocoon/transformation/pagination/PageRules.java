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

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for the immutable pagination rules for each page.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:bhtek@yahoo.com">Boon Hian Tek</a>
 * @version CVS $Id: PageRules.java,v 1.2 2004/03/08 14:03:31 cziegeler Exp $
 */
public class PageRules {

    public String elementName;
    public String elementURI;
    public int elementCount = 0;
    public int charCount = 0;
    public int unitLinks = 0;
    private List rangeLinks = new ArrayList();

    public boolean match(String element, String namespace) {
        boolean elementMatches = ((this.elementName!=null) &&
                                  this.elementName.equals(element));

        if (this.elementURI==null) {
            return elementMatches;
        } else {
            return elementMatches && this.elementURI.equals(namespace);
        }
    }

    public boolean match(String namespace) {
        return ((this.elementURI!=null) &&
                (this.elementURI.equals(namespace)));
    }

    public Integer[] getRangeLinks() {
        return (Integer[]) this.rangeLinks.toArray(new Integer[this.rangeLinks.size()]);
    }

    public void addRangeLink(Integer rangeLink) {
        this.rangeLinks.add(rangeLink);
    }

    public void addRangeLink(int rangeLink) {
        this.addRangeLink(new Integer(rangeLink));
    }

    public void addRangeLink(String rangeLink) {
        this.addRangeLink(new Integer(rangeLink));
    }
}
