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
package org.apache.cocoon.ojb.jdo.components;

import javax.jdo.PersistenceManager;

import org.apache.avalon.framework.component.Component;

/**
 *  Interface of the JDO Persistent Manager Factory.
 * It is used to get the Persistence Manager to interact with JDO using OJB
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: JdoPMF.java,v 1.3 2004/06/25 14:49:56 cziegeler Exp $
*/
public interface JdoPMF extends Component {
    
    String ROLE = JdoPMF.class.getName();
	
    /**
    * get a Persitence Manager.
	*/
    PersistenceManager getPersistenceManager();
}
