(function() {

  goog.provide('gn_search_geodatastore');




  goog.require('gn_search');
  goog.require('gn_search_geodatastore_config');
  goog.require('gn_search_geodatastore_directive');
  goog.require('gn_login_controller');
  goog.require('geodatastore_login');
  goog.require('geodatastore_fileupload');
	goog.require('geodatastore_upload_service');

  var module = angular.module('gn_search_geodatastore',
      ['geodatastore_fileupload', 'gn_search', 'geodatastore_login', 'gn_login_controller', 'ngRoute', 'gn_search_geodatastore_config',
       'gn_search_geodatastore_directive', 'gn_mdactions_directive', 'geodatastore_upload_service']);

  // Define the translation files to load
  module.constant('$LOCALES', ['geodatastore']);

  module.config(['$translateProvider', '$LOCALES',
    function($translateProvider, $LOCALES) {
      $translateProvider.useLoader('localeLoader', {
        locales: $LOCALES,
        prefix: '../../catalog/views/geodatastore/locales/',
        suffix: '.json'
      });

      //var lang = location.href.split('/')[5].substring(0, 2) || 'en';
      var lang = 'du';
      $translateProvider.preferredLanguage(lang);
      moment.lang(lang);
    }]);

  module.controller('gnsSearchController', [
    '$scope', 'gnSearchSettings', 'GdsUploadFactory',
    function($scope, gnSearchSettings, GdsUploadFactory) {
      $scope.searchObj = {
        permalink: false,
        params: {
          sortBy: 'popularity',
		  filter: '',
          from: 1,
          to: 9
        }
      };
    }]);	   

  module.controller('geoDataStoreMainController', [
		'$scope',
    '$http',
    '$translate',
		'$log',
    '$filter',
    'gnUtilityService',
    'gnSearchSettings',
    'gnViewerSettings',	
    'Metadata',
		'gdsSearchManagerService',
		'GdsUploadFactory',
	function($scope, $http, $translate, $log, $filter,
             gnUtilityService, gnSearchSettings, gnViewerSettings,Metadata, gdsSearchManagerService, GdsUploadFactory) {
		$scope.loadCatalogInfo();
    $scope.searchResults = { records: [] };
		$scope.totalNotPublished = 0;
    $scope.totalPublished = 0;
		$scope.GdsUploadFactory = GdsUploadFactory;
    $scope.tab = "upload";


		$scope.$watch('user', function() {
			$scope.updateResults(1);
		})

	  $scope.updateResults = function(page ,any, order){
			if (!order) {
				order="changeDate";
			}
			gdsSearchManagerService.search({
				from: (page - 1)* 5 + 1,
				sortBy: order,
        sortOrder: 'desc',
				pageSize: 5,
				status: ($scope.tab == "upload") ? 'draft' : 'published'
			}).then(function(data) {
				$scope.searchResults = data;
        if ($scope.tab == 'upload') {
			  	$scope.totalNotPublished = data.count;
        } else if ($scope.tab == 'published') {
          $scope.totalPublished = data.count;
        }
			}, function(error) {
					$log.error("Error in search: " + error);
			});
		};
	  
	  //get the status of a dataset, a dataset can be published if all fields are completed
	  $scope.getStatus = function(md){
			if (md.defaultTitle != '' && md.abstract != '' && md.lineage != '') {
				return 'publish';
			}	else {
				return 'metadataMissing';
			}
	  }
	  
	  $scope.updateResults(1);
	  
	  $scope.mdSelected;
	  $scope.hasSelected = false;
	  $scope.formModified = false;

		$scope.setMD = function(md) {
			$scope.mdSelected = angular.copy(md);
			$scope.hasSelected = true;
      if (md.topicCategories && md.topicCategories.length > 0) {
        $scope.mdSelected.topicCategory = $filter('orderBy')($scope.mdSelected.topicCategories)[0];
      } else {
        $scope.mdSelected.topicCategory = null;
      }

			/*
			* set the form fields
			*/
			if (!md.keywords) {
			 md.keywords = [];
			}
			$("#tw").val(md.keywords.join(','));
		};

    $scope.$watch('mdSelected.topicCategory', function(newValue, oldValue) {
      $log.debug("topicCategory changed: " +  oldValue + "--> " + newValue);
      if ($scope.mdSelected) {
        if (newValue !== null && newValue !== undefined) {
          $scope.mdSelected.topicCategories = [newValue];
        } else {
          $scope.mdSelected.topicCategories = [];
        }
      }
    });

	  
		//grab the filetype either from format or from file extension
		$scope.getFileType = function (md) {
			var ftype = "";
			if (!md.fileType) {
				var fprops = md.url.split('/');
				if (fprops.length > 3 && fprops[fprops.length - 1] != '') {
					var type = fprops[fprops.length - 1].split(".");
					if (type.length >= 2) {
						ftype = type[1];
					}
				} else {
					ftype = "";
				}
			} else {
				ftype = md.fileType;
			}
			return GdsUploadFactory.getFileIcon(ftype);

		};


	  $scope.setBackground = function (md) {
			var mdIdentifier = md.identifier || md['geonet:info'].uuid;
			var selectedIdentifier = null;
			if ($scope.hasSelected) {
				if ($scope.mdSelected.identifier) {
					selectedIdentifier = $scope.mdSelected.identifier;
				} else if ($scope.mdSelected['geonet:info'] && $scope.mdSelected['geonet:info'].uuid) {
					selectedIdentifier = $scope.mdSelected['geonet:info'].uuid;
				}
			}
			if (mdIdentifier == selectedIdentifier) {
				return 'modify';
			} else {
				return null;
			}
	  }




		
		$scope.setTab = function(val){
			//if form modified&&not saved, warn to loose changes?
			$scope.tab=val;
			$scope.hasSelected = false;
			$scope.mdSelected = null;
			GdsUploadFactory.clearList();
			$scope.updateResults(1);
		}
		
	  //grab the filename from metadata, for now take the first link, later check which link is the correct link, sometimes filename is empty then use file desc
	  $scope.getFileName = function (md) {
			if (!md.url || md.url.length  == 0) {
				return md.title
			} else {
				var fprops = md.url.split('/');
				if (fprops.length > 3 && fprops[fprops.length - 1] != '') {
					return fprops[fprops.length - 1];
				} else {
					return md.title;
				}
			}
	  }
	  
  }]);
  
  module.controller("geoDataStoreController", ['$scope', function ($scope) {

  }]);


})();
