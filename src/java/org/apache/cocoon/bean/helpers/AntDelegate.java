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
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: AntDelegate.java,v 1.2 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public class AntDelegate {

    public static int process(Document xconf, String uriGroup) throws Exception {
        CocoonBean cocoon = new CocoonBean();
        OutputStreamListener listener = new OutputStreamListener(System.out);
        cocoon.addListener(listener);
        BeanConfigurator.configure(xconf, cocoon, "", uriGroup, listener);

        System.out.println(CocoonBean.getProlog());

        cocoon.initialize();
        cocoon.process();
        cocoon.dispose();

        listener.complete();
        return listener.isSuccessful() ? 0 : 1;
    }
}

