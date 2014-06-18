define(['backbone','underscore', 'jquery' ,'easyui'], function(Backbone, _ , $) {
    var comboBox = Backbone.View.extend({
        constructor: function(options) {
            var self = this;
            var viewOptionsNames = ['collection','el','value', 'getter'];
            var comboOpts = _.omit(options, viewOptionsNames);
            var viewOpts = _.pick(options,viewOptionsNames);

            this.$el = viewOpts.el;

            this.render = function() {
                comboOpts = _.extend(comboOpts,{
                    mode: 'remote',
                    loader: function(param, success, error) {
                        var fetchParams = {
                            success: function() {
                                success(viewOpts.collection.toJSON());
                            },
                            error: function() {
                                error('Combo loader error.');
                            }
                        };
                        if(param.q) {
                            fetchParams.query = param.q;
                        }
                        viewOpts.collection.fetch(fetchParams);
                        return true;
                    }
                });
                this.$el.combobox(comboOpts);
            };

            this.getValue = function() {
                return this.$el.combobox('getValue');
            };

            this.setValue = function(value) {
                if(_.isObject(value)) {
                    if(viewOpts.getter) {
                        this.$el.combobox('select', _.escape(viewOpts.getter(value)));
                    } else {
                        this.$el.combobox('select', value);
                    }
                } else {
                    this.$el.combobox('select', _.escape(value));
                }
            };

            this.isValid = function() {
                return this.$el.combobox('isValid');
            };

            this.map = function(model, field) {
                this.listenTo(model,'change:'+field,function(model, value, options){
                    if(!value) return false;
                    if(viewOpts.getter) {
                        self.setValue(viewOpts.getter(value));
                    } else {
                        self.setValue(value);
                    }
                });
            };
            Backbone.View.apply(this,arguments);
        }
    });
    return comboBox;
});
