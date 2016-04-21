(function () {
    goog.provide('abc');

    var module = angular.module('ngr_search_home_controller', []);

    module.controller('NgrSearchHomeController', ['$scope', '$location', '$log',
        function ($scope, $location, $log) {

        $scope.searchHomeParams = {
            bboxes: null
        };

        $scope.performSearchHome = function() {
            $log.debug('NgrSearchHomeController -> performSearchHome()');
            var any = $scope.searchHomeParams.anyParameter;
            var geometry = $scope.searchHomeParams.geometry;

            var searchParams = {
              any_OR_geokeyword_OR_title_OR_keyword: any
            };
            if (geometry) {
                angular.extend(searchParams, {geometry: geometry});
            }
            $log.debug('Search parameters', searchParams);
            $location.path('/search').search(searchParams);

        };

        // watch for changes in home search parameters
        $scope.$watch('searchHomeParams.bboxes', function(newValue, oldValue) {
            $log.info("searchHomeParams.bboxes chagit stnged");
        });

        $scope.$on('$locationChangeSuccess', function(event, newUrl) {
            var activeTab = $location.path().
                match(/^(\/[a-zA-Z0-9]*)($|\/.*)/)[1];
            $log.debug("active tab -> " + activeTab);
            // reset search paramameters
            if (activeTab === '/home') {
                $scope.searchHomeParams = {
                    anyParameter: null,
                    geometry: null,
                    bboxes: null
                };
            }
        });

    }]);

})();
