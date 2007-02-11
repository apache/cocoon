/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.cocoon.slop.interfaces;

/** General constants for the SLOP block
 *
 * @author <a href="mailto:bdelacretaz@apache.org">Bertrand Delacretaz</a>
 * @version CVS $Id: SlopConstants.java,v 1.3 2004/03/05 13:02:23 bdelacretaz Exp $
 */

public interface SlopConstants {
    String SLOP_NAMESPACE_URI = "http://apache.org/cocoon/slop/parser/1.0";

    // element names
    String SLOP_ROOT_ELEMENT = "parsed-text";
    String SLOP_LINE_ELEMENT = "line";
    String SLOP_EMPTY_LINE_ELEMENT = "empty-line";

    // attribute names
    String SLOP_ATTR_LINENUMBER = "line-number";
}
