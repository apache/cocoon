/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
 include  the following  acknowledgment:  "This product includes  software
 developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 Alternately, this  acknowledgment may  appear in the software itself,  if
 and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
 used to  endorse or promote  products derived from  this software without
 prior written permission. For written permission, please contact
 apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
 "Apache" appear  in their name,  without prior written permission  of the
 Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.cocoon.woody.samples.bindings;

/**
 * ValuesBean used in the 01values test.
 * @author Marc Portier
 * @version $Id: ValuesBean.java,v 1.3 2004/02/12 02:52:18 antonio Exp $
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
