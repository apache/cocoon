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
package org.apache.cocoon.components.flow.apples;

import org.apache.cocoon.ProcessingException;

/**
 * A special version of ApplesProcessor that interprets the parameter passed to
 * instantiateController as service/bean name instead of classname. The class is
 * probably most useful with spring container integration.
 *
 * Declare your flow in sitemap as &lt;map:flow language=&quot;service-apples&quot/&gt>
 * Define your AppleController beans in block/config/spring/ and call them from
 * sitemap by &lt;map:call function=&quot;beanName&quot/&gt>
 *
 * Please remember to declare your StatelessAppleControllers as singletons. If
 * you wish to use continuations beans have to be declared as non-singletons.
 *
 * You are of course free to use any container features in your beans like
 * dependency injection.
 *
 * @version $Id$
 */
public class ServiceApplesProcessor extends ApplesProcessor {
    protected AppleController instantiateController(String beanName) throws Exception {
        Object bean = this.manager.lookup(beanName);
        if (!(bean instanceof AppleController))
            throw new ProcessingException("The bean called is not a AppleController");
        return (AppleController) bean;
    }
}
