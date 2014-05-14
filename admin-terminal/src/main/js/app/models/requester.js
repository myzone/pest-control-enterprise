define(function() {
    function Requester(host) {
        var guid = (function() {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }
            return function() {
                return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                    s4() + '-' + s4() + s4() + s4();
            };
        })();
        this.send = function(request, сallback) {
            request.id=guid();
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

    return Requester("http://127.0.0.1:9292/");
});