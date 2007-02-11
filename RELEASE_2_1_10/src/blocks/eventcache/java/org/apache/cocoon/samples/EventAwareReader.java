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
package org.apache.cocoon.samples;

import java.io.IOException;
import java.io.Serializable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.reading.ResourceReader;
import org.apache.excalibur.source.SourceValidity;

/**
 * @author Max Pfingsthorn (mpfingsthorn@hippo.nl)
 *
 */
public class EventAwareReader extends ResourceReader {

	public void generate() throws IOException, ProcessingException {
		try {
			long DELAY_SECS = this.parameters.getParameterAsLong("DELAY_SECS", 2);
			Thread.sleep(DELAY_SECS * 1000L);
        } catch (InterruptedException ie) {
          // Not much that can be done...
        }
		super.generate();
	}
	
	public Serializable getKey() {
        final Request request = ObjectModelHelper.getRequest(this.objectModel);
        // for our test, pages having the same value of "pageKey" will share
        // the same cache location
        String key = request.getParameter("pageKey") ;
        return ((key==null||"".equals(key)) ? "foo" : key);
    }
	
	public SourceValidity getValidity() {
        final Request request = ObjectModelHelper.getRequest(this.objectModel);
        String key = request.getParameter("pageKey") ;
        return new EventValidity(
                   new NamedEvent(
                       (key==null||"".equals(key)) ? "foo" : key));
    }

}
