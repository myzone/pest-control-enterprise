require.config({
    baseUrl: 'app',
    shim: {
        easyui: ['jquery'],
        underscore: {
            exports: '_'
        },
        backbone: {
            deps: [
                'underscore',
                'jquery'
            ],
            exports: 'Backbone'
        },
        localization: ['easyui']
    },
    paths: {
        jquery: '../jquery',
        underscore: '../underscore',
        backbone: '../backbone',
        easyui: '../easyui',
        localization: '../easyui-lang-ru',
        moment: '../moment'
    }
});

require(['main']);