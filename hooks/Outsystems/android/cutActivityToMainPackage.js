var fs = require('fs');
var path = require('path');

const constants={
    javaSrcPath : path.join("platforms","android","app","src","main","java"),
    kotlinSrcPath : path.join("platforms","android","app","src","main","kotlin"),
    pluginJavaPath : path.join("com","outsystems","bluegps"),
    fileName : "MapActivity.kt"
}

module.exports = function (context) {
    
    console.log("Cuting Activity To Main Package!");
    var Q = require("q");
    var deferral = new Q.defer();

    var rawConfig = fs.readFileSync("config.xml", 'ascii');
    var match = /^<widget[\s|\S]* id="([\S]+)".+?>$/gm.exec(rawConfig);
    if(!match || match.length != 2){
        throw new Error("id parse failed");
    }

    var appId = match[1];
    appId = appId.replace(/\./g,"/")

    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;

    var srcFile = path.join(projectRoot,constants.javaSrcPath,constants.pluginJavaPath,constants.fileName)
    var destFile = path.join(projectRoot,constants.javaSrcPath,appId,constants.fileName)

    if(fs.existsSync(srcFile)){
        var content = fs.readFileSync(srcFile, {encoding:'utf8'});
        ensureDirectoryExistence(destFile)
        fs.writeFileSync(destFile, content,{flag: "w+"});
        fs.unlinkSync(srcFile)
        console.log("Finished Cuting Activity To Main Package!",);
    }else{
        console.warn("Activity File Not Found!");
    }
        

    deferral.resolve();

    return deferral.promise;
}

function ensureDirectoryExistence(filePath) {
    var dirname = path.dirname(filePath);
    if (fs.existsSync(dirname)) {
      return true;
    }
    ensureDirectoryExistence(dirname);
    fs.mkdirSync(dirname);
  }