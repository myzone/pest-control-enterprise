define([
    'underscore',
    'backbone',
    'jquery',
    'easyui' ],
function( _, Backbone, $) {
    var Textarea = Backbone.View.extend({
        initialize: function(opts) {
            var self = this;
            var viewOptionsNames = ['el','value', 'getter'];

            this.setValue = function(value) {
                this.$el.val(_.escape(value));
                this.$el.validatebox('validate');
            };

            this.getValue = function() {
                return _.escape(this.$el.val());
            };

            this.map = function(model, field) {
                this.listenTo(model,'change:'+field,function(model, value, options){
                    if(!value) return false;
                    if(opts.getter) {
                        self.setValue(opts.getter(value));
                    } else {
                        self.setValue(value);
                    }
                });
            };

            this.isValid = function() {
                return this.$el.validatebox('isValid');
            };

            this.render = function() {
                if(opts.value) {
                    this.$el.val(_.escape(opts.value));
                }
                this.$el.validatebox(_.omit(opts, viewOptionsNames));
            };
        }
    });
    return Textarea;
});
