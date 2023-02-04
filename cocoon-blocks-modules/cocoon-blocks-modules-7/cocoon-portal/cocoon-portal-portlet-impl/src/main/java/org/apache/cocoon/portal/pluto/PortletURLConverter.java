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

package org.apache.cocoon.portal.pluto;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.util.StringUtils;
import org.apache.cocoon.portal.om.CopletInstance;

/**
 * Create the URL for a portlet.
 *
 * @version $Id$
 */
public class PortletURLConverter {

    public static final String ACTION = "ac";
    public static final String MODE = "md";
    public static final String PORTLET_ID = "pid";
    public static final String PREFIX = "_";
    public static final String PARAM = "pm";
    public static final String STATE = "st";

    private final Map urlData = new HashMap();
    private final Map parameters = new HashMap();
    private String portletId;

    /**
     * Constructor used when the URL will be marshalled.
     * @param cid The coplet id.
     */
    public PortletURLConverter(CopletInstance cid) {
        this.portletId = cid.getId();
    }

    /**
     * Constructor used when the URL will be unmarshalled.
     * @param eventData The url data.
     */
    public PortletURLConverter(String eventData) {
        StringTokenizer tokenizer = new StringTokenizer(eventData, "/");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (isParameter(token)) {
                String name = decodeParameterName(token);
                String key = encodeParameterName(name);
                this.parameters.put(key, token.substring(key.length()));
            } else {
                StringTokenizer tokens = new StringTokenizer(token, PREFIX);

                if (tokens.countTokens() > 1) {
                    String key = tokens.nextToken();
                    if (key.equals(PORTLET_ID)) {
                        this.portletId = tokens.nextToken();
                    } else {
                        String value = tokens.nextToken();
                        urlData.put(key, value);
                    }
                }
            }
        }
    }

    /**
     * Return the PortletMode
     * @return The PortletMode
     */
    public PortletMode getMode() {
        String mode = (String)urlData.get(getModeKey());
        if (mode != null) {
            return new PortletMode(mode);
        }
        return PortletMode.VIEW;
    }

    /**
     * Return the WindowState
     * @return The WindowState
     */
    public WindowState getState() {
        String state = (String) urlData.get(getStateKey());
        if (state != null) {
            return new WindowState(state);
        }
        return WindowState.NORMAL;
    }

    /**
     * Return the indicator if this is an action.
     * @return true if this is an action URL, false if a render URL.
     */
    public boolean isAction() {
        return (urlData.get(getActionKey()) != null);
    }

    /**
     * Indicates that the URL is an action.
     */
    public void setAction() {
        urlData.put(getActionKey(),ACTION.toUpperCase());
    }

    /**
     * Sets the PortletMode.
     * @param mode The PortletMode
     */
    public void setMode(PortletMode mode) {
        urlData.put(getModeKey(), mode.toString());
    }

    /**
     * Sets the WindowState
     * @param state The WindowState
     */
    public void setState(WindowState state) {
        urlData.put(getStateKey(), state.toString());
    }

    /**
     * Returns the portlet id.
     * @return The portlet id.
     */
    public String getPortletId() {
        return this.portletId;
    }

    /**
     * Returns the request parameters for this portlet URL.
     * @return A Map containing the parameters for this URL.
     */
    public Map getParameters() {
        Map map = new HashMap();
        Iterator iter = this.parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String key = decodeParameterName((String) entry.getKey());
            String[] values = decodeParameterValues((String)entry.getValue());
            map.put(key, values);
        }
        return map;
    }

    /**
     * Adds the parameter, replacing a parameter with the same name.
     * @param name The parameter name
     * @param values An array of Strings.
     */
    public void setParam(String name, String[] values) {
        this.parameters.put(encodeParameterName(name), encodeParameterValues(values));
    }

    /**
     * Returns the marshalled URL.
     * @return A String containing the marshalled URL.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PORTLET_ID).append(PREFIX).append(portletId);
        Iterator iter = urlData.entrySet().iterator();
        while (iter.hasNext()) {
            buffer.append("/");
            Map.Entry entry = (Map.Entry)iter.next();
            buffer.append(entry.getKey()).append(PREFIX).append(entry.getValue());
        }
        iter = this.parameters.entrySet().iterator();
        while (iter.hasNext()) {
            buffer.append("/");
            Map.Entry entry = (Map.Entry) iter.next();
            buffer.append(entry.getKey()).append(PREFIX).append(entry.getValue());
        }
        return buffer.toString();
    }

    private String getActionKey() {
        return ACTION;
    }

    private String getModeKey() {
        return MODE;
    }

    private String getStateKey() {
        return STATE;
    }

    private String getParamKey() {
        return PARAM + PREFIX;
    }

    private boolean isParameter(String key) {
        return key.startsWith(getParamKey());
    }

    private String decodeParameterName(String encodedParamName) {
        StringTokenizer tokenizer = new StringTokenizer(encodedParamName, PREFIX);
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        // Skip the key
        tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        String name = tokenizer.nextToken();
        return decodeValue(name);
    }

    private String[] decodeParameterValues(String encodedParamValues) {
        StringTokenizer tokenizer = new StringTokenizer(encodedParamValues, PREFIX);
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        String _count = tokenizer.nextToken();
        int count = Integer.valueOf(_count).intValue();
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            if (!tokenizer.hasMoreTokens()) {
                return null;
            }
            values[i] = decodeValue(tokenizer.nextToken());
        }
        return values;
    }

    private String decodeValue(String value) {
        String result = value;
        result = StringUtils.replace(result, "0x1", "_");
        result = StringUtils.replace(result, "0x2", ".");
        result = StringUtils.replace(result, "0x3", "/");
        result = StringUtils.replace(result, "0x4", "\r");
        result = StringUtils.replace(result, "0x5", "\n");
        result = StringUtils.replace(result, "0x6", "<");
        result = StringUtils.replace(result, "0x7", ">");
        result = StringUtils.replace(result, "0x8", " ");
        result = StringUtils.replace(result, "0x0", "0x");
        return result;
    }

    private String encodeParameterName(String paramName) {
        StringBuffer returnvalue = new StringBuffer(50);
        returnvalue.append(getParamKey()).append(encodeValue(paramName));
        return returnvalue.toString();
    }

    private String encodeParameterValues(String[] paramValues) {
        StringBuffer returnvalue = new StringBuffer(100);
        returnvalue.append(paramValues.length);
        for (int i = 0; i < paramValues.length; i++) {
            returnvalue.append("_");
            returnvalue.append(encodeValue(paramValues[i]));
        }
        return returnvalue.toString();
    }

    private String encodeValue(String value) {
        String result = value;
        result = StringUtils.replace(result, "0x", "0x0");
        result = StringUtils.replace(result, "_", "0x1");
        result = StringUtils.replace(result, ".", "0x2");
        result = StringUtils.replace(result, "/", "0x3");
        result = StringUtils.replace(result, "\r", "0x4");
        result = StringUtils.replace(result, "\n", "0x5");
        result = StringUtils.replace(result, "<", "0x6");
        result = StringUtils.replace(result, ">", "0x7");
        result = StringUtils.replace(result, " ", "0x8");
        return result;
    }
}