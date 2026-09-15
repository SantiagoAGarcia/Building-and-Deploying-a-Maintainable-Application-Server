function greet() {
    const name = document.getElementById("name").value;

    fetch("/hello?name=" + encodeURIComponent(name))
        .then(response => response.text())
        .then(message => {
            document.getElementById("result").innerHTML = message;
        })
        .catch(err => {
            document.getElementById("result").innerHTML = "Error: " + err;
        });
}

function getPi() {
    fetch("/pi")
        .then(response => response.text())
        .then(value => {
            document.getElementById("pi-result").innerHTML = value;
        })
        .catch(err => {
            document.getElementById("pi-result").innerHTML = "Error: " + err;
        });
}
