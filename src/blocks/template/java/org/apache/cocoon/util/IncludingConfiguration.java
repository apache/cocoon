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
package org.apache.cocoon.util;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

public class IncludingConfiguration extends DefaultConfiguration {
    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

    public IncludingConfiguration(Configuration conf, ServiceManager manager) {
        super(conf.getName());
        addAllAttributes(conf);
        addAllChildren(conf);
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
            Configuration[] confs = getChildren("include");
            for (int i = 0; i < confs.length; i++) {
                Source source = resolver.resolveURI(confs[i].getValue());
                addAllChildren(builder.build(source.getInputStream()));
                removeChild(confs[i]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (resolver != null)
                manager.release(resolver);
        }
    }

}