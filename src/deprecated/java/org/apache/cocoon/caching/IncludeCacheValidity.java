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
 * @version CVS $Id: IncludeCacheValidity.java,v 1.1 2003/03/09 00:06:55 pier Exp $
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
