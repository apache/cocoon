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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.CascadingException;

/**
 * This exception is thrown by a <code>URIMatcher</code> or by a
 * <code>URITranslator</code> when there's something wrong with the matching or
 * translation patterns.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PatternException.java,v 1.2 2004/03/08 14:03:30 cziegeler Exp $
 */
public class PatternException extends CascadingException {

    /**
     * Construct a new <code>PatternException</code> instance.
     */
    public PatternException(String message) {
        super(message, null);
    }

    /**
     * Creates a new <code>PatternException</code> instance.
     *
     * @param ex an <code>Exception</code> value
     */
    public PatternException(Exception ex) {
        super(ex.getMessage(), ex);
    }

    /**
     * Construct a new <code>PatternException</code> that references
     * a parent Exception.
     */
    public PatternException(String message, Throwable t) {
        super(message, t);
    }
}
