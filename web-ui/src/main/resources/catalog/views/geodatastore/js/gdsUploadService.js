/**
 * Created by JuanLuis on 13/07/2015.
 */
(function() {
  goog.provide('geodatastore_upload_service');

  var module = angular.module('geodatastore_upload_service', []);

  module.factory('GdsUploadFactory', [function(){
    var uploadedFiles = {};

    uploadedFiles.list = [];

    uploadedFiles.add = function(file) {
      uploadedFiles.list.unshift(file);
    };

    uploadedFiles.getFileIcon = function(file) {
      var type = file ? (file.type || file.fileType || file) : 'unknown';
      var iconClass = "fa-file-o"
      type = type.toLowerCase();
      if (type.contains("pdf")) {
        iconClass = "fa-file-pdf-o";
      } else if (type.startsWith("image/") || ["png", "gif", "jpg", "tiff", "tif", "jp2"].indexOf(file) > 0) {
        iconClass = "fa-file-image-o";
      } else if (type.startsWith("video/")) {
        iconClass = "fa-file-video-o";
      } else if (type.startsWith("audio/")) {
        iconClass = "fa-file-audio-o";
      } else if (type.contains("zip") || type == "rar" || type.contains("compressed")) {
        iconClass = "fa-file-archive-o"
      } else if (type.contains("excel") || type.contains("sheet") || ["xls","xlsx","ods"].indexOf(file) ) {
        iconClass = "fa-file-excel-o";
      } else if (type.contains("word") || type.contains("opendocument.text") || ["doc","docx","rtf"].indexOf(file) > 0) {
        iconClass = "fa-file-word-o";
      } else if (type.contains("powerpoint") || type.contains("presentation") || ["ppt","pptx"].indexOf(file)) {
        iconClass = "fa-file-powerpoint-o";
      } else if (type === "text/plain" || type === "application/rtf") {
        iconClass = "fa-file-text-o";
      }  else if (["csv"].indexOf(file) > 0) {
        iconClass = "pdok-i-csv";
      } else if (type === "text/css" || type === "text/html" || type === "application/rdf" || type === "application/rdf+xml"
          || type === "text/sgml" || type.contains("xml")) {
        iconClass = "fa-file-code-o";
      }
      return iconClass;
    };

    return uploadedFiles;
  }]);

  module.factory('gdsSearchManagerService', [
      '$q',
      '$http',
      function($q, $http) {
        var url = "../../geodatastore/api/datasets";


        var search = function(params, errorFn) {
          var parameters = params || {};
          var defer = $q.defer();
          $http.get(url, {
            params: parameters
          }).success(function(data, status) {
            defer.resolve(data);
          }).error(function(data, status) {
            defer.reject(errorFn);
          });
          return defer.promise;
        };




        return {
          search: search
        };
      }
  ]);
})();