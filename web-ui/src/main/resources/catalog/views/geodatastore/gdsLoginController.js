(function () {
  goog.provide('geodatastore_login');

  var module = angular.module('geodatastore_login', []);

  module.controller('GdsLoginController', ['$scope', '$http', '$location', '$rootScope',
    function ($scope, $http, $location, $rootScope) {
      var authenticate = function (credentials, callback) {

        var cred = credentials || {};
        $http.post('../../j_spring_security_check#' + $location.path(), $.param(cred),
            {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).success(function (data) {
              $scope.loadCatalogInfo().then(function () {
                callback && callback();
              });

            }).error(function () {
              $scope.authenticated = false;
              $scope.loadCatalogInfo().then(function () {
                callback && callback();
              });
            });


      }

      $scope.loadCatalogInfo();
      $scope.credentials = {};
      $scope.login = function () {
        authenticate($scope.credentials, function () {
          if ($scope.authenticated) {
            $scope.signinFailure = false;
          } else {
            $scope.signinFailure = true;
          }
        });
      };

    }]);

})();