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
    
    @objc(initializeSDK:) func initializeSDK(command : CDVInvokedUrlCommand) {
        let sdkKey = command.argument(at: 0) as! String
        let sdkSecret = command.argument(at: 1) as! String
        let endpoint = command.argument(at: 2) as! String
        let env = EnvironmentModel(useBlueGPSAuth: false, endpoint: endpoint, key: sdkKey, secret: sdkSecret)
        BlueGPS.setupSDK(env)
        BlueGPSPlugin.sdkCredentials = SDKCredentialsModel(sdkCredentials: SynapsesCredentialModel(sdkKey: sdkKey,
                                    sdkSecret: sdkSecret),
                                    loggedUser: nil,
                                    microsoftToken: nil)
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: "Succesfully initiliazed the sdk"), callbackId: command.callbackId)
    }
    
    @objc(initializeToken:) func initializeToken(command : CDVInvokedUrlCommand){
        let sdkToken = command.argument(at: 0) as! String
        let endpoint = command.argument(at: 1) as! String
        let env = EnvironmentModel(useBlueGPSAuth: false, endpoint: endpoint, timeout: 30)
        
       // commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.error, messageAs: "Not Implemented Yet"), callbackId: command.callbackId)
        BlueGPS.setupSDK(env)
        BlueGPS.shared.setEnvironment(token: sdkToken) { responseSetEnv in
            if responseSetEnv.code == 200 || responseSetEnv.code == 201 {
                BlueGPSPlugin.sdkCredentials = SDKCredentialsModel(sdkCredentials: nil, loggedUser: nil, microsoftToken: nil, token: sdkToken)
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: "Succesfully initiliazed the sdk"), callbackId: command.callbackId)
            } else {
                self.commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.instantiationException, messageAs: "Init Error: SetEnv returned code: \(responseSetEnv.code), message: \(responseSetEnv.message ?? "")"), callbackId: command.callbackId)
            }
        }
    }

    @objc(login:) func login(command: CDVInvokedUrlCommand) {
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
    
    @objc(openMap:)func openMap(command:CDVInvokedUrlCommand) {
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
            BlueGPSPlugin.mapConfig.style.indication.opacity = (indication["opacity"] as! Double)
        }
        if (indication["radiusMeter"] != nil) {
            BlueGPSPlugin.mapConfig.style.indication.radiusMeter = (indication["radiusMeter"] as! Double)
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
            BlueGPSPlugin.mapConfig.style.navigation.jumpOpacity = (navigation["jumpOpacity"] as! Double)
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
            BlueGPSPlugin.mapConfig.style.navigation.strokeOpacity = (navigation["strokeOpacity"] as! Double)
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
    
    @objc(openMapBlock:)func openMapBlock(command:CDVInvokedUrlCommand) {
        if command.arguments.count > 0 {
            do {
                let configurationMapData = (command.argument(at: 0) as? String ?? "").data(using: .utf8)
                guard let configurationMapData = configurationMapData else {
                    commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.jsonException, messageAs: "Json Error: ConfigurationMap is not a valid data"), callbackId: command.callbackId)
                    return
                }
                let configurationMap = try JSONSerialization.jsonObject(with: configurationMapData, options: []) as? [String:AnyObject]
                let screenHeight = command.argument(at: 1) as? Int
                let heightJsTop = command.argument(at: 2) as? Int
                let originPointData = (command.argument(at: 3) as? String ?? "").data(using: .utf8)
                var originPoint: [String : AnyObject]? = nil
                if originPointData != nil {
                    originPoint = try JSONSerialization.jsonObject(with: originPointData!, options: []) as? [String:AnyObject]
                }
                let destinationPointData = (command.argument(at: 4) as? String ?? "").data(using: .utf8)
                var destinationPoint: [String:AnyObject]? = nil
                if destinationPointData != nil {
                    destinationPoint = try JSONSerialization.jsonObject(with: destinationPointData!, options: []) as? [String:AnyObject]
                }
                guard let configurationMap = configurationMap else {
                    //TODO Show Error
                    commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.jsonException, messageAs: "Json Error: ConfigurationMap is null"), callbackId: command.callbackId)
                    print("Json Error: ConfigurationMap is null")
                    return
                }
                
                let tagid: String = (configurationMap["tagId"] as? String) ?? ""
                
                BlueGPSPlugin.mapConfig = ConfigurationModel(auth: BlueGPSPlugin.sdkCredentials,
                                                             tagid: tagid,
                                                             toolbox: ToolboxModel(),
                                                             show: ShowModel(),
                                                            callbackType: nil)
                let showConfig: [String: AnyObject] = configurationMap["show"] as! [String: AnyObject]
                let style: [String: AnyObject] = configurationMap["style"] as! [String: AnyObject]
                let icons:[String:AnyObject] = style["icons"] as! [String : AnyObject]
                let navigation:[String:AnyObject] = style["navigation"] as! [String : AnyObject]
                let indication:[String:AnyObject] = style["indication"] as! [String : AnyObject]
                
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
                    BlueGPSPlugin.mapConfig.style.indication.opacity = (indication["opacity"] as! Double)
                }
                if (indication["radiusMeter"] != nil) {
                    BlueGPSPlugin.mapConfig.style.indication.radiusMeter = (indication["radiusMeter"] as! Double)
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
                    BlueGPSPlugin.mapConfig.style.navigation.iconSource = navigation["iconSource"] as? String
                }
                if (navigation["jumpColor"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.jumpColor = navigation["jumpColor"] as! String
                }
                if (navigation["jumpOpacity"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.jumpOpacity = (navigation["jumpOpacity"] as! Double)
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
                    BlueGPSPlugin.mapConfig.style.navigation.stroke = navigation["stroke"] as? String
                }
                if (navigation["strokeLinecap"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.strokeLinecap = navigation["strokeLinecap"] as! String
                }
                if (navigation["strokeLinejoin"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.strokeLinejoin = navigation["strokeLinejoin"] as! String
                }
                if (navigation["strokeOpacity"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.strokeOpacity = (navigation["strokeOpacity"] as! Double)
                }
                if (navigation["strokeWidthMeter"] != nil) {
                    BlueGPSPlugin.mapConfig.style.navigation.strokeWidthMeter = navigation["strokeWidthMeter"] as! Double
                }
                if (navigation["velocityOptions"] != nil) {
                    var velocityOptions:[String:Double] = [:]
                    let velocityObjects = navigation["velocityOptions"] as? [AnyObject]
                    if !(velocityObjects?.isEmpty ?? false) {
                        velocityObjects?.forEach { velocityOject in
                            let velocityOption = velocityOject as! [String:AnyObject]
                            velocityOptions[velocityOption["key"] as! String] = velocityOption["value"] as? Double
                        }
                        BlueGPSPlugin.mapConfig.style.navigation.velocityOptions = velocityOptions
                    }
                }
                
                let sourceMapId: Int? = originPoint?["mapId"] as? Int
                let sourceX: Double? = originPoint?["x"] as? Double
                let sourceY: Double? = originPoint?["y"] as? Double
                
                let destMapId: Int? = destinationPoint?["mapId"] as? Int
                let destX: Double? = destinationPoint?["x"] as? Double
                let destY: Double? =  destinationPoint?["y"] as? Double
                
                let sourceModel = MapPositionModel(mapId: sourceMapId ?? 0, tagid: tagid, roomId: nil, areaId: nil, x: sourceX ?? 0.0, y: sourceY ?? 0.0, data: nil)
                let destModel = MapPositionModel(mapId: destMapId ?? 0, tagid: tagid, roomId: nil, areaId: nil, x: destX ?? 0.0, y: destY ?? 0.0, data: nil)
                
                mapView = MapViewController()
                
                mapView?.webConsole = commandDelegate
                mapView?.callbackId = command.callbackId
                mapView?.sourcePoint = sourceModel
                mapView?.destPoint = destModel
                
                
                /*
                 "bookingType":"DESK",
                 "name":"180_A",
                 "id":"31",
                 "mapId":12,
                 "x":1209,
                 "y":1355
                 */
                
                if #available(iOS 13, *){}
                viewController.present(mapView!, animated: true, completion: nil)

            } catch {
                commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.jsonException, messageAs: "Json Error: Problem with serialization of json - \(error.localizedDescription)"), callbackId: command.callbackId)
            }
            
        } else {
            commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.jsonException, messageAs: "Json has no parameters, please check the json"), callbackId: command.callbackId)
        }
    }
    
    @objc(closeBlock:)func closeBlock(command: CDVInvokedUrlCommand) {
        viewController.dismiss(animated: true, completion: nil)
        commandDelegate.send(CDVPluginResult(status: CDVCommandStatus.ok, messageAs: "Succesfully closed the map"), callbackId: command.callbackId)
    }

    @objc(startAdv:)func startAdv(command:CDVInvokedUrlCommand) {
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
    
    @objc(stopAdv:)func stopAdv(command:CDVInvokedUrlCommand) {
        BlueGPS.shared.stopAdvertisingRegion();
    }
}