/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
cocoon.load("resource://org/apache/cocoon/components/jxforms/flow/javascript/JXForm.js");

function calculator(form) {
  var model = {
    numberA: 0,
    numberB: 0,
    operator: "plus",
    result: 0
  };
  form.setModel(model);
  form.sendView("calc/NumberA.xml");
  form.sendView("calc/NumberB.xml");
  form.sendView("calc/Operator.xml");
  switch (model.operator) {
  case "plus":
    model.result = Number(model.numberA) + Number(model.numberB);
    break;
  case "minus":
    model.result = model.numberA - model.numberB;
    break;
  case "multiply":
    model.result = model.numberA * model.numberB;
    break;
  case "divide":
    model.result = model.numberA / model.numberB;
    break;
  }
  form.sendView("calc/Result.xml");
  calculator(form); // repeat (Note recursive tail call)
}
