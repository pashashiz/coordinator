angular.module('securedApp')
    .controller('NavigationCtrl', ['$location', '$rootScope', '$scope', 'AuthService',
        function($location, $rootScope, $scope, AuthService) {
            $scope.logout = function() {
                AuthService.logout().finally(
                    function () {
                        $rootScope.authenticated = false;
                        $location.path("/login");
                    });
            };
        }
    ]);