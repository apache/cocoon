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
package org.apache.cocoon.precept;

import java.util.Collection;

import org.apache.avalon.framework.component.Component;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 15, 2002
 * @version CVS $Id: Instance.java,v 1.4 2004/03/05 13:02:18 bdelacretaz Exp $
 */
public interface Instance extends Component {
    public String ROLE = "org.apache.cocoon.precept.Instance";

    public void setValue(String xpath, Object value) throws PreceptorViolationException, InvalidXPathSyntaxException;

    public void setValue(String xpath, Object value, Context context) throws PreceptorViolationException, InvalidXPathSyntaxException;

    public Object getValue(String xpath) throws InvalidXPathSyntaxException, NoSuchNodeException;

    public Collection getNodePaths();

    public void setPreceptor(Preceptor preceptor);

    public Preceptor getPreceptor();

    public void toSAX(ContentHandler handler, boolean constraints) throws SAXException;

    public long getLastModified();
}
