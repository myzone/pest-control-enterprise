define(['backbone'], function(Backbone) {

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

    return TicketsModel;
});
