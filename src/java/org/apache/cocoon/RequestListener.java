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

package org.apache.cocoon;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.environment.Environment;


/**
 * This is called from with {@link Cocoon#process(Environment)}, before any
 * requests are passed onto the {@link Processor} and after the request has been
 * processed. It allows all requests to be logged or monitored.
 * NB Cocoon does not require an instance of this Component to function, but if
 * there is one it will be used.
 */
public interface RequestListener extends Component {
    String ROLE = RequestListener.class.getName();

    /** 
     * <p>In this method you can call, for example:
     * <code>Request req=ObjectModelHelper.getRequest(env.getObjectModel());</code>
     * And then, you could use the following:
     * <ul>
     * <li>req.getRequestURI()</li>
     * <li>req.getQueryString()</li>
     * <li>req.getSession().getId()</li>
     * <li>req.getLocale().getLanguage().toString()</li>
     * </ul>
     * <p>
     * @param environment as supplied to {@link Processor#process(Environment)}
     *                    from within {@link Cocoon#process(Environment)}.
     */
    public void onRequestStart(Environment environment);

    /** 
     * <p>This method is called when a request has completed. This method is
     * called before the response is committed.
     * @param environment as supplied to {@link Processor#process(Environment)}
     *                    from within {@link Cocoon#process(Environment)}.
     */
    public void onRequestEnd(Environment environment);
    /** 
     * <p>This method is called when an exception has occurred processing the request.
     * @param environment as supplied to {@link Processor#process(Environment)}
     *                    from within {@link Cocoon#process(Environment)}.
     * @param throwable the error that occurred processing the request.
     */
    public void onRequestException(Environment environment, Throwable throwable);
}
