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

package org.apache.cocoon.forms.formmodel;

import junit.framework.Assert;

import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.forms.datatype.typeimpl.IntegerType;

/**
 * Test case for CForm's group widget and inheritance
 *
 * @version $Id$
 */

public class GroupTestCase extends ContainerTestCase {

    /**
     * checks for correct inheritance
     */
    public void testInheritance() throws Exception {
        Form form = WidgetTestHelper.loadForm(getManager(), this, "GroupTestCase.model.xml");
        Field field1 = (Field)((Group)form.getChild("group2")).getChild("field1");
        Field field2 = (Field)((Group)form.getChild("group2")).getChild("field2");
        Field field3 = (Field)((Group)form.getChild("group2")).getChild("field3");
        Group group2 = (Group)form.getChild("group2");
        
        Assert.assertNotNull("Inherited field present", field1 );
        Assert.assertNotNull("Added field present", field2 );
        Assert.assertNotNull("Internally inherited field present", field3 );
        
        // check datatype
        Assert.assertTrue("Datatype of internally inherited field", field3.getDatatype() instanceof IntegerType);
    }
}
