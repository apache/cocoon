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
package org.apache.cocoon.xml;

import org.w3c.dom.Node;

/**
 * This interface must be implemented by classes willing
 * to provide an XML representation of their current state.
 *
 * <p>This interface exists in both Cocoon 1 and Cocoon 2 and to ensure
 * a minimal compatibility between the two versions.</p>
 *
 * <p>Cocoon 2 only objects can implement the SAX-only <code>XMLizable</code>
 * interface.</p>
 *
 * @author <a href="mailto:sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a> for the original XObject class
 * @version CVS $Id$
 */
public interface XMLFragment extends org.apache.excalibur.xml.sax.XMLizable {

    /**
     * Appends children representing the object's state to the given node.
     */
    void toDOM(Node node) throws Exception;
}
