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
package org.apache.cocoon.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimePart;

/**
 *  Description of the Class
 *
 * @author Bernhard Huber
 * @since 26 October 2002
 * @version CVS $Id: IndexerCtPref.java,v 1.4 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class IndexerCtPref implements ContentTypePreference {
    /**
     *  Description of the Method
     *
     *@param  part  Description of the Parameter
     *@return       Description of the Return Value
     */
    public int preference(MimePart part) {
        try {
            if (part.isMimeType("text/html")) {
                return 5;
            }
            if (part.isMimeType("text/*")) {
                return 10;
            }
            if (part.isMimeType("text")) {
                return 9;
            }
            return 0;
        } catch (MessagingException messagingexception) {
            return 0;
        }
    }
}


