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
  return parseFloat(cocoon.request.getParameter(name));
}

function getOperator(a, b)
{
  cocoon.sendPageAndWait("page/getOperator", { "a" : a, "b" : b });
  return cocoon.request.operator;
}

function sendResult(a, b, op, result)
{
  cocoon.sendPage("page/displayResult",
           { 
                "a" : a, 
                "b" : b, 
                "operator" : op, 
                "result" : result 
           }
  );
}
