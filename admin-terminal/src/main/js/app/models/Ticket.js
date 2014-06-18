define(['models/Requester','models/SessionModel','backbone', 'literals', 'underscore'], function(requester, session, Backbone, literals, _){
    var Ticket = Backbone.Model.extend({
        defaults: {
            session: session,
            id: null,
            status: 'OPEN', // need to validate
            executor: null,
            availabilityTime: [], // format?
            customer: null, // required field. need to register new customer or use existing customer (by name)
            pestType: null, // required field {name: 'key'}
            taskHistory: null,
            problemDescription: null, //need to escape
            comment: '' // need to escape
        },

        constructor: function(session) {
            var allowedStatuses = literals.statusClasses;
            var needValidation = ['session','status','customer','pestType','problemDescription'];
            var editableFields = ['session','id','status','availabilityTime','customer','pestType','problemDescription','comment','taskHistory','executor'];
            var lastModifiedAttrs = [];
            var oldSet = this.set;

            this.validate = function(attributes, options) {
                if(_.indexOf(allowedStatuses, this.attributes['status']) === -1) {
                    return literals['validationError'];
                }
                var values = _.values(_.pick(this.attributes,needValidation));
                for(var i=0;i<values.length;i++) {
                    if(values[i] === null || values[i] === undefined) return literals['validationError'];
                }
            };

            this.set = function (key, val) {
                var result = oldSet.apply(this,arguments);
                if(this.has('id')) {
                    this.id = this.get('id');
                }
                return result;
            };

            function syncCreate(model, options) {
                delete model.attributes.id;
                requester.allocateTask(model.attributes, function(response) {
                    if(response !== null && response.result) {
                        model.unset('comment');
                        options.success(response.result);
                    } else {
                        options.error('Create ticket failed');
                    }

                });
                return true;
            }

            function syncRead(model, options) {
                requester.getTasks(
                    model.get('session'),
                    [{
                        name: 'taskById',
                        id: model.get('id')
                    }],
                    function(response) {
                        if(response !== null && response.result) {
                            response = response.result.data[0];
                            model.unset('comment');
                            options.success(response);
                        } else {
                            options.error('Ticket read failed.');
                        }
                    }
                );
            }

            function syncUpdate(model, options) {
                var changes = {
                    task: {id: model.get('id')}
                }
                var keys = _.keys(_.omit(model.attributes,'id'));
                var values = Array.apply(null, new Array(keys.length))
                    .map(function(){ return null; });
                var partialChanges = _.object(keys, values);
                partialChanges = _.extend(partialChanges, changes,
                    _.omit(model.attributes, 'taskHistory','id','executor','customer'));
                requester.editTask(partialChanges, function(response) {
                    if(response !== null && response.result) {
                        model.unset('comment');
                        options.success(response.result);
                    } else {
                        options.error('Ticket update failed');
                    }
                });
            }

            this.sync = function(method, model,options) {
                var methodMap = {
                    create: syncCreate,
                    read: syncRead,
                    update: syncUpdate,
                    delete: function() {console.log('Delete unsupported');}
                }
                return methodMap[method](model,options);
            };
            Backbone.Model.apply(this, arguments);
        },

        initialize: function() {

            var self = this;
            var defaults = this.defaults;

            this.clear = function() {
                this.attributes = _.extend(this.attributes,defaults);
                this.trigger('change', this);
                return this;
            };
        }
    });
    return Ticket;
});
