(function() {

  goog.provide('gn_search_ngr_config');

  var module = angular.module('gn_search_ngr_config', []);

  module
      .run([
        'gnSearchSettings',
        'gnViewerSettings',
        'gnOwsContextService',
        'gnMap',
        function(searchSettings, viewerSettings, gnOwsContextService, gnMap) {
          // Load the context defined in the configuration
          viewerSettings.defaultContext =
            viewerSettings.mapConfig.viewerMap ||
            '../../map/config-viewer.xml';

          // Keep one layer in the background
          // while the context is not yet loaded.
          viewerSettings.bgLayers = [
            gnMap.createLayerForType('osm')
          ];

          viewerSettings.servicesUrl =
            viewerSettings.mapConfig.listOfServices || {};

          var bboxStyle = new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(255,0,0,1)',
              width: 2
            }),
            fill: new ol.style.Fill({
              color: 'rgba(255,0,0,0.3)'
            })
          });
          searchSettings.olStyles = {
            drawBbox: bboxStyle,
            mdExtent: new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'orange',
                width: 2
              })
            }),
            mdExtentHighlight: new ol.style.Style({
              stroke: new ol.style.Stroke({
                color: 'orange',
                width: 3
              }),
              fill: new ol.style.Fill({
                color: 'rgba(255,255,0,0.3)'
              })
            })

          };

          /*******************************************************************
             * Define maps
             */
          proj4.defs("EPSG:28992","+proj=sterea +lat_0=52.15616055555555 +lon_0=5.38763888888889 +k=0.9999079 +x_0=155000 +y_0=463000 +ellps=bessel +towgs84=565.417,50.3319,465.552,-0.398957,0.343988,-1.8774,4.0725 +units=m +no_defs");
			ol.proj.get('EPSG:28992').setExtent([-285401.92,22598.08,595401.92,903401.92]);
			ol.proj.get('EPSG:28992').setWorldExtent([ -1.65729160235431,48.0405018704265,11.2902578747914,55.9136415748388]);
			var matrixIds=[];
			var matrixIds2=[];
			for(var i=0;i<=12;++i){if(i<10){
			matrixIds[i]="0"+i;
			matrixIds2[i]="EPSG:28992:"+i
			}else{matrixIds[i]=""+i;
			matrixIds2[i]="EPSG:28992:"+i
			}}
			var resolutions=[3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84];
			var tileLayers= [new ol.layer.Tile({
			title:'BRT',attribution:'PDOK',
			source: new ol.source.WMTS({
			url: 'http://geodata.nationaalgeoregister.nl/tiles/service/wmts/brtachtergrondkaartgrijs',
			layer: 'brtachtergrondkaartgrijs',
			matrixSet: 'EPSG:28992',
			format: 'image/png',
			projection: ol.proj.get('EPSG:28992'),
			tileGrid: new ol.tilegrid.WMTS({
			origin: [-285401.92,903402.0],
			resolutions: resolutions,
			matrixIds: matrixIds2
			}),
			wrapX: true
			})
			}),new ol.layer.Tile({
			title:'Luchtfoto',attribution:'PDOK',visible:false,
			source: new ol.source.WMTS({
			url: 'http://geodata1.nationaalgeoregister.nl/luchtfoto/wmts?style=default&',
			layer: 'luchtfoto',
			matrixSet: 'nltilingschema',
			format: 'image/jpeg',
			projection: ol.proj.get('EPSG:28992'),
			tileGrid: new ol.tilegrid.WMTS({
			origin: [-285401.92,903402.0],
			resolutions: resolutions,
			matrixIds: matrixIds,
			style:'default'
			}),
			wrapX: true
			})
			})];
			//important to set the projection info here (also), used as view configuration
			var mapsConfig = {
			resolutions: resolutions,
			extent: [-285401.92,22598.08,595401.92,903401.92],
			projection: 'EPSG:28992',
			center: [150000, 450000],
			zoom: 7
			};
			// Add backgrounds to TOC
			viewerSettings.bgLayers = tileLayers;
			viewerSettings.servicesUrl = {};
			//Configure the ViewerMap
			var viewerMap = new ol.Map({
			controls:[],
			layers: tileLayers,
			view: new ol.View(mapsConfig)
			});
			//configure the SearchMap
			var searchMap = new ol.Map({
			controls:[],
			layers: [tileLayers[0]],
			view: new ol.View(mapsConfig)
			});


          /** Facets configuration */
          searchSettings.facetsSummaryType = 'hits';

          /*
             * Hits per page combo values configuration. The first one is the
             * default.
             */
          searchSettings.hitsperpageValues = [20, 50, 100];

          /* Pagination configuration */
          searchSettings.paginationInfo = {
            hitsPerPage: searchSettings.hitsperpageValues[0]
          };

          /*
             * Sort by combo values configuration. The first one is the default.
             */
          searchSettings.sortbyValues = [{
            sortBy: 'relevance',
            sortOrder: ''
          }, {
            sortBy: 'changeDate',
            sortOrder: ''
          }, {
            sortBy: 'title',
            sortOrder: 'reverse'
          }, {
            sortBy: 'rating',
            sortOrder: ''
          }, {
            sortBy: 'popularity',
            sortOrder: ''
          }, {
            sortBy: 'denominatorDesc',
            sortOrder: ''
          }, {
            sortBy: 'denominatorAsc',
            sortOrder: 'reverse'
          }];

          /* Default search by option */
          searchSettings.sortbyDefault = searchSettings.sortbyValues[0];

          /* Custom templates for search result views */
          searchSettings.resultViewTpls = [{
                  tplUrl: '../../catalog/views/ngr/templates/card.html',
                  tooltip: 'Grid',
                  icon: 'fa-th'
                }];

          // For the time being metadata rendering is done
          // using Angular template. Formatter could be used
          // to render other layout

          // TODO: formatter should be defined per schema
          // schema: {
          // iso19139: 'md.format.xml?xsl=full_view&&id='
          // }
          searchSettings.formatter = {
            // defaultUrl: 'md.format.xml?xsl=full_view&id='
            defaultUrl: 'md.format.xml?xsl=xsl-view&uuid=',
            defaultPdfUrl: 'md.format.pdf?xsl=full_view&uuid=',
            list: [{
            //  label: 'inspire',
            //  url: 'md.format.xml?xsl=xsl-view' + '&view=inspire&id='
            //}, {
            //  label: 'full',
            //  url: 'md.format.xml?xsl=xsl-view&view=advanced&id='
            //}, {
              label: 'full',
              url: 'md.format.xml?xsl=full_view&uuid='
            }]
          };

          // Set the default template to use
          searchSettings.resultTemplate =
              searchSettings.resultViewTpls[0].tplUrl;

          // Set custom config in gnSearchSettings
          angular.extend(searchSettings, {
            viewerMap: viewerMap,
            searchMap: searchMap
          });
        }]);
})();
