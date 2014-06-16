define(['backbone', 'underscore', 'models/PestType','models/Requester'], function(Backbone, _ , PestType, requester){
    var pestTypes = Backbone.Collection.extend({
        model: PestType,
        constructor: function() {
            function isSessionStorageAvailable() {
                try {
                    return 'sessionStorage' in window && window['sessionStorage'] !== null;
                } catch (e) {
                    return false;
                }
            }

            var isStorageAvail = isSessionStorageAvailable();

            this.sync = function(method, model, options) {
                if(method !=='read') return false;
                var pestTypes = null;
                if(isStorageAvail) {
                    pestTypes = JSON.parse(sessionStorage.getItem('pestTypes'));
                }
                if(pestTypes === null) {
                    requester.getPestTypes(function(response) {
                        if(response !==null && response.result!== undefined) {
                            sessionStorage.setItem('pestTypes',JSON.stringify(response.result.data));
                            options.success(response.result.data);
                        } else {
                            options.error(response);
                        }

                    });
                } else {
                    options.success(pestTypes);
                }
            };
            Backbone.Collection.apply(this,arguments);
        }
    });
    return pestTypes;
});
