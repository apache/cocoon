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
