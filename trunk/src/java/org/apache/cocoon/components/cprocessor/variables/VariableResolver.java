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
package org.apache.cocoon.components.cprocessor.variables;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for handling {...} pattern substitutions in sitemap statements.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: VariableResolver.java,v 1.1 2003/12/28 21:03:17 unico Exp $
 */
public abstract class VariableResolver {

    public static final Map EMPTY_MAP = Collections.unmodifiableMap(new java.util.HashMap(0));

    protected final String originalExpr;
    
    protected VariableResolver(String expr) {
        this.originalExpr = expr;
    }

    public final String toString() {
        return this.originalExpr;
    }

    /**
     * Compare two VariableResolvers
     */
    public boolean equals(Object object) {
        if (object instanceof VariableResolver) {
            VariableResolver other = (VariableResolver)object;
            return (this.originalExpr == null && other.originalExpr == null) ||
                   (this.originalExpr.equals(other.originalExpr));
        } else {
            return false;
        }
    }

    /**
     * generate HashCode
     * needed to determine uniqueness within hashtables
     */
    public int hashCode() {
        return this.originalExpr == null ? 0 : this.originalExpr.hashCode();
    }

    /**
     * Resolve all {...} patterns using the values given in the object model.
     */
    public String resolve(Map objectModel) throws PatternException {
        return resolve(null, objectModel);
    }

    /**
     * Resolve all {...} patterns using the values given in the list of maps and the object model.
     */
    public abstract String resolve(InvokeContext context, Map objectModel) throws PatternException;

    /**
     * Build a <code>Parameters</code> object from a Map of named <code>VariableResolver</code>s and
     * a list of Maps used for resolution.
     *
     * @return a fully resolved <code>Parameters</code>.
     */
    public static Parameters buildParameters(Map expressions, InvokeContext context, Map objectModel) throws PatternException {
        if (expressions == null || expressions.size() == 0) {
            return Parameters.EMPTY_PARAMETERS;
        }

        Parameters result = new Parameters();

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            result.setParameter(
                ((VariableResolver)entry.getKey()).resolve(context, objectModel),
                ((VariableResolver)entry.getValue()).resolve(context, objectModel)
            );
        }

        return result;
    }

    /**
     * Build a <code>Map</code> from a Map of named <code>ListOfMapResolver</code>s and
     * a list of Maps used for resolution.
     *
     * @return a fully resolved <code>Map</code>.
     */
    public static Map buildMap(Map expressions, InvokeContext context, Map objectModel) throws PatternException {
        int size;
        if (expressions == null || (size = expressions.size()) == 0) {
            return EMPTY_MAP;
        }

        Map result = new HashMap(size);

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            result.put(
                ((VariableResolver)entry.getKey()).resolve(context, objectModel),
                ((VariableResolver)entry.getValue()).resolve(context, objectModel)
            );
        }

        return result;
    }

//    /**
//     * Release a <code>Map</code> of expressions.
//     */
//    public static void release(Map expressions) {
//        Iterator iter = expressions.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry entry = (Map.Entry)iter.next();
//            ((VariableResolver)entry.getKey()).release();
//            ((VariableResolver)entry.getValue()).release();
//        }
//    }
}
