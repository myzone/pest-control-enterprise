define([
        'underscore',
        'backbone',
        'literals' ],
function( _, Backbone, literals) {

    var Status = Backbone.Model.extend({
        defaults: {
            value: '',
            text: ''
        },
        sync: function() {}
    });

    var StatusCollection = Backbone.Collection.extend({
        model: Status,
        fetch: function(options) {
            options.success();
        }
    });
    var getInstance = _.once(function(){
        var keys = _.keys(literals.statusDescriptions);
        var values = _.values(literals.statusDescriptions);
        var data= [];
        for(var i=0;i<keys.length; i++) {
            data.push({
                value: keys[i],
                text: values[i]
            });
        }
        var statusCollection = new StatusCollection(data);
        return statusCollection;
    });
    return getInstance();
});
