(function() {
  goog.provide('geodatastore_edit_metadata_controller');

  goog.require('geodatastore_upload_service');
  goog.require('gn_utility_directive');
  goog.require('gn_popup_service');

  var module = angular.module('geodatastore_edit_metadata_controller', [
    'blueimp.fileupload',
    'geodatastore_upload_service',
    'gn_utility_directive',
    'gn_popup_service',
    'pascalprecht.translate'

  ]);

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


  module.controller('GdsEditMetadataController', ['$scope', 'GdsUploadFactory', '$log', 'gnPopup', '$translate', '$q',
    function($scope, GdsUploadFactory, $log, gnPopup, $translate, $q) {
      $scope.GdsUploadFactory = GdsUploadFactory;
      $scope.publish = false;

      $scope.mdToEdit = null;

      $scope.$watch("editMdForm.$dirty", function(newValue) {
        GdsUploadFactory.setDirty(newValue);

      });

      $scope.$watch(GdsUploadFactory.getMdSelected, function() {
        $log.debug("watch on GdsUploadFactory.getMdSelected changed");
        var isSameMetadata =  false;
        if ($scope.mdToEdit && GdsUploadFactory.getMdSelected().identifier === $scope.mdToEdit.identifier){
          isSameMetadata = true;
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
          var updateUrl = '../../geodatastore/api/dataset/' + $scope.mdToEdit.identifier;
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
            previewMaxWidth: 100,
            previewMaxHeight: 100,
            previewCrop: true,
            maxNumberOfFiles: 1
          };
          $scope.clear($scope.queue);
        }
      });

      // watch md object for changes and evaluate if it is publishable. Then save this state to md.$publishable property.
      // $publishable starts with a '$' character so it is not take in account for detecting object changes
      $scope.$watch('mdToEdit', function(newValue, oldValue) {
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
      var successfulUpload = function(e, response) {
        var data = (response ? response.result: e);
        if ($scope.uploadDeferred) {
          $scope.uploadDeferred.resolve(data);
        }
        $scope.saved = true;
        if (!data.error) {
          GdsUploadFactory.setMdSelected(data);
          // reset form status
          $scope.editMdForm.$setPristine();
          $scope.editMdForm.$setUntouched();
          GdsUploadFactory.replace($scope.searchResults.metadata, data);
          gnPopup.createModal({
            title: '<strong>' + $translate('edit.success.title') + '</strong>',
            content: '<div class="alert alert-success" role="alert">' + $translate('edit.success.content') + '</div>'
          }, $scope);
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

      var failedUpload = function(e, response) {
        var error =  {};
        try {
          error = (response && response.jqXHR ? angular.fromJson(response.jqXHR.responseText): e);
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
        gnPopup.createModal({
          title: '<strong>' + $translate('edit.error.title') + '</strong>',
          content: '<div class="alert alert-danger" role="alert">'
          + "<p><i class='fa fa-exclamation-circle fa-fw' aria-hidden='true'></i>"
          + "<span class='sr-only'>Error: </span><span data-translate=''>update.error.listTitle</span></p>"
          + '<ul>'
          + '<li data-ng-repeat="message in messages">{{message | translate}}</li>'
          + '</ul></div>'
        }, $scope);
      };

      var filesChanged = function(e, data) {
        if (data.files.length > 0) {
          $scope.editMdForm.$setDirty();
        }
      };

      var getMetadataJson = function() {
        var metadata = [{
          name: 'metadata',
          value: angular.toJson($scope.mdToEdit)
        }, {
           name: 'publish',
           value: $scope.publish
          }
        ];
        return metadata;
      }



      $scope.getMdSelected = function() {
        return GdsUploadFactory.getMdSelected();
      }

      $scope.processSubmit = function() {
        $scope.error = false;
        $scope.messages = [];
        $scope.saved = false;
        if ($scope.queue && $scope.queue.length > 0) {
          // call submit in blueimp.fileupload
          $scope.uploadDeferred = $q.defer();
          $scope.submit();
          return $scope.uploadDeferred.promise;
        } else {
          // manually send the form without the thumbnail.
          return GdsUploadFactory.saveMetadata($scope.mdToEdit).then(
              successfulUpload,
              failedUpload
          );
        }
      }

    }]);

})();