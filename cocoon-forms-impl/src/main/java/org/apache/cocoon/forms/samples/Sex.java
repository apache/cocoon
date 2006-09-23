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
package org.apache.cocoon.forms.samples;


/**
 * Description of Sex.
 * @version $Id$
 */
public class Sex {

    public static final Sex MALE = new Sex("M");
    public static final Sex FEMALE = new Sex("F");
    private String code;

    private Sex(String code) { this.code = code; }

    public String toString() {
      // Will probably have some i18n support here
      switch(code.charAt(0)) {
          case 'M' : return this.getClass().getName() + ".MALE";
          case 'F' : return this.getClass().getName() + ".FEMALE";
          default : return "unknown"; // Should never happen
      }
    }
}
