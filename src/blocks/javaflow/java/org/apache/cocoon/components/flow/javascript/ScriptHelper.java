/*
 * Created on 24.06.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.cocoon.components.flow.javascript;

import java.util.List;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;

/**
 * @author stephan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ScriptHelper extends Configurable, Initializable {
	
	public void register(String source);

	public void callFunction(String funName, List params) throws Exception;
}
