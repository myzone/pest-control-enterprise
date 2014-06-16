define(
    ['backbone', 'literals', 'models/SessionModel', 'models/Ticket', 'models/Requester', 'underscore'],
    function(Backbone, literals, session, Ticket, requester, _) {

    var TicketsModel = Backbone.Collection.extend({
        model: Ticket,
        initialize: function() {
            var self = this;
            var sessionModel = session;

            var columns=literals.ticketsGridColumns;
            var data=[];
            var statusClasses=literals.statusClasses;
            var statusDescription=literals.statusDescriptions;

            this.listenTo(sessionModel,'statusChanged',function() {
                if(sessionModel.isLoggedIn())
                    this.trigger('refresh');
            });

            this.getColumns = function() {
                return columns;
            };



            this.getData = function(param, successCb, errorCb) {
                var total = param.page*param.rows;

                if(sessionModel.getSessionId()===null) {
                    errorCb("Session error!");
                    return false;
                }

                if(self.models.length<total || !param.rows) {
                    var count = param.rows;
                    var offset = (param.page-1)*param.rows;
                    var response = null;
                    var filters = [];
                    if(param.rows) {
                        filters = [{
                            name: 'paging',
                            offset: offset,
                            count: count
                        }];
                    }
                    requester.getTasks(
                        sessionModel, filters,
                        function(response) {
                            if(response !==null && response.result!== undefined) {
                                self.set(response.result.data,{silent:true});
                                var totalField = self.models.length;
                                if(response.result.data.length === count) {
                                    totalField++;
                                }
                                var response = {
                                    total: totalField,
                                    rows: self.models
                                };
                                successCb(response);
                            } else {
                                errorCb("Some error!");
                            }
                        });
                } else {
                    var response={
                        total: self.models.length+1,
                        rows: self.slice((param.page-1)*param.rows,(param.page-1)*param.rows+param.rows)
                    };
                    successCb(response);
                }
                return true;
            };
        }
    });

    return TicketsModel;
});
