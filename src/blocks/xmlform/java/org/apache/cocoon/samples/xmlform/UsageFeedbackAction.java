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
package org.apache.cocoon.samples.xmlform;

import java.util.Map;
import org.apache.cocoon.acting.AbstractXMLFormAction;

/**
 * This action implements a REST web service
 *
 * @author Ivelin Ivanov <ivelin@apache.org>
 * @version CVS $Id: UsageFeedbackAction.java,v 1.3 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class UsageFeedbackAction
  extends AbstractXMLFormAction
{

  // Web Service Response names
  final String SERVICE_RESPONSE_OK = "ok";
  final String SERVICE_RESPONSE_ERROR = "error";


  public Map perform ()
  {

    // When form-view is not provided,
    // only data format validation is performed during population
    // but not consequetive data content validation (i.e. no Schematron validation)
    // Therefore, we will validate "manually"
    getForm().validate();
    
    if ( getForm().getViolations () != null )
    {
      return page( SERVICE_RESPONSE_ERROR );
    }
    else
    {
      return page( SERVICE_RESPONSE_OK );
    }

  }

}

