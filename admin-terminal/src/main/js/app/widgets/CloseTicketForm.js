define([
        'backbone',
        'underscore',
        'literals',
        'models/SessionModel',
        'models/Requester',
        'jquery',
        'easyui'
],
function(Backbone, _, literals, session, requester, $){
    var CloseTicketForm = Backbone.View.extend({
        constructor: function(options) {
            var self = this;
            var ticket = options.ticket;
            var defaults = _.clone(ticket.defaults);
            defaults.status = 'CLOSED';
            defaults.session = session;
            defaults.task = {id: ticket.get('id')};

            this.render = function() {
                $.parser.parse(this.$el.append(_.template($('#close-ticket-template').html())(literals.closeTicketDialog)));
                $('.close-ticket').dialog('open');
                this.$el = $('.close-ticket');

                this.$('.close').click(function() {
                    requester.editTask(defaults,function(response){
                        if(response !== null && response.result) {
                            ticket.set(response.result);
                            self.trigger('closed', ticket);
                            $('.close-ticket').dialog('close');
                            self.remove();
                        } else {
                            self.trigger('ticketCloseError',literals.ticketCloseError);
                        }
                    });
                });

                this.$('.cancel').click(function() {
                    $('.close-ticket').dialog('close');
                    self.remove();
                });
            };
            Backbone.View.apply(this,arguments);
        }
    });
    return CloseTicketForm;
});
