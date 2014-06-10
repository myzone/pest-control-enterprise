define(['models/Requester','backbone', 'literals'], function(requester, Backbone, literals){

    var SessionModel = Backbone.Model.extend({
        set: function() {},
        destroy: function() {},
        save: function() {},
        fetch: function() {},
        clear: function() {},
        initialize: function() {
            var self = this;

            var isActiveSession = false;
            var userName = '';
            var sessionId = null;

            this.toJSON = function() {
                return {id: sessionId};
            };

            //try to load session from sessionStorage
            if(isSessionStorageAvailable()) {
                var session = JSON.parse(sessionStorage.getItem('session'));
                if(session !== null) {
                    sessionId = session.id;
                    userName = session.user;
                    requester.getTasks(this,[
                        {
                            name: 'taskById',
                            id: -1
                        }
                    ],function(response){
                        if(response === null || response.exception) {
                            userName = '';
                            sessionId = false;
                        } else {
                            isActiveSession = true;
                            self.trigger('statusChanged');
                        }
                    });
                }
            }

            function isSessionStorageAvailable() {
                try {
                    return 'sessionStorage' in window && window['sessionStorage'] !== null;
                } catch (e) {
                    return false;
                }
            }

            function checkSession() {
                if(!isActiveSession) {
                    self.trigger('notAuthorizedError',literals['notAuthorizedError']);
                }
                return isActiveSession;
            }

            function loginCallback(response) {
                if (response != null && response.result.types.indexOf('Admin') != -1) {
                    isActiveSession = true;
                    userName = response.result.login;
                    sessionId = response.result.id;
                    if(isSessionStorageAvailable()) {
                        sessionStorage.setItem('session',JSON.stringify({id:sessionId,user:userName}));
                    }
                    self.trigger('statusChanged');
                } else {
                    self.trigger('loginError',literals['loginError']);
                }
            }

            function logoutCallback(response) {
                isActiveSession = false;
                userName = '';
                sessionId = null;
                self.trigger('statusChanged');
            }

            this.isLoggedIn = function() {
                return isActiveSession;
            };

            this.getUserName = function() {
                if(checkSession()) {
                    return userName;
                }
            };

            this.logout = function() {
                if(!isActiveSession) {
                    return;
                }
                if(isSessionStorageAvailable()) {
                    sessionStorage.removeItem('session');
                }
                requester.endSession(this,logoutCallback);
            };

            this.getSessionId = function() {
                if(checkSession()) {
                    return sessionId;
                } else {
                    return null;
                }
            };

            this.login = function(login, pass) {
                requester.beginSession(login, pass, loginCallback);
            }
        }
    });

    return SessionModel;
});
