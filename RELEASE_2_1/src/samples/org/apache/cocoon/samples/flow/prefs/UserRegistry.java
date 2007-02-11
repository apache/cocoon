/*
    UserRegistry.java

    Maintains a list of registered users.

    Author: Ovidiu Predescu <ovidiu@apache.org>
    Date: August 28, 2002

 */

package org.apache.cocoon.samples.flow.prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a list of registered users. This is a very simple class,
 * there is no persistence of the users, but such thing should be easy
 * to add.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since August 28, 2002
 * @version CVS $Id: UserRegistry.java,v 1.2 2003/03/16 18:03:53 vgritsenko Exp $
 */
public class UserRegistry
{
  static UserRegistry userRegistry = new UserRegistry();

  Map registeredUsers = new HashMap();

  public static UserRegistry getUserRegistry()
  {
    return userRegistry;
  }

  protected UserRegistry()
  {
  }

  public synchronized boolean addUser(User user)
  {
    if (registeredUsers.containsKey(user.getLogin()))
      return false;

    registeredUsers.put(user.getLogin(), user);
    return true;
  }

  public boolean removeUser(User user)
  {
    return registeredUsers.remove(user) != null;
  }

  /**
   * Checks is a particular login name is taken or not.
   *
   * @param loginName a <code>String</code> value
   * @return true if <code>loginName</code> is taken, false otherwise
   */
  public boolean isLoginNameTaken(String loginName)
  {
    return registeredUsers.get(loginName) != null;
  }

  /**
   * Returns the {@link User} object which represents an user. Note that
   * we require a password to be present, to avoid presenting private
   * information to anyone.
   *
   * @param loginName a <code>String</code> value
   * @param password a <code>String</code> value
   * @return an <code>User</code> value
   */
  public User getUserWithLogin(String loginName, String password)
  {
    User user = (User)registeredUsers.get(loginName);

    if (user == null)
      return null;

    return password.equals(user.getPassword()) ? user : null;
  }
}
