define(['underscore','backbone','jquery','easyui'], function( _ , Backbone, $) {

    var TicketForm = Backbone.View.extend({
        template: _.template($('#ticket-form-template').html()),
        initialize: function() {
            var self = this;

            this.render = function() {
                this.$el = $('<div/>');
                this.$el.html(this.template({
                    lblTitle: 'Создать заявку',
                    lblPestType: 'Тип вредителя',
                    lblSelectPestType: 'Пожалуйста, выберите тип вредителя...',
                    lblBriefDescription: 'Краткое описание проблемы',
                    lblCustomerName: 'Имя клиента',
                    lblHouseNumber: 'Номер/Название дома',
                    lblStreet: 'Улица',
                    lblTown: 'Город/Населенный пункт',
                    lblPreferredDay: 'Предпочитаемая дата приезда специалиста',
                    lblEmail: 'Email',
                    lblPhoneNumber: 'Телефон',
                    lblSubmit: 'Создать',
                    lblCancel: 'Отмена'
                }));
                $('body').append(this.$el);
                $.parser.parse(this.$el);
            };
        }
    });

    return TicketForm;
});
