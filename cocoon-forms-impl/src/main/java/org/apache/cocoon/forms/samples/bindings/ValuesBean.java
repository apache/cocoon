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
package org.apache.cocoon.forms.samples.bindings;

/**
 * ValuesBean used in the 01values test.
 * @version $Id$
 */
public class ValuesBean {
    private String simple = "Simple";
    private String writeOnly = "Write-Only";
    private String readOnly = "Read-Only";
    private String date = "19700506";
    private String diffIn = "Diff-in/out";
    private String diffOut;
    private String onUpdate = "On Update";
    private int updateCount = 0;
    private boolean bool = true;
    private String other = "This field is not involved in the form.";

    public String toString() {
        return "ValuesBean[\n"
        +"\tsimple=" +simple +"\n"
        +"\treadonly=" +readOnly +"\n"
        +"\twriteonly=" +writeOnly +"\n"
        +"\tdiff-in=" +diffIn +"\n"
        +"\tdiff-out=" +diffOut +"\n"
        +"\tdate=" +date +"\n"
        +"\tbool=" +bool +"\n"
        +"\tonupdate=" + onUpdate +"\n"
        +"\tupdateCount=" + updateCount +"\n"
        +"\tother=" + other +"\n";
    }

    /**
     * @return Returns the bool.
     */
    public boolean isBool() {
        return bool;
    }

    /**
     * @param bool The bool to set.
     */
    public void setBool(boolean bool) {
        this.bool = bool;
    }

    /**
     * @return Returns the date.
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date The date to set.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return Returns the diffIn.
     */
    public String getDiffIn() {
        return diffIn;
    }

    /**
     * @param diffIn The diffIn to set.
     */
    public void setDiffIn(String diffIn) {
        this.diffIn = diffIn;
    }

    /**
     * @return Returns the diffOut.
     */
    public String getDiffOut() {
        return diffOut;
    }

    /**
     * @param diffOut The diffOut to set.
     */
    public void setDiffOut(String diffOut) {
        this.diffOut = diffOut;
    }

    /**
     * @return Returns the onUpdate.
     */
    public String getOnUpdate() {
        return onUpdate;
    }

    /**
     * @param onUpdate The onUpdate to set.
     */
    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * @return Returns the other.
     */
    public String getOther() {
        return other;
    }

    /**
     * @param other The other to set.
     */
    public void setOther(String other) {
        this.other = other;
    }

    /**
     * @return Returns the readOnly.
     */
    public String getReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return Returns the simple.
     */
    public String getSimple() {
        return simple;
    }

    /**
     * @param simple The simple to set.
     */
    public void setSimple(String simple) {
        this.simple = simple;
    }

    /**
     * @return Returns the updateCount.
     */
    public int getUpdateCount() {
        return updateCount;
    }

    /**
     * @param updateCount The updateCount to set.
     */
    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * @return Returns the writeOnly.
     */
    public String getWriteOnly() {
        return writeOnly;
    }

    /**
     * @param writeOnly The writeOnly to set.
     */
    public void setWriteOnly(String writeOnly) {
        this.writeOnly = writeOnly;
    }
}
