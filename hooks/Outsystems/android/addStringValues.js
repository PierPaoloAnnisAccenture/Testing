var fs = require('fs');
var path = require('path');

const constants={
    resPath : path.join("platforms","android","app","src","main","res"),
    stringsPath : path.join("values","strings.xml"),
    colorsPath : path.join("values","colors.xml"),
    stylesPath : path.join("values","styles.xml"),
    pluginId : "cordova-outsystems-bluegps"
}

module.exports = function (context) {
    
    console.log("Adding values!");
    var Q = require("q");
    var deferral = new Q.defer();

    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;

    var files = [constants.stringsPath,constants.colorsPath,constants.stylesPath]

    

    files.forEach(file => {
        var srcFile = path.join(projectRoot,"plugins",constants.pluginId,"resources","android",file);
        var destFile = path.join(projectRoot,constants.resPath,file);

        var newContent = fs.readFileSync(srcFile, {encoding:'utf8'});
        var content
        if(!fs.existsSync(destFile)){
            content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n"+newContent
        }else{
            content = fs.readFileSync(destFile, {encoding:'utf8'});
            content = content.replace("</resources>",newContent);
        }
        fs.writeFileSync(destFile, content);

        console.log("Added string "+file+"!");
    });

    
    console.log("Finished Adding values!",);
    
        

    deferral.resolve();

    return deferral.promise;
}