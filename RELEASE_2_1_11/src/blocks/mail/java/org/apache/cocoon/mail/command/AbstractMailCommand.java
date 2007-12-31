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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * An abstract MailCommand template
 *
 * @author Bernhard Huber
 * @since 23 October 2002
 * @version $Id$
 */
public abstract class AbstractMailCommand extends AbstractLogEnabled
                                          implements MailCommand {

    /**
     * List of result objects
     */
    private List result;


    /**
     *  Constructor for the AbstractMailCommand object
     */
    public AbstractMailCommand() {
        result = new ArrayList();
    }


    /**
     *  Gets the results attribute of the AbstractMailCommand object
     *
     *@return    The results value
     */
    public List getResults() {
        return result;
    }


    /**
     *  Adds a result to the Result attribute of the AbstractMailCommand object
     *
     *@param  o  The result to be added to the Result attribute
     */
    public void addResult(Object o) {
        result.add(o);
    }


    /**
     *  Adds a list of results to the Results attribute of the AbstractMailCommand object
     *
     *@param  list  The list of results to be added to the Results attribute
     */
    public void addResults(List list) {
        result.addAll(list);
    }


    /**
     *  Return an iterator over the results
     *
     *@return    result iterator
     */
    public Iterator iterator() {
        return result.iterator();
    }


    /**
     *  Execute this command
     *
     *@exception  MessagingException  thrown if executing of this MailCommand fails
     */
    public abstract void execute() throws MessagingException;
}
