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

import javax.mail.internet.MimePart;

/**
 *  Description of the Interface
 *
 * @author Bernhard Huber
 * @since 26 October 2002
 * @version CVS $Id: ContentTypePreference.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public interface ContentTypePreference {

    /**
     *  Description of the Method
     *
     *@param  mimepart  Description of the Parameter
     *@return           Description of the Return Value
     */
    public int preference(MimePart mimepart);
}


