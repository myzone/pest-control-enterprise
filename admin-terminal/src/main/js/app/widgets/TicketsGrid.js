define(['backbone','jquery','easyui'],function(Backbone, $) {

    var TicketsGrid = Backbone.View.extend({
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

    return TicketsGrid;
});