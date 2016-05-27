'use strict';

angular.module('zeppelinWebApp')
  .factory('utils', function () {
    var utils = {};

    function flattenedKeys(obj, parentKey) {
       return _.map(obj, function (value, key) {
         if (_.isPlainObject(value)) {
           return flattenedKeys(value, key);
         }
         return parentKey ? parentKey + '.' + key : key;
      });
    }

    utils.getFlatObjectGraph = function (obj) {
      return _.flattenDeep(flattenedKeys(obj));
    };


    utils.getPath = function (obj, path) {
      return _.reduce(path.split('.'), function (o, token) {
        return o && o[token];
      }, obj)
    };

    return utils;
  });
