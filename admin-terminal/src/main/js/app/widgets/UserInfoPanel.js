define(['underscore','backbone','jquery','easyui'], function( _ , Backbone, $) {
    var LoggedUserInfoView = Backbone.View.extend({
        template: _.template($('#user-info-panel-template').html()),
        initialize: function() {
            var self=this;
            var rendered = false;

            function onSessionStatusChanged() {
                if(self.model.isLoggedIn()) {
                    self.$('.lbl-text').html('Вы вошли как:');
                    self.$('.user-login').html(self.model.getUserName());
                    self.$('.user-login').show();
                    self.$('.logout-button').show();
                } else {
                    self.$('.lbl-text').html('Вы не вошли в систему.');
                    self.$('.user-login').hide();
                    self.$('.logout-button').hide();
                }
            }

            this.listenTo(this.model,"statusChanged",onSessionStatusChanged);

            this.render = function() {
                if(rendered) return this;
                this.$el.html(this.template({lblExit: 'Выйти'}));
                $.parser.parse(this.$el);
                onSessionStatusChanged();
                this.$('.logout-button').click(function() {
                    self.model.logout();
                });
                rendered = true;
                return this;
            }
        }
    });

    return LoggedUserInfoView;
});
