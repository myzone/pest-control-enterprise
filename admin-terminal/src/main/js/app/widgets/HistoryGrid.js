define(['backbone','underscore','literals','moment','jquery','easyui'],function(Backbone, _, literals, moment, $) {

    var TicketsGrid = Backbone.View.extend({
        constructor: function(options) {
            var self = this;
            var collection = options.collection;

            moment.lang('ru');
            var defaultView = $.fn.datagrid.defaults.view;

            var customView = {
                render: defaultView.render,
                renderFooter: defaultView.renderFooter,
                renderRow: function(target, fields, frozen, rowIndex, rowData) {
                    var handler = {
                        time: function(row, model) {
                            var instant = model.get('instant');
                            row.time = moment.unix(instant).calendar();
                        },
                        user: function(row, model) {
                            var user = model.get('causer').name;
                            row.user = user;
                        },
                        comment: function(row, model) {
                            var shortDescr = model.get('comment');
                            if(shortDescr.length>80) {
                                shortDescr = shortDescr.substring(0,80)+'...';
                            }
                            row.comment = '<pre>'+shortDescr+'</pre>';
                        }
                    };
                    var result = {};
                    _.each(fields, function(key){
                        handler[key](result,rowData);
                    });
                    return defaultView.renderRow(target, fields, frozen, rowIndex, result);
                },
                refreshRow: defaultView.refreshRow,
                onBeforeRender: defaultView.onBeforeRender,
                onAfterRender: defaultView.onAfterRender,
                insertRow: defaultView.insertRow,
                updateRow: defaultView.updateRow
            };

            this.render = function() {
                this.$el.datagrid({
                    columns: literals.historyGridColumns,
                    singleSelect:true,
                    rownumbers:true,
                    pagination:false,
                    data: collection.models,
                    fit:true,
                    view: customView
                });
            };

            Backbone.View.apply(this,arguments);
        }
    });

    return TicketsGrid;
});
