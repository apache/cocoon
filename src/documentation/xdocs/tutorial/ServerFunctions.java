/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package rmitest;

// import necessary packages from java.rmi
import java.rmi.Remote; 
import java.rmi.RemoteException; 


/**
 *
 * <p>
 * The <code>ServerFunctions</code> extends the <code>Remote</code>
 * interface and defines the methods that need to be implemented by
 * a RMI server application that wants to cooperate with the 
 * <code>RMIGenerator</code> of this package.
 *
 * @author <a href="mailto:Erwin.Hermans@cs.kuleuven.ac.be">Erwin Hermans</a>
 *         (Student Computer Science Department KULeuven, 2001-2002)
 * @version CVS $Id: ServerFunctions.java,v 1.3 2004/03/06 02:26:14 antonio Exp $
 */
public interface ServerFunctions extends Remote { 

	// meant to be able to query the server about its name
	/**
	 * This method returns a String, containing a well-formed XML fragment/document. 
	 * This String should contain information about the application implementing 
	 * this interface. Choosing what information exactly is put into this String is 
	 * left to the application designer.
	 */
	String sayHello() throws RemoteException; 

	/**
	 * This method returns a String, containing a well-formed XML fragment/document. 
	 * To determine the information that should be returned, a String is passed to 
	 * this method. This <code>src</code> attribuut of the <code>map:generate</code>
	 * element will be used to give this String a value.
	 *
	 * <b>Note:</b> This String can <strong>possibly</strong> be the <strong>empty
	 * string</strong>. The application designer should keep this in mind.
	 * 
	 */
	String getResource(String file) throws RemoteException;

}
