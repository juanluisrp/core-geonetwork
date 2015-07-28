(function() {
  goog.provide('geodatastore_edit_metadata_controller');

  goog.require('geodatastore_upload_service');

  var module = angular.module('geodatastore_edit_metadata_controller', [
    'blueimp.fileupload',
    'geodatastore_upload_service'
  ]);

  module.controller('GdsEditMetadataController', ['$scope', 'GdsUploadFactory', '$log',
    function($scope, GdsUploadFactory, $log) {
      $scope.GdsUploadFactory = GdsUploadFactory;
      $scope.publish = true;

      /**
       * File upload callback for successful upload requests. This callback is the equivalent to the success callback
       * provided by jQuery ajax() and will also be called if the server returns a JSON response with an error property.
       *
       */
      var successfulUpload = function(e, data) {
        console.log(data);

      };

      var failedUpload = function(e, data) {

      };

      var getMetadataJson = function() {
        var metadata = [{
          name: 'metadata',
          value: angular.toJson($scope.mdSelected)
        }, {
           name: 'publish',
          value: $scope.publish
          }
        ];
        return metadata;
      }

      $scope.datasetEditOptions = {
        url: '../../geodatastore/api/dataset/' + $scope.mdSelected.identifier,
        type: 'POST',
        dropZone: $('#thumbnailDropZone'),
        sequentialUploads: true,
        autoUpload: false,
        dataType: 'json',
        formData: getMetadataJson,
        done: successfulUpload,
        fail: failedUpload,
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
        previewMaxWidth: 100,
        previewMaxHeight: 100,
        previewCrop: true,
        maxNumberOfFiles: 1
      };

      $scope.processSubmit = function() {
        $log.debug("Save clicked");
        if ($scope.queue && $scope.queue.length > 0) {
          // call submit in blueimp.fileupload
          $scope.submit();
        } else {
          // manually send the form without the thumbnail.
          GdsUploadFactory.saveMetadata($scope.mdSelected).then(
              function(data) {
                $scope.mdSelected = data;
                $scope.mdSelected.saved = true;
                GdsUploadFactory.replace($scope.searchResults.metadata, data);
              },
              function(error) {
                $scope.mdSelected.error = true;
                if (error && error.error) {
                  $scope.mdSelected.messages = error.messages;
                } else {
                  $log.error("Error updating metadata: " + error);
                  $scope.mdSelected.messages = ["update.server.error"];
                }
              });
        }
      }

    }]);

})();