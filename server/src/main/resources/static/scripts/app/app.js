angular.module('securedApp', ['ngRoute', 'securityServices'])
    .config(function($routeProvider, $httpProvider) {
        $routeProvider.when('/', {
            templateUrl : 'views/home.html',
            controller : 'HomeCtrl'
        }).when('/login', {
            templateUrl : 'views/login.html',
            controller : 'LoginCtrl'
        }).otherwise('/');
        // With the following header Spring Security responds to it by not sending a “WWW-Authenticate” header
        // in a 401 response, and thus the browser will not pop up an authentication dialog
        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
    });