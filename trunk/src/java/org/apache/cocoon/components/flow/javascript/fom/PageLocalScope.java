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
package org.apache.cocoon.components.flow.javascript.fom;

/**
 * @version CVS $Id: PageLocalScope.java,v 1.2 2004/03/08 13:57:39 cziegeler Exp $
 */
public interface PageLocalScope {

    public boolean has(PageLocal local, String name);

    public boolean has(PageLocal local, int index);

    public Object get(PageLocal local, String name);

    public Object get(PageLocal local, int index);

    public void put(PageLocal local, String name, Object value);

    public void put(PageLocal local, int index, Object value);

    public void delete(PageLocal local, String name);

    public void delete(PageLocal local, int index);

    public Object[] getIds(PageLocal local);

    public Object getDefaultValue(PageLocal local, Class hint);

    public PageLocal createPageLocal();
}
