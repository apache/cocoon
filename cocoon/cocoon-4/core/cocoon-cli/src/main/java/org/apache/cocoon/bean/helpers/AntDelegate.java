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

package org.apache.cocoon.bean.helpers;

import org.w3c.dom.Document;
import org.apache.cocoon.bean.CocoonBean;
import org.apache.cocoon.bean.helpers.OutputStreamListener;
import org.apache.cocoon.bean.helpers.BeanConfigurator;

/**
 * Delegate class for use by the Cocoon Ant task. Allows Ant to run
 * Cocoon with a single method call that can happily be started with 
 * introspection (due to classpath issues).
 *  
 * @version $Id$
 */
public class AntDelegate {

    public static int process(Document xconf, String uriGroup) throws Exception {
        CocoonBean cocoon = new CocoonBean();
        OutputStreamListener listener = new OutputStreamListener(System.out);
        cocoon.addListener(listener);
        BeanConfigurator.configure(xconf, cocoon, "", uriGroup, listener);

        System.out.println(CocoonBean.getProlog());

        if (!cocoon.isPrecompileOnly() && cocoon.getTargetCount() ==0) {
            listener.messageGenerated("Please, specify at least one starting URI.");
            System.exit(1);
        }

        cocoon.initialize();
        cocoon.process();
        cocoon.dispose();

        listener.complete();
        return listener.isSuccessful() ? 0 : 1;
    }
}

