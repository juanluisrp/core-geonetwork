(function() {

  goog.provide('gn_search_geodatastore');




  goog.require('gn_search');
  goog.require('gn_search_geodatastore_config');
  goog.require('gn_search_geodatastore_directive');
  goog.require('gn_login_controller');
  goog.require('geodatastore_login');
  goog.require('geodatastore_fileupload');

  var module = angular.module('gn_search_geodatastore',
      ['geodatastore_fileupload', 'gn_search', 'geodatastore_login', 'gn_login_controller', 'ngRoute', 'gn_search_geodatastore_config',
       'gn_search_geodatastore_directive', 'gn_mdactions_directive']);


  module.controller('geoDataStoreMainController', ['$scope', function($scope) {
    $scope.loadCatalogInfo();

  }]);
  module.controller("geoDataStoreController", ['$scope', function ($scope) {



  }]);


})();
