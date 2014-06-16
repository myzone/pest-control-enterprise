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
            var panel = null;
            var tabsContainer = null;
            var tabIndex = null;
            var oldCloseCallback = null;

            this.$el = options.el;

            function submit() {
                var fields = {
                    pestType: {name: self.$('.pestType').combobox('getValue')},
                    problemDescription: self.$('.problemDescription').val(),
                    availabilityTime: []//[new Date(self.$('#availabilityTime').datebox('getValue')).getTime()]
                }
                customerForm.submit(function() {
                    _.extend(fields,customerForm.getValue());
                    if(!ticket) {
                        ticket = new Ticket({session: session});
                    }
                    ticket.set(fields);
                    ticket.save({},{
                        success: function() {
                            self.trigger('registered',ticket);
                            close();
                        },
                        error: function() {
                            self.trigger('requestRegistrationError',literals.requestRegistrationError);
                        }
                    });
                }, function() {
                    self.trigger('customerUpdateError',literals.customerUpdateError);
                });
            }

            function close() {
                tabIndex = tabsContainer.tabs('getTabIndex',panel);
                tabsContainer.tabs('close',tabIndex);
            }

            this.render = function() {

                this.$el.tabs('add', {
                    title: literals.ticketForm.lblTitle,
                    selected: true,
                    closable: true
                });

                panel = this.$el.tabs('getSelected');
                tabsContainer = this.$el;
                tabIndex = tabsContainer.tabs('getTabIndex',panel);

                var opts = this.$el.tabs('options');
                oldCloseCallback = opts.onClose;
                opts.onClose = function(title,index) {
                    //if(index === tabIndex) self.off();
                    opts.onClose = oldCloseCallback;
                    if(oldCloseCallback) {
                        oldCloseCallback(title,index);
                    }
                };

                $.parser.parse(panel.html(this.template(literals.ticketForm)));
                this.$el = panel;

                if(ticket) {
                    this.$('.editTicket').css('display','block');
                }

                comboPestTypes = new ComboBox({
                    collection: pestTypes,
                    el: this.$('.pestType'),
                    valueField: 'name',
                    textField: 'name',
                    dummy: literals.ticketForm.lblSelectPestType
                });
                comboPestTypes.render();

                customerForm = new CustomerForm({el: this.$('table'), session: session});
                customerForm.render();

                this.$('.button-ok').click(submit);
                this.$('.button-cancel').click(close);
            };
            Backbone.View.apply(this,arguments);
        }
    });

    return TicketForm;
});
