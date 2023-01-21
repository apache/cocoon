/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;

import org.apache.commons.lang.ObjectUtils;

/**
 * Utility class for handling {...} pattern substitutions in sitemap statements.
 *
 * @version $Id$
 */
public abstract class VariableResolver {

    public final static String ROLE = VariableResolver.class.getName();

    protected String originalExpr;


    protected VariableResolver() {
    }
    
    protected VariableResolver(String expr) {
        this.originalExpr = expr;
    }

    public abstract void setExpression(String expression) throws PatternException;

    public final String toString() {
        return this.originalExpr;
    }

    /**
     * Compare two VariableResolvers
     */
    public boolean equals(Object object) {
        //noinspection SimplifiableIfStatement
        if (!(object instanceof VariableResolver)) {
            return false;
        }

        return ObjectUtils.equals(this.originalExpr, ((VariableResolver) object).originalExpr);
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
        Location location;
        if (expressions instanceof Locatable) {
            location = ((Locatable) expressions).getLocation();
        } else {
            location = Location.UNKNOWN;
        }
        if (expressions == null || expressions.size() == 0 && location.equals(Location.UNKNOWN)) {
            return Parameters.EMPTY_PARAMETERS;
        }

        SitemapParameters result = new SitemapParameters(location);

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.setParameter(
                    ((VariableResolver) entry.getKey()).resolve(context, objectModel),
                    ((VariableResolver) entry.getValue()).resolve(context, objectModel)
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
            return Collections.EMPTY_MAP;
        }

        Map result;
        if (expressions instanceof Locatable) {
            result = new SitemapParameters.LocatedHashMap(((Locatable) expressions).getLocation(), size);
        } else {
            result = new HashMap(size);
        }

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            result.put(
                    ((VariableResolver) entry.getKey()).resolve(context, objectModel),
                    ((VariableResolver) entry.getValue()).resolve(context, objectModel)
            );
        }

        return result;
    }
}
