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
package org.apache.cocoon.forms.samples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Sample bean used in the form2 binding demo.
 * 
 * @version $Id: Form2Bean.java,v 1.1 2004/03/09 10:34:08 reinhard Exp $
 */
public class Form2Bean {
    private String email;

    private String phoneCountry;
    private String phoneZone;
    private String phoneNumber;

    private String ipAddress;
    private Date birthday;
    private int aNumber;
    private boolean choose;
    private Sex sex;

    private Collection contacts = new ArrayList();
    private Collection drinks = new ArrayList();

    /**
     * @return Returns the sex.
     */
    public Sex getSex() {
        return sex;
    }

    /**
     * @param sex The sex to set.
     */
    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Form2Bean() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneCountry() {
        return phoneCountry;
    }

    public void setPhoneCountry(String phoneCountry) {
        this.phoneCountry = phoneCountry;
    }

    public String getPhoneZone() {
        return phoneZone;
    }

    public void setPhoneZone(String phoneZone) {
        this.phoneZone = phoneZone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getaNumber() {
        return aNumber;
    }

    public void setaNumber(int aNumber) {
        this.aNumber = aNumber;
    }

    public boolean isChoose() {
        return choose;
    }

    public void setChoose(boolean choose) {
        this.choose = choose;
    }

    public Collection getDrinks() {
        return drinks;
    }

    public void setDrinks(Collection drinks) {
        this.drinks = drinks;
    }

    public void addDrink(String drink) {
        drinks.add(drink);
    }

    public Collection getContacts() {
        return contacts;
    }

    public void setContacts(Collection contacts) {
        this.contacts = contacts;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public String toString() {
        return "email = " + email + ", phoneCountry = " + phoneCountry + ", phoneZone = " + phoneZone + ", phoneNumber = " + phoneNumber + ", ipAddress = " + ipAddress + ", contacts = " + contacts.toString();
    }
}
