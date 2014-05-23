define(['underscore','backbone','jquery','easyui'], function( _ , Backbone, $) {
    var LoggedUserInfoView = Backbone.View.extend({
        template: _.template($('#user-info-panel-template').html()),
        initialize: function() {
            var self=this;
            function onSessionStatusChanged() {
                if(self.model.isLoggedIn()) {
                    self.$('.lbl-text').html('Вы вошли как:');
                    self.$('.user-name').html(self.model.getUserName());
                    self.$('.user-name').show();
                    self.$('.logout-button').show();
                } else {
                    self.$('.lbl-text').html('Вы не вошли в систему.');
                    self.$('.user-name').hide();
                    self.$('.logout-button').hide();
                }
            }

            this.listenTo(this.model,"statusChanged",onSessionStatusChanged);

            this.render = function() {
                this.$el.html(this.template({lblExit: 'Выйти'}));
                $.parser.parse(this.$el);
                onSessionStatusChanged();
                this.$('.logout-button').click(function() {
                    self.model.logout();
                });
                return this;
            }
        }
    });

    return LoggedUserInfoView;
});