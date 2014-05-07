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
                    ticketid:i,
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
            }
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
});