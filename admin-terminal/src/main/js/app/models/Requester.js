define(['underscore'], function( _ ) {
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

        function send(request,callback) {
            var cb = callback;
            request.id=guid();
            var xhr = new XMLHttpRequest();
            xhr.onload = function() {
                var response = JSON.parse(xhr.responseText);
                if(!response.exception) {
                    cb(response);
                } else {
                    cb(null);
                }

            };
            xhr.onerror = function(data) {
                cb(null);
            };
            xhr.open("POST", host, true);
            xhr.send(JSON.stringify(request));
        }

        this.beginSession = function(login, password, callback) {
            function internalCallback(response) {
                if(response !== null && response.result) response.result.login=login;
                callback(response);
            }
            send({
                procedure: "beginSession",
                argument: {
                    user: {
                        login: login
                    },
                    password: password
                }
            }, internalCallback);
        };

        this.endSession = function(session, callback) {
            send({
                procedure: "endSession",
                argument: session
            },callback);
        };

        this.allocateTask = function(ticket, callback) {
            send({
                procedure: "allocateTask",
                argument: ticket
            }, callback);
        };

        this.editTask  = function(changes, callback) {
            send({
                procedure: "editTask",
                argument: changes
            }, callback);
        }

        this.getTasks = function(session,filters, callback) {
            var arguments = {
                session: session,
                filters: filters
            };
            send({
                procedure: "getTasks",
                argument: arguments
            }, callback);
        }

        return this;
    }

    return Requester("http://127.0.0.1:9292/");
});
