define(['backbone','underscore','literals','jquery','easyui'],
 function(Backbone, _ , literals, $){
    var TicketsInfoPanel = Backbone.View.extend({
        constructor: function(options) {

            var model = options.model;
            var values = Array.apply(null, new Array(literals.statusClasses.length)).map(function(){ return 0; });
            var counter = _.object(literals.statusClasses, values);
            counter.total = 0;

            this.listenTo(model,'sync2', function(collection, options){
                if(!collection) return;
                delete counter['total'];
                for(var key in counter) {
                    counter[key] = collection.where({status: key}).length;
                }
                counter.total = collection.models.length;
                this.render();
            });

            this.render = function() {
                var ticketsStatus = _.template($('#tickets-status-template').html());
                var options = {};
                options = _.extend(options, literals.statusInfo, counter);
                this.$el.html(ticketsStatus(options));
            };
            Backbone.View.apply(this,arguments);
        }
    });
     return TicketsInfoPanel;
});
