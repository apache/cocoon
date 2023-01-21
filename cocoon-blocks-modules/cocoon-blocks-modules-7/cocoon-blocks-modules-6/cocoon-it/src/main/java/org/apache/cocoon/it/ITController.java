package org.apache.cocoon.it;

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
import java.util.List;

import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Redirector;

public class ITController implements Interpreter {

    public void callFunction(String funName, List params, Redirector redirector) throws Exception {
        redirector.sendStatus(201);
    }

    public void forwardTo(String uri, Object bizData, WebContinuation continuation, Redirector redirector)
            throws Exception {
    }

    public void handleContinuation(String continuationId, List params, Redirector redirector) throws Exception {
        redirector.sendStatus(202);
    }

    public String getInterpreterID() {
        return null;
    }

    public String getScriptExtension() {
        return null;
    }

    public void register(String source) {
    }

    public void setInterpreterID(String interpreterID) {
    }

}
