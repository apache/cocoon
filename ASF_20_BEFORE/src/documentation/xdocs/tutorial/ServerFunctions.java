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
 * @version CVS $Id: ServerFunctions.java,v 1.2 2003/03/16 18:03:53 vgritsenko Exp $
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
