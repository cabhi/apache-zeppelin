'use strict';

angular.module('zeppelinWebApp')
  .service('DataTypes', function ($resource) {
    this.map = $resource('config/data-type.map.json').get();

    this.format = function (key, value) {
      switch (this.map[key]) {
        case 'DateTime':
          return moment(value).format('DD-MMM-YYYY hh:mm:ss A');
        case 'millisecond':
          return value + 'ms';
        case 'byte':
          return value + 'bytes';
        default:
          return value;
      }
    };
  });
