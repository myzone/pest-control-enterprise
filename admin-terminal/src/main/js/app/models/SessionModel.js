define(['models/Requester','backbone'], function(requester,Backbone){

    var SessionModel = Backbone.Model.extend({
        initialize: function() {
            var self = this;

            var isActiveSession = false;
            var userName = '';
            var sessionId = null;


            function checkSession() {
                if(!isActiveSession) {
                    self.trigger('notAuthorizedError',{
                        title: 'Ошибка авторизации',
                        text: 'Вы не авторизированы или время сессии истекло.'
                    });
                }
                return isActiveSession;
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
                requester.send({
                    procedure: "endSession",
                    argument: {
                        id: sessionId
                    }
                }, function() {
                    isActiveSession = false;
                    userName = '';
                    sessionId
                    self.trigger('statusChanged');
                });

            };

            this.getSessionId = function() {
                if(checkSession()) {
                    return sessionId;
                } else {
                    return null;
                }
            };

            this.login = function(login, pass) {
                requester.send({
                    procedure: "beginSession",
                    argument: {
                        user: {
                            name: login
                        },
                        password: pass
                    }
                }, function(response) {
                    if (response != null && response.result.types.indexOf("Admin") != -1) {
                        isActiveSession = true;
                        userName = login;
                        sessionId = response.result.id;

                        self.trigger('statusChanged');
                    } else {
                        self.trigger('loginError',{
                            title: 'Ошибка авторизации',
                            text: 'Введен неправильный логин или пароль.'
                        });
                    }
                });
            }
        }
    });

    return SessionModel;
});
