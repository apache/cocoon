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
package org.apache.cocoon.template.v2.script;

import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.template.v2.tag.Tag;

public class TagRepository extends AbstractLogEnabled implements Configurable {
    HashMap tagMap = new HashMap();

    public void configure(Configuration conf) throws ConfigurationException {
        configureTags(conf.getChildren("tag"));
    }

    public void configureTags(Configuration[] confs)
            throws ConfigurationException {
        for (int i = 0; i < confs.length; i++) {
            registerTag(confs[i].getAttribute("namespace"), confs[i]
                    .getAttribute("name"), confs[i].getAttribute("src"));
        }
    }

    public void registerTag(String namespace, String name, String src) {
        try {
            registerTag(namespace, name, Class.forName(src));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found", e);
        }

    }

    public void registerTag(String namespace, String name, Class clazz) {
        tagMap.put(createKey(namespace, name), clazz);
        if (getLogger().isInfoEnabled())
            getLogger().info("Registered tag: namespace=" + namespace + ", name="
                    + name + ", src=" + clazz.getName());
    }

    public Tag getTag(String namespace, String name) {
        try {
            Class type = (Class) tagMap.get(createKey(namespace, name));
            return (Tag) type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(String namespace, String name) {
        return tagMap.containsKey(createKey(namespace, name));
    }

    protected String createKey(String namespace, String name) {
        return namespace + " " + name;
    }
}