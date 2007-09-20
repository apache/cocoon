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
package org.apache.cocoon.mail.command;

import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;

/**
 * Execute a list of commands.
 *
 * @since 23 October 2002
 * @version $Id$
 */
public class MailCommands extends AbstractMailCommand {

    /**
     * the list of commands
     */
    private List commands;


    /**
     *  Constructor for the MailCommands object
     *
     *@param  commands  a list of AbstractMailCommand entries
     */
    public MailCommands(List commands) {
        this.commands = commands;
    }


    /**
     *  Execute the list of commands
     *  <p>
     *    Gather all results of all commands.
     *  </p>
     *
     *@exception  MessagingException  Description of the Exception
     */
    public void execute() throws MessagingException {
        Iterator i = commands.iterator();
        while (i.hasNext()) {
            AbstractMailCommand command = (AbstractMailCommand) i.next();
            command.execute();
            addResults(command.getResults());
        }
    }
}
