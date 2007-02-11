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
package org.apache.cocoon.components.flow.javascript.fom;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @version CVS $Id: PageLocalImpl.java,v 1.2 2004/01/28 16:07:17 vgritsenko Exp $
 */
public class PageLocalImpl extends ScriptableObject implements PageLocal {

    private PageLocalScope scope; // null if this is the prototype
    private String id;

    public PageLocalImpl() {
        this.id = String.valueOf(System.identityHashCode(this));
    }

    public void setPageLocalScope(PageLocalScope scope) {
        this.scope = scope;
    }

    public Object getId() {
        return id;
    }

    public String getClassName() {
        return "PageLocal";
    }

    public boolean has(String name, Scriptable start) {
        if (scope == null) {
            return super.has(name, start);
        }
        return scope.has(this, name);
    }

    public boolean has(int index, Scriptable start) {
        if (scope == null) {
            return super.has(index, start);
        }
        return scope.has(this, index);
    }

    public void put(String name, Scriptable start, Object value) {
        if (scope == null) {
             super.put(name, start, value);
        } else {
            scope.put(this, name, value);
        }
    }

    public void put(int index, Scriptable start, Object value) {
        if (scope == null) {
             super.put(index, start, value);
        } else {
            scope.put(this, index, value);
        }
    }

    public Object get(String name, Scriptable start) {
        if (scope == null) {
            return super.get(name, start);
        }
        return scope.get(this, name);
    }

    public Object get(int index, Scriptable start) {
        if (scope == null) {
            return super.get(index, start);
        }
        return scope.get(this, index);
    }

    public void delete(int index) {
        if (scope == null) {
            super.delete(index);
        } else {
            scope.delete(this, index);
        }
    }

    public void delete(String name) {
        if (scope == null) {
            super.delete(name);
        } else {
            scope.delete(this, name);
        }
    }

    public Object[] getIds() {
        if (scope == null) {
            return super.getIds();
        }
        return scope.getIds(this);
    }

    public Object getDefaultValue(Class hint) {
        if (scope == null) {
            return super.getDefaultValue(hint);
        }
        return scope.getDefaultValue(this, hint);
    }

}
