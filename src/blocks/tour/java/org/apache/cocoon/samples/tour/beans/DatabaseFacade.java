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

package org.apache.cocoon.samples.tour.beans;

import java.util.LinkedList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;

/** Provides access to the "database", which is in nothing more than an in-memory data structure.
 *  
 *  @author bdelacretaz@codeconsult.ch
 */
 
public class DatabaseFacade {
    /** singleton */
    private static final DatabaseFacade m_instance = new DatabaseFacade();

    /** list of tasks (simulated database) */
    private final LinkedList m_tasks = new LinkedList();

    /** simulated data */
    final String title [] = {
        "Design DB interface",
        "Load test data",
        "User interface design",
        "Usability test",
        "Market acceptance study",
        "Investors demo",
        "Public press release",
        "TV interview",
        "Local radio interview",
        "Location shooting for promo video",
        "Government group demo",
        "Test users screening",
        "Promo video casting",
        "Promo video editing",
        "Audio sweetening",
        "Sound design",
        "Games rules evaluation"
    };

    /** simulated data */
    final String name [] = {
        "Donald Duck",
        "Miles Davis",
        "Leonardo DaVinci",
        "Rodney Curtis",
        "Foad Zee"
    };

    /** simulated data */
    final String commentText [] = {
        "Revised with management",
        "Checked budget",
        "Called Mr.Smith about it, he's ok with the current state",
        "Asked Fred Flintstone for a budget extension",
        "Looked at the planning, logistics says we won't make it"
    };

    private DatabaseFacade() {
        loadTestData();
    }

    public static DatabaseFacade getInstance() {
        return m_instance;
    }

    /** get our list of tasks */
    public List getTasks() {
        return m_tasks;
    }

    /** get a single TaskBean */
    public TaskBean getTaskBeanById(int id) throws Exception {
        // inefficient but ok for this demo!
        TaskBean result = null;
        for(Iterator it=m_tasks.iterator(); it.hasNext(); ) {
            final TaskBean tb = (TaskBean)it.next();
            if(tb.getId() == id) {
                result = tb;
                break;
            }
        }

        if(result == null) {
            throw new Exception("Not found: TaskBean having id=" + id);
        }
        return result;
    }

    /** for tests, simulate data */
    private void loadTestData() {
        for(int i=0; i < title.length; i++) {
            m_tasks.add(generateTask(title[i],i));
        }
    }

    /** create a TaskBean and generate a random number of comments in it */
    private TaskBean generateTask(String title,int index) {
        final TaskBean tb = new TaskBean();
        tb.setTaskName(title);
        tb.setAssignedTo(name[index % name.length]);

        final int nComments = (int)(Math.random() * 20);
        final long MSEC_OFFSET = (1000L * 3600L * 24L) + (1000L * 60 * 12);
        long timestamp = new Date().getTime() - nComments * MSEC_OFFSET;
        for(int i=0; i < nComments; i++) {
            final TaskCommentBean tcb = new TaskCommentBean();
            tcb.setDate(new Date(timestamp));
            tcb.setComment(commentText[i % commentText.length]);
            tb.addComment(tcb);
            timestamp += MSEC_OFFSET;
        }

        return tb;
    }

    /** version info */
    public String getVersion() {
        return "$Id: DatabaseFacade.java,v 1.1 2004/05/11 06:52:29 crossley Exp $";
    }
}
