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
import java.util.Random;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;

/**
 * GuessGameApple shows an easy Apples implementation for a number guessing game.
 */
public class GuessGameApple extends AbstractLogEnabled implements AppleController {

    private final int random = (new Random()).nextInt(10) + 1;
    private int guesses = 0;

    public String toString() {
        return "GuessGameApple[ random=" + this.random + " | guesses=" + this.guesses + "]";
    }

    public void process(AppleRequest req, AppleResponse res) throws ProcessingException {
        
        String hint      = "No hints yet.";
        String targetURI = "guess/guess.jx";
        
        int newGuess = -1;
        String newGuessString = req.getCocoonRequest().getParameter("guess");
        
        if (newGuessString != null) {
            newGuess = Integer.parseInt(newGuessString);
            this.guesses++;

            if (this.random == newGuess) {
                targetURI = "guess/success.jx";
            } else {
                if (this.random < newGuess) {
                    hint = "Try lower.";
                } else {
                    hint = "Try higher.";
                }
            }
        }

        getLogger().debug(toString());

        Map bizdata = new HashMap();
        bizdata.put("random" , "" + this.random);
        bizdata.put("guesses", "" + this.guesses);
        bizdata.put("hint"   , hint);

        res.sendPage(targetURI, bizdata);
    }
    
}
