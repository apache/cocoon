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
var a, b, op;

function calculator()
{
  a = getNumber("a");
  b = getNumber("b", a);
  op = getOperator(a, b);

  if (op == "plus")
    sendResult(a, b, op, a + b);
  else if (op == "minus")
    sendResult(a, b, op, a - b);
  else if (op == "multiply")
    sendResult(a, b, op, a * b);
  else if (op == "divide")
    sendResult(a, b, op, a / b);
  else
    sendResult("Error: Unkown operator!");
}

function getNumber(name, a, b)
{
  var uri = "page/getNumber" + name.toUpperCase();
  cocoon.sendPageAndWait(uri, { "a" : a, "b" : b });
  print(cocoon.request);
  for (i in cocoon.request) {
    print(i + " = " + cocoon.request[i]);
  }
  return parseFloat(cocoon.request.getParameter(name));
}

function getOperator(a, b)
{
  cocoon.sendPageAndWait("page/getOperator", { "a" : a, "b" : b });
  return cocoon.request.getParameter("operator");
}

function sendResult(a, b, op, result)
{
  cocoon.sendPage("page/displayResult",
           { "a" : a, "b" : b, "operator" : op, "result" : result });
}

