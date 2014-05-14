define([
        'models/SessionModel',
        'models/TicketsModel',
        'widgets/LoginForm',
        'widgets/TicketsGrid',
        'widgets/TicketForm',
        'widgets/UserInfoPanel',
        'jquery',
        'easyui',
        'localization'
    ],

    function(SessionModel, TicketsModel, LoginForm, TicketsView, TicketForm, LoggedUserInfoView, $) {
        $.parser.parse();

        var session = new SessionModel;
        session.on('loginError',function(msg) {
            $.messager.alert(msg.title,msg.text,'error');
        });

        session.on('notAuthorizedError',function(msg) {
            var loginForm = new LoginForm({
                el: $('.windows')[0],
                model: session
            });
            loginForm.render();
        });

        var md = new TicketsModel;
        md.setSession(session);

        var tv = new TicketsView({
            el: $('#ticketsView')[0],
            model: md
        });
        tv.setToolbar('#ticketsToolbar');
        tv.setIdField('ticketid');
        tv.render();
        tv.on('createRequest', function(){
            var form = new TicketForm;
            form.render();
        });
        var userInfoView = new LoggedUserInfoView({
            el: $('.user-info-container'),
            model: session
        });
        userInfoView.render();

});
