define(['models/Requester','backbone', 'literals'], function(requester, Backbone, literals){

    var SessionModel = Backbone.Model.extend({
        initialize: function() {
            var self = this;

            var isActiveSession = false;
            var userName = '';
            var sessionId = null;


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
                requester.endSession(sessionId,logoutCallback);
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
