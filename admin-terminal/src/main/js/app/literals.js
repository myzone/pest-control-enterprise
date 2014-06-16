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
           lblStatus: 'Стутс заявки',
           lblComment: 'Комментарий',
           lblCustomer: 'Информация о клиенте',
           lblTitle: 'Добавить заявку',
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
       },
       requestRegistrationError: {
           title: 'Ошибка регистрации вызова',
           text: 'Возникла ошибка при регистрации нового вызова. Обратитесь к системному администратору.'
       },
       customerUpdateError: {
           title: 'Ошибка регистрации клиента',
           text: 'Возникла ошибка при регистрации / обновлении клиента. Обратитесь к системному администратору.'
       },
       statusClasses: ['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','CANCELED'],
       statusDescriptions: {
           'OPEN': 'Открыта',
           'ASSIGNED': 'Назначена',
           'IN_PROGRESS': 'Исполняется',
           'RESOLVED': 'Завершена',
           'CLOSED': 'Закрыта',
           'CANCELED': 'Отменена'
       },
       ticketsGridColumns: [[
           {field:'colorStatus',title:''},
           {field:'ticketid',title:'Заявка #'},
           {field:'status',title:'Состояние'},
           {field:'creationDate',title:'Создана'},
           {field:'age',title:'Возраст'},
           {field:'lastModified',title:'Последнее изменение'},
           {field:'type',title:'Тип заявки / Описание'},
           {field:'worker',title:'Ответственный сотрудник'}
       ]]

   };
   return strings;
});
