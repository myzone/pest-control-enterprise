define(['backbone', 'underscore', 'models/Requester'], function(Backbone, _ , requester){
    var customer = Backbone.Model.extend({
        defaults: {
            name: '',
            address: {
                representation: '',
                latitude: null,
                longitude: null
            },
            cellPhone: ''
        },
        validate: function(attributes, options) {
            var validators = {
                name: /^[A-za-zА-Яа-я-\s]+$/ig ,
                cellPhone: /^[0-9]+$/ig,
                email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ig
            };
            var result = true;
            for(var key in validators) {
                if(attributes[key] === undefined) return 'error';
                result = validators[key].test(attributes[key]);
                if(result === false) return 'error';
            }
        },
        constructor: function() {

            var is_new = true;

            var syncHandlers = {
                read: function(model, options){
                    requester.getCustomers(
                        model.get('session'),[{
                            name: 'customerByName',
                            customer: model.get('name')
                        }],
                        function(response){
                            if(response !==null && response.result!== undefined) {
                                is_new = false;
                                options.success(response.result.data[0]);
                            } else {
                                options.error(response);
                            }
                    });
                },
                create: function(model, options){
                    requester.registerCustomer(
                        model.get('session'),model,
                        function(response){
                            if(response !==null && response.result!== undefined) {
                                is_new = false;
                                options.success(response.result);
                            } else {
                                options.error(response);
                            }
                        });
                },
                update: function(model, options){
                    requester.editCustomer(model.get('session'),model,
                        function(response){
                            if(response !==null && response.result!== undefined) {
                                options.success(response.result);
                            } else {
                                options.error(response);
                            }
                        });
                },
                delete: function(){}
            };

            var originalSet = this.set;

            this.isNew = function() {
                return is_new;
            };

            this.set = function() {
                originalSet.apply(this,arguments);
                if(this.attributes.name) {
                    originalSet.call(this,{id: this.attributes.name});
                    if(this.isValid()) is_new=false;
                }
                return this;
            };

            this.toJSON = function() {
                return _.omit(this.attributes, 'id','session');
            };

            this.sync = function(method, model, options) {
                return syncHandlers[method](model,options);
            };

            Backbone.Model.apply(this,arguments);
        }
    });
    return customer;
});
