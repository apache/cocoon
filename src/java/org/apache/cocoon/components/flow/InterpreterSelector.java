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
package org.apache.cocoon.components.flow;

import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 *
 * @version CVS $Id: InterpreterSelector.java,v 1.3 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class InterpreterSelector extends ExcaliburComponentSelector
  implements Configurable, ThreadSafe
{
  private String defaultLanguage;

  public void configure(Configuration config)
    throws ConfigurationException
  {
    super.configure(config);

    defaultLanguage = config.getAttribute("default", null);

    // Finish the initialization of the already created components
    Configuration[] configurations = config.getChildren("component-instance");
    if (configurations.length == 0)
      throw new ConfigurationException("No languages defined!");

    for (int i = 0; i < configurations.length; i++) {
      Configuration conf = configurations[i];
      String hint = conf.getAttribute("name").trim();

      if (!this.getComponentHandlers().containsKey(hint)) {
        throw new ConfigurationException(
          "Could not find component for hint: " + hint
        );
      }

      if (i == 0 && defaultLanguage == null)
        defaultLanguage = hint;
    }
  }

  public String getDefaultLanguage()
  {
    return defaultLanguage;
  }
}
