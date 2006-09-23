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
package org.apache.cocoon.jcr;

import javax.jcr.Repository;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * @version $Id$
 */
public class JNDIRepository extends AbstractRepository {

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);

        Configuration rsrcConfig = config.getChild("jndi-resource");
        String name = rsrcConfig.getAttribute("name");

        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            this.delegate = (Repository) ctx.lookup(name);
        } catch (NamingException e) {
            throw new ConfigurationException("Cannot lookup JNDI entry '" + name + "'", e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignored) {
                    // Ignored
                }
            }
        }
    }

}
