/**
 * Created by JuanLuis on 13/07/2015.
 */
(function() {
  goog.provide('geodatastore_upload_service');

  var module = angular.module('geodatastore_upload_service', []);

  module.factory('GdsUploadFactory', [
    '$q',
    '$http',
    '$log',
    '$rootScope',
    function($q, $http, $log) {
      var uploadedFiles = {};
      var selected = null;
      var mdDirty = false;

      uploadedFiles.setDirty = function(isDirty) {
        mdDirty = isDirty;
      };

      uploadedFiles.isDirty = function() {
        return mdDirty;
      }


      uploadedFiles.list = [];
      uploadedFiles.setMdSelected = function(md) {
        selected = md;
        $log.debug("setMdSelected called: " + angular.toJson(md));
      };
      uploadedFiles.getMdSelected = function() {
        return selected;
      }

      uploadedFiles.add = function(file) {
        uploadedFiles.list.unshift(file);
      };

      uploadedFiles.clearList = function() {
        uploadedFiles.list = [];
      };

      uploadedFiles.getFileIcon = function(file) {
        var type = 'unknown';
        if (file instanceof File) {
          type = file.type
        } else if (file instanceof String) {
          type = file;
        } else if (file && file.fileType) {
          type = file.fileType;
        }
        var iconClass = "fa-file-o";
        type = type.toLowerCase();
        if (type.contains("pdf")) {
          iconClass = "fa-file-pdf-o";
        } else if (type.startsWith("image/") || ["png", "gif", "jpg", "tiff", "tif", "jp2"].indexOf(file) >= 0) {
          iconClass = "fa-file-image-o";
        } else if (type.startsWith("video/")) {
          iconClass = "fa-file-video-o";
        } else if (type.startsWith("audio/")) {
          iconClass = "fa-file-audio-o";
        } else if (type.contains("zip") || type == "rar" || type.contains("compressed")) {
          iconClass = "fa-file-archive-o"
        } else if (type.contains("excel") || type.contains("sheet") || ["xls","xlsx","ods"].indexOf(file) >= 0 ) {
          iconClass = "fa-file-excel-o";
        } else if (type.contains("word") || type.contains("opendocument.text") || ["doc","docx","rtf"].indexOf(file) >= 0) {
          iconClass = "fa-file-word-o";
        } else if (type.contains("powerpoint") || type.contains("presentation") || ["ppt","pptx"].indexOf(file) >= 0) {
          iconClass = "fa-file-powerpoint-o";
        } else if (type === "text/plain" || type === "application/rtf") {
          iconClass = "fa-file-text-o";
        }  else if (["csv"].indexOf(file) >= 0) {
          iconClass = "pdok-i-csv";
        } else if (type === "text/css" || type === "text/html" || type === "application/rdf" || type === "application/rdf+xml"
            || type === "text/sgml" || type.contains("xml")) {
          iconClass = "fa-file-code-o";
        }
        return iconClass;
      };

      uploadedFiles.saveMetadata = function(md) {
        var defer = $q.defer();
        if (md && md.identifier ) {
          var request = new FormData();
          var url = "../../geodatastore/api/dataset/" + md.identifier;

          request.append("metadata", angular.toJson(md));
          $http.post(url, request, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
          }).success(function (data) {
            if (data.error) {
              defer.reject(data);
            } else {
              defer.resolve(data);
            }
          }).error(function(error) {
            defer.reject(error);
          });
        } else {
          $log.debug("GdsUploadService.saveMetadata -> No metadata or metadata without identifier");
        }
        return defer.promise;
      };

      uploadedFiles.replace = function(mdList, md) {
        var id = md.identifier;
        for (var i = 0; i < mdList.length; i++) {
          var mdInArray = mdList[i];
          if (mdInArray && mdInArray.identifier == id) {
            var copy = angular.copy(md);
            delete copy.saved;
            mdList[i] = copy;
          }
        }
      };

      uploadedFiles.deleteMetadata = function(md) {
        var defer = $q.defer();
        if (md && md.identifier ) {
          var  url = "../../geodatastore/api/dataset/" + md.identifier;
          $http.delete(url, {
            responseType: "json"
          }).success(function (data) {
            if (data.error) {
              defer.reject(data);
            } else {
              defer.resolve(data);
            }
          }).error(function(error) {
            if (error && error.error && error.error.message) {
              defer.reject({error: true, messages: [error.error.message]});
            } else {
              defer.reject(error);
            }
          });
        } else {
          defer.reject();
        }

        return defer.promise;

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