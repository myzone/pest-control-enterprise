define([
    'literals',
    'underscore',
    'backbone',
    'models/PestTypes',
    'models/Ticket',
    'widgets/ComboBox',
    'widgets/CustomerForm',
    'jquery',
    'easyui'], function( literals, _, Backbone, PestTypes, Ticket, ComboBox, CustomerForm, $) {

    var TicketForm = Backbone.View.extend({
        template: _.template($('#ticket-form-template').html()),
        constructor: function(opts) {
            var options = opts;
            var self = this;
            var pestTypes = new PestTypes;
            var comboPestTypes = null;
            var customerForm = null;
            var session = options.session;
            var ticket = options.ticket;

            this.$el = options.el;

            function onOk() {
                var fields = {
                    pestType: {name: self.$('#pestType').combobox('getValue')},
                    problemDescription: self.$('#problemDescription').val(),
                    availabilityTime: [new Date(self.$('#availabilityTime').datebox('getValue')).getTime()]
                }
                customerForm.submit(function() {
                    _.extend(fields,customerForm.getValue());
                    if(!ticket) {
                        ticket = new Ticket({session: session});
                    }
                    ticket.set(fields);
                    ticket.save({},{
                        success: function() {
                            alert('Success!');
                        },
                        error: function() {
                            alert('Error when save ticket!');
                        }
                    });
                }, function() {
                    alert('Error on customer!');
                });
            }

            function onCancel() {

            }

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

                this.$('.button-ok').click(onOk);
                this.$('.button-cancel').click(onCancel);
            };
            Backbone.View.apply(this,arguments);
        }
    });

    return TicketForm;
});
