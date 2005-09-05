function passcode(length) {
  var alphabet = [ 'a', 'a', 'a', 'b', 'c', 'd', 'e', 'e', 'e', 'f', 'g', 'h', 'i',
                   'i', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'o', 'o', 'p', 'q', 'r',
                   's', 't', 'u', 'u', 'u', 'v', 'w', 'x', 'y', 'z' ]; 

  var result = "";
  for (var x = 0; x < length; x++) {
    var offset = Math.random() * alphabet.length;
    result += alphabet[Math.floor(offset)];
  }
  return result;
} 

function simple() {
  var session = cocoon.session;

  session.setAttribute("captcha", passcode(7));
  cocoon.sendPageAndWait("simple.jx");
  var parameters = null;

  while (true) {
    parameters = {
      "supplied": cocoon.request["captcha"],
      "expected": session.getAttribute("captcha"),
    };

    if (parameters['supplied'].equals(parameters['expected'])) break;

    session.setAttribute("captcha", passcode(7));
    cocoon.sendPageAndWait("simple-failure.jx", parameters);
  }

  session.invalidate();
  cocoon.sendPageAndWait("simple-success.jx", parameters);
}
