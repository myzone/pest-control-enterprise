define(function() {
   var strings = {
       notAuthorizedError: {
           title: 'Ошибка авторизации',
           text: 'Вы не авторизированы или время сессии истекло.'
       },
       loginError: {
           title: 'Ошибка авторизации',
           text: 'Введен неправильный логин или пароль.'
       },
       validationError: 'Validation error',
       ticketForm: {
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
       },
       customerForm: {
           lblCustomerName: 'Имя клиента',
           lblAddress: 'Адрес',
           lblEmail: 'Email',
           lblPhoneNumber: 'Телефон'
       }
   }
   return strings;
});
