
function main() {
    
    var secret = generateSecret();
   
    while (true) {
        cocoon.sendPageAndWait("main.jxt", {secret:secret});

        if (cocoon.parameters.msg == "image") {
            cocoon.sendPage("auth.jpg", {text:secret});
            return;
        } else {
    
            var input = cocoon.request.get("secret");

            if (input == secret) {
                break;
            }
        }
    }
    
    cocoon.sendPage("success.jxt", {secret:secret});
    
}

function generateSecret() {
    
      var characters = "!@#$%^&*(){}[]<>.,ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
      var passwordlength = 7;
    
      var password = "";
      var randomnumber = 0;
      
      for (var n = 0; n < passwordlength; n++) {
         randomnumber = Math.floor(characters.length*Math.random());
         password += characters.substring(randomnumber,randomnumber + 1) 
      }
      
      return password;
}
    
