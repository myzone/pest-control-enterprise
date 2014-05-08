$(document).ready(function(){
    var TicketsModel = Backbone.Model.extend({
        initialize: function() {
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
            }

            this.getData = function(param, successCb, errorCb) {
                var response={
                    total: data.length,
                    rows: data.slice((param.page-1)*param.rows,(param.page-1)*param.rows+param.rows)
                };
                successCb(response);
                return true;
            }
        }
    });

    var TicketsView = Backbone.View.extend({
        initialize: function() {
            var ptoolbar="";
            var pIdFiled="";

            this.setToolbar = function(selector) {
                ptoolbar=selector;
            }

            this.setIdField = function(fieldName) {
                pIdFiled=fieldName;
            }

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
                return this;
            }
        }
    });
    var SessionModel = Backbone.Model.extend({
        initialize: function() {
            var isActiveSession = false;
            var userName = '';

            this.isLoggedIn = function() {
                return isActiveSession;
            }

            this.getUserName = function() {
                return userName;
            }

            this.logout = function() {
                isActiveSession = false;
                userName = '';
                this.trigger('statusChanged');
            }

            this.login = function(login, password) {
                if(login==='gleab' && password==='test') {
                    isActiveSession = true;
                    userName = login;
                    this.trigger('statusChanged');
                } else {
                    this.trigger('error',{title:'Ошибка авторизации',text:'Введен неправильный логин или пароль.'});
                }
            }
        }
    });

    var LoggedUserInfoView = Backbone.View.extend({
        template: _.template($('#user-info-panel-template').html()),
        initialize: function() {
            var self=this;
            function onStatusChanged() {
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

            this.listenTo(this.model,"statusChanged",onStatusChanged);

            this.render = function() {
                this.$el.html(this.template());
                this.$('.logout-button').linkbutton({
                    iconCls: 'icon-power'
                });
                onStatusChanged();
                this.$('.logout-button').click(function() {
                    self.model.logout();
                });
                return this;
            }
        },
        events: {
            "click .logout-button": "logout"
        }
    });

    var LoginForm = Backbone.View.extend({
        template: _.template($('#login-form-template').html()),
        initialize: function() {

        },
        render: function() {
            this.$el.html(this.template({
                lblLogin:       'Логин',
                lblPassword:    'Пароль',
                lblOk:          'Ок',
                lblCancel:      'Отмена'
            }));
            this.$('.login-form').window({
                title: 'Войти в систему',
                width:300,
                height:180,
                modal:true,
                collapsible: false,
                minimizable: false,
                maximizable: false,
                resizable: false
            });
            this.$('.login-form').window('open');
        }
    });

    var md = new TicketsModel;
    var tv = new TicketsView({
        el: $('#ticketsView')[0],
        model: md
    });
    tv.setToolbar('#ticketsToolbar');
    tv.setIdField('ticketid');
    tv.render();

    var session = new SessionModel;
    session.on('error',function(msg) {
        $.messager.alert(msg.title,msg.text,'error');
    });
    var userInfoView = new LoggedUserInfoView({el:$('.user-info-container'),model:session});
    userInfoView.render();

    var loginForm = new LoginForm({el:$('.windows')[0]});
    loginForm.render();
});