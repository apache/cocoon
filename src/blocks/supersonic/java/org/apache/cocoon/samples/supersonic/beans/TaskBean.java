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

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/** Stores data of a Task
 *
 */

public class TaskBean {
    private String m_taskName;
    private String m_assignedTo;
    private final int m_id;
    private final LinkedList m_comments = new LinkedList();
    public static int m_idCounter;

    public TaskBean() {
        synchronized(TaskBean.class) {
            m_id = ++m_idCounter;
        }
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("TaskBean #" + m_id + " (" + m_taskName + "," + m_assignedTo + ")");
        for(Iterator it = m_comments.iterator(); it.hasNext(); ) {
            sb.append("\n\t");
            sb.append(it.next());
        }
        return sb.toString();
    }

    public int getId() {
        return m_id;
    }

    public String getTaskName() {
        return m_taskName;
    }

    public void setTaskName(String m_taskName) {
        this.m_taskName = m_taskName;
    }

    public String getAssignedTo() {
        return m_assignedTo;
    }

    public void setAssignedTo(String m_assignedTo) {
        this.m_assignedTo = m_assignedTo;
    }

    public List getComments() {
        return m_comments;
    }

    /** replace comments with c (trivial example implementation) */
    public void setComments(List c) {
        m_comments.clear();
        Collections.copy(m_comments,c);
    }

    public void addComment(TaskCommentBean tcb) {
        m_comments.add(tcb);
    }
}
