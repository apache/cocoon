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
 * @version CVS $Id: PageLocalScopeHolder.java,v 1.1 2004/02/20 18:53:46 sylvain Exp $
 */
public class PageLocalScopeHolder implements PageLocalScope {

    private Scriptable scope;
    private PageLocalScopeImpl delegate;

    public PageLocalScopeHolder(Scriptable scope) {
        this.scope = scope;
    }

    public boolean has(PageLocal local, String name) {
        return delegate.has(local, name);
    }

    public boolean has(PageLocal local, int index) {
        return delegate.has(local, index);
    }

    public Object get(PageLocal local, String name) {
        return delegate.get(local, name);
    }

    public Object get(PageLocal local, int index) {
        return delegate.get(local, index);
    }

    public void put(PageLocal local, String name, Object value) {
        delegate.put(local, name, value);
    }

    public void put(PageLocal local, int index, Object value) {
        delegate.put(local, index, value);
    }

    public void delete(PageLocal local, String name) {
        delegate.delete(local, name);
    }

    public void delete(PageLocal local, int index) {
        delegate.delete(local, index);
    }

    public Object[] getIds(PageLocal local) {
        return delegate.getIds(local);
    }

    public Object getDefaultValue(PageLocal local, Class hint) {
        return delegate.getDefaultValue(local, hint);
    }

    public void setDelegate(PageLocalScopeImpl delegate) {
        this.delegate = delegate;
    }

    public PageLocalScopeImpl getDelegate() {
        return delegate;
    }

    public PageLocal createPageLocal() {
        PageLocalImpl pageLocal = new PageLocalImpl();
        pageLocal.setPrototype(ScriptableObject.getClassPrototype(scope,
                                                                  pageLocal.getClassName()));
        pageLocal.setParentScope(scope);
        pageLocal.setPageLocalScope(this);
        return pageLocal;
    }
}
