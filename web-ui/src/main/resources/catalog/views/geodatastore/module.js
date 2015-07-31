(function() {

  goog.provide('gn_search_geodatastore');




  goog.require('gn_search');
  goog.require('gn_search_geodatastore_config');
  goog.require('gn_search_geodatastore_directive');
  goog.require('gn_login_controller');
	goog.require('gn_utility_directive');
  goog.require('geodatastore_login');
  goog.require('geodatastore_fileupload');
	goog.require('geodatastore_upload_service');
	goog.require('geodatastore_edit_metadata_controller');

  var module = angular.module('gn_search_geodatastore',
      ['geodatastore_fileupload', 'gn_search', 'geodatastore_login', 'gn_login_controller', 'ngRoute', 'gn_search_geodatastore_config',
       'gn_search_geodatastore_directive', 'gn_mdactions_directive', 'geodatastore_upload_service', 'bootstrap-tagsinput',
	   'geodatastore_edit_metadata_controller', 'gn_utility_directive', 'pascalprecht.translate', 'ui.bootstrap.modal', 'ngAnimate',
      'ui.bootstrap.tooltip']);

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
    '$modal',
    '$q',
	function($scope, $http, $translate, $log, $filter,
           gnUtilityService, gnSearchSettings, gnViewerSettings,Metadata, gdsSearchManagerService, GdsUploadFactory,
           $modal, $q) {
		$scope.loadCatalogInfo();
    $scope.searchResults = { records: [], metadata:[] };
		$scope.totalNotPublished = 0;
    $scope.totalPublished = 0;
		$scope.GdsUploadFactory = GdsUploadFactory;
    $scope.tab = "upload";
    $scope.perPage = 8;
	$scope.page = 1;

		$scope.$watch('user', function() {
			$scope.updateResults(1);
		})

	  $scope.updateResults = function(page ,any, order){
			$scope.page = page;
			if (!order) {
				order="changeDate";
			}
			gdsSearchManagerService.search({
				from: (page - 1)* $scope.perPage + 1,
				sortBy: order,
				sortOrder: 'desc',
				pageSize: $scope.perPage,
				status: ($scope.tab == "upload") ? 'draft' : 'published'
			}).then(function(data) {
			    GdsUploadFactory.clearList();
				$scope.searchResults = data;
				if ($scope.tab == 'upload') {
					$scope.totalNotPublished = data.count;
				} else if ($scope.tab == 'published') {
					$scope.totalPublished = data.count;			   
				} 
				$scope.pages = new Array(Math.ceil(data.count/$scope.perPage)); 
			}, function(error) {
					$log.error("Error in search: " + error);
			});
		};
	  
	  //get the status of a dataset, a dataset can be published if all fields are completed
	  $scope.getStatus = function(md){
			if (md.$publishable) {
				return 'publish';
			}	else {
				return 'metadataMissing';
			}
	  }
	  
	  $scope.pages = [];
	  $scope.updateResults(1);
	  //$scope.mdSelected;
	  $scope.hasSelected = false;
	  $scope.formModified = false;

		$scope.setMD = function(md) {
			$log.debug(md);

      var selectedInService = GdsUploadFactory.getMdSelected();

      // If you are trying to change selected card and edit form is dirty, ask user what to do with changes
      if (GdsUploadFactory.isDirty() && selectedInService && selectedInService.identifier && selectedInService.identifier !== md.identifier) {
        $log.debug("Show confirmation modal");
        var modalInstance = $modal.open({
          scope: $scope,
          templateUrl: '../../catalog/views/geodatastore/templates/dirtyFormModal.html',
          controller: 'ChangeCardModalController',
          resolve: {
            metadata: function () {
              return md;
            }
          }
        });

        modalInstance.result.then($scope.$setMdSelected,
            function () {
              $log.debug("Card change cancelled");
            }
        );

      } else {
       $scope.$setMdSelected(md);
      }
		};

    $scope.$setMdSelected = function(md) {
      GdsUploadFactory.setMdSelected(angular.copy(md));
      if (md.topicCategories && md.topicCategories.length > 0) {
        GdsUploadFactory.getMdSelected().topicCategory = $filter('orderBy')(GdsUploadFactory.getMdSelected().topicCategories)[0];
      } else {
        GdsUploadFactory.getMdSelected().topicCategory = null;
      }
      $scope.hasSelected = true;
    };

    $scope.getMdSelected = function() {
      return GdsUploadFactory.getMdSelected();
    };

    $scope.getTopicCategoryAsString = function() {
      var selected = GdsUploadFactory.getMdSelected();
      if (selected) {
        return selected.topicCategory;
      }
      else {
        return undefined;
      }
    };

    $scope.$watch($scope.getTopicCategoryAsString, function(newValue, oldValue) {
      $log.debug("topicCategory changed: " +  oldValue + "--> " + newValue);
      var selected = GdsUploadFactory.getMdSelected();
      if (selected) {
        if (newValue !== null && newValue !== undefined) {
          selected.topicCategories = [newValue];
        } else {
          selected.topicCategories = [];
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
				if (GdsUploadFactory.getMdSelected().identifier) {
					selectedIdentifier = GdsUploadFactory.getMdSelected().identifier;
				} else if (GdsUploadFactory.getMdSelected()['geonet:info'] && $scope.mdSelected['geonet:info'].uuid) {
					selectedIdentifier = GdsUploadFactory.getMdSelected()['geonet:info'].uuid;
				}
			}
			if (mdIdentifier == selectedIdentifier) {
				return 'modify';
			} else {
				return null;
			}
	  };

		/**
		 * Set the active tab.
		 * @param val current tab.
		 */
		$scope.setTab = function(val){
			//if form modified && not saved, warn to loose changes?
			$scope.tab = val;
			$scope.hasSelected = false;
      GdsUploadFactory.setMdSelected(null);
			GdsUploadFactory.clearList();
      GdsUploadFactory.setDirty(false);
			$scope.updateResults(1);
		};
		
	  //grab the filename from metadata, for now take the first link, later check which link is the correct link, sometimes filename is empty then use file desc
	  $scope.getFileName = function (md) {
			if (md instanceof File) {
				return md.name;
			}
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
	  };

		$scope.saveMetadata = function() {
			GdsUploadFactory.saveMetadata(GdsUploadFactory.getMdSelected()).then(
					function(){},
					function(error) {});
		};

    $scope.showDeleteConfirm = function(md, evt) {
      if (evt) {
        evt.stopPropagation();
      }
      var modalInstance = $modal.open({
        scope: $scope,
            templateUrl: '../../catalog/views/geodatastore/templates/deleteFormModal.html',
            controller: 'ConfirmDeletedModalController',
            resolve: {
              metadata: function () {
                return md;
              }
            }
      });
      modalInstance.result.then(
          function(result) {
          // User clicks on Delete button in modal
            GdsUploadFactory.removeFromList(result);
            for(var i= $scope.searchResults.metadata.length - 1; i >= 0 ; i--) {
              var srMd = $scope.searchResults.metadata[i];
                if (GdsUploadFactory.getMdSelected() && (result.identifier === GdsUploadFactory.getMdSelected().identifier)) {
                  $scope.hasSelected = false;
                  GdsUploadFactory.setMdSelected({});
                }
              if (srMd.identifier === result.identifier) {
                $scope.searchResults.metadata.splice(i, 1);
                $scope.totalNotPublished = $scope.totalNotPublished - 1;
              }
            }
            return result;
          }, function(dismiss) {
            return $q.reject(dismiss);
          }
      );
    };
	  
  }]);

  module.controller('ChangeCardModalController', ['$scope', '$modalInstance', 'metadata',
    function ($scope, $modalInstance, metadata) {

    $scope.metadata = metadata;

    $scope.changeCard = function () {
      $modalInstance.close($scope.metadata);
    };
  }]);

  module.controller('ConfirmDeletedModalController', ['$scope', '$modalInstance', 'metadata', 'GdsUploadFactory',
      function($scope, $modalInstance, metadata, GdsUploadFactory) {
        $scope.acceptDelete = function() {
          $scope.deleteWorking = true;
          return GdsUploadFactory.deleteMetadata(metadata).then(function() {
            $scope.deleteWorking = false;
            return $modalInstance.close(metadata);
          }, function(error) {
            $scope.deleteWoring = false;
            $scope.deleteError = true;
            $scope.deleteErrorMessages = error.messages;
          });
        }
      }
  ]);

  module.controller('gdsCardController', ['$scope', 'GdsUploadFactory', function($scope, GdsUploadFactory) {
    // watch md object for changes and evaluate if it is publishable. Then save this state to md.$publishable property.
    // $publishable starts with a '$' character so it is not take in account for detecting object changes
    $scope.$watch('md', function(newValue, oldValue) {
      if (newValue) {
        var isPublishable = GdsUploadFactory.isPublishable(newValue);
        newValue.$publishable = isPublishable;
      }

    }, true);

  }]);


  
  module.controller("geoDataStoreController", ['$scope', function ($scope) {

  }]);


})();
