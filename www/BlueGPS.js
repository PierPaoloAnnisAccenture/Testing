var exec = require('cordova/exec');

exports.init = function (success, error,sdkKey,sdkSecret,sdkEndpoint,enableNetwork) {
    exec(success, error, 'BlueGPS', 'initializeSDK', [sdkKey,sdkSecret,sdkEndpoint,enableNetwork]);
};

exports.login = function (success, error,username,password,token) {
    exec(success, error, 'BlueGPS', 'login', [username,password,token]);
};

exports.openMap = function (success, error,tagID,style,showMap) {
    exec(success, error, 'BlueGPS', 'openMap', [tagID,style,showMap]);
};

exports.startAdv = function (success, error) {
    exec(success, error, 'BlueGPS', 'startAdv', []);
};

exports.stopAdv = function (success, error) {
    exec(success, error, 'BlueGPS', 'stopAdv', []);
};