define(['underscore','backbone','jquery','easyui'], function( _ , Backbone, $) {

    var LoginForm = Backbone.View.extend({
        template: _.template($('#login-form-template').html()),
        initialize: function() {
            var self = this;

            this.listenToOnce(this.model,"statusChanged",onSessionStatusChanged);

            this.render = function () {
                this.$el.html(this.template({
                    lblTitle: 'Войти в систему',
                    lblLogin: 'Логин',
                    lblPassword: 'Пароль',
                    lblOk: 'Ок',
                    lblCancel: 'Отмена'
                }));
                $.parser.parse(this.$el);
                this.$el=$('.login-form');
                this.el=$('.login-form')[0];
                this.$('.button-ok').click(submitLoginForm);
                this.$('.button-cancel').click(cancel);
                this.$('input').keyup(function(e) {
                    if (e.keyCode == 13) {
                        submitLoginForm();
                    }
                });
                this.$el.dialog({
                    onClose: function() {
                        self.remove();
                    }
                });

                return this;
            }

            function submitLoginForm() {
                var login = self.$('.login').val();
                var password = self.$('.password').val();
                self.model.login(login, password);
            }

            function onSessionStatusChanged() {
                if(self.model.isLoggedIn()) {
                    self.$el.dialog('close');
                    //self.remove();
                }
            }

            function cancel() {
                self.$el.dialog('close');
                //self.remove();
            }
        }

    });

    return LoginForm;
});
