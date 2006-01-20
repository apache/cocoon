cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function do_a() {
    var data = {}
    data.value="shared value"
    var form = new Form("form-a.xml");
    while (true) {
        form.showForm("form-a", data)
        do_b(data)
    }
}

function do_b(data) {
    var form = new Form("form-b.xml");
    form.showForm("form-b", data)
}
