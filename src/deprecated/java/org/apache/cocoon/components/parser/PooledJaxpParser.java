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
package org.apache.cocoon.components.parser;


/**
 * @deprecated <code>PooledJaxpParser</code> is now the default parser for Cocoon
 *             and has thus been moved to {@link JaxpParser} - this class will be
 *             removed in a future release.
 *             The Avalon XML Parser is now used inside Cocoon. This role
 *             will be removed in future releases.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PooledJaxpParser.java,v 1.2 2004/03/05 13:02:39 bdelacretaz Exp $
 */
public class PooledJaxpParser extends JaxpParser {

}
