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

package org.apache.cocoon.samples.supersonic.beans;

import java.util.Date;

/** Comments attached to a TaskBean
 */

public class TaskCommentBean {
    private final int m_id;
    public static int m_idCounter;

    public TaskCommentBean() {
        synchronized(TaskBean.class) {
            m_id = ++m_idCounter;
        }
    }

    public String toString() {
        return "TaskCommentBean #" + m_id + " (" + m_date + "," + m_comment + ")";
    }

    public int getId() {
        return m_id;
    }

    public Date getDate() {
        return m_date;
    }

    public void setDate(Date m_date) {
        this.m_date = m_date;
    }

    public String getComment() {
        return m_comment;
    }

    public void setComment(String m_comment) {
        this.m_comment = m_comment;
    }

    private Date m_date;
    private String m_comment;
}
