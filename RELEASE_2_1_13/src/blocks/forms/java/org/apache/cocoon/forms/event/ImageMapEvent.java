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
package org.apache.cocoon.forms.event;

import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.ImageMap;

/**
 * Currently this event originates from a {@link org.apache.cocoon.forms.formmodel.ImageMap}
 * widget.
 * 
 */
public class ImageMapEvent extends ActionEvent {

    public ImageMapEvent(Widget source, String actionCommand) {
        super(source, actionCommand);
    }
    
    public int getX() {
    	return ((ImageMap)(source)).getX();
    }

    public int getY() {
    	return ((ImageMap)(source)).getY();
    }

}
