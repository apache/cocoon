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
package org.apache.cocoon.caching;

import org.apache.excalibur.source.Source;
import org.apache.cocoon.environment.SourceResolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A validation object used in CachingCIncludeTransformer
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:maciejka@tiger.com.pl">Maciek Kaminski</a>
 * @version CVS $Id: IncludeCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class IncludeCacheValidity implements CacheValidity {

    private List sources;
    private List timeStamps;

    private boolean isNew;

    private SourceResolver resolver;

    public IncludeCacheValidity(SourceResolver resolver) {
        this.resolver = resolver;
        sources = new ArrayList();
        timeStamps = new ArrayList();
        isNew = true;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew2False() {
        isNew = false;
        resolver = null;
    }

    public void add(String source, long timeStamp) {
        this.sources.add(source);
        this.timeStamps.add(new Long(timeStamp));
    }

    public boolean isValid(CacheValidity validity) {
        if (validity instanceof IncludeCacheValidity) {
            SourceResolver otherResolver = ((IncludeCacheValidity) validity).resolver;

            for(Iterator i = sources.iterator(), j = timeStamps.iterator(); i.hasNext();) {
                String src = ((String)i.next());
                long timeStamp = ((Long)j.next()).longValue();
                Source otherSource = null;
                try {
                    otherSource = otherResolver.resolveURI(src);
                    if(otherSource.getLastModified() != timeStamp ||
                        timeStamp == 0)
                        return false;
                } catch (Exception e) {
                    return false;
                } finally {
                    otherResolver.release(otherSource);
                }
            }
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer b = new StringBuffer("Include Validity[");
        for(Iterator i = sources.iterator(), j = timeStamps.iterator(); i.hasNext();) {
            b.append('{');
            b.append(i.next());
            b.append(':');
            b.append(j.next());
            b.append('}');
            if(i.hasNext()) b.append(':');
        }
        b.append(']');
        return b.toString();
    }
}
