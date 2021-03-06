/**
 * UpnpDevices plugin for Cordova/Phonegap
 *
 * Copyright (c) 2014 Laguillaumie sylvain <s.lagui@free.fr> Converted to Cordova 3.0 format
 * MIT license
 *
 * @author Laguillaumie Sylvain
 * Copyright (c) Ubukey Ltd. 2014. All Rights Reserved.
 * Available under the terms of the MIT License.
 * 
 */

/*global module, console, require*/
/*jshint -W097 */
'use strict';
var exec = require('cordova/exec');

var UpnpDevices = {
	start: function (type,callback) {
		return exec(function (result) {
			if (callback) {
				callback(result);
			}

		},UpnpDevices.fail, "UpnpDevices", "start", '');
	},
	fail: function (o) {
		console.error("Error " + JSON.stringify(o));
	}
};

module.exports = UpnpDevices;
