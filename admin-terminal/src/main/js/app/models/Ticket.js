define(['models/Requester','backbone', 'literals', 'underscore'], function(requester, Backbone, literals, _){
    var Ticket = Backbone.Model.extend({
        defaults: {
            id: null,
            status: 'OPEN', // need to validate
            worker: null,
            availabilityTime: [], // format?
            customer: null, // required field. need to register new customer or use existing customer (by name)
            pestType: null, // required field {name: 'key'}
            problemDescription: null, //need to escape
            comment: null // need to escape
        },

        validate: function(attributes, options) {

            var allowedStatuses = ['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','CANCELED'];
            var needValidation = ['status','customer','pestType','problemDescription','comment'];

            if(_.indexOf(allowedStatuses, this.attributes['status']) === -1) {
                return literals['validationError'];
            }
            var values = _.values(_.pick(this.attributes,needValidation));
            for(var i=0;i<values.length;i++) {
                if(values[i] === null || values[i] === undefined) return literals['validationError'];
            }
        },

        initialize: function() {

            var self = this;
            var lastModifiedAttrs = null;
            var editableFields = ['id','status','availabilityTime','customer','pestType','problemDescription','comment'];

            var defaults = this.defaults;

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
                this.attributes = _.extend(this.attributes,attrs);
                this.trigger('change', this);
                return this;
            };

            this.unset = function() {
               // restricted
            };

            this.parse = function(response, options) {
                console.log(response);
            };

            this.clear = function() {
                this.attributes = _.extend(this.attributes,defaults);
                this.trigger('change', this);
                return this;
            };

            this.sync = function(method, model,options) {
                console.log(method);
                if(method === 'create') this.set('id','1');
                options.success({hi:'hello'});
            };
        }
    });
    return Ticket;
});
