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

// simple number guessing game in Flowscript
// based on the Cocoon Flow tutorial:
// http://cocoon.apache.org/2.1/userdocs/flow/tutor.html

function public_startGuessNumber() {
  var max = cocoon.parameters["maxValue"];
  var toGuess = Math.round(Math.random() * max);
  if(toGuess == 0) toGuess = 1;
  var hint = "Guess a number between 1 and " + max;
  var tries = 0;

  // show and process input form, until correct answer is given
  while (true) {
    cocoon.sendPageAndWait("number-guess/views/guess", {"toGuess" : toGuess, "hint" : hint, "tries" : tries});
    var answer = parseInt( cocoon.request.get("answer") );

    tries++;

    if(answer) {
     if(answer > toGuess) {
          hint = "The number you entered (" + answer + ") is too big";
      } else if(answer < toGuess) {
          hint = "The number you entered (" + answer + ") is too small";
      } else {
          break;
      }
    }
  }

  cocoon.sendPage("number-guess/views/success", {"toGuess" : toGuess, "answer" : answer, "tries" : tries} );
}