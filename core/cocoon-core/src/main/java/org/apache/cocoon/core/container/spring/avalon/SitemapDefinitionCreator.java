/*
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;


/**
 * @version $Id$
 * @since 2.2
 */
public class SitemapDefinitionCreator {

    public static String createDefinition(String uriPrefix) {
        final StringBuffer buffer = new StringBuffer();
        addHeader(buffer);
        // Settings
        // Avalon
        buffer.append("<avalon:sitemap uriPrefix=\"");
        buffer.append(uriPrefix);
        buffer.append("\"/>");
        addFooter(buffer);
        return buffer.toString();
    }

    protected static void addHeader(StringBuffer buffer) {
        buffer.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"");
        buffer.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        buffer.append(" xmlns:util=\"http://www.springframework.org/schema/util\"");
        buffer.append(" xmlns:cocoon=\"http://org.apache.cocoon/core\"");
        buffer.append(" xmlns:avalon=\"http://org.apache.cocoon/avalon\"");
        buffer.append(" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd");
        buffer.append(" http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd");
        buffer.append(" http://org.apache.cocoon/core http://org.apache.cocoon/core.xsd");
        buffer.append(" http://org.apache.cocoon/avalon http://org.apache.cocoon/avalon.xsd\">");
    }

    protected static void addFooter(StringBuffer buffer) {
        buffer.append("</beans>");
    }
}
