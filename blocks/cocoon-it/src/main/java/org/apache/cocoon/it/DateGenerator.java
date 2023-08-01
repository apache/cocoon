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
package org.apache.cocoon.it;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;

public class DateGenerator extends AbstractGenerator implements CacheableProcessingComponent {

    public void generate() throws IOException, SAXException, ProcessingException {
        this.contentHandler.startDocument();
        this.contentHandler.startElement("", "date", "date", new AttributesImpl());
        SimpleDateFormat format = new SimpleDateFormat("E, yyyy MMMM dd, hh:mm:ss.SSS, z");
        String now = format.format(new Date());
        this.contentHandler.characters(now.toCharArray(), 0, now.length());
        this.contentHandler.endElement("", "date", "date");
        this.contentHandler.endDocument();
    }

    public Serializable getKey() {
        return "1";
    }

    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

}
