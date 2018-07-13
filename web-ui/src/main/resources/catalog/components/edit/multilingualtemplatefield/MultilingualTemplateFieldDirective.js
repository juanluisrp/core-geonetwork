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
  goog.provide('gn_multilingual_template_field_directive');

  var module = angular.module('gn_multilingual_template_field_directive', []);

  module.directive('gnMultilingualTemplateField', ['$log', '$timeout',
    function($log, $timeout) {

    return {
      restrict: 'A',
      transclude: true,
      templateUrl: '../../catalog/components/edit/' +
      'multilingualtemplatefield/partials/multilingualtemplatefield.html',
      scope: {
        id: '@gnTemplateField'
      },
      link: function(scope, element, attrs) {
        var recursiveCalls = 0;
        $log.info('Linking multilingualtemplatefield directive.');
        var multilingualFormFieldSelector =
          'div[data-ng-transclude] > input.form-control[id^=gn-field-],' +
          'div[data-ng-transclude] > textarea.form-control[id^=gn-field-]';

        var attachListeners = function() {
          var multilingualInputs = element.find(multilingualFormFieldSelector);
          if (multilingualInputs.length === 0 && recursiveCalls++ < 10) {
            $log.info("Schedule recursive call");
            $timeout(attachListeners);
          } else {
            multilingualInputs.each(function() {
              var inputEl = $(this);
              var langId = inputEl.attr('lang');
              $log.info("Language detected: " + langId, inputEl);

              inputEl.change( updateMultilingualString);

            });
          }
        };
        var updateMultilingualString = function() {
          $log.info('Multilingual field changed. Calculating the new replace string...')
          var multilingualInputs = element.find(multilingualFormFieldSelector);
          var newValueObject = [];
          multilingualInputs.each(function() {
            var currentField = $(this);
            $log.info(currentField.attr('id') + ' - ' + currentField.attr('lang') + ' - ' + currentField.val());
            var ref = currentField.attr('name');
            var langId = currentField.attr('lang');
            var value = currentField.val();
            newValueObject.push({ref: ref, lang: langId, value: value});
          });
          var newValueString = generateNewMultilingualString(newValueObject);
          $(element).find('.multilingual-control').val(newValueString).trigger('change');
        };

        var generateNewMultilingualString = function(fieldsArray) {
          var multilingualString = '';
          var internalSeparator = ':::';
          var fieldSeparator = '@@@';
          for (var i = 0; i < fieldsArray.length; i++) {
            var field = fieldsArray[i];
            multilingualString += field.ref + internalSeparator + field.lang + internalSeparator + field.value;
            if (i !== fieldsArray.length - 1) {
              multilingualString += fieldSeparator;
            }
          }
          return multilingualString;
        };

        $timeout(attachListeners());
      } // end of link
    }; // end of return
  }]); //end of directive

})();
