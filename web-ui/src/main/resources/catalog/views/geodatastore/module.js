(function() {

  goog.provide('gn_search_geodatastore');




  goog.require('gn_search');
  goog.require('gn_search_geodatastore_config');
  goog.require('gn_search_geodatastore_directive');

  var module = angular.module('gn_search_geodatastore',
      ['gn_search', 'ngRoute', 'gn_search_geodatastore_config',
       'gn_search_geodatastore_directive', 'gn_mdactions_directive']);
  

  module.controller('geoDataStoreMainController', [function() {

  }]);
  module.controller("geoDataStoreController", ['$scope', function ($scope) {



  }]);


})();
