define(['backbone','underscore'], function(Backbone, _) {
    var HistoryItem = Backbone.Model.extend({
        defaults: {
            instant: 0,
            causer: {name: ''},
            comment: ''
        },
        sync: function() {}
    });

    var History = Backbone.Collection.extend({
        model: HistoryItem
    });

    return History;
});
