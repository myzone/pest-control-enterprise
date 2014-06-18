define([
    'literals',
    'underscore',
    'backbone',
    'models/PestTypes',
    'models/Ticket',
    'models/StatusCollection',
    'models/History',
    'widgets/ComboBox',
    'widgets/CustomerForm',
    'widgets/Textarea',
    'widgets/HistoryGrid',
    'jquery',
    'easyui'],
function( literals, _, Backbone, PestTypes, Ticket, statusCollection, History, ComboBox, CustomerForm, Textarea, HistoryGrid, $) {

    var TicketForm = Backbone.View.extend({
        constructor: function(opts) {
            var oldRemove = this.remove;
            var options = opts;
            var self = this;
            var pestTypes = new PestTypes;
            var session = options.session;
            var ticket = options.ticket;

            var customerForm = null;
            var panel = null;
            var tabsContainer = null;
            var tabIndex = null;
            var literalsType = null;
            var controls = {};

            this.$el = options.el;

            if(ticket) {
                this.template = _.template($('#edit-ticket-form-template').html());
                literalsType = 'editTicketForm';
            } else {
                this.template = _.template($('#ticket-form-template').html());
                literalsType = 'ticketForm';
            }

            function prepareForm() {
                var control = null;
                for(var key in controls) {
                    control = controls[key];
                    control.setValue(ticket.get(key));
                }
            }

            function submitTicket() {
                var fields = {
                    availabilityTime: []
                };
                for(var key in controls) {
                    if(key === 'pestType') {
                        fields[key] = {name: controls[key].getValue()};
                        continue;
                    }
                    fields[key] = controls[key].getValue();
                }
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
            }

            function submit() {
                if(!self.isValid()) {
                    self.trigger('invalid',literals.requiredFieldsError);
                    return false;
                }
                customerForm.submit(function() {
                    submitTicket();
                }, function() {
                    self.trigger('customerUpdateError',literals.customerUpdateError);
                });
            }

            function close() {
                tabIndex = tabsContainer.tabs('getTabIndex',panel);
                tabsContainer.tabs('close',tabIndex);
                self.remove();
            }

            this.isValid = function() {
                for(var key in controls) {
                    if(!controls[key].isValid()) return false;
                }
                return true;
            };

            this.remove = function() {
                for(var key in controls) {
                    controls[key].remove();
                }
                customerForm.remove();
                oldRemove.apply(this, arguments);
            };

            this.render = function() {

                this.$el.tabs('add', {
                    title: literals[literalsType].lblTitle,
                    selected: true,
                    closable: false
                });

                panel = this.$el.tabs('getSelected');
                tabsContainer = this.$el;
                tabIndex = tabsContainer.tabs('getTabIndex',panel);
                $.parser.parse(panel.html(this.template(literals[literalsType])));

                this.$el = panel;

                var comboPestTypes = new ComboBox({
                    collection: pestTypes,
                    el: this.$('.pestType'),
                    valueField: 'name',
                    textField: 'name',
                    dummy: literals[literalsType].lblSelectPestType,
                    required: true,
                    editable: false,
                    getter: function(value) {return value.name;}
                });
                comboPestTypes.render();
                controls.pestType = comboPestTypes;

                var textarea = new Textarea({
                    el:this.$('.problemDescription'),
                    required:true,
                    validType:'length[1,2048]'
                });
                textarea.render();
                controls.problemDescription = textarea;

                customerForm = new CustomerForm({el: this.$('table:first'), session: session, ticket: ticket});
                customerForm.render();
                controls.customer = customerForm;

                if(ticket) {
                    var statusCombo = new ComboBox({
                        collection: statusCollection,
                        el: this.$('.status'),
                        valueField: 'value',
                        textField: 'text',
                        required: true,
                        editable: false
                    });
                    statusCombo.render();
                    controls.status = statusCombo;

                    var comment = new Textarea({
                        el:this.$('.comment'),
                        required:true,
                        validType:'length[1,2048]'
                    });
                    comment.render();
                    controls.comment = comment;

                    var history = new History(ticket.get('taskHistory'));
                    var historyGrid = new HistoryGrid({
                        el:this.$('.historyView'),
                        collection: history
                    });
                    historyGrid.render();

                    prepareForm();
                }

                this.$('.button-ok').click(submit);
                this.$('.button-cancel').click(close);
            };
            Backbone.View.apply(this,arguments);
        }
    });

    return TicketForm;
});
