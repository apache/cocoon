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

package org.apache.cocoon.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class UserManager {

    static UserManager instance;
    
    HashMap passwords = new HashMap();
    HashMap names = new HashMap();
    
    protected UserManager(InputStream stream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(stream));
        while (true) {
            String line = input.readLine();
            if (line != null) {
                if (!line.startsWith("#") && !line.equals("")) {
                    StringTokenizer st = new StringTokenizer(line,":");
                    String name = st.nextToken();
                    String password = st.nextToken();
                    passwords.put(name,password);
                    String fullname = st.nextToken();
                    names.put(name,fullname);
                }
            } else {
                break;
            }
        }
    }
    
    public static UserManager getInstance(InputStream stream) throws IOException {
        if (instance == null) {
            instance = new UserManager(stream);
        }
        return instance;
    }

    public boolean isValidName(String name) {
        return passwords.containsKey(name);    
    }
    
    public boolean isValidPassword(String name, String password) {
        String storedPassword = (String) passwords.get(name);
        return (storedPassword != null) && (storedPassword.equals(password));
    }
    
    public String getFullName(String name) {
        return (String) names.get(name);
    }
}
