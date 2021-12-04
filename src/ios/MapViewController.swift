//
//  ViewController.swift
//  BlueGPS-SampleApp
//
//  Created by Costantino Pistagna on 30/04/21.
//

import UIKit
import Combine
import SynapsesSDK

class MapViewController: UIViewController, DynamicMapViewDelegate, UITableViewDelegate,UITableViewDataSource {
    
    private var lastMapItem:MapPositionModel?
    private var prevMapItem:MapPositionModel?
    
    func didReceiveEvent(_ event: MapEvent, payload: Any?) {
        if event.type == "mapClick" {
            prevMapItem = lastMapItem
            lastMapItem = payload as? MapPositionModel
            //focus on this item to do gotofrom
        }
        print("received event: \(event.type ?? "n/a") - \(payload.debugDescription)")
    }
    
    func didReceiveError(_ error: Error) {
        print("received error: \(error.localizedDescription)")
    }
    
    private var mapView: DynamicMapView?
    @IBOutlet var leftBarButtonItem: UIBarButtonItem?
    @IBOutlet var rightBarButtonItem: UIBarButtonItem?
    @IBOutlet var navigationBar: UINavigationItem?
    var tableView: UITableView?
    
    private var rightbtnImage:UIImage?
    private var leftbtnImage:UIImage?
    private var tempLeftBarButton: UIBarButtonItem?
    private var tempRightBarButton: UIBarButtonItem?
    
    private var currentState:Int = 0
    private var hideRoomLayer = false

    private var configuration: ConfigurationModel!
    private var dataSource = [FloorModel]()

    override func viewDidLoad() {
        super.viewDidLoad()
        
        navigationBar?.title = "Map view";

        // Subscriber is not required, but could be useful if you wanna listen as a stream
        // to current PeripheralManagerState
        if #available(iOS 13.0, *) {
            let subscriber = BlueGPS.shared.peripheralManagerStatePublisher
                .receive(on: DispatchQueue.main)
                .sink { state in
                    print("Publisher: \(state.rawValue)")
                }
        } else {
            // Fallback on earlier versions
        }
        mapView = DynamicMapView(frame: CGRect(x: 0.0, y: 76, width: 414, height: 820))
        self.view.addSubview(mapView!)

        BlueGPS.shared.initSDK { response in
            if response.code != 200 {
                print(response.message ?? "Generic Error Occurred")
            } else {
                BlueGPS.shared.getConfiguration { response in
                    if response.code == 200,
                       let payload = response.payload?.value as? NetworkResponseConfiguration
                    {
                        BlueGPS.shared.startAdvertisingRegion(with: payload.iosadvConf) { manager, error in
                            if let error = error {
                                print(error)
                            }
                        }
                    } else {
                        print(response.message ?? "Generic Error Occurred")
                    }
                }
            }
        }
        
        tableView = UITableView()
        tableView?.register(UITableViewCell.self, forCellReuseIdentifier: "customViewCell")
        tableView?.register(UITableViewCell.self, forCellReuseIdentifier: "labelViewCell")
        tableView?.frame = CGRect(x: 0.0, y: 76, width: 414, height: 820)
        tableView?.delegate = self
        tableView?.dataSource = self
        tableView?.reloadData()
        tableView?.isHidden = true
        self.view.addSubview(tableView!)
        
        mapView?.getFloor({ [unowned self] results, error in
            if let results = results {
                dataSource = results
                tableView?.reloadData()
            }
        })
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        /// We could load mapView with just credentials
        //  self.mapView?.load("/api/public/resource/sdk/mobile.html", credentials: credentials)
        
        // delegation is required in order to receive events from Map.
        // self.mapView?.delegate = self

        /// Or we can forge a specific configuration as follow
        self.mapView?.load("/api/public/resource/sdk/mobile.html", configuration: BlueGPSPlugin.mapConfig, delegate: self)
    }
    
    // MARK: - IBActions
    
    @IBAction func leftButtonDidPress() {
        /// In this example we ask SDK to show Toolbox GUI configurator and change the mapControl layer according to the response.
        switch currentState {
            case 0:
                BlueGPS.shared.showToolboxGUI(parameters: BlueGPSPlugin.mapConfig.toolbox.mapControl) { [unowned self] model in
                    if let model = model {
                        BlueGPSPlugin.mapConfig.toolbox.mapControl = model
                        self.mapView?.sdkInit(BlueGPSPlugin.mapConfig, { operationId, error in
                            if let operationId = operationId {
                                print(operationId)
                            } else if let error = error {
                                print(error)
                            }
                        })
                    }
                }
                break
            case 1:
                showWebinteractions()
                break
            default:
                break
        }
        
    }
    
    @IBAction func rightButtonDidPress() {
        switch currentState {
        case 0:
            showWebinteractions()
            break
        case 2:
            showMap()
            break
        default:
            break
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if(currentState == 1){
            tableView.deselectRow(at: indexPath, animated: true)
            let aRow = dataSource[indexPath.row]
            self.mapView?.gotoFloor(aRow)
            showWebinteractions()
            return;
        }
        switch indexPath.row {
        case 0:
            mapView?.resetView({ operationId, error in
                if let operationId = operationId {
                    print(operationId)
                } else if let error = error {
                    print(error)
                }
            })
            rightButtonDidPress()
        case 1: //rotate
            let anAlertCtrl = UIAlertController(title: "Rotate",
                                                message: "Insert angle degree",
                                                preferredStyle: .alert)
            anAlertCtrl.addAction(UIAlertAction(title: "Ok",
                                                style: .default,
                                                handler: { [unowned self] action in
                                                    if let textField = anAlertCtrl.textFields?.first,
                                                       let text = textField.text,
                                                       let newValue = Int(text)
                                                    {
                                                        mapView?.rotate(newValue, { operationId, error in
                                                            if let operationId = operationId {
                                                                print(operationId)
                                                            } else if let error = error {
                                                                print(error)
                                                            }
                                                        })
                                                        rightButtonDidPress()
                                                    }
                                                }))
            anAlertCtrl.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            anAlertCtrl.addTextField { textField in
                textField.keyboardType = .numberPad
            }
            self.present(anAlertCtrl, animated: true, completion: nil)
        case 2: //rotateAbsolute
            let anAlertCtrl = UIAlertController(title: "Rotate Absolute",
                                                message: "Insert angle degree",
                                                preferredStyle: .alert)
            anAlertCtrl.addAction(UIAlertAction(title: "Ok",
                                                style: .default,
                                                handler: { [unowned self] action in
                                                    if let textField = anAlertCtrl.textFields?.first,
                                                       let text = textField.text,
                                                       let newValue = Int(text)
                                                    {
                                                        mapView?.rotateAbsolute(newValue, { operationId, error in
                                                            if let operationId = operationId {
                                                                print(operationId)
                                                            } else if let error = error {
                                                                print(error)
                                                            }
                                                        })
                                                        rightButtonDidPress()
                                                    }
                                                }))
            anAlertCtrl.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            anAlertCtrl.addTextField { textField in
                textField.keyboardType = .numberPad
            }
            self.present(anAlertCtrl, animated: true, completion: nil)
        case 3: //hideRoom
            hideRoomLayer = !hideRoomLayer
            mapView?.hideRoomLayer(hideRoomLayer, { operationId, error in
                if let operationId = operationId {
                    print(operationId)
                } else if let error = error {
                    print(error)
                }
            })
        case 4: //nextFloor
            mapView?.nextFloor({ operationId, error in
                if let operationId = operationId {
                    print(operationId)
                } else if let error = error {
                    print(error)
                }
            })
            rightButtonDidPress()
        case 5: //showTag
            let anAlertCtrl = UIAlertController(title: "Show Tag",
                                                message: "Insert tag to search for:",
                                                preferredStyle: .alert)
            anAlertCtrl.addAction(UIAlertAction(title: "Ok",
                                                style: .default,
                                                handler: { [unowned self] action in
                                                    if let textField = anAlertCtrl.textFields?.first,
                                                       let text = textField.text
                                                    {
                                                        mapView?.showTag(text, { operationId, error in
                                                            if let operationId = operationId {
                                                                print(operationId)
                                                            } else if let error = error {
                                                                print(error)
                                                            }
                                                        })
                                                        rightButtonDidPress()
                                                    }
                                                }))
            anAlertCtrl.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            anAlertCtrl.addTextField(configurationHandler: nil)
            self.present(anAlertCtrl, animated: true, completion: nil)
        case 6: //getFloor
            showFloor()
        case 7: //gotoFromMe
            if lastMapItem != nil{
                //MapPositionModel(mapId: 3, x: -62.49, y: 55.04)
                mapView?.gotoFromMe(lastMapItem!, navigationMode: true, { operationId, error in
                    if let operationId = operationId {
                        print(operationId)
                        self.showMap()
                    } else if let error = error {
                        print(error)
                    }
                })
            }
            break
        case 8: //goto
            if lastMapItem != nil  && prevMapItem != nil{
                //MapPositionModel(mapId:3, x: 0.0, y: 74.0)
                //MapPositionModel(mapId: 3, x: -62.49, y: 55.04)
                mapView?.goto(source: prevMapItem!,
                              dest: lastMapItem!,
                              navigationMode: true, { operationId, error in
                                if let operationId = operationId {
                                    print(operationId)
                                    self.showMap()
                                } else if let error = error {
                                    print(error)
                                }
                            })
            }
            
            break
        case 9: //Remove Navigation
            mapView?.removeNavigation() { operationId, error in
                if let operationId = operationId {
                    print(operationId)
                    self.showMap()
                } else if let error = error {
                    print(error)
                }
            }
        default: break
        }
    }
    
    func showWebinteractions(){
        navigationBar?.title = "Web Interactions";
        currentState = 2
        if rightBarButtonItem == nil {
            rightBarButtonItem = tempRightBarButton
            navigationBar?.rightBarButtonItem = tempRightBarButton
        }
        rightbtnImage = rightBarButtonItem?.image
        rightBarButtonItem?.image = nil
        if leftBarButtonItem?.image == nil {
            leftBarButtonItem?.image = leftbtnImage
        }
        tempLeftBarButton = leftBarButtonItem
        leftBarButtonItem = nil
        navigationBar?.leftBarButtonItem = nil
        tableView?.isHidden = false
        mapView?.isHidden = true
        tableView?.reloadData()
    }
    
    func showFloor(){
        navigationBar?.title = "Floors";
        currentState = 1
        
        leftBarButtonItem = tempLeftBarButton
        navigationBar?.leftBarButtonItem = tempLeftBarButton
        
        rightBarButtonItem?.image = rightbtnImage
        tempRightBarButton = rightBarButtonItem
        rightBarButtonItem = nil
        navigationBar?.rightBarButtonItem = nil
        
        leftbtnImage = leftBarButtonItem?.image
        leftBarButtonItem?.image = nil
        mapView?.isHidden = true
        tableView?.reloadData()
    }
    
    func showMap(){
        navigationBar?.title = "Map view";
        currentState = 0
        rightBarButtonItem?.image = rightbtnImage
        leftBarButtonItem = tempLeftBarButton
        navigationBar?.leftBarButtonItem = tempLeftBarButton
        tableView?.isHidden = true
        mapView?.isHidden = false
    }
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if currentState == 1{
            return dataSource.count
        }else{
            return 10
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        if(currentState == 1){
            let cell = tableView.dequeueReusableCell(withIdentifier: "labelViewCell", for: indexPath) as! UITableViewCell
            let aRow = dataSource[indexPath.row]
            cell.textLabel?.text = aRow.name
            return cell
        }else{
            let cell = tableView.dequeueReusableCell(withIdentifier: "customViewCell", for: indexPath) as! UITableViewCell

            switch indexPath.row {
            case 0:
                cell.textLabel?.text = "ResetView()"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "arrow.2.squarepath")
                } else {
                    // Fallback on earlier versions
                }
            case 1:
                cell.textLabel?.text = "Rotate"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "rotate.right")
                } else {
                    // Fallback on earlier versions
                }
            case 2:
                cell.textLabel?.text = "Rotate absolute"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "crop.rotate")
                } else {
                    // Fallback on earlier versions
                }
            case 3:
                cell.textLabel?.text = "Hide Room Layer"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "eye.slash")
                } else {
                    // Fallback on earlier versions
                }
            case 4:
                cell.textLabel?.text = "Next Floor"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "forward")
                } else {
                    // Fallback on earlier versions
                }
            case 5:
                cell.textLabel?.text = "Show Tag"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "eye")
                } else {
                    // Fallback on earlier versions
                }
            case 6:
                cell.textLabel?.text = "Get Floor"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "list.number")
                } else {
                    // Fallback on earlier versions
                }
            case 7:
                cell.textLabel?.text = "GotoFromMe"
                cell.imageView?.image = UIImage(named: "gotoFromMe")
            case 8:
                cell.textLabel?.text = "Goto"
                cell.imageView?.image = UIImage(named: "goto")
            case 9:
                cell.textLabel?.text = "Remove Navigation"
                if #available(iOS 13.0, *) {
                    cell.imageView?.image = UIImage(systemName: "location.slash")
                } else {
                    // Fallback on earlier versions
                }
            default: break
            }
            return cell
        }
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
}
