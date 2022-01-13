# cordova-outsystems-bluegps

detail about version
https://success.outsystems.com/Support/Release_Notes/Mobile_Apps_Build_Service_Versions

1. Install cordova version 10
  
  >> npm install -g cordova@10.0.0
  
2. Create Cordova Project
 
 >> cordova create < <NameProject> > com.example.plugins < <NameProject> > 
  
3. open the folder < <NameProject> >
  >> cd < <NameProject> >
  
4. add android platform version (Cordova Engine version)
  >> cordova platform add android@10.1.1
  
*4. add ios platform version (Cordova Engine version)
  >> cordova platform add ios@6.2.0
  
  
 5 add this plugin 
  >> cordova plugin add https://github.com/PierPaoloAnnisAccenture/Testing.git
  
  
 6 build android
  >> cordova build android
  
  
  7 open android studio
  8 run the project
  
  9 open chrome for inspect
    chrome://inspect
  
  10 open the process
  
  11 type the function that you want to test that it is inside WWW/blueGPS.js
  ex
    >> BlueGPS.init(()=>{console.log("success")}, function(msg){console.log("error" + msg)}, "", "", "", false)
  
  
  

