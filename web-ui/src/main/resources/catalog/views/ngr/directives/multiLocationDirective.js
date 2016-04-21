(function(){
    goog.provide('ngr_multi_location_directive');

    goog.require('gn_thesaurus_service');

    var module = angular.module('ngr_multi_location_directive', ['gn_thesaurus_service']);

    module.directive('ngrMultiLocation', ['gnThesaurusService',
        function(gnThesaurusService) {
            return {
                restrict: 'A',
                scope: {
                    model: "=bboxes",
                    geometry: "="
                },
                link: function (scope, element, attrs) {
                    console.log("Linking ngrMultiLocation id=" + attrs["id"]);
                    scope.thesaurusKey = attrs.thesaurusKey || '';
                    scope.max = gnThesaurusService.DEFAULT_NUMBER_OF_RESULTS;

                    // overwrite with accessor
                    Object.defineProperty(scope, 'model', {
                        get: function () {
                            return scope._model;
                        },

                        set: function (value) {
                            debugger; // sets breakpoint
                            scope._model = value;
                        }
                    });
                    if (!angular.isArray(scope.model)) {
                        scope.model = [];
                    }

                    var updateBoundingBox = function() {
                        if (!scope.model || scope.model.length == 0) {
                            scope.geometry = null;
                            return null;
                        }
                        var multiPolygon = null;

                        angular.forEach(scope.model, function(value){
                            if (value && value.props && value.props.geo) {
                                // Get geometry and add it to the multipolygon.
                                var coordinates = new Array();
                                var geo = value.props.geo;
                                var north = parseFloat(geo.north);
                                var south = parseFloat(geo.south);
                                var west = parseFloat(geo.west);
                                var east = parseFloat(geo.east);

                                coordinates.push([west, south]);
                                coordinates.push([west, north]);
                                coordinates.push([east, north]);
                                coordinates.push([east, south]);
                                coordinates.push([west, south]);
                                if (!multiPolygon) {
                                    multiPolygon = new ol.geom.MultiPolygon([[coordinates]], 'XY');
                                } else {
                                    var poly = new ol.geom.Polygon([coordinates], 'XY');
                                    multiPolygon.appendPolygon(poly);
                                }
                            }
                        });
                        if (multiPolygon) {
                            var extent = multiPolygon.getExtent()
                            var calculatedBbox = [
                                [extent[0], extent[1]],
                                [extent[2], extent[1]],
                                [extent[2], extent[3]],
                                [extent[0], extent[3]],
                                [extent[0], extent[1]]
                            ];
                            var calculatedBboxGeometry = new ol.geom.Polygon([calculatedBbox], 'XY');
                            var wktFormat = new ol.format.WKT();
                            var wktString = wktFormat.writeGeometry(calculatedBboxGeometry);
                            scope.geometry = wktString;
                        } else {
                            scope.geometry = null;
                        }
                    };

                    scope.$watchCollection('model', updateBoundingBox);
                    var keywordsAutocompleter = gnThesaurusService.getKeywordAutocompleter({
                        thesaurusKey: scope.thesaurusKey
                    });

                    var source = keywordsAutocompleter.ttAdapter();
                    // Init tagsinput object
                    var tagsinput = $(element).tagsinput({

                    });

                    $(element).bind('itemRemoved', function(itemRemoved) {
                       console.log("Item removed", itemRemoved);
                        scope.$apply(function() {
                            if (!angular.isArray(scope.model)) {
                                scope.model = [];
                            }
                            removeFromModelByLabel(itemRemoved.item);
                        });
                    });
                    // init typeahead
                    var internalInput =  tagsinput[0].input();
                    var container = tagsinput[0].$container;
                    if (container) {
                        internalInput.on('focus', function() {
                            container.addClass('focused');
                        });
                        internalInput.on('blur', function() {
                            container.removeClass('focused');
                        })
                    }

                    internalInput.typeahead({
                        minLenght: 0,
                        highlight: true,
                        autoselect: true
                    }, {
                        name: 'keyword',
                        displayKey: 'label',
                        source: source
                    }).bind('typeahead:selected typeahead:autocompleted', $.proxy(function(obj, datum) {
                        this.tagsinput('add', datum.label);
                        this.tagsinput('input').typeahead('close');
                        //this.tagsinput('input').val('');
                        this.tagsinput('input').typeahead('val', '');
                        scope.$apply(function(){
                            if (!angular.isArray(scope.model)) {
                                scope.model = [];
                            }
                            addItemToModel(datum);
                        });
                    }, element)).bind('typeahead:selected', function(e, suggestion) {
                        console.log('typeahead:selected -> ' + suggestion);
                    }).bind('typeahead:autocompleted', function(e, suggestion) {
                        console.log('typeahead:autocompleted -> ' + suggestion);
                    });

                    // When clicking the element trigger input
                    // to show autocompletion list.
                    // https://github.com/twitter/typeahead.js/issues/798
                    internalInput.on('typeahead:opened', function () {
                        var initial = internalInput.val(),
                            ev = $.Event('keydown');
                        ev.keyCode = ev.which = 40;
                        internalInput.trigger(ev);
                        if (internalInput.val() != initial) {
                            internalInput.val('');
                        }
                        return true;
                    });

                    var searchInModel = function(datum) {
                        // First check if datum already exists in model
                        var found = false, index = -1;
                        for (var i = 0; i < scope.model.length && !found; i++) {
                            if (datum && datum.props && datum.props.uri) {
                                var item = scope.model[i];
                                if (item && item.props && item.props.uri  && datum.props.uri === item.props.uri) {
                                    found = true;
                                    index = i;
                                }
                            }
                        }
                        return index;
                    };

                    var addItemToModel = function (datum) {
                      if (searchInModel(datum) == -1) {
                          scope.model.push(datum);
                      }
                    };

                    var removeFromModelByLabel = function(label) {
                        for (var i = 0; i < scope.model.length; i++) {
                            var item = scope.model[i];
                            if (item && item.label && item.label === label) {
                                // Remove element from the model
                                scope.model.splice(i, 1);
                            }
                        }
                    };
                }
            };

        }
    ]);

}());