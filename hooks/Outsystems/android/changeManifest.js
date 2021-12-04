var fs = require('fs');
var path = require('path');

var constants = {
    manifestPath:"platforms/android/app/src/main/AndroidManifest.xml"
}

module.exports = function (context) {
    
    console.log("Start changing Manifest!");
    var Q = require("q");
    var deferral = new Q.defer();


    var manifestContent = fs.readFileSync(constants.manifestPath, {encoding:'utf8'});

    if(manifestContent.indexOf("alwaysRetainTaskState") > -1){
        if(manifestContent.indexOf("alwaysRetainTaskState=\"true") <= -1){
            manifestContent = manifestContent.replace(new RegExp("([\s|\S]*alwaysRetainTaskState=\")(.{5})([\s|\S]*)","g"),(m,m1,m2)=>{
                return m1+"true"+m2;
            });
        }
        
    }else{
        manifestContent = manifestContent.replace(new RegExp("([\s|\S]*<application)([\s|\S]*)","g"),(m,m1,m2)=>{
            return m1+" android:alwaysRetainTaskState=\"true\""+m2;
        });
    }

    fs.writeFileSync(constants.manifestPath, manifestContent);
    
    console.log("Finished changing Manifest!");

    deferral.resolve();

    return deferral.promise;
}