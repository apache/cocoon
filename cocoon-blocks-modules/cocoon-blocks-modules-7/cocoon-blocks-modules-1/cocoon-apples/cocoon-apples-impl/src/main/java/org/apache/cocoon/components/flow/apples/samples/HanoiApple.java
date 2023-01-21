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
package org.apache.cocoon.components.flow.apples.samples;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;

/**
 * HanoiApple shows an apple maintaining the state of the fanous puzzle.
 */
public class HanoiApple extends AbstractLogEnabled implements AppleController {

    public static final int NONE = -1;
    public static final int SRC = 0;
    public static final int AUX = 1;
    public static final int DST = 2;
    
    // full state of the puzzle is in the following variables
    public Stack[] stacks;
    public Object floatingDisk = null;
    public int moves = 0;
    public int puzzleSize  = 0;


    public String toString() {
        return "HanoiApple[ stacks=" + this.stacks + " | floatingDisk=" + this.floatingDisk
                + " | moves = " + this.moves + "]";
    }

    
    public void process(AppleRequest req, AppleResponse res) throws ProcessingException {

        // processing
        if (stacks == null) {
            String requestSize = req.getCocoonRequest().getParameter("size");
            if (requestSize != null) {
                try {
                    int size = Integer.parseInt(requestSize);
                    intializeStacks(size);
                } catch (NumberFormatException ignore) {
                }
            }
        } else {
            // decide selected column
            String requestStack = req.getCocoonRequest().getParameter("stack");
            if (requestStack != null) {
                try {
                    int stackNdx = Integer.parseInt(requestStack);
                    if (this.floatingDisk != null) {
                        // we are in the middle of a move --> complete move if it is allowed
                        if (   this.stacks[stackNdx].size() == 0 
                            || ((Integer)this.floatingDisk).intValue() < ((Integer)this.stacks[stackNdx].peek()).intValue()) {
                                
                            this.stacks[stackNdx].push(this.floatingDisk);
                            this.floatingDisk = null;
                            this.moves++;
                        }
                    } else {
                        if (this.stacks[stackNdx].size() != 0) {
                            this.floatingDisk = this.stacks[stackNdx].pop();    
                        }                        
                    }
                } catch (RuntimeException ignore) {
                   //NUMBERFORMAT
                   //ARRAYINDEXOUTOFBOUNDS                    
                }
            }
        }

        getLogger().debug(toString());

        //view generation
        if (stacks == null) {
            res.sendPage("hanoi/intro.jx", null);
        } else {
            Map bizdata = new HashMap();
            bizdata.put("stacks"      , this.stacks);
            bizdata.put("moves"       , "" + this.moves);
            bizdata.put("floatingDisk", this.floatingDisk);
            bizdata.put("nextMove"    , this.floatingDisk==null ? "Lift it!" : "Drop it!");
            bizdata.put("puzzleSize"  , "" + this.puzzleSize);

            res.sendPage("hanoi/hanoi.jx", bizdata);
        }

    }


    private void intializeStacks(int size) {
        if (size > 2) {
            this.stacks = new Stack[3];
            for (int i = 0; i < 3; i++) {
                this.stacks[i] = new Stack();
            }
            for (int i = size; i > 0; i--) {
                this.stacks[0].push(new Integer(i));
            }
            this.puzzleSize = size;
        }
    }

}
