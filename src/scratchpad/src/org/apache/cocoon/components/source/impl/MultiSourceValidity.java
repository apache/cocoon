/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

/**
 * An aggregated validity for multiple sources.
 * 
 * @author <a href="http://www.apache.org/~sylvain">Sylvain Wallez</a>
 * @version CVS $Id: MultiSourceValidity.java,v 1.1 2003/08/01 17:21:15 sylvain Exp $
 */
public class MultiSourceValidity implements SourceValidity{

    private long expiry;
    private long delay;
    private List data = new ArrayList();
    private boolean isClosed = false;
    
    /** SourceResolver. Transient in order not to be serialized */
    private transient SourceResolver resolver;
    
    public MultiSourceValidity(SourceResolver resolver, long delay) {
        this.resolver = resolver;
        this.expiry = System.currentTimeMillis() + delay;
        this.delay = delay;
    }
    
    public void addSource(Source src) {
        if (this.data != null) {
            SourceValidity validity = src.getValidity();
            if (validity == null) {
                // one of the sources has no validity : this object will always be invalid
                this.data = null;
            } else {
                // Add the validity and URI to the list
                this.data.add(validity);
                this.data.add(src.getURI());
            }
        }
    }
    
    public void close() {
        this.isClosed = true;
        this.resolver = null;
    }
    
    public int isValid() {
        if (System.currentTimeMillis() <= expiry) {
            return 1;
        }
        expiry = System.currentTimeMillis() + delay;
        
        if (data == null || !isClosed) {
            return -1;
        } else {
            return computeStatus(null);
        }
    }

    public int isValid(SourceValidity newValidity) {
        if (System.currentTimeMillis() <= expiry) {
            return 1;
        }
        expiry = System.currentTimeMillis() + delay;

        if (data == null || !isClosed) {
            return -1;
        }
        
        if (newValidity instanceof MultiSourceValidity) {
            return computeStatus(((MultiSourceValidity)newValidity).resolver);
        } else {
            // Don't know
            return -1;
        }
    }
    
    private int computeStatus(SourceResolver resolver) {
        for (int i = 0; i < data.size(); i+=2) {
            SourceValidity validity = (SourceValidity)data.get(i);
            switch(validity.isValid()) {
                case -1:
                    // invalid : stop examining
                    return -1;
                case 1:
                    // valid : just continue to next source
                    break;
                case 0:
                    // don't know : check with the new source
                    if (resolver == null) {
                        // we have no resolver : definitely don't know (need to have one)
                        return 0;
                    }
                    try {
                        Source newSrc = resolver.resolveURI((String)data.get(i+1));
                        int value = validity.isValid(newSrc.getValidity());
                        resolver.release(newSrc);
                        if (value != 1) {
                            return -1;
                        }
                    } catch(IOException ioe) {
                        return -1;
                    }
            }
        }
        
        // All items checked successfully
        return 1;
    }
}
