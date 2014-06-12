define(['backbone','underscore', 'jquery' ,'easyui'], function(Backbone, _ , $) {
    var comboBox = Backbone.View.extend({
        constructor: function(options) {
            var opts = options;

            this.$el = options.el;

            this.render = function() {
                this.$el.combobox({
                    valueField: opts.valueField,
                    textField: opts.textField,
                    mode: 'remote',
                    editable: false,
                    loader: function(param, success, error) {
                        opts.collection.fetch({
                            success: function() {
                                success(opts.collection.toJSON());
                            },
                            error: function() {
                                error('Combo loader error.');
                            }
                        });
                        return true;
                    }
                });

            };

            this.getValue = function() {
                return this.$el.combobox('getValue');
            };



        }
    });
    return comboBox;
});
