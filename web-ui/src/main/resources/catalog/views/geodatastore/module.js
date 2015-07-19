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
    'gnUtilityService',
    'gnSearchSettings',
    'gnViewerSettings',	
    'Metadata',
		'gnSearchManagerService',
		'GdsUploadFactory',
	function($scope, $http, $translate,
             gnUtilityService, gnSearchSettings, gnViewerSettings,Metadata,gnSearchManagerService, GdsUploadFactory) {
		$scope.loadCatalogInfo();
    $scope.searchResults = { records: [] };
		$scope.total = 0;
		$scope.GdsUploadFactory = GdsUploadFactory;

		$scope.test = function() {
			alert("Click!");
		}

		$scope.$watch('user', function() {
			$scope.updateResults(1);
		})

	  $scope.updateResults = function(page,any,order){
	    if (!any) any="";
		if (!order) order="changeDate";
		
		  gnSearchManagerService.gnSearch({
				_isTemplate: 'n',
				_content_type:'json',
				fast: 'index',
				type: 'dataset',
				_owner: $scope.user.id,
				from: (page-1)*5+1,
				any: any,
				sortBy:order,
				to: page*5
		}).then(function(data) {
				var searchResults = { records: []};
				for (var i = 0; i < data.metadata.length; i++) {
					searchResults.records.push(new Metadata(data.metadata[i]));
		  	}
		  
		    $scope.searchResults = searchResults;
			$scope.total = data.count;
		  });
	  }
	  
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
			$scope.mdSelected = md;
			$scope.hasSelected = true;
			/*
			* set the form fields
			*/
			if (!md.keywords) {
			 md.keywords = [];
			}
			$("#tw").val(md.keywords.join(','));
		};
	  
		//grab the filetype either from format or from file extension
		$scope.getFileType = function (md) {
			var ftype="";
			if (!md.fileType) {
				fprops = md.link[0].split('|');
				if (fprops.length > 3 && fprops[3] != '') {
					type = fprops[3]
				} else {
					ftype = fprops[0].split(".")[1];
				}
			} else {
				ftype = md.fileType;
				return GdsUploadFactory.getFileIcon(ftype);
			}

			if (["zip","rar","application/zip"].indexOf(ftype) > 0) {
				return "fa-file-archive-o";
			} else if (["xls","xlsx","ods"].indexOf(ftype) > 0) {
				return "fa-file-excel-o";
			} else if (["doc","docx","rtf"].indexOf(ftype) > 0) {
				return "fa-file-word-o";
			} else if (["ppt","pptx"].indexOf(ftype) > 0) {
				return "fa-file-powerpoint-o";
			} else if (["csv"].indexOf(ftype) > 0) {
				return "pdok-i-csv";
			} else {
			 	return "fa-file-code-o";
			}
		};

		$scope.setBgNewFile = function(file) {
			var selectedIdentifier = null;
			if ($scope.hasSelected) {
				if ($scope.mdSelected.identifier) {
					selectedIdentifier = $scope.mdSelected.identifier;
				} else if ($scope.mdSelected['geonet:info'] && $scope.mdSelected['geonet:info'].uuid) {
					selectedIdentifier = $scope.mdSelected['geonet:info'].uuid;
				}
			}
			if (file.identifier  == selectedIdentifier) {
				return 'modify';
			} else {
				return null;
			}

		};
	  $scope.setBG = function (md) {
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

		$scope.getFileNameForNewFiles = function(newFile) {
			return newFile.title;
		};

		$scope.tab = "upload";
		$scope.getTab = function (key,val){
			if ($scope.tab == key) return val; else return "";
		}
		
		$scope.setTab = function(val){
			//if form modified&&not saved, warn to loose changes?
			$scope.tab=val;
		}
		
	  //grab the filename from metadata, for now take the first link, later check which link is the correct link, sometimes filename is empty then use file desc
	  $scope.getFileName = function (md) {
			if (md.link.length == 0) {
				return md.defaultTitle
			} else {
				fprops = md.link[0].split('|');
				if (fprops[0]!='') {
					return fprops[0]
				} else if (fprops[1]!='') {
					return fprops[1]
				}	else {
					return fprops[2];
				}
			}
	  }
	  
  }]);
  
  module.controller("geoDataStoreController", ['$scope', function ($scope) {

  }]);


})();
