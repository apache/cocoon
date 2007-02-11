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
package org.apache.cocoon.samples.flow.java;

import java.util.Date;

import org.apache.cocoon.components.flow.java.AbstractContinuable;
import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.cocoon.forms.binding.BindingException;
import org.apache.cocoon.forms.flow.java.FormInstance;
import org.apache.cocoon.forms.formmodel.BooleanField;
import org.apache.cocoon.forms.formmodel.Field;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.samples.Contact;
import org.apache.cocoon.forms.samples.Form2Bean;
import org.apache.cocoon.forms.samples.Sex;

public class FormFlow extends AbstractContinuable {

    public void doEditForm1() {

        FormInstance form = new FormInstance("forms/form1.xml");

        Field birthDate = (Field) form.getChild("birthdate");
        birthDate.setValue(new Date());

        Repeater repeater = (Repeater) form.getChild("contacts");
        repeater.addRow();
        Field field = (Field) repeater.getWidget(0, "firstname");
        field.setValue("Jules");

        repeater.addRow();
        field = (Field) repeater.getWidget(1, "firstname");
        field.setValue("Lucien");

        form.show("form/form1");

        sendPage("page/form1-result", new VarMap().add("email", ((Field)form.getChild("email")).getValue())
                                                  .add("somebool", ((BooleanField)form.getChild("somebool")).getValue())
                                                  .add("firstname", ((Field)((Repeater)form.getChild("contacts")).getWidget(1, "firstname")).getValue()));
    }

    public void doEditForm2() throws BindingException {
        Form2Bean bean = new Form2Bean();

        // fill bean with some data to avoid users having to type to much
        bean.setEmail("yourname@yourdomain.com");
        bean.setIpAddress("10.0.0.1");
        bean.setPhoneCountry("32");
        bean.setPhoneZone("2");
        bean.setPhoneNumber("123456");
        bean.setBirthday(new java.util.Date());
        bean.setSex(Sex.FEMALE);
        Contact contact = new Contact();
        contact.setId(1);
        contact.setFirstName("Hermann");
        bean.addContact(contact);

        FormInstance form = new FormInstance("forms/form2.xml", "forms/form2-binding.xml");
        form.load(bean);
        form.show("form/form2");
        form.save(bean);
                                         
        sendPage("page/form2-result", new VarMap().add("form2bean", bean));
    }
}
