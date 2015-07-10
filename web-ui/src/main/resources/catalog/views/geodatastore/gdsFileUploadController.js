(function() {
  goog.provide('geodatastore_fileupload');

  goog.require('geodatastore_dropzone_directive');

  var module = angular.module('geodatastore_fileupload', [
      'blueimp.fileupload', 'geodatastore_dropzone_directive'
  ]);

  module.controller('GdsFileUploadController', ['$scope',
    function($scope) {
      $scope.datasetUploadOptions = {
        url: '../../geodatastore/api/dataset',
        dropZone: $('#dropzone'),
        sequentialUploads: true,
        autoUpload: true

      };

      $scope.getFileIcon = function(file) {
        var type = file ? (file.type || 'unknown') : 'unknown';
        var iconClass = "fa-file-o"
        type = type.toLowerCase();
        if (type === "application/pdf") {
          iconClass = "fa-file-pdf-o";
        } else if (type.startsWith("image/")) {
          iconClass = "fa-file-image-o";
        } else if (type.startsWith("video/")) {
          iconClass = "fa-file-video-o";
        } else if (type.startsWith("audio/")) {
          iconClass = "fa-file-audio-o";
        } else if (type.contains("zip") || type.contains("compressed")) {
          iconClass = "fa-file-archive-o"
        } else if (type.contains("excel") || type.contains("sheet")) {
          iconClass = "fa-file-excel-o";
        } else if (type.contains("word") || type.contains("opendocument.text")) {
          iconClass = "fa-file-word-o";
        } else if (type.contains("powerpoint") || type.contains("presentation")) {
          iconClass = "fa-file-powerpoint-o";
        } else if (type === "text/plain" || type === "application/rtf") {
          iconClass = "fa-file-text-o";
        } else if (type === "text/css" || type === "text/html" || type === "application/rdf" || type === "application/rdf+xml"
            || type === "text/sgml" || type === "application/xml") {
          iconClass = "fa-file-code-o";
        }
        return iconClass;
      };

  }]);

})();