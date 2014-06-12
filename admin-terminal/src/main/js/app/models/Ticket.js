define(['models/Requester','backbone', 'literals', 'underscore'], function(requester, Backbone, literals, _){
    var Ticket = Backbone.Model.extend({
        defaults: {
            session: null,
            id: null,
            status: 'OPEN', // need to validate
            worker: null,
            availabilityTime: [], // format?
            customer: null, // required field. need to register new customer or use existing customer (by name)
            pestType: null, // required field {name: 'key'}
            problemDescription: null, //need to escape
            comment: '' // need to escape
        },

        constructor: function(session) {
            var allowedStatuses = ['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','CANCELED'];
            var needValidation = ['session','status','customer','pestType','problemDescription'];
            var editableFields = ['session','id','status','availabilityTime','customer','pestType','problemDescription','comment'];
            var lastModifiedAttrs = [];
            var ticketHistory = null;

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
                var attrs;
                if (key == null) return this;
                // Handle both `"key", value` and `{key: value}` -style arguments.
                if (typeof key === 'object') {
                    attrs = key;
                } else {
                    (attrs = {})[key] = val;
                }
                attrs = _.pick(attrs,editableFields);
                lastModifiedAttrs = _.union(lastModifiedAttrs, _.keys(attrs));
                this.attributes = _.extend(this.attributes,attrs);
                this.trigger('change', this);
                return this;
            };

            this.parse = function(response, options) {
                ticketHistory = response.taskHistory;
                return response;
            };

            this.getTicketHistory = function() {
                return ticketHistory;
            };

            function syncCreate(model, options) {
                delete model.attributes.id;
                requester.allocateTask(model.attributes, function(response) {
                    options.success(response.result);
                    lastModifiedAttrs.length=0;
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
                        }
                        options.success(response);
                        lastModifiedAttrs.length=0;
                    }
                );
            }

            function syncUpdate(model, options) {
                var changes = {
                    task: {id: model.get('id')}
                }
                var keys = _.keys(model.attributes);
                var values = Array.apply(null, new Array(keys.length))
                    .map(function(){ return null; });
                var partialChanges = _.object(keys, values);
                partialChanges = _.extend(partialChanges, changes,
                    _.pick(model.attributes, lastModifiedAttrs, 'session'));
                requester.editTask(partialChanges, function(response) {
                    options.success(response.result);
                    lastModifiedAttrs.length=0;
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

            this.unset = function() {
               // restricted
            };

            this.clear = function() {
                this.attributes = _.extend(this.attributes,defaults);
                this.trigger('change', this);
                return this;
            };
        }
    });
    return Ticket;
});
