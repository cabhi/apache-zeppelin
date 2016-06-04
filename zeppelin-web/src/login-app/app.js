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
  var loginApp = angular.module('loginApp', [
    'ngCookies',
    'ngAnimate',
    'ngRoute',
    'ngSanitize',
    'angular-websocket',
    'ui.bootstrap'
  ])
    .config(function ($httpProvider, $routeProvider) {
      // withCredentials when running locally via grunt
      $httpProvider.defaults.withCredentials = true;

      $routeProvider.when("/login", {
        templateUrl: "login-app/login/login.html",
        controller: "LoginCtrl"
      }).otherwise({
        redirectTo: "/login"
      });
    }).run(function ($http, baseUrlSrv) {
      function auth() {
        $http.defaults.withCredentials = true;

        return $http.get(baseUrlSrv.getRestApiBase() + '/security/ticket').then(function (response) {
          window.location.href = '/';
        }, function (errorResponse) {
          // Handle error case
        });
      }
      auth();
    });
} ());

