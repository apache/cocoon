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
 *  A simple MimePart preference selecting algorithm
 *
 * @author Bernhard Huber
 * @since 26. Oktober 2002
 * @version CVS $Id: MailCtPref.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public class MailCtPref implements ContentTypePreference {

    /**
     *  Yield preference
     *
     *@param  part  Examine the content of this part
     *@return       preference, a higher preference value signals higher preference,
     *  eg. a part evaluating preference 10, signals preference over
     *    a part evaluating preference of 5
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

