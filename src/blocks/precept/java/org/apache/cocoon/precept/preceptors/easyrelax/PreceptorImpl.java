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
package org.apache.cocoon.precept.preceptors.easyrelax;

import org.apache.cocoon.precept.*;

import org.apache.cocoon.precept.preceptors.AbstractPreceptor;


import java.util.*;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 14, 2002
 * @version CVS $Id: PreceptorImpl.java,v 1.2 2003/03/16 17:49:05 vgritsenko Exp $
 */
public class PreceptorImpl extends AbstractPreceptor {
    HashMap index = new HashMap();


    public Collection validate(Instance instance, String xpath, Context context) throws InvalidXPathSyntaxException, NoSuchNodeException {
        Collection violations = null;
        Collection constraints = (Collection) getConstraintsFor(xpath);
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
