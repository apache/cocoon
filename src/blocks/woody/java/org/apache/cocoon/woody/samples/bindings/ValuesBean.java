/*
 * File ValuesBean.java created by mpo on Dec 26, 2003 | 4:01:03 PM
 * 
 * (c) 2003 - Outerthought BVBA
 */
package org.apache.cocoon.woody.samples.bindings;

/**
 * ValuesBean used in the 01values test.
 * @version $Id: ValuesBean.java,v 1.2 2004/02/11 10:43:32 antonio Exp $
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
