define([
        'models/SessionModel',
        'models/TicketsModel',
        'models/Ticket',
        'models/Customer',
        'widgets/LoginForm',
        'widgets/TicketsGrid',
        'widgets/TicketForm',
        'widgets/UserInfoPanel',
        'jquery',
        'easyui',
        'localization'
    ],

    function(SessionModel, TicketsModel, Ticket, Customer, LoginForm, TicketsView, TicketForm, LoggedUserInfoView, $) {
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

        session.on('statusChanged', function() {
           if(session.isLoggedIn() === true) {
               /*var customer = new Customer(
                   {
                       session: session,
                       name: 'Петров Гена'
                   });
               console.log(customer.isValid());
               console.log(customer.isNew());
               customer.fetch({
                   success: function() {
                       customer.set('cellPhone',customer.get('cellPhone')+'7');
                       console.log(customer.isValid());
                       console.log(customer.isNew());
                       customer.save();
                   }
               });*/
               /*var ticket = new Ticket({id:1, session: session});
               ticket.fetch({
                   success: function() {
                       _.delay(function() {
                           ticket.set('problemDescription','new comment');
                           ticket.set('comment','new comment');
                           console.log(ticket.save({},{
                               success: function() {
                                   alert(JSON.stringify(ticket));
                                   console.log(ticket.getTicketHistory());
                                   console.log(JSON.stringify(ticket));
                               }
                           }));
                       },100);
                   }
               });*/
//               ticket.set('comment', 'another comment!');
               /*ticket.set({
                   session: session,
                   customer: {name:'Ivan'},
                   pestType: {name:'crap'},
                   problemDescription: 'AAAAA!',
                   comment: '111111111111111'
               });*/
               //ticket.save();
           }
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
            var form = new TicketForm({el: $('#tabs'), session: session});
            form.render();
        });
        var userInfoView = new LoggedUserInfoView({
            el: $('.user-info-container'),
            model: session
        });
        userInfoView.render();
});
