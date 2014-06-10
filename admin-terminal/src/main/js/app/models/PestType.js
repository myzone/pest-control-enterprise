define(['backbone', 'underscore'], function(Backbone, _ ){
    var pestType = Backbone.Model.extend({
        defaults: {
            name: '',
            description: ''
        },
        constructor: function() {
            var originalSet = this.set;
            this.set = function() {
                originalSet.apply(this,arguments);
                originalSet.call(this,{id: this.attributes.name});
            };
            this.toJSON = function() {
                return _.omit(this.attributes, 'id');
            };
            Backbone.Model.apply(this,arguments);
        },
        initialize: function() {
            // readonly model
            this.fetch = function() {};
            this.save = function() {};
            this.clear = function() {};
        }
    });
    return pestType;
});
