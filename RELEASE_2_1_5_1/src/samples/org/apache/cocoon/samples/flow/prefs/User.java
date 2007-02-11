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
/*
    User.java

    Representation of a user.

    Author: Ovidiu Predescu <ovidiu@apache.org>
    Date: August 28, 2002

 */
package org.apache.cocoon.samples.flow.prefs;

/**
 *
 * @version CVS $Id: User.java,v 1.3 2004/03/06 02:26:16 antonio Exp $
 */
public class User
{
  String login;
  String password;
  String firstName;
  String lastName;
  String email;

  public User(String login, String password,
              String firstName, String lastName, String email)
  {
    this.login = login;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public int hashCode()
  {
    return login.hashCode();
  }

  public boolean equals(Object obj)
  {
    User anotherUser = (User)obj;
    return anotherUser.login.equals(login);
  }
  
  /**
   * Sets the value of login
   *
   * @param argLogin Value to assign to this.login
   */
  public void setLogin(String argLogin)
  {
    this.login = argLogin;
  }
  /**
   * Gets the value of login
   *
   * @return the value of login
   */
  public String getLogin() 
  {
    return this.login;
  }

  /**
   * Gets the value of password
   *
   * @return the value of password
   */
  public String getPassword() 
  {
    return this.password;
  }

  /**
   * Sets the value of password
   *
   * @param argPassword Value to assign to this.password
   */
  public void setPassword(String argPassword)
  {
    this.password = argPassword;
  }

  /**
   * Gets the value of firstName
   *
   * @return the value of firstName
   */
  public String getFirstName() 
  {
    return this.firstName;
  }

  /**
   * Sets the value of firstName
   *
   * @param argFirstName Value to assign to this.firstName
   */
  public void setFirstName(String argFirstName)
  {
    this.firstName = argFirstName;
  }

  /**
   * Gets the value of lastName
   *
   * @return the value of lastName
   */
  public String getLastName() 
  {
    return this.lastName;
  }

  /**
   * Sets the value of lastName
   *
   * @param argLastName Value to assign to this.lastName
   */
  public void setLastName(String argLastName)
  {
    this.lastName = argLastName;
  }

  /**
   * Gets the value of email
   *
   * @return the value of email
   */
  public String getEmail() 
  {
    return this.email;
  }

  /**
   * Sets the value of email
   *
   * @param argEmail Value to assign to this.email
   */
  public void setEmail(String argEmail)
  {
    this.email = argEmail;
  }
}
