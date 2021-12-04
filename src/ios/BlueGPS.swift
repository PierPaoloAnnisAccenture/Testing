//
//  BlueGPS.swift
//  test
//
//  Created by Luis Bouça on 12/11/2021.
//

import Foundation
import SynapsesSDK

@objc(BlueGPS) class BlueGPSPlugin: CDVPlugin{
    
    public static var sdkCredentials: SDKCredentialsModel = SDKCredentialsModel()
    public static var mapConfig:ConfigurationModel = ConfigurationModel()
    private var mapView:MapViewController?
    
    @objc(initializeSDK:) func initializeSDK(command : CDVInvokedUrlCommand){
        let sdkKey = command.argument(at: 0) as! String
        let sdkSecret = command.argument(at: 1) as! String
        let endpoint = command.argument(at: 2) as! String
        BlueGPS.setupSDK(EnvironmentModel(endpoint: endpoint, key: sdkKey, secret: sdkSecret))
        BlueGPSPlugin.sdkCredentials = SDKCredentialsModel(sdkCredentials: SynapsesCredentialModel(sdkKey: sdkKey,
                                    sdkSecret: sdkSecret),
                                    loggedUser: nil,
                                    microsoftToken: nil)
    }
    
    @objc(login:) func login(command: CDVInvokedUrlCommand){
        let username = command.argument(at: 1) as! String
        let password = command.argument(at: 2) as! String
        
        if username == "" {
            BlueGPS.shared.clearLoggedUser()
            BlueGPS.shared.initSDK { (response) in
                //...
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: response.code), callbackId: command.callbackId)
            }
        }else{
            BlueGPS.shared.setupLoggedUser(username: username, password: password){
                response in
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: response.message), callbackId:command.callbackId)
            }
        }
    }
    @objc(openMap:)func openMap(command:CDVInvokedUrlCommand){
        let tagid = command.argument(at: 0) as! String
        let styleConfig = (try? JSONSerialization.jsonObject(with: (command.argument(at: 1) as! String).data(using: .utf8)!, options: [])) as? [String:AnyObject]
        let showConfig = (try? JSONSerialization.jsonObject(with: (command.argument(at: 2) as! String).data(using: .utf8)!, options: [])) as? [String:AnyObject]
        if styleConfig == nil || showConfig == nil {
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: "JSON could not be read!"), callbackId: command.callbackId)
            return
        }
        
        BlueGPSPlugin.mapConfig = ConfigurationModel(auth: BlueGPSPlugin.sdkCredentials,
                                                     tagid: tagid,
                                                     toolbox: ToolboxModel(),
                                                     show: ShowModel(),
                                                     callbackType: nil)
        let icons:[String:AnyObject] = styleConfig!["icons"] as! [String : AnyObject]
        let navigation:[String:AnyObject] = styleConfig!["navigation"] as! [String : AnyObject]
        let indication:[String:AnyObject] = styleConfig!["indication"] as! [String : AnyObject]
        
        if (icons["name"] != nil) {
            BlueGPSPlugin.mapConfig.style.icons.name = icons["name"] as! String
        }
        if (icons["align"] != nil) {
            BlueGPSPlugin.mapConfig.style.icons.align = icons["align"] as! String
        }
        if (icons["vAlign"] != nil) {
            BlueGPSPlugin.mapConfig.style.icons.vAlign = icons["vAlign"] as! String
        }
        if (indication["destColor"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.destColor = indication["destColor"] as! String
        }
        if (indication["followZoom"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.followZoom = indication["followZoom"] as! Bool
        }
        if (indication["iconDestination"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.iconDestination = indication["iconDestination"] as? String
        }
        if (indication["iconHAlign"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.iconHAlign = indication["iconHAlign"] as! String
        }
        if (indication["opacity"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.opacity = Int(indication["opacity"] as! Double)
        }
        if (indication["radiusMeter"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.radiusMeter = Int(indication["radiusMeter"] as! Double)
        }

        
        if (navigation["animationTime"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.animationTime = navigation["animationTime"] as! Double
        }
        if (navigation["autoZoom"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.autoZoom = navigation["autoZoom"] as! Bool
        }
        if (navigation["iconDestination"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.iconDestination = navigation["iconDestination"] as? String
        }
        if (navigation["iconSource"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.iconSource = navigation["iconSource"] as! String
        }
        if (navigation["jumpColor"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.jumpColor = navigation["jumpColor"] as! String
        }
        if (navigation["jumpOpacity"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.jumpOpacity = Int(navigation["jumpOpacity"] as! Double)
        }
        if (navigation["jumpRadiusMeter"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.jumpRadiusMeter = navigation["jumpRadiusMeter"] as! Double
        }
        if (navigation["navigationStep"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.navigationStep = navigation["navigationStep"] as! Double
        }
        if (navigation["showVoronoy"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.showVoronoy = navigation["showVoronoy"] as! Bool
        }
        if (navigation["stroke"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.stroke = navigation["stroke"] as! String
        }
        if (navigation["strokeLinecap"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.strokeLinecap = navigation["strokeLinecap"] as! String
        }
        if (navigation["strokeLinejoin"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.strokeLinejoin = navigation["strokeLinejoin"] as! String
        }
        if (navigation["strokeOpacity"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.strokeOpacity = Int(navigation["strokeOpacity"] as! Double)
        }
        if (navigation["strokeWidthMeter"] != nil) {
            BlueGPSPlugin.mapConfig.style.navigation.strokeWidthMeter = navigation["strokeWidthMeter"] as! Double
        }
        if (navigation["velocityOptions"] != nil) {
            var velocityOptions:[String:Double] = [:]
            let velocityObjects = navigation["velocityOptions"] as! [AnyObject];
            velocityObjects.forEach { velocityOject in
                let velocityOption = velocityOject as! [String:AnyObject]
                velocityOptions[velocityOption["key"] as! String] = velocityOption["value"] as? Double
            }
            BlueGPSPlugin.mapConfig.style.navigation.velocityOptions = velocityOptions
        }
        
        
        if (showConfig!["all"] != nil) {
            BlueGPSPlugin.mapConfig.show.all = showConfig!["all"] as! Bool
        }
        if (showConfig!["me"] != nil) {
            BlueGPSPlugin.mapConfig.show.me = showConfig!["me"] as! Bool
        }
        if (showConfig!["room"] != nil) {
            BlueGPSPlugin.mapConfig.show.room = showConfig!["room"] as! Bool
        }
        if (showConfig!["park"] != nil) {
            BlueGPSPlugin.mapConfig.show.park = showConfig!["park"] as! Bool
        }
        if (showConfig!["desk"] != nil) {
            BlueGPSPlugin.mapConfig.show.desk = showConfig!["desk"] as! Bool
        }
        
        mapView = MapViewController()
        if #available(iOS 13, *){}
        viewController.present(mapView!, animated: true, completion: nil)
    }
    
    @objc(startAdv:)func startAdv(command:CDVInvokedUrlCommand){
        BlueGPS.shared.getConfiguration { response in
            
            if response.code == 200, let payload = response.payload?.value as? NetworkResponseConfiguration
            {
                BlueGPS.shared.startAdvertisingRegion(with: payload.iosadvConf) { [unowned self] manager, error in
                    if let error = error {
                        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error,messageAs:error.localizedDescription), callbackId: command.callbackId)
                    } else {
                        self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok), callbackId: command.callbackId)
                    }
                }
            }
        }
    }
    
    @objc(stopAdv:)func stopAdv(command:CDVInvokedUrlCommand){
        BlueGPS.shared.stopAdvertisingRegion();
    }
}
