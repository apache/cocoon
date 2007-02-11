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
package org.apache.cocoon.components.store;

import org.apache.avalon.framework.component.Component;
import java.util.Iterator;

/**
 * Interface for the StoreJanitors
 *
 * @deprecated Use the Avalon Excalibur Store instead.
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @version CVS $Id: StoreJanitor.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public interface StoreJanitor extends Component {

    String ROLE = "org.apache.cocoon.components.store.StoreJanitor";

    /** register method for the stores */
    void register(Store store);

    /** unregister method for the stores */
    void unregister(Store store);

    /** get an iterator to list registered stores */
    Iterator iterator();
}
