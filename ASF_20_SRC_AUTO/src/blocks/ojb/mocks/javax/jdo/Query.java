/*
 * Created on 08-oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package javax.jdo;

import java.util.Collection;
import java.util.Map;

/**
 * @author agallardo
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract public interface Query
{
	abstract public void close(Object o);
	abstract public void closeAll();
	abstract public void compile();
	abstract public void declareImports(String s);
	abstract public void declareParameters(String s);
	abstract public void declareVariables(String s);
	abstract public Object execute();
	abstract public Object execute(Object o);
	abstract public Object execute(Object o, Object p);
	abstract public Object execute(Object o, Object p, Object q);
	abstract public Object executeWithArray(Object[] o);
	abstract public Object executeWithMap(Map m);
	abstract public boolean getIgnoreCache();
	abstract public PersistenceManager getPersistentManager();
	abstract public void setIgnoreCandidates(Collection c);
	abstract public void setClass(Class c);
	abstract public void setFilter(String s);
	abstract public void setIgnoreCache(boolean b);
	abstract public void setOrdering(String s);
	
	
	
}
