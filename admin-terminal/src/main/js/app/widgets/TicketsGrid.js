define(['backbone','literals','moment','jquery','easyui'],function(Backbone, literals, moment, $) {

    var TicketsGrid = Backbone.View.extend({
        initialize: function() {
            var self = this;
            var ptoolbar="";
            var pIdFiled="";
            var rendered = false;

            moment.lang('ru');
            var defaultView = $.fn.datagrid.defaults.view;


            /*this.listenTo(this.model, 'add change', function(ticket) {
                this.$el.datagrid('reload');
            });*/

            var customView = {
                render: defaultView.render,
                renderFooter: defaultView.renderFooter,
                renderRow: function(target, fields, frozen, rowIndex, rowData) {
                    var colorStatus = _.template($('#color-status-template').html());
                    var typeColumn = _.template($('#ticket-type-template').html());
                    var handler = {
                        colorStatus: function(row,model) {
                            row.colorStatus = colorStatus({status: model.get('status').toLowerCase()});
                        },
                        ticketid: function(row,model) {
                            row.ticketid = model.id;
                        },
                        status: function(row, model) {
                          row.status = literals.statusDescriptions[model.get('status')]
                        },
                        creationDate: function(row, model) {
                            var history = model.get('taskHistory');
                            row.creationDate = moment.unix(history[0].instant).format('DD.MM.YY HH:mm:ss');
                        },
                        age: function(row, model) {
                            var history = model.get('taskHistory');
                            row.age = moment.unix(history[0].instant).fromNow(true);
                        },
                        lastModified: function(row, model) {
                            var history = model.get('taskHistory');
                            row.lastModified = moment.unix(history[history.length-1].instant).calendar();
                        },
                        type: function(row, model) {
                            var shortDescr = model.get('problemDescription');
                            if(shortDescr.length>80) {
                                shortDescr = shortDescr.substring(0,80)+'...';
                            }
                            row.type = typeColumn({
                               pest: model.get('pestType').name,
                               descr: shortDescr
                            });
                        },
                        worker: function(row, model) {
                            var executor = model.get('executor');
                            var executorName = '';
                            if(executor)
                                executorName = executor.name;
                            row.worker = executorName;
                        }
                    };
                    var result = {};
                    _.each(fields, function(key){
                        handler[key](result,rowData);
                        rowData.rowIndex = rowIndex;
                    });
                    return defaultView.renderRow(target, fields, frozen, rowIndex, result);
                },
                refreshRow: defaultView.refreshRow,
                onBeforeRender: defaultView.onBeforeRender,
                onAfterRender: defaultView.onAfterRender,
                insertRow: defaultView.insertRow,
                updateRow: defaultView.updateRow
            };

            this.reload = function() {
                this.$el.datagrid('reload');
            };

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
                if(rendered) {
                    this.$el.datagrid('reload');
                    return this;
                }
                this.$el.datagrid({
                    toolbar: ptoolbar,
                    columns: this.model.getColumns(),
                    idField: pIdFiled,
                    singleSelect:true,
                    rownumbers:true,
                    pagination:false,
                    loader: this.model.getData,
                    fit:true,
                    view: customView
                });
                $('#createTicket').click(function(){
                    self.trigger('createTicket');
                });
                $('#reloadData').click(function() {
                   self.$el.datagrid('reload');
                   self.trigger('refresh');
                });
                $('#editTicket').click(function() {
                    var ticket = self.$el.datagrid('getSelected');
                    if(ticket) {
                        self.trigger('editTicket',ticket);
                    }
                });
                $('#closeTicket').click(function() {
                    var ticket = self.$el.datagrid('getSelected');
                    if(ticket) {
                        self.trigger('closeTicket',ticket);
                    }
                });
                rendered = true;
                return this;
            }
        }
    });

    return TicketsGrid;
});