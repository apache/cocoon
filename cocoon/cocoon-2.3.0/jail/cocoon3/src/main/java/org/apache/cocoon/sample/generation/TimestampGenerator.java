/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sample.generation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.sax.AbstractSAXGenerator;
import org.apache.cocoon.sax.SAXConsumer;
import org.apache.cocoon.xml.sax.ImmutableAttributesImpl;
import org.xml.sax.SAXException;

public class TimestampGenerator extends AbstractSAXGenerator {

    public void execute() {
        SAXConsumer consumer = this.getSAXConsumer();
        try {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            consumer.startDocument();
            consumer.startElement("", "timestamp", "timestamp", new ImmutableAttributesImpl());
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
            consumer.characters(timestamp.toCharArray(), 0, timestamp.length());
            consumer.endElement("", "timestamp", "timestamp");

            consumer.endDocument();
        } catch (SAXException e) {
            throw new ProcessingException(e);
        }
    }
}
