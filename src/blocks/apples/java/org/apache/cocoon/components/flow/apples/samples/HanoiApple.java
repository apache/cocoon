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
