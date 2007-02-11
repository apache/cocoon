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
package org.apache.cocoon.precept.stores.bean.test;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Mar 20, 2002
 * @version CVS $Id: CocoonInstallationBean.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class CocoonInstallationBean {

    private UserBean user;
    private SystemBean system;
    private int number;
    private String live_url;
    private boolean publish;

    public CocoonInstallationBean() {
        user = new UserBean();
        system = new SystemBean();
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public SystemBean getSystem() {
        return system;
    }

    public void setSystem(SystemBean system) {
        this.system = system;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLive_url() {
        return live_url;
    }

    public void setLive_url(String live_url) {
        this.live_url = live_url;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

}
