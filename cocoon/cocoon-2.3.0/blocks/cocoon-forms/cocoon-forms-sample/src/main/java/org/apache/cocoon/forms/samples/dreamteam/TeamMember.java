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
package org.apache.cocoon.forms.samples.dreamteam;

/**
 * TeamMember
 */
public class TeamMember {
    private String memberId = null;
    private String name = null;
    private String position = null;
    private String country = null;

    public TeamMember() {
        super();
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberID) {
        this.memberId = memberID;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        if (name == null) {
            return "Name not set!";
        }
        String lastName = name.substring(name.indexOf(" ") + 1);
        if (lastName.length() == 0) {
            lastName = name;
        }
        return lastName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String profession) {
        this.position = profession;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String toString() {
        String result = "<member id='" + memberId + "'><name>" + name
                + "</name><position>" + position + "</position><country>"
                + country + "</country></member>";
        return result;
    }

}
