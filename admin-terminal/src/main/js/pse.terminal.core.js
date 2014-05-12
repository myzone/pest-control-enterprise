$(document).ready(function(){

    var requester = Requester("http://127.0.0.1:9292/");

    var SessionModel = Backbone.Model.extend({
        initialize: function() {
            var self = this;

            var isActiveSession = false;
            var userName = '';
            var sessionId = null;
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

                isActiveSession = false;
                userName = '';

                self.trigger('statusChanged');
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
                    id: guid(),
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

    var TicketsModel = Backbone.Model.extend({
        initialize: function() {
            var self = this;
            var sessionModel;

            var columns=[[
                {field:'colorStatus',title:''},
                {field:'ticketid',title:'Заявка #'},
                {field:'status',title:'Состояние'},
                {field:'creationDate',title:'Создана'},
                {field:'age',title:'Возраст'},
                {field:'lastModified',title:'Последнее изменение'},
                {field:'type',title:'Тип заявки / Описание'},
                {field:'worker',title:'Ответственный сотрудник'}
            ]];
            var data=[];
            var statusClasses=['opened','assigned','started','finished','closed'];
            var statusDescription=['Открыта','Назначена','Исполняется','Завершена','Закрыта'];
            var persons=['-','Вася Пупкин','Иван Иванов','Петр Петров','Иван Иванов'];
            for(var i=0; i<100;i++) {
                var mod=i % 5;
                var sample= {
                    colorStatus:"<div class='color-status "+statusClasses[mod]+"'></div>",
                    ticketid:i+1000,
                    status:statusDescription[mod],
                    creationDate:'2014-05-06 10:40',
                    age:'1 ч. 30 мин.',
                    lastModified:'2014-05-06 10:40',
                    type:'<b>Тараканы</b><br/><span>Они заполонили всю планету!</span>',
                    worker:persons[mod]
                };
                data[i]=sample;
            }

            this.getColumns = function() {
                return columns;
            };

            this.getData = function(param, successCb, errorCb) {
                if(sessionModel.getSessionId()===null) {
                    errorCb("Session error!");
                    return false;
                }
                var response={
                    total: data.length,
                    rows: data.slice((param.page-1)*param.rows,(param.page-1)*param.rows+param.rows)
                };
                successCb(response);
                return true;
            };

            this.setSession = function(session) {
                sessionModel=session;
                this.listenTo(sessionModel,'statusChanged',function() {
                    if(sessionModel.isLoggedIn())
                        this.trigger('refresh');
                });
            }
        }
    });

    var TicketsView = Backbone.View.extend({
        initialize: function() {
            var self = this;
            var ptoolbar="";
            var pIdFiled="";

            this.listenTo(this.model,'refresh',function() {
                this.$el.datagrid('reload');
            });

            this.setToolbar = function(selector) {
                ptoolbar=selector;
            };

            this.setIdField = function(fieldName) {
                pIdFiled=fieldName;
            };

            this.render = function() {
                this.$el.datagrid({
                    toolbar: ptoolbar,
                    columns: this.model.getColumns(),
                    idField: pIdFiled,
                    singleSelect:true,
                    rownumbers:true,
                    pagination:true,
                    loader: this.model.getData,
                    fit:true
                });
                $('#createRequest').click(function(){
                    self.trigger('createRequest');
                });
                return this;
            }
        }
    });

    var LoggedUserInfoView = Backbone.View.extend({
        template: _.template($('#user-info-panel-template').html()),
        initialize: function() {
            var self=this;
            function onSessionStatusChanged() {
                if(self.model.isLoggedIn()) {
                    self.$('.lbl-text').html('Вы вошли как:');
                    self.$('.user-name').html(self.model.getUserName());
                    self.$('.user-name').show();
                    self.$('.logout-button').show();
                } else {
                    self.$('.lbl-text').html('Вы не вошли в систему.');
                    self.$('.user-name').hide();
                    self.$('.logout-button').hide();
                }
            }

            this.listenTo(this.model,"statusChanged",onSessionStatusChanged);

            this.render = function() {
                this.$el.html(this.template({lblExit: 'Выйти'}));
                $.parser.parse(this.$el);
                onSessionStatusChanged();
                this.$('.logout-button').click(function() {
                    self.model.logout();
                });
                return this;
            }
        }
    });

    var LoginForm = Backbone.View.extend({
        template: _.template($('#login-form-template').html()),
        initialize: function() {
            var self = this;

            this.listenToOnce(this.model,"statusChanged",onSessionStatusChanged);

            this.render = function () {
                this.$el.html(this.template({
                    lblTitle: 'Войти в систему',
                    lblLogin: 'Логин',
                    lblPassword: 'Пароль',
                    lblOk: 'Ок',
                    lblCancel: 'Отмена'
                }));
                $.parser.parse(this.$el);
                this.$el=$('.login-form');
                this.el=$('.login-form')[0];
                this.$('.button-ok').click(submitLoginForm);
                this.$('.button-cancel').click(cancel);
                this.$('input').keyup(function(e) {
                    if (e.keyCode == 13) {
                        submitLoginForm();
                    }
                });
                this.$el.dialog({
                    onClose: function() {
                        self.remove();
                    }
                });

                return this;
            }

            function submitLoginForm() {
                var login = self.$('.login').val();
                var password = self.$('.password').val();
                self.model.login(login, password);
            }

            function onSessionStatusChanged() {
                if(self.model.isLoggedIn()) {
                    self.$el.dialog('close');
                    //self.remove();
                }
            }

            function cancel() {
                self.$el.dialog('close');
                //self.remove();
            }
        }

    });

    var TicketForm = Backbone.View.extend({
        template: _.template($('#ticket-form-template').html()),
        initialize: function() {
            var self = this;

            this.render = function() {
                this.$el = $('<div/>');
                this.$el.html(this.template({
                    lblTitle: 'Создать заявку',
                    lblPestType: 'Тип вредителя',
                    lblSelectPestType: 'Пожалуйста, выберите тип вредителя...',
                    lblBriefDescription: 'Краткое описание проблемы',
                    lblCustomerName: 'Имя клиента',
                    lblHouseNumber: 'Номер/Название дома',
                    lblStreet: 'Улица',
                    lblTown: 'Город/Населенный пункт',
                    lblPreferredDay: 'Предпочитаемая дата приезда специалиста',
                    lblEmail: 'Email',
                    lblPhoneNumber: 'Телефон',
                    lblSubmit: 'Создать',
                    lblCancel: 'Отмена'
                }));
                $('body').append(this.$el);
                $.parser.parse(this.$el);
            };
        }
    });

    var session = new SessionModel;
    session.on('loginError',function(msg) {
        $.messager.alert(msg.title,msg.text,'error');
    });

    session.on('notAuthorizedError',function(msg) {
        var loginForm = new LoginForm({
            el: $('.windows')[0],
            model: session
        });
        loginForm.render();
    });

    var md = new TicketsModel;
    md.setSession(session);

    var tv = new TicketsView({
        el: $('#ticketsView')[0],
        model: md
    });
    tv.setToolbar('#ticketsToolbar');
    tv.setIdField('ticketid');
    tv.render();
    tv.on('createRequest', function(){
        var form = new TicketForm;
        form.render();
    });
    var userInfoView = new LoggedUserInfoView({
        el: $('.user-info-container'),
        model: session
    });
    userInfoView.render();

    //session.trigger('notAuthorizedError');
});