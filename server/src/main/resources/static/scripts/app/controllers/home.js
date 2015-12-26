angular.module('securedApp')
    .controller('HomeCtrl', ['$location', '$rootScope', '$scope', 'AuthService',
        function($location, $rootScope, $scope, AuthService) {
            $rootScope.location = "home";
            if (!$rootScope.authenticated || !$rootScope.principal) {
                AuthService.getUser()
                    .success(function(data) {
                        $rootScope.principal = data;
                        $rootScope.authenticated = true;
                        $location.path("/");
                    }).error(function() {
                        $rootScope.authenticated = false;
                        $location.path("/login");
                    });
            }
        }
    ]);