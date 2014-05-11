function Requester(host) {

    this.send = function(request, сallback) {
        var xhr = new XMLHttpRequest();
        xhr.onload = function() {
            сallback(JSON.parse(xhr.responseText));
        };
        xhr.onerror = function(data) {
            сallback(null);
        };
        xhr.open("POST", host, true);
        xhr.send(JSON.stringify(request));
    };

    return this;
}