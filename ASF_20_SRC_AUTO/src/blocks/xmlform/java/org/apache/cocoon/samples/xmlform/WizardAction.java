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

import org.apache.cocoon.acting.AbstractXMLFormAction;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.components.xmlform.FormListener;

import java.util.Map;

/**
 * This action demonstrates
 * a relatively complex form handling scenario.
 *
 * @author Ivelin Ivanov <ivelin@apache.org>
 * @version CVS $Id: WizardAction.java,v 1.3 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class WizardAction
  extends AbstractXMLFormAction
  implements FormListener
{

  // different form views
  // participating in the wizard
  final String VIEW_START = "start";
  final String VIEW_USERID = "userIdentity";
  final String VIEW_DEPLOYMENT = "deployment";
  final String VIEW_SYSTEM = "system";
  final String VIEW_CONFIRM = "confirm";
  final String VIEW_END = "end";

  // action commands used in the wizard
  final String CMD_START = "start";
  final String CMD_NEXT = "next";
  final String CMD_PREV = "prev";

  /**
   * The first callback method which is called
   * when an action is invoked.
   *
   * It is called before population and validation. 
   *
   *
   * @return null if the Action is prepared to continue - the normal case.
   * an objectModel map which will be immediately returned by the action.
   *
   * This method is a good place to handle buttons with Cancel
   * kind of semantics. For example
   * <pre>if getCommand().equals("Cancel") return page("input");</pre>
   *
   */
  protected Map prepare()
  { 

    // following is navigation logic for the GUI version
    if ( getCommand() == null )
      {
        // initial link
        return page( VIEW_START );
      }
    else if ( getCommand().equals( CMD_START ) )
    {
      // reset workflow state if necessary
    
      // remove old form      
      Form.remove( getObjectModel(), getFormId() );
      
      // create new form
      getForm();

      return page( VIEW_USERID );
    }
    // safe lookup, side effects free
    else if ( Form.lookup ( getObjectModel(), getFormId() ) == null)
      {
        // session expired
        return page( VIEW_START );
      }


    // nothing special
    // continue with form population;
    return super.PREPARE_RESULT_CONTINUE;
  }


  /**
   * Invoked after form population
   * 
   * Responsible for implementing the state machine 
   * of the flow control processing 
   * a single form page or a form wizard.
   *
   * Semanticly similar to Struts Action.perform()
   *
   * Take appropriate action based on the command
   *
   */
  public Map perform ()
  {

    // get the actual model which this Form encapsulates
    // and apply additional buziness logic to the model
    UserBean  jBean = (UserBean) getForm().getModel();
    jBean.incrementCount();

    // set the page flow control parameter
    // according to the validation result
    if ( getCommand().equals( CMD_NEXT ) &&
      getForm().getViolations () != null )
    {
      // errors, back to the same page
      return page( getFormView() );
    }
    else
    {
      // validation passed
      // continue with flow control

      // clear validation left overs in case the user
      // did not press the Next button
      getForm().clearViolations();

      // get the user submitted command (through a submit button)
      String command = getCommand();
      // get the form view which was submitted
      String formView = getFormView();

      // apply state machine (flow control) rules
      if ( formView.equals ( VIEW_USERID ) )
      {
        if ( command.equals( CMD_NEXT ) )
        {
          return page( VIEW_DEPLOYMENT );
        }
      }
      else if ( formView.equals ( VIEW_DEPLOYMENT ) )
      {
        if ( command.equals( CMD_NEXT ) )
        {
          return page( VIEW_SYSTEM );
        }
        else if( command.equals( CMD_PREV ) )
          return page( VIEW_USERID );
      }
      else if ( formView.equals ( VIEW_SYSTEM ) )
      {
        if ( command.equals( CMD_NEXT ) )
        {
          return page(  VIEW_CONFIRM );
        }
        else if( command.equals( CMD_PREV ) )
          return page( VIEW_DEPLOYMENT );
      }
      else if ( formView.equals ( VIEW_CONFIRM ) )
      {
        if ( command.equals( CMD_NEXT ) )
        {
          Form.remove( getObjectModel(), getFormId() );
          return page( VIEW_END );
        }
        else if( command.equals( CMD_PREV ) )
          return page( VIEW_SYSTEM );
      }
    }

    // should never reach this statement
    return page( VIEW_START );

  }





  /**
   *
   * FormListener callback
   * called in the beginning of Form.populate()
   * before population starts.
   *
   * This is the place to intialize the model for this request.
   * 
   * This method should not handle unchecked check boxes
   * when the form is session scope, which is the most common case.
   * It should only do so, if the form is request scoped.
   *
   */
  public void reset( Form form )
  {
    // nothing to do in this case
    // unchecked check boxes are handled by the framework !
    return;
  }


  /**
   * FormListener callback
   *
   * Invoked during Form.populate();
   *
   * It is invoked before a request parameter is mapped to
   * an attribute of the form model.
   *
   * It is appropriate to use this method for filtering
   * custom request parameters which do not reference
   * the model.
   *
   * Another appropriate use of this method is for graceful filtering of invalid
   * values, in case that knowledge of the system state or
   * other circumstainces make the standard validation
   * insufficient. For example if a registering user choses a username which
   * is already taken - the check requires database transaction, which is
   * beyond the scope of document validating schemas.
   * Of course customized Validators can be implemented to do
   * this kind of domain specific validation
   * instead of using this method.
   *
   *
   * @return false if the request parameter should not be filtered.
   * true otherwise.
   */
  public boolean filterRequestParameter (Form form, String parameterName)
  {
    // in this example we do not expect "custom" parameters
    return false;
  }



}

