define(['backbone', 'underscore', 'models/PestType','models/Requester'], function(Backbone, _ , PestType, requester){
    var pestTypes = Backbone.Collection.extend({
        model: PestType,
        constructor: function() {
            this.sync = function(method, model, options) {
                if(method !=='read') return false;
                requester.getPestTypes(function(response) {
                    if(response !==null && response.result!== undefined) {
                        options.success(response.result.data);
                    } else {
                        options.error(response);
                    }

                });
            };
            Backbone.Collection.apply(this,arguments);
        }
    });
    return pestTypes;
});
