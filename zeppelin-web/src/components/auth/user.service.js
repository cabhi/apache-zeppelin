/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('zeppelinWebApp').factory('principal', function ($q, $http, $httpParamSerializer, localStorageService, baseUrlSrv) {
  var principal = {};

  var setTicket = function (ticket) {
    if (ticket) {
      localStorageService.set('ticket', ticket);
      localStorageService.set('authenticated', true);
    } else {
      localStorageService.remove('ticket');
      localStorageService.remove('authenticated');
    }
  };

  principal.isAuthenticated = function () {
    return localStorageService.get('authenticated');
  };

  principal.checkAuthenticationWithServer = function () {
    return $http.get(baseUrlSrv.getRestApiBase() + '/security/ticket').then(function (response) {
      setTicket(angular.fromJson(response.data).body);
      return true;
    }, function (errorResponse) {
      setTicket(null);
    });
  };

  principal.authenticate = function (identity) {
    return $http({
      method: 'POST',
      url: baseUrlSrv.getRestApiBase() + '/login',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: $httpParamSerializer({
        'userName': identity.userName,
        'password': identity.password
      })
    }).then(function successCallback(response) {
      setTicket(angular.fromJson(response.data).body);
    }, function errorCallback(errorResponse) {
      setTicket(null);
    });
  };

  principal.getTicket = function () {
    return localStorageService.get('ticket');
  };

  principal.logout = function () {
    setTicket(null);
  };

  return principal;
});
