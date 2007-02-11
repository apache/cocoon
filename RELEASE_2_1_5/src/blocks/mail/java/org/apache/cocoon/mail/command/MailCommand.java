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
package org.apache.cocoon.mail.command;

import javax.mail.MessagingException;

/**
 *  This interface the basic contract of a MailCommand
 *
 * @author Bernhard Huber
 * @since 23. Oktober 2002
 * @version CVS $Id: MailCommand.java,v 1.3 2004/03/05 13:02:00 bdelacretaz Exp $
 */
public interface MailCommand {
    /**
     *  Execute this MailCommand
     *
     * @exception  MessagingException  Description of the Exception
     */
    void execute() throws MessagingException;
}


