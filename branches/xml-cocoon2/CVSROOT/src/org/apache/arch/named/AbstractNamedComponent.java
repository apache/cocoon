/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.named;

import java.util.Hashtable;

import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;

// FIXME: This depends on Cocoon code!!!
import org.apache.cocoon.Parameters;

/**
 * Base implementation of <code>NamedComponent</code>.
 * This class preprocesses configuration information in order to simplify
 * initialization of derived classes.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:50 $
 */
public abstract class AbstractNamedComponent
  implements NamedComponent, Configurable
{
  /**
   * Map <i>parameter</i> configuration values to <code>Parameters</code>.
   * Further "regular" configuration processing is still possible by means
   * of the <code>setAdditionalConfiguration</code> method.
   *
   * @param conf Sitemap-supplied configuration information
   * @exception ConfigurationException If an error occurs
   */
  public void setConfiguration(Configuration conf)
    throws ConfigurationException
  {
    Parameters params = Parameters.fromConfiguration(conf);

    try {
      this.setParameters(params);
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage(), conf);
    }

    this.setAdditionalConfiguration(conf);
  }

  /**
   * Process basic configuration information. Subclasses must implement this
   * method to process configuration information supplied as <i>parameter</i>
   * values.
   *
   * @param params Parameter values
   * @exception Exception If an error occurs during processing
   */
  protected abstract void setParameters(Parameters params) throws Exception;

  /**
   * Further process configuration information. This empty method can be
   * overriden by derived classes when configuration information other than
   * <i>parameter</i> values is supplied through the sitemap configuration
   * file.
   *
   * @param conf The additional configuration
   * @exception ConfigurationException If an error occurs
   */
  protected void setAdditionalConfiguration(Configuration conf)
    throws ConfigurationException
  {
  }

  /**
   * Get a named parameter throwing an exception if not found. This
   * method ensures a required parameter has a value
   *
   * @param PARAM_NAME Param description
   * return the value
   * @exception EXCEPTION_NAME If an error occurs
   * @return The named parameter as a <code>String</code>
   */
  protected static String getRequiredParameter(Parameters params, String name)
    throws IllegalArgumentException
  {
    String value = params.getParameter(name, null);

    if (value == null) {
      throw new IllegalArgumentException(
        "Missing required parameter '" + name + "'"
      );
    }

    return value;
  }
}

