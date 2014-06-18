define([
        'backbone',
        'underscore',
        'models/Customer',
        'models/SessionModel',
        'models/Requester'],
function(Backbone, _, Customer, session, requester){
    var CustomersAutocomplete = Backbone.Collection.extend({
        model: Customer,
        constructor: function() {
            var query = '%';
            var fetch = this.fetch;
            this.fetch = function(options) {
                if(options.query) query = '%'+_.escape(options.query)+'%';
                options.reset = true;
                return fetch.apply(this,arguments);
            };

            this.sync = function(method, model, options) {
                if(method!=='read') return false;
                requester.getCustomers(
                    session,
                    [{
                        name: 'customerAutocomplete',
                        search: query
                    }],
                    function(response) {
                        if(response !==null && response.result!== undefined) {
                            var customers = response.result.data;
                            options.success(customers);
                        } else {
                            options.error('customer loader failed');
                        }
                    }
                );
                return true;
            };
            Backbone.Collection.apply(this,arguments);
        }
    });
    return new CustomersAutocomplete;
});
