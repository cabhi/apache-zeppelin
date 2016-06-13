/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function () {
  var isLoggedIn = false;

  var zeppelinWebApp = angular.module('zeppelinWebApp', [
    'ngCookies',
    'ngAnimate',
    'ngSanitize',
    'angular-websocket',
    'ui.ace',
    'ui.bootstrap',
    'ngRoute',
    'ui.router',
    'as.sortable',
    'ngTouch',
    'ngDragDrop',
    'angular.filter',
    'monospaced.elastic',
    'puElasticInput',
    'xeditable',
    'ngToast',
    'focus-if',
    'ngResource',
    'ui.grid',
    'ui.grid.expandable',
    'ui.grid.selection',
    'ui.grid.pinning',
    'ui.grid.infiniteScroll',
    'ui.grid.autoResize',
    'ui.grid.grouping',
    'ui.grid.resizeColumns',
    'ui.grid.exporter',
    'ngJsonExplorer',
    'angularjs-dropdown-multiselect',
    'ui.sortable',
    'infinite-scroll',
    'LocalStorageModule'
  ])

    .filter('breakFilter', function () {
      return function (text) {
        if (!!text) {
          return text.replace(/\n/g, '<br />');
        }
      };
    })

    .config(function ($httpProvider, $stateProvider, $urlRouterProvider, ngToastProvider, localStorageServiceProvider) {
      // withCredentials when running locally via grunt
      $httpProvider.defaults.withCredentials = true;

      $stateProvider
        .state('app', {
          abstract: true,
          templateUrl: 'app/main/main.html',
          controller: 'MainCtrl'
        })
        .state('app.notebook', {
          url: '/notebook/:noteId',
          templateUrl: 'app/notebook/notebook.html',
          controller: 'NotebookCtrl'
        })
        .state('app.paragraph', {
          url: '/notebook/:noteId/paragraph/:paragraphId?',
          templateUrl: 'app/notebook/notebook.html',
          controller: 'NotebookCtrl'
        })
        .state('app.paragraphQuery', {
          url: '/notebook/:noteId/paragraph?=:paragraphId',
          templateUrl: 'app/notebook/notebook.html',
          controller: 'NotebookCtrl'
        })
        .state('app.interpreter', {
          url: '/interpreter',
          templateUrl: 'app/interpreter/interpreter.html',
          controller: 'InterpreterCtrl'
        })
        .state('app.configuration', {
          url: '/configuration',
          templateUrl: 'app/configuration/configuration.html',
          controller: 'ConfigurationCtrl'
        })
        .state('app.search', {
          url: '/search/:searchTerm',
          templateUrl: 'app/search/result-list.html',
          controller: 'SearchResultCtrl'
        })
        .state('login', {
          url: '/login',
          templateUrl: 'app/auth/login/login.html',
          controller: 'LoginCtrl'
        })
        .state('app.default', {
          url: '/default',
          template: 'Please wait..',
          controller: ["$rootScope", "$rootScope", "$location", function ($scope, $rootScope, $location) {
            if ($rootScope.firstNoteId) {
              $location.path('/notebook/' + $rootScope.firstNoteId);
            } else {
              $scope.$on('firstNoteId', function (event, noteId) {
                $location.path('/notebook/' + noteId);
              });
            }
          }]
        });

      $urlRouterProvider.otherwise('/default');

      localStorageServiceProvider.setPrefix('zeppelin');

      ngToastProvider.configure({
        dismissButton: true,
        dismissOnClick: false,
        timeout: 6000
      });
    })

    .run(function ($rootScope, $location, $state) {
      $('#pageLoader').show();
      $rootScope.$on("$stateChangeStart", function (event, nextRoute, currentRoute) {
        $('#pageLoader').show();
      });
      $rootScope.$on('$stateChangeError', function (event, current, previous, rejection) {
        $('#pageLoader').hide();
      });
      $rootScope.$on('$viewContentLoaded', function () {
        $('#pageLoader').hide();
      });
    })

    .run(function ($rootScope, $location, $state, principal) {
      $rootScope.$on('$stateChangeStart', function (event, toState) {
        console.log($location.path());
        if (!principal.isAuthenticated() && toState.name !== 'login') {
          $state.transitionTo('login');
          event.preventDefault();
        } else if (principal.isAuthenticated() && toState.name === 'login') {
          $state.transitionTo('app.default');
          event.preventDefault();
        }
      });
    })

    .run(function ($rootScope, principal) {
      $rootScope.ticket = principal.getTicket();
    });

  /**
   * Bootstrap application only after setting up the authentication state
   */
  angular.module('mockApp', []).provider({
    $rootElement: function () {
      this.$get = function () {
        return angular.element('<div ng-app></div>');
      };
    }
  });

  function auth() {
    var $http = angular.injector(['ng', 'mockApp']).get('$http');
    var baseUrlSrv = angular.injector(['zeppelinWebApp', 'mockApp']).get('baseUrlSrv');
    var localStorageService = angular.injector(['zeppelinWebApp', 'mockApp']).get('localStorageService');
    // withCredentials when running locally via grunt
    $http.defaults.withCredentials = true;

    return $http.get(baseUrlSrv.getRestApiBase() + '/security/ticket').then(function (response) {
      localStorageService.set('authenticated', true);
      localStorageService.set('ticket', angular.fromJson(response.data).body);
    }).catch(function () {
      localStorageService.remove('authenticated');
      localStorageService.remove('ticket');
    });
  }

  function bootstrapApplication() {
    angular.bootstrap(document, ['zeppelinWebApp']);
  }

  angular.element(document).ready(function () {
    auth().then(bootstrapApplication);
  });
}());

