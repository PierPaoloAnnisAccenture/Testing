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

exports.navigationMap = function (success, error,tagID,style,showMap) {
    exec(success, error, 'BlueGPS', 'navigationMap', [tagID,style,showMap, {
                                                                              "origin":{
                                                                                 "bookingType":null,
                                                                                 "name":"Elevator",
                                                                                 "mapId":11,
                                                                                 "x":32.64,
                                                                                 "y":6.3
                                                                              },
                                                                              "destination":{
                                                                                 "bookingType":"desk",
                                                                                 "name":"Desk180",
                                                                                 "mapId":11,
                                                                                 "x":-11.05,
                                                                                 "y":-3.12
                                                                              }
                                                                           }
]);
};

exports.startAdv = function (success, error) {
    exec(success, error, 'BlueGPS', 'startAdv', []);
};

exports.stopAdv = function (success, error) {
    exec(success, error, 'BlueGPS', 'stopAdv', []);
};