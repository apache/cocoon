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
package org.apache.cocoon.components.treeprocessor.variables;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for handling {...} pattern substitutions in sitemap statements.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: VariableResolver.java,v 1.4 2004/03/08 12:07:39 cziegeler Exp $
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

        SitemapParameters result = new SitemapParameters();
        if ( expressions instanceof SitemapParameters.ExtendedHashMap ) {
            result.setStatementLocation(((SitemapParameters.ExtendedHashMap)expressions).getLocation());    
        }
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

        Map result;
        if ( expressions instanceof SitemapParameters.ExtendedHashMap ) {
            Configuration config = ((SitemapParameters.ExtendedHashMap)expressions).getConfiguration();
            result = new SitemapParameters.ExtendedHashMap(config, size );   
        } else {
            result = new HashMap(size);
        }

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
