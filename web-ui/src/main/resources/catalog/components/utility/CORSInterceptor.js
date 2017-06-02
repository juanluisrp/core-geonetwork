/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_cors_interceptor');

  var module = angular.module('gn_cors_interceptor', []);

  /**
   * CORS Interceptor
   *
   * This interceptor checks if each AJAX call made in AngularJS needs a proxy
   * or not.
   */

  module.config([
    '$httpProvider',
    function($httpProvider) {
      $httpProvider.interceptors.push([
        '$q',
        '$injector',
        'gnGlobalSettings',
        'gnLangs',
        '$location',
        function($q, $injector, gnGlobalSettings, gnLangs, $location) {

          /**
           * Return the target hostname with protocol and port. For example https://www.example.com:80
           * @param targetUrl the url to check
           * @returns {string} a URL string with protocol, hostname and port
           */
          var getTargetHostname = function(targetUrl) {
            var parser = document.createElement('a');
            parser.href = targetUrl;
            var targetHostname = parser.protocol + "//" + parser.hostname + ":";
            if (!parser.port) {
              if (parser.protocol === 'http') {
                targetHostname += '80';
              } else if (parser.protocol === 'https') {
                targetHostname += '443';
              }
            } else {
              targetHostname += parser.port;
            }
            return targetHostname;
          };

          return {
            request: function(config) {
              if (gnLangs.current) {
                config.headers['Accept-Language'] = gnLangs.current;
              }
              if (config.url.indexOf('http', 0) === 0) {
                var url = config.url.split('/');
                url = url[0] + '/' + url[1] + '/' + url[2] + '/';

                if ($.inArray(url, gnGlobalSettings.requireProxy) != -1) {
                  // require proxy
                  config.url = gnGlobalSettings.proxyUrl +
                      encodeURIComponent(config.url);
                }
              }

              return $q.when(config);
            },
            responseError: function(response) {
              var config = response.config;

              if (config.nointercept) {
                return $q.when(config);
              // let it pass
              } else if (!config.status || config.status == -1) {
                var defer = $q.defer();

                if (config.url.indexOf('http', 0) === 0) {

                  // get current service hostname
                  var gnHostname = $location.protocol() + "://" + $location.host() + ":" + $location.port();
                  var targetHostName = getTargetHostname(config.url);
                  if (targetHostName === gnHostname) {
                    // if the target URL is in the GN host, don't use proxy and reject the promise.
                    return $q.reject(response);
                  } else {
                    // if the target URL is in other site/protocol/port that GN, use the proxy to make the request.
                    var url = config.url.split('/');
                    url = url[0] + '/' + url[1] + '/' + url[2] + '/';

                    if ($.inArray(url, gnGlobalSettings.requireProxy) == -1) {
                      gnGlobalSettings.requireProxy.push(url);
                    }

                    $injector.invoke(['$http', function($http) {
                      // This modification prevents interception (infinite
                      // loop):

                      config.nointercept = true;

                      // retry again
                      $http(config).then(function(resp) {
                        defer.resolve(resp);
                      }, function(resp) {
                        defer.reject(resp);
                      });
                    }]);
                  }
                } else {
                  return $q.reject(response);
                }

                return defer.promise;
              } else {
                return response;
              }
            }
          };

        }
      ]);
    }
  ]);

})();
