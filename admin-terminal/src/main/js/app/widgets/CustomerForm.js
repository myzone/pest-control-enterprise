define(
    [
        'backbone',
        'underscore',
        'literals',
        'widgets/ComboBox',
        'widgets/Textarea',
        'models/Requester',
        'models/Customer',
        'models/CustomersAutocomplete',
        'models/AddressAutocomplete',
        'jquery',
        'easyui'],

function(Backbone, _ , literals, ComboBox, Textarea, requester, Customer, customersAutocomplete, addressAutocomplete, $) {

   var customerForm = Backbone.View.extend({
       constructor: function(opts) {
           var options = opts;
           var self = this;
           var ticket = opts.ticket;
           var controls = {};

           var customer = new Customer({
                session: options.session
           });

           this.template = _.template($('#customer-form-template').html());
           this.$el = options.el;

           function onSelectCustomer(record) {
               if(!record) return;
               if(record.name) {
                   record.id = record.name;
                   customer.id = record.name
               }
               customer.set(record);
           }

           function onSelectAddress(record) {
               if(!record) return;
               customer.set('address',record);
           }

           this.submit = function(callback, onerror) {
               if(!this.isValid()) {
                   self.trigger('invalid',literals.requiredFieldsError);
                   return false;
               }
               var fields = {};
               for(var key in controls) {
                   if(key === 'address') {
                       fields[key] = customer.get(key);
                       fields[key].representation = controls[key].getValue();
                       continue;
                   }
                   fields[key] = controls[key].getValue();
               }
               customer.set(fields,{silent:true});
                /*if(customer.has('address')) {
                    var addr = customer.get('address');
                    addr.representation =  _.escape(self.$('.address').combobox('getValue'));
                    customer.set('address',addr,{silent: true});
                }
                customer.set({
                    name: _.escape(self.$('.customerName').combobox('getValue')),
                    email: _.escape(self.$('.email').val()),
                    cellPhone: _.escape(self.$('.cellPhone').val())
                },{silent:true});*/

                if(customer.get('id')!==customer.get('name')) {
                    customer.unset('id');
                }
                customer.save({},{
                    success: callback,
                    error: onerror
                })
           };

           this.getValue = function() {
                return {name: customer.get('name')};
           };

           this.isValid = function() {
               for(var key in controls) {
                   if(!controls[key].isValid()) return false;
               }
               return true;
           };

           this.setValue = function() {};

           this.render = function() {
               $.parser.parse(this.$el.append(this.template(literals.customerForm)));
               var customerField = new ComboBox({
                   el:  this.$('.name'),
                   valueField: 'name',
                   textField: 'name',
                   mode: 'remote',
                   editable: true,
                   required:true,
                   validType:['customerName','length[1,100]'],
                   hasDownArrow:false,
                   onSelect: onSelectCustomer,
                   collection: customersAutocomplete
               });
               customerField.render();
               customerField.map(customer,'name');
               controls.name = customerField;

               var address = new ComboBox({
                   el:this.$('.address'),
                   collection: addressAutocomplete,
                   textField: 'representation',
                   mode: 'remote',
                   editable: true,
                   required:true,
                   validType:'length[1,200]',
                   hasDownArrow:false,
                   onSelect: onSelectAddress,
                   getter: function(address) {return address.representation;}
               });
               address.render();
               address.map(customer,'address');
               controls.address = address;

               var email = new Textarea({
                   el: this.$('.email'),
                   required:true,
                   validType:['email','length[1,100]']
               });
               email.render();
               email.map(customer,'email');
               controls.email = email;

               var cellPhone = new Textarea({
                   el: this.$('.cellPhone'),
                   required:true,
                   validType:['phone','length[4,40]']
               });
               cellPhone.render();
               cellPhone.map(customer,'cellPhone');
               controls.cellPhone = cellPhone;

               if(ticket) {
                   customer.set({
                       id: ticket.get('customer').name,
                       name: ticket.get('customer').name
                   });
               }
               if(ticket) {
                   customer.fetch();
               }
           };

           Backbone.View.apply(this,arguments);
       }
   });
    return customerForm;
});
