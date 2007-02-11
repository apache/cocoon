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
package org.apache.cocoon.precept.preceptors.easyrelax;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Context;
import org.apache.cocoon.precept.Instance;
import org.apache.cocoon.precept.InvalidXPathSyntaxException;
import org.apache.cocoon.precept.NoSuchNodeException;
import org.apache.cocoon.precept.PreceptorViolationException;
import org.apache.cocoon.precept.preceptors.AbstractPreceptor;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 14, 2002
 * @version CVS $Id: PreceptorImpl.java,v 1.5 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class PreceptorImpl extends AbstractPreceptor {
    HashMap index = new HashMap();


    public Collection validate(Instance instance, String xpath, Context context) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Collection violations = null;
        Collection constraints = getConstraintsFor(xpath);
        if (constraints != null) {
            Object value = instance.getValue(xpath);
            for (Iterator it = constraints.iterator(); it.hasNext();) {
                Constraint constraint = (Constraint) it.next();
                if (!constraint.isSatisfiedBy(value, context)) {
                    if (violations == null) {
                        violations = new HashSet();
                    }
                    violations.add(constraint);
                }
            }
            return (violations);
        }
        else {
            return (null);
        }
    }

    public Collection validate(Instance instance, Context context) throws InvalidXPathSyntaxException {
        Collection allViolations = null;
        Collection xpaths = instance.getNodePaths();
        for (Iterator it = xpaths.iterator(); it.hasNext();) {
            String xpath = (String) it.next();
            try {
                Collection violations = validate(instance, xpath, context);
                if (violations != null) {
                    if (allViolations == null) {
                        allViolations = new HashSet();
                    }
                    allViolations.addAll(violations);
                }
            }
            catch (NoSuchNodeException e) {
                getLogger().error("hm.. the instance just told us about the nodes!");
            }
        }
        return (allViolations);
    }

    public Collection getConstraintsFor(String xpath) throws NoSuchNodeException {
        AbstractPreceptorNode node = (AbstractPreceptorNode) index.get(xpath);
        if (node != null) {
            List constraints = node.getConstraints();
            if (constraints != null) {
                getLogger().debug(constraints.size() + " constraints for [" + String.valueOf(xpath) + "]");
                return (constraints);
            }
            else {
                getLogger().debug("no constraints for [" + String.valueOf(xpath) + "]");
                return (null);
            }
        }
        else {
            throw new NoSuchNodeException(xpath);
        }
    }

    public void buildInstance(Instance instance) {
        try {
            for (Iterator it = index.keySet().iterator(); it.hasNext();) {
                String xpath = (String) it.next();
                AbstractPreceptorNode node = (AbstractPreceptorNode) index.get(xpath);
                if (node instanceof ElementPreceptorNode) {
                    for (int i = 0; i < ((ElementPreceptorNode) node).getMinOcc(); i++) {
                        String s = xpath;
                        if (i != 0) {
                            s += "[" + (i + 1) + "]";
                        }
                        getLogger().debug("building node [" + String.valueOf(s) + "]");
                        instance.setValue(s, "");
                    }
                }
                else {
                    getLogger().debug("building node [" + String.valueOf(xpath) + "]");
                    instance.setValue(xpath, "");
                }
            }
        }
        catch (InvalidXPathSyntaxException e) {
            getLogger().error("hm.. the preceptor should know how to build the instance!");
        }
        catch (PreceptorViolationException e) {
            getLogger().error("hm.. the preceptor should know how to build the instance!");
        }
    }

    public boolean isValidNode(String xpath) throws InvalidXPathSyntaxException {
        StringBuffer currentPath = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(xpath, "/", false);
        boolean first = true;
        while (tok.hasMoreTokens()) {
            String level = tok.nextToken();
            if (!first) {
                currentPath.append("/");
            }
            else {
                first = false;
            }

            if (level.startsWith("@")) {
                currentPath.append(level);
                AbstractPreceptorNode node = (AbstractPreceptorNode) index.get(currentPath.toString());
                if (node != null) {
                    getLogger().debug("found attribute node [" + String.valueOf(currentPath) + "] in index");
                    return (true);
                }
                else {
                    getLogger().debug("could not find attribute [" + String.valueOf(currentPath) + "] in index");
                    return (false);
                }
            }
            else {
                String levelName;
                int levelInt = 1;
                int open = level.indexOf("[");
                if (open > 0) {
                    int close = level.indexOf("]", open);
                    if (close > 0) {
                        try {
                            levelInt = Integer.parseInt(level.substring(open + 1, close));
                            levelName = level.substring(0, open);
                        }
                        catch (NumberFormatException e) {
                            getLogger().debug("invalid syntax [" + String.valueOf(level) + "]");
                            throw new InvalidXPathSyntaxException(level);
                        }
                    }
                    else {
                        getLogger().debug("invalid syntax [" + String.valueOf(level) + "]");
                        throw new InvalidXPathSyntaxException(level);
                    }
                }
                else {
                    levelName = level;
                }

                currentPath.append(levelName);
                AbstractPreceptorNode node = (AbstractPreceptorNode) index.get(currentPath.toString());
                if (node != null) {
                    getLogger().debug("found node [" + String.valueOf(currentPath) + "] in index");

                    if (node instanceof ElementPreceptorNode) {
                        if (((ElementPreceptorNode) node).getMaxOcc() != ElementPreceptorNode.UNBOUND && levelInt > ((ElementPreceptorNode) node).getMaxOcc()) {
                            getLogger().debug(String.valueOf(levelName) + "[" + levelInt + "] exceeds maximal occurrences [" + ((ElementPreceptorNode) node).getMaxOcc() + "]");
                            return (false);
                        }
                    }

                    if (!tok.hasMoreTokens()) return (true);
                }
                else {
                    getLogger().debug("could not find [" + String.valueOf(currentPath) + "] in index");
                    return (false);
                }
            }
        }
        return (false);
    }
}
