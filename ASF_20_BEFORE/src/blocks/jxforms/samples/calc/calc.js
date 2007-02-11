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
