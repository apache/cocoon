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
package org.apache.cocoon.xml;

/**
 * This interfaces identifies classes that consume XML data, receiving
 * notification of SAX events.
 * <br>
 * This interface unites the idea of SAX <code>ContentHandler</code> and
 * <code>LexicalHandler</code>.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: XMLConsumer.java,v 1.3 2004/03/08 14:04:00 cziegeler Exp $
 */
public interface XMLConsumer extends org.apache.excalibur.xml.sax.XMLConsumer {
}
