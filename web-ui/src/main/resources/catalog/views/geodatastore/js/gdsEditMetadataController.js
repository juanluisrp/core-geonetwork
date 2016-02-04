(function () {
  goog.provide('geodatastore_edit_metadata_controller');

  goog.require('geodatastore_upload_service');
  goog.require('gn_utility_directive');
  goog.require('gn_popup_service');
  goog.require('gds_thesaurus_directive');
  goog.require('geodatastore_api_urls');
  goog.require('geodatastore_registry_service');

  var module = angular.module('geodatastore_edit_metadata_controller', [
    'geodatastore_api_urls',
    'blueimp.fileupload',
    'geodatastore_upload_service',
    'gn_utility_directive',
    'gn_popup_service',
    'pascalprecht.translate',
    'gds_thesaurus_directive',
    'geodatastore_registry_service'

  ]);

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


  module.controller('GdsEditMetadataController', ['$scope', 'GdsUploadFactory', '$log', 'gnPopup', '$translate', '$q',
      'gdsRegistryService', 'UPDATE_DATASET_URL',
    function ($scope, GdsUploadFactory, $log, gnPopup, $translate, $q, gdsRegistryService, UPDATE_DATASET_URL) {
      $scope.GdsUploadFactory = GdsUploadFactory;
      $scope.publish = false;

      $scope.mdToEdit = null;

      gdsRegistryService.getLicenses().then(function (data) {
        $scope.licenseList = data;

      });
      gdsRegistryService.getDenominators().then(function (data){
        $scope.resolutionList = data;
      });
      gdsRegistryService.getTopicCategories().then(function(data) {
        $scope.topicCategoryList = data;
      });

      $scope.itemKeyToNumber = function (item) {
        return Number(item.key);
      };



      $scope.$watch("editMdForm.$dirty", function (newValue) {
        GdsUploadFactory.setDirty(newValue);

      });

      $scope.$watch("editState.isEditing", function(newValue) {
        if (newValue && $scope.tab === 'published') {
          $scope.publish = true;
        }  else {
          $scope.publish = false;
        }
      });

      $scope.$watch(GdsUploadFactory.getMdSelected, function () {
        //$log.debug("watch on GdsUploadFactory.getMdSelected changed");
        var isSameMetadata = false;
        if ($scope.mdToEdit && GdsUploadFactory.getMdSelected().identifier === $scope.mdToEdit.identifier) {
          isSameMetadata = true;
          var mdSelected = GdsUploadFactory.getMdSelected();
          if (mdSelected && $scope.mdToEdit) {
            $scope.mdToEdit.location = mdSelected.location;
            $scope.mdToEdit.locationUri = mdSelected.locationUri;
          }
        }
        if (!GdsUploadFactory.getMdSelected()) {
          $scope.editMdForm.$setPristine();
          $scope.editMdForm.$setUntouched();
          GdsUploadFactory.setDirty(false);
        }

        if (!isSameMetadata) {
          $scope.mdToEdit = GdsUploadFactory.getMdSelected();
          // reset form status
          $scope.editMdForm.$setPristine();
          $scope.editMdForm.$setUntouched();
          var updateUrl = UPDATE_DATASET_URL + $scope.mdToEdit.identifier;
          $scope.datasetEditOptions = {
            url: updateUrl,
            type: 'POST',
            dropZone: $('#thumbnailDropZone'),
            sequentialUploads: true,
            autoUpload: false,
            dataType: 'json',
            formData: getMetadataJson,
            done: successfulUpload,
            fail: failedUpload,
            change: filesChanged,
            acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
            previewMaxWidth: 243,
            previewMaxHeight: 243,
            previewCrop: false,
            maxNumberOfFiles: 1
        };
          $scope.clear($scope.queue);
        }
      });

      // watch md object for changes and evaluate if it is publishable. Then save this state to md.$publishable property.
      // $publishable starts with a '$' character so it is not take in account for detecting object changes
      $scope.$watch('mdToEdit', function (newValue, oldValue) {
        if (newValue) {
          var isPublishable = GdsUploadFactory.isPublishable(newValue);
          newValue.$publishable = isPublishable;
        }

      }, true);


      /**
       * File upload callback for successful upload requests. This callback is the equivalent to the success callback
       * provided by jQuery ajax() and will also be called if the server returns a JSON response with an error property.
       *
       */
      var successfulUpload = function (e, response) {
        var data = (response ? response.result : e);
        if ($scope.uploadDeferred) {
          $scope.uploadDeferred.resolve(data);
        }
        $scope.saved = true;
        if (!data.error) {
          GdsUploadFactory.setMdSelected(data);

          // reset form status
          $scope.editMdForm.$setPristine();
          $scope.editMdForm.$setUntouched();
          GdsUploadFactory.replace(GdsUploadFactory.list, data);
          GdsUploadFactory.replace($scope.searchResults.metadata, data);
          GdsUploadFactory.setDirty(false);
          if ($scope.tab === 'published') {
            $scope.editState.isEditing = false;
          }
        } else {
          $scope.error = true;
          $scope.messages = data.messages;
          gnPopup.createModal({
            title: '<strong>' + $translate('edit.error.title') + '</strong>',
            content: '<div class="alert alert-danger" role="alert">'
            + "<p><i class='fa fa-exclamation-circle fa-fw' aria-hidden='true'></i>"
            + "<span class='sr-only'>Error: </span><span data-translate=''>update.error.listTitle</span></p>"
            + '<ul>'
            + '<li data-ng-repeat="message in messages">{{message | translate}}</li>'
            + '</ul></div>'
          }, $scope);
        }

      };

      var failedUpload = function (e, response) {
        var error = {};
        try {
          error = (response && response.jqXHR ? angular.fromJson(response.jqXHR.responseText) : e);
        } catch (exception) {
          $log.error(response.jqXHR.responseText);
        }
        if ($scope.uploadDeferred) {
          $scope.uploadDeferred.resolve(error);
        }

        $scope.error = true;
        if (error && error.error) {
          $scope.messages = error.messages;
        } else {
          $log.error("Error updating metadata: " + error);
          $scope.messages = ["update.server.error"];
        }
        if (e.reqStatus != 401) {
          gnPopup.createModal({
            title: '<strong>' + $translate('edit.error.title') + '</strong>',
            content: '<div class="alert alert-danger" role="alert">'
            + "<p><i class='fa fa-exclamation-circle fa-fw' aria-hidden='true'></i>"
            + "<span class='sr-only'>Error: </span><span data-translate=''>update.error.listTitle</span></p>"
            + '<ul>'
            + '<li data-ng-repeat="message in messages">{{message | translate}}</li>'
            + '</ul></div>'
          }, $scope);
        }
      };

      var filesChanged = function (e, data) {
        if (data.files.length > 0) {
          $scope.editMdForm.$setDirty();
          var file = $scope.queue.pop();
           $log.debug("File removed from queue: " + file);
        }
      };

      var getMetadataJson = function () {
        var data = angular.extend({}, $scope.mdToEdit);
        data.topicCategories = [data.topicCategory];
        var metadata = [{
          name: 'metadata',
          value: angular.toJson(data)
        }, {
          name: 'publish',
          value: $scope.publish
        }
        ];
        return metadata;
      };


      $scope.getMdSelected = function () {
        return GdsUploadFactory.getMdSelected();
      };

      $scope.processSubmit = function (mustPublish) {
        $scope.error = false;
        $scope.messages = [];
        $scope.saved = false;

        if (mustPublish && !$scope.mdToEdit.$publishable) {
          var invalidFields = GdsUploadFactory.getInvalidFields($scope.mdToEdit);
          $scope.invalidFields = invalidFields;
          $scope.uploadDeferred = $q.defer();
          gnPopup.createModal({
            title: 'updatePublished.fieldErrors.title',
            content: '<div class="alert alert-danger" role="alert">'
                + "<p><i class='fa fa-exclamation-circle fa-fw' aria-hidden='true'></i>"
                + "<span class='sr-only'>Error: </span><span data-translate=''>updatePublished.fieldErrors.header</span></p>"
                + "<p data-translate=''>updatePublished.fieldErrors.body</p>"
                + "<p data-translate=''>updatePublished.fieldErrors.listOfFields</p>"
                + '<ul>'
                + '<li data-ng-repeat="field in invalidFields">{{field | translate}}</li>'
                + '</ul></div>',
            onCloseCallback: function(modal) {
              $scope.invalidFields = null;
              $scope.uploadDeferred.resolve();
            }
          }, $scope);
          return $scope.uploadDeferred.promise;
        }


        if ($scope.queue && $scope.queue.length > 0) {
          // call submit in blueimp.fileupload
          $scope.uploadDeferred = $q.defer();
          $scope.submit();
          return $scope.uploadDeferred.promise;
        } else {
          // manually send the form without the thumbnail.
          return GdsUploadFactory.saveMetadata($scope.mdToEdit, mustPublish).then(
              successfulUpload,
              failedUpload
          );
        }
      }
    }]);
})();
