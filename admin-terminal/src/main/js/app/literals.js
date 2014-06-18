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
       editTicketForm: {
           lblStatus: 'Статус заявки',
           lblComment: 'Комментарий',
           lblTitle: 'Редактировать заявку',
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
           lblSubmit: 'Сохранить',
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
       ticketCloseError: {
           title: 'Ошибка закрытия заявки',
           text: 'Возникла ошибка при закрытии заявки. Обратитесь к системному администратору.'
       },
       requiredFieldsError: {
           title: 'Ошибка',
           text: 'Пожалуйста, заполните все необходимые поля.'
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
       statusInfo: {
           'lblTotal': 'Всего',
           'lblOpen': 'Открыто',
           'lblAssigned': 'Назначено',
           'lblInProgress': 'Исполняется',
           'lblResolved': 'Завершено',
           'lblClosed': 'Закрыто',
           'lblCanceled': 'Отменено'
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
       ]],
       closeTicketDialog: {
           lblCloseTicket: 'Закрыть заявку',
           lblClose: 'Закрыть',
           lblCancel: 'Отмена'
       }

   };
   return strings;
});
