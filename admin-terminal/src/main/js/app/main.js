define([
        'models/SessionModel',
        'models/TicketsModel',
        'widgets/LoginForm',
        'widgets/TicketsGrid',
        'widgets/UserInfoPanel',
        'widgets/TicketForm',
        'backbone',
        'underscore',
        'jquery',
        'easyui',
        'localization'
    ],
    function(session, TicketsModel, LoginForm, TicketsView, LoggedUserInfoView, TicketForm, Backbone, _, $) {
    //var mainRouter = new Router;
    //Backbone.history.start();
        $.parser.parse();

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

        /*session.on('statusChanged', function() {
           if(session.isLoggedIn() === true) {
               var customer = new Customer(
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
               });
               //ticket.save();
           }
        });*/

        var md = new TicketsModel;

        var tv = new TicketsView({
            el: $('#ticketsView')[0],
            model: md
        });
        tv.setToolbar('#ticketsToolbar');
        tv.setIdField('ticketid');
        tv.render();
        tv.on('createTicket', function(){
            var form = new TicketForm({el: $('#tabs'), session: session});
            form.render();
            form.on('requestRegistrationError',function(msg) {
                $.messager.alert(msg.title,msg.text,'error');
            });
            form.on('customerUpdateError',function(msg) {
                $.messager.alert(msg.title,msg.text,'error');
            });
            form.on('registered',function(ticket){
                md.add(ticket);
            });
        });

        tv.on('editTicket', function(ticket){
            var form = new TicketForm({el: $('#tabs'), session: session, ticket: ticket});
            form.render();
        });

        var userInfoView = new LoggedUserInfoView({
            el: $('.user-info-container'),
            model: session
        });
        userInfoView.render();
});
