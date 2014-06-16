define(
    [
        'backbone',
        'underscore',
        'literals',
        'models/Requester',
        'models/Customer',
        'jquery',
        'easyui'],

function(Backbone, _ , literals, requester, Customer, $) {
   var customerForm = Backbone.View.extend({
       constructor: function(opts) {
           var options = opts;
           var self = this;

           var customer = new Customer({
                session: options.session
           });

           this.template = _.template($('#customer-form-template').html());
           this.$el = options.el;

           function onSelectCustomer(record) {
               customer.set('id',record.name);
               customer.set(record);
           }

           function onSelectAddress(record) {
               customer.set('address',record);
           }

           customer.on('change',function(){
               //if(this.isValid()) {
                   self.$('.address').combobox('setValue', _.escape(this.get('address').representation));
                   //self.$('#address').combobox('validate');

                   self.$('.email').attr('value', _.escape(this.get('email')));
                   self.$('.email').validatebox('validate');

                   self.$('.cellPhone').attr('value', _.escape(this.get('cellPhone')));
                   self.$('.cellPhone').validatebox('validate');
               //}
           });

           this.submit = function(callback, onerror) {
                if(customer.has('address')) {
                    var addr = customer.get('address');
                    addr.representation =  _.escape(self.$('.address').combobox('getValue'));
                    customer.set('address',addr,{silent: true});
                }
                customer.set({
                    name: _.escape(self.$('.customerName').combobox('getValue')),
                    email: _.escape(self.$('.email').val()),
                    cellPhone: _.escape(self.$('.cellPhone').val())
                },{silent:true});
                if(customer.get('id')!==customer.get('name')) {
                    customer.unset('id');
                }
                customer.save({},{
                    success: callback,
                    error: onerror
                })
           };

           this.getValue = function() {
                return {customer: {name: customer.get('name')}};
           };

           this.render = function() {
               $.parser.parse(this.$el.append(this.template(literals.customerForm)));

               this.$('.customerName').combobox({
                   valueField: 'name',
                   textField: 'name',
                   mode: 'remote',
                   editable: true,
                   required:true,
                   validType:'length[1,100]',
                   hasDownArrow:false,
                   onSelect: onSelectCustomer,
                   loader: function(param, success, error) {
                       if(param.q) {
                           requester.getCustomers(
                               options.session,
                               [{
                                   name: 'customerAutocomplete',
                                   search: '%' + _.escape(param.q) + '%'
                               }],
                               function(response) {
                                   if(response !==null && response.result!== undefined) {
                                        success(response.result.data);
                                   } else {
                                       error('customer loader failed');
                                   }
                               }
                           )
                       } else {
                           return false;
                       }
                       return true;
                   }
               });

               this.$('.address').combobox({
                   valueField: 'representation',
                   textField: 'representation',
                   mode: 'remote',
                   editable: true,
                   required:true,
                   validType:'length[1,200]',
                   hasDownArrow:false,
                   onSelect: onSelectAddress,
                   loader: function(param, success, error) {
                       if(param.q) {
                           $.getJSON('http://nominatim.openstreetmap.org/search?format=json&q='+ _.escape(param.q),function(data){
                              var results = [];
                              for(var i=0;i<data.length;i++) {
                                  results.push({
                                     representation: data[i].display_name,
                                     longitude: data[i].lon,
                                     latitude: data[i].lat
                                  });
                              }
                              success(results);
                           });
                       } else {
                           return false;
                       }
                       return true;
                   }
               });

           };

           Backbone.View.apply(this,arguments);
       }
   });
    return customerForm;
});
