(function () {

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
  goog.require('geodatastore_readonly_metadata_controller');

  var module = angular.module('gn_search_geodatastore',
      ['geodatastore_fileupload', 'gn_search', 'geodatastore_login', 'gn_login_controller', 'ngRoute', 'gn_search_geodatastore_config',
        'gn_search_geodatastore_directive', 'gn_mdactions_directive', 'geodatastore_upload_service', 'bootstrap-tagsinput',
        'geodatastore_edit_metadata_controller', 'gn_utility_directive', 'pascalprecht.translate', 'ui.bootstrap.modal', 'ngAnimate',
        'ui.bootstrap.tooltip', 'geodatastore_readonly_metadata_controller']);

  // Define the translation files to load
  module.constant('$LOCALES', ['geodatastore']);

  module.config(['$translateProvider', '$LOCALES',
    function ($translateProvider, $LOCALES) {
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
    function ($scope, $http, $translate, $log, $filter,
              gnUtilityService, gnSearchSettings, gnViewerSettings, Metadata, gdsSearchManagerService, GdsUploadFactory,
              $modal, $q) {
      $scope.loadCatalogInfo();

      $scope.editState = {
        isEditing: true
      };

      $scope.searchResults = {records: [], metadata: []};
      $scope.totalNotPublished = 0;
      $scope.totalPublished = 0;
      $scope.GdsUploadFactory = GdsUploadFactory;
      $scope.tab = "upload";
      $scope.ngr.url = gn.ngr-url;
      //gn.ngr-url is defined by a param in loadjscss.xsl, which should be filled by deploy-it, else it will contain '{{' 
      if (!$scope.ngr.url or $scope.ngr.url.indexOf('{{')>0) $scope.ngr.url = 'http://www.nationaalgeoregister.nl/geonetwork';
      $scope.perPage = 8;
      $scope.page = 1;
      $scope.searchParams = {
        selectedOrder: 'changeDate',
        searchQuery: null,
        sortDirection: 'desc'
      };

      $scope.orderList = [
        {
          fieldName: '_title',
          labelKey: 'title'
        }, {
          fieldName:'changeDate',
          labelKey: 'changeDate'
        }];

      $scope.orderListUpload = [
        {
          fieldName:'changeDate',
          labelKey: 'changeDate'
        }];

      $scope.$watch('user', function () {
        $scope.getResultsSummary('draft');
        $scope.getResultsSummary('published');
        $scope.updateResults(1);
      });

      $scope.sortResults = function() {
        var query = $scope.searchParams.searchQuery;
        var page = $scope.page;
        var fieldOrder = $scope.searchParams.selectedOrder;
        var direction = $scope.searchParams.sortDirection;
        if (!$scope.page || isNaN($scope.page)) {
          page = 1;
        }

        return $scope.updateResults(page, query, fieldOrder, direction);
      };

      $scope.toggleSortOrder = function() {
        if ($scope.searchParams.sortDirection === 'desc') {
          $scope.searchParams.sortDirection = 'asc';
        } else {
          $scope.searchParams.sortDirection = 'desc';
        }
        $scope.sortResults();
      };

      $scope.search = function() {
        var searchQuery = $scope.searchParams.searchQuery;
        var page = 1;
        var sortOrder = $scope.searchParams.selectedOrder;
        var sortDirection = $scope.searchParams.sortDirection;
        return $scope.updateResults(page, searchQuery, sortOrder, sortDirection);
      };

      $scope.clearSearch = function () {
        $scope.searchParams.searchQuery = null;
        return $scope.search();
      }

      $scope.searchFromKeypress = function(evt) {
        if (evt.which === 13) {
          evt.preventDefault();
          $scope.search();
        }
      };



      $scope.$watch('tab', function (newValue, oldValue) {
        // Retrieve the register count for the not selected tab.
        if (newValue) {
          $scope.searchParams = {
            selectedOrder: 'changeDate',
            searchQuery: null,
            sortDirection: 'desc'
          };
          $scope.page = 1,
          $scope.getResultsSummary('draft');
          $scope.getResultsSummary('published');
          $scope.hasSelected = false;
          GdsUploadFactory.setMdSelected(null);
          GdsUploadFactory.clearList();
          GdsUploadFactory.setDirty(false);
          $scope.updateResults(1);
          $scope.resetEditState(newValue);
        }
      });

      $scope.updateResultsAndPreserveSearch = function(page) {
        $scope.updateResults(page, $scope.searchParams.searchQuery, $scope.searchParams.selectedOrder,
            $scope.searchParams.sortDirection);
      };

      $scope.updateResults = function (page, any, order, sortDirection) {
        $scope.page = page;
        var orderParam = order;
        if (!order) {
          orderParam = "changeDate";
        }
        var searchParams = {
          from: (page - 1) * $scope.perPage + 1,
          sortBy: orderParam,
          sortOrder: 'desc',
          pageSize: $scope.perPage,
          status: ($scope.tab === 'upload') ? 'draft' : 'published'
        };

        if (any) {
          $scope.filterActive = true;
         /* var queryObject = {};
          queryObject['title_OR_abstract'] = any;*/
          angular.extend(searchParams, {q: "*"+any+"*"});
        } else {
          $scope.filterActive = false;
        }
        if (sortDirection) {
          angular.extend(searchParams, {sortOrder: sortDirection});
        }


        $scope.searching = true;
        gdsSearchManagerService.search(searchParams).then(function (data) {
          GdsUploadFactory.clearList();
          $scope.searchResults = data;
          $scope.filterCount = data.count;
          if ($scope.tab === 'upload') {
            //$scope.totalNotPublished = data.count;
          } else if ($scope.tab === 'published') {
            //$scope.totalPublished = data.count;
          }
          var cnt = Math.ceil(data.count / $scope.perPage);
          if (cnt < -1) cnt = 1;
          $scope.pages = new Array(cnt);
        }, function (error) {
          $log.error("Error in search: " + error);
        }).finally(function() {
          $scope.searching= false;
        });
      };

      $scope.getResultsSummary = function (status) {
        gdsSearchManagerService.search({
          status: status,
          summaryOnly: true
        }).then(function (data) {
          if (status === 'draft') {
            $scope.totalNotPublished = data.count;
          } else if (status === 'published') {
            $scope.totalPublished = data.count;
          }
        })
      }

      //get the status of a dataset, a dataset can be published if all fields are completed
      $scope.getStatus = function (md) {
        if (md.$publishable) {
          return 'publish';
        } else {
          return 'metadataMissing';
        }
      }

      $scope.pages = [];
      $scope.updateResults(1);
      //$scope.mdSelected;
      $scope.hasSelected = false;
      $scope.formModified = false;

      $scope.setMD = function (md) {
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

          modalInstance.result.then(
              function(md) {
                GdsUploadFactory.setDirty(false);
                $scope.$setMdSelected(md);
              },
              function () {
                $log.debug("Card change cancelled");
              }
          );

        } else {
          $scope.$setMdSelected(md);
        }
      };

      /**
       * Reset the edit state depending on the tab selected.
       */
      $scope.resetEditState = function(selectedTab) {
        if (!GdsUploadFactory.isDirty()) {

          if (selectedTab === 'upload') {
            $scope.editState.isEditing = true;
          } else if (selectedTab === 'published') {
            $scope.editState.isEditing = false;
          }
        }
      };

      $scope.$setMdSelected = function (md) {
        GdsUploadFactory.setMdSelected(angular.copy(md));
        if (md.topicCategories && md.topicCategories.length > 0) {
          GdsUploadFactory.getMdSelected().topicCategory = $filter('orderBy')(GdsUploadFactory.getMdSelected().topicCategories)[0];
        } else {
          GdsUploadFactory.getMdSelected().topicCategory = null;
        }
        $scope.hasSelected = true;
        if (!GdsUploadFactory.isDirty()) {
          $scope.resetEditState($scope.tab);
        }
      };

      $scope.getMdSelected = function () {
        return GdsUploadFactory.getMdSelected();
      };

      $scope.getTopicCategoryAsString = function () {
        var selected = GdsUploadFactory.getMdSelected();
        if (selected) {
          return selected.topicCategory;
        }
        else {
          return undefined;
        }
      };

      $scope.$watch($scope.getTopicCategoryAsString, function (newValue, oldValue) {
        $log.debug("topicCategory changed: " + oldValue + "--> " + newValue);
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
      $scope.setTab = function (val) {
        //if form modified && not saved, warn to loose changes?
        $scope.tab = val;
      };

      //grab the filename from metadata, for now take the first link, later check which link is the correct link, sometimes filename is empty then use file desc
      $scope.getFileName = function (md) {
        if (md instanceof File) {
          return md.name;
        }
        if (!md.url || md.url.length == 0) {
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

      $scope.saveMetadata = function () {
        GdsUploadFactory.saveMetadata(GdsUploadFactory.getMdSelected()).then(
            function () {
            },
            function (error) {
            });
      };

      $scope.publishMetadata = function (md) {
        return GdsUploadFactory.saveMetadata(md, true).then(
            function (data) {
              if (data.status === 'published') {
                GdsUploadFactory.removeFromList(md, $scope.searchResults.metadata);
                GdsUploadFactory.removeFromList(md);
                $scope.totalPublished = $scope.totalPublished + 1;
                if (GdsUploadFactory.getMdSelected() && GdsUploadFactory.getMdSelected().identifier === md.identifier) {
                  GdsUploadFactory.setMdSelected({});
                  $scope.hasSelected = false;
                }
			  }
            }, function (error) {
              var modalInstance = $modal.open({
                templateUrl: '../../catalog/views/geodatastore/templates/publishFormModal.html',
                controller: 'PublishModalController',
                resolve: {
                  metadata: function () {
                    return error;
                  }
                }
              });
              modalInstance.result;
            }
        );
      };

      $scope.showDeleteConfirm = function (md, evt) {
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
            function (result) {
              // User clicks on Delete button in modal
              GdsUploadFactory.removeFromList(result);
              for (var i = $scope.searchResults.metadata.length - 1; i >= 0; i--) {
                var srMd = $scope.searchResults.metadata[i];
                if (GdsUploadFactory.getMdSelected() && (result.identifier === GdsUploadFactory.getMdSelected().identifier)) {
                  $scope.hasSelected = false;
                  GdsUploadFactory.setMdSelected({});
                }
                if (srMd.identifier === result.identifier) {
                  $scope.searchResults.metadata.splice(i, 1);
                  if ($scope.tab === 'upload') {
                    $scope.totalNotPublished = $scope.totalNotPublished - 1;
                  } else  if ($scope.tab = 'published') {
                    $scope.totalPublished = $scope.totalPublished - 1;
                  }
                  if ($scope.filterActive && $scope.filterCount) {
                    $scope.filterCount = $scope.filterCount - 1;
                  }
                }
              }
              return result;
            }, function (dismiss) {
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
    function ($scope, $modalInstance, metadata, GdsUploadFactory) {
      $scope.acceptDelete = function () {
        $scope.deleteWorking = true;
        return GdsUploadFactory.deleteMetadata(metadata).then(function () {
          $scope.deleteWorking = false;
          return $modalInstance.close(metadata);
        }, function (error) {
          $scope.deleteWoring = false;
          $scope.deleteError = true;
          $scope.deleteErrorMessages = error.messages;
        });
      }
    }
  ]);

  module.controller('PublishModalController', ['$scope', '$modalInstance', 'metadata', function ($scope, $modalInstance, metadata) {
    $scope.publishErrorMessages = metadata.messages;
  }]);

  module.controller('gdsCardController', ['$scope', 'GdsUploadFactory', function ($scope, GdsUploadFactory) {
    // watch md object for changes and evaluate if it is publishable. Then save this state to md.$publishable property.
    // $publishable starts with a '$' character so it is not take in account for detecting object changes
    $scope.$watch('md', function (newValue, oldValue) {
      if (newValue) {
        var isPublishable = GdsUploadFactory.isPublishable(newValue);
        newValue.$publishable = isPublishable;
      }

    }, true);

    $scope.startPublish = function (evt) {
      if (evt) {
        evt.stopPropagation();
      }

      return $scope.publishMetadata($scope.md);
    }
  }]);


  module.controller("geoDataStoreController", ['$scope', function ($scope) {

  }]);


})();
