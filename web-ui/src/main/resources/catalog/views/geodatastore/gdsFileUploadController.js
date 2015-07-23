(function() {
  goog.provide('geodatastore_fileupload');

  goog.require('geodatastore_dropzone_directive');
  goog.require('geodatastore_upload_service');

  var module = angular.module('geodatastore_fileupload', [
      'blueimp.fileupload', 'geodatastore_dropzone_directive',
      'geodatastore_upload_service'
  ]);

  module.controller('GdsFileUploadController', ['$scope', 'GdsUploadFactory',
    function($scope, GdsUploadFactory) {
      $scope.GdsUploadFactory = GdsUploadFactory;

      /**
       * File upload callback for successful upload requests. his callback is the equivalent to the success callback
       * provided by jQuery ajax() and will also be called if the server returns a JSON response with an error property.
       *
       */
      var successfulUpload = function(e, data) {
        //console.log(data);
        if (!data.result.error) {
          GdsUploadFactory.add(data.result);
          $scope.clear(data.files);
        } else {
			failedUpload(e,data);
		}
      };

      var failedUpload = function(e, data) {
	    var err = data.files[0];
		err.error=true;
		err.identifier="error-"+Math.random();
		if (data.errorThrown) err.status=data.errorThrown;
		if (data.response()&&data.response().jqXHR.responseJSON.messages) 
			err.message=data.response().jqXHR.responseJSON.messages[0];
		else 
			err.message="An error occured";
		GdsUploadFactory.add(err);
		$scope.clear(data.files);
      }

      $scope.datasetUploadOptions = {
        url: '../../geodatastore/api/dataset',
        dropZone: $('#dropzone'),
        sequentialUploads: true,
        autoUpload: true,
        done: successfulUpload,
        fail: failedUpload
      };

  }]);

})();