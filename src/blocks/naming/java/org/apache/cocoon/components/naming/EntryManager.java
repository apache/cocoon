package org.apache.cocoon.components.naming;

import org.apache.avalon.framework.component.*;
import javax.naming.directory.*;
import java.util.Map;
import org.apache.cocoon.ProcessingException;


/**
 *	The <code>EntryManager</code> is an Avalon Component for managing the Entries in a Javax Naming Directory.
 *  This is the interface implemented by {@link org.apache.cocoon.components.naming.LDAPEntryManager LDAPEntryManager}.
 *	@author Jeremy Quinn <a href="http://apache.org/~jeremy">http://apache.org/~jeremy</a>.
 */

public interface  EntryManager extends Component {
	String ROLE = EntryManager.class.getName();
	int ADD_ATTRIBUTE = DirContext.ADD_ATTRIBUTE; 
	int REMOVE_ATTRIBUTE = DirContext.REMOVE_ATTRIBUTE;
	int REPLACE_ATTRIBUTE = DirContext.REPLACE_ATTRIBUTE;
	
	public void create(String entry_name, Map entity_attributes) throws ProcessingException ;
	
	public Map get(String entry_name) throws ProcessingException;

	public Map find(Map match_attributes) throws ProcessingException;

	public Map find(String context, Map match_attributes) throws ProcessingException;
	
	public void modify(String entry_name, int mod_operand, Map mod_attributes) throws ProcessingException;
	
}

