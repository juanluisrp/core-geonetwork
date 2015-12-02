(function () {
  goog.provide('geodatastore_login');

  goog.require('gds_login_service');

  var module = angular.module('geodatastore_login', ['ui.bootstrap.showErrors', 'ngAnimate', 'gds_login_service']);
  module.config(['$animateProvider', function($animateProvider){
    // do not animate the elements with CSS class fa-spinner.
    $animateProvider.classNameFilter(/^((?!(fa-spinner)).)*$/);
  }]);

  module.controller('GdsLoginController', ['$scope', '$http', 'gdsLoginFactory',
    function ($scope, $http, gdsLoginFactory) {

      $scope.loginForm = {};

      $scope.loadCatalogInfo();


      $scope.login = function () {
        $scope.signinFailure = false;
        if ($scope.gnSigningForm.$valid) {
          $scope.loginWorking = true;
          gdsLoginFactory.login($.param($scope.loginForm)).then(
              function(data) {
                if (data.status === true) {
                  // We are logged-in
                  $scope.loadCatalogInfo().finally(function() {
                    $scope.signinFailure = false;
                    $scope.loginWorking = false;
                  });
                } else {
                  $scope.signinFailure = true;
                  $scope.loginWorking = false;
                }
              },
              function() {
                $scope.signinFailure = true;
                $scope.loginWorking = false;
              }
          ).finally(function() {
            $scope.loginWorking = false;
          });
        }
      };

    }]);

})();