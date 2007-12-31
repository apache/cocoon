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
package org.apache.cocoon.faces.context;

import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;

/**
 * JSF Context Factory Implementation
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class FacesContextFactoryImpl extends FacesContextFactory {
    // private static final String FALLBACK_FACTORY = "com.sun.faces.context.FacesContextFactoryImpl";
    private static final String FALLBACK_FACTORY = "net.sourceforge.myfaces.context.FacesContextFactoryImpl";

    private FacesContextFactory fallback;


    public FacesContextFactoryImpl() {
        try {
            this.fallback =
                (FacesContextFactory) Class.forName(FALLBACK_FACTORY).newInstance();
        } catch (Exception ignored) {
        }
    }

    public FacesContext getFacesContext(Object context,
                                        Object request,
                                        Object response,
                                        Lifecycle lifecycle)
    throws FacesException {
        try {
            if (!(context instanceof Context)) {
                throw new FacesException("Context must be instance of " + Context.class.getName());
            }

            if (!(request instanceof Request)) {
                throw new FacesException("Request must be instance of " + Request.class.getName());
            }

            if (!(response instanceof Response)) {
                throw new FacesException("Response must be instance of " + Response.class.getName());
            }

            return new FacesContextImpl(new ExternalContextImpl((Context) context,
                                                                (Request) request,
                                                                (Response) response));
        } catch (FacesException e) {
            try {
                return this.fallback.getFacesContext(context, request, response, lifecycle);
            } catch (Exception ignored) {
                throw e;
            }
        }
    }
}
