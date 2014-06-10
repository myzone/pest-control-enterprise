define([
    'literals',
    'underscore',
    'backbone',
    'models/PestTypes',
    'widgets/ComboBox',
    'widgets/CustomerForm',
    'jquery',
    'easyui'], function( literals, _, Backbone, PestTypes, ComboBox, CustomerForm, $) {

    var TicketForm = Backbone.View.extend({
        template: _.template($('#ticket-form-template').html()),
        constructor: function(opts) {
            var options = opts;
            var self = this;
            var pestTypes = new PestTypes;
            var comboPestTypes = null;
            var customerForm = null;
            var session = options.session;

            this.$el = options.el;


            this.render = function() {
                this.$el.tabs('add', {
                    title: literals.ticketForm.lblTitle,
                    selected: true,
                    closable: true
                });
                var panel = this.$el.tabs('getSelected');
                $.parser.parse(panel.html(this.template(literals.ticketForm)));
                this.$el = panel;
                comboPestTypes = new ComboBox({
                    collection: pestTypes,
                    el: $('#pestType'),
                    valueField: 'name',
                    textField: 'name',
                    dummy: literals.ticketForm.lblSelectPestType
                });
                comboPestTypes.render();

                customerForm = new CustomerForm({el: this.$('table'), session: session});
                customerForm.render();
            };
            Backbone.View.apply(this,arguments);
        }
    });

    return TicketForm;
});
