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

import org.apache.cocoon.components.flow.java.*;
import org.apache.cocoon.forms.flow.java.*;
import org.apache.cocoon.forms.formmodel.*;
import org.apache.cocoon.forms.binding.*; 
import org.apache.cocoon.forms.samples.*;
                 
import java.util.Date;

public class FormFlow extends AbstractFormFlow {

    public void doEditForm1() {

        Form form = loadForm("forms/form1.xml");

        Field birthDate = (Field) form.getWidget("birthdate");
        birthDate.setValue(new Date());

        Repeater repeater = (Repeater) form.getWidget("contacts");
        repeater.addRow();
        Field field = (Field) repeater.getWidget(0, "firstname");
        field.setValue("Jules");

        repeater.addRow();
        field = (Field) repeater.getWidget(1, "firstname");
        field.setValue("Lucien");

        getRequest().setAttribute("form1", form);
        showForm(form, "form/form1");

        sendPage("page/form1-result", new VarMap().add("email", ((Field)form.getWidget("email")).getValue())
						                                      .add("somebool", ((BooleanField)form.getWidget("somebool")).getValue())
																								  .add("firstname", ((Field)((Repeater)form.getWidget("contacts")).getWidget(1, "firstname")).getValue()));
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

        Form form = loadForm("forms/form2.xml");
        Binding binding = loadBinding("forms/form2-binding.xml");
        binding.loadFormFromModel(form, bean);
        showForm(form, "form/form2");
        binding.saveFormToModel(form, bean);
                                         
        sendPage("page/form2-result", new VarMap().add("form2bean", bean));
    }
}
