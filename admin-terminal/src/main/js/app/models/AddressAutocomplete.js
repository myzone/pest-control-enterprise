define([
        'backbone',
        'underscore',
        'jquery'
    ],
function(Backbone, _, $){
    var Address = Backbone.Model.extend({
        defaults: {
            representation: '',
            longitude: null,
            latitude: null
        },
        sync: function(){}
    });

    var AddressAutocomplete = Backbone.Collection.extend({
        model: Address,
        constructor: function() {
            var query = '';
            var fetch = this.fetch;

            this.fetch = function(options) {
                if(options.query) query = options.query;
                options.reset = true;
                return fetch.apply(this,arguments);
            };

            this.sync = function(method, model, options) {
                if (method !== 'read') return false;
                $.getJSON('http://nominatim.openstreetmap.org/search?format=json&q='+ query,function(data){
                    var results = [];
                    for(var i=0;i<data.length;i++) {
                        results.push({
                            representation: data[i].display_name,
                            longitude: data[i].lon,
                            latitude: data[i].lat
                        });
                    }
                    options.success(results);
                });
                return true;
            };
            Backbone.Collection.apply(this,arguments);
        }
    });
    return new AddressAutocomplete;
});
