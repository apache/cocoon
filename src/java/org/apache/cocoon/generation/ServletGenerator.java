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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id: ServletGenerator.java,v 1.7 2004/03/08 14:02:43 cziegeler Exp $
 */
public abstract class ServletGenerator extends ServiceableGenerator {

    protected Request request;
    protected Response response;
    protected Context context;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, par);
        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
        this.context = ObjectModelHelper.getContext(objectModel);
    }

    /**
     * Recycle the generator by removing references
     */
    public void recycle() {
        super.recycle();
        this.request = null;
        this.response = null;
        this.context = null;
    }
}
