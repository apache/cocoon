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
package org.apache.cocoon.matching;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.sitemap.PatternException;

/**
 * A matcher that can prepare patterns during sitemap setup for faster match at request time.
 * This is also a regular matcher, meaning the sitemap can decide either to prepare the pattern
 * or to match with a request-time evaluated pattern (for {..} substitution).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PreparableMatcher.java,v 1.2 2004/01/05 08:17:30 cziegeler Exp $
 */
public interface PreparableMatcher extends Matcher {

    /**
     * Prepares a pattern in a form that allows faster match. For example, a regular
     * expression matcher can precompile the expression and return the corresponding
     * object. This method is called once for each pattern used with a particular matcher
     * class. The returned value is then passed back as the <code>preparedPattern</code>
     * parameter of {@link #preparedMatch(Object, Map, Parameters)}.
     *
     * @param pattern The pattern to prepare. Depending on the implementation the pattern
     *                can contain wildcards or regular expressions.
     * @return an optimized representation of the pattern.
     * @throws a <code>PatternException</code> if the pattern couldn't be prepared.
     */
    Object preparePattern(String pattern) throws PatternException;

    /**
     * Matches the prepared pattern against some values in the object model (most often the
     * <code>Request</code>) and returns a <code>Map</code> object with replacements
     * for wildcards contained in the pattern.
     *
     * @param preparedPattern The preparedPattern to match against, as returned by {@link #preparePattern(String)}.
     * @param objectModel     The <code>Map</code> with objects of the calling environment
     *                        which can be used to select values this matchers matches against.
     * @return                a <code>Map</code> object with replacements for wildcards/regular-expressions
     *                        contained in the pattern. If the return value is null there was no match.
     */
    Map preparedMatch(Object preparedPattern, Map objectModel, Parameters parameters);
}



