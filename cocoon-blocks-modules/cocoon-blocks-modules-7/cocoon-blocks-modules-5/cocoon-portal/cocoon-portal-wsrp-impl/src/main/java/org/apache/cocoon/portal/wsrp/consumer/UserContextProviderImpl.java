/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.wsrp.consumer;

import oasis.names.tc.wsrp.v1.types.Contact;
import oasis.names.tc.wsrp.v1.types.EmployerInfo;
import oasis.names.tc.wsrp.v1.types.PersonName;
import oasis.names.tc.wsrp.v1.types.UserProfile;

import org.apache.wsrp4j.consumer.util.ConsumerConstants;
import org.apache.wsrp4j.util.Constants;

/**
 * This is the default implementation just returning an empty
 * user context.<br/>
 *
 * @version $Id$
 */
public class UserContextProviderImpl implements UserContextProvider {

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.UserContextProvider#createUserContext(java.lang.String)
     */
    public UserContextExtension createUserContext(String userId) {
        final UserContextExtension userContext = new UserContextExtension();

        userContext.setUserContextKey(userId);

        UserProfile userProfile = new UserProfile();
        this.fill(userProfile, userContext);

        PersonName personName = new PersonName();
        this.fill(personName, userContext);

        userProfile.setName(personName);
        userContext.setProfile(userProfile);

        userContext.setUserAuthentication(ConsumerConstants.PASSWORD);
        this.setSupportedLocales(userContext);
        return userContext;
    }

    /**
     * Sets the supportedLocales out of an individual location
     * This method can be overwritten in sub classes.<br/>
     *
     * @param userContext
     */
    protected void setSupportedLocales(UserContextExtension userContext) {
        String[] supportedLocales = new String[2];
        supportedLocales[0] = Constants.LOCALE_EN_US;
        supportedLocales[1] = Constants.LOCALE_DE_DE;
        userContext.setSupportedLocales(supportedLocales);
    }

    /**
     * Fill the user profile.<br/>
     * This method can be overwritten in sub classes.<br/
     *
     * @param profile
     * @param context
     */
    protected void fill(UserProfile profile, UserContextExtension context) {
        profile.setEmployerInfo(new EmployerInfo());
        profile.setHomeInfo(new Contact());
        profile.setBusinessInfo(new Contact());
    }

    /**
     * Fill the name.<br/>
     * This method can be overwritten in sub classes.<br/>
     *
     * @param name
     * @param context
     */
    protected void fill(PersonName name, UserContextExtension context) {
        name.setNickname(context.getUserContextKey());
    }
}
