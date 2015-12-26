angular.module('securedApp')
    .controller('LoginCtrl', ['$location', '$rootScope', '$scope', 'AuthService',
        function($location, $rootScope, $scope, AuthService) {
            $rootScope.location = "login";
            $scope.login = function() {
                AuthService.login($scope.credentials.username, $scope.credentials.password)
                    .success(function(data) {
                        $rootScope.principal = data;
                        $rootScope.authenticated = true;
                        $scope.error = false;
                        $location.path("/");
                    }).error(function() {
                        $rootScope.authenticated = false;
                        $scope.error = true;
                        $location.path("/login");
                    });
            };
        }
    ]);