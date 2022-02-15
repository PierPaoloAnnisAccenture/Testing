package com.outsystems.bluegps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;

import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mobilecop.bluegps.NavigationExtKt;
import $appid.MapActivity;
import $appid.NavigationActivity;
import $appid.PoiField;
import $appid.R;

import com.synapseslab.bluegps_sdk.component.map.BlueGPSMapView;
import com.synapseslab.bluegps_sdk.core.BlueGPSLib;
import com.synapseslab.bluegps_sdk.data.model.advertising.AdvertisingStatus;
import com.synapseslab.bluegps_sdk.data.model.environment.SdkEnvironment;
import com.synapseslab.bluegps_sdk.data.model.map.*;
import com.synapseslab.bluegps_sdk.data.model.stats.NavInfo;
import com.synapseslab.bluegps_sdk.service.BlueGPSAdvertisingService;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import kotlin.jvm.functions.Function2;

public class BlueGPS extends CordovaPlugin {



    private final String INIT = "initializeSDK";
    private final String OPENMAP_BLOCK = "openMapBlock";
    private final String REFRESH_BLOCK = "refreshBlock";
    private final String REFRESH_HEIGHT_BLOCK = "refreshHeightBlock";
    private final String GO_TO_FLOOR_BLOCK = "gotoFloor";
    private final String CLOSE_BLOCK = "closeBlock";
    private final String START_NAVIGATION_BLOCK = "startNavigationBlock";
    private final String CURR_FLOOR_BLOCK = "currentFloor";




    public static SdkEnvironment sdkEnvironment;
    public static ConfigurationMap configurationMap;
    private BlueGPSAdvertisingService blueGPSAdvertisingService = null;
    private CallbackContext callback;
    private final String appId = "com.saipem.plugins"; 
    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };



    BlueGPSMapView blueGPS;

    boolean blueGPSinizialized = false;
    ViewGroup.LayoutParams mainLayoutBefore;
    TextView floorView;
    FrameLayout secondView;
    JSONObject originJSON;
    JSONObject destinationJSON;
    String floorName;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Intent serviceIntent = new Intent(cordova.getActivity(), BlueGPSAdvertisingService.class);
        cordova.getActivity().bindService(
                serviceIntent,
                advertisingServiceConnection,
                Context.BIND_AUTO_CREATE);
        cordova.getActivity().registerReceiver(
                advertisingServiceReceiver,
                new IntentFilter(BlueGPSAdvertisingService.ACTION_ADV));
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0]== PackageManager.PERMISSION_DENIED){
                Toast.makeText(cordova.getActivity(),
                        "Location Permission is required for navigation!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        boolean status = false;
        PluginResult result;
        switch (action) {

            case INIT:
                if(!hasPermisssion()){
                    PermissionHelper.requestPermissions(this,1,permissions);
                }
                cordova.getThreadPool().execute(() -> {

                    try {
                        sdkEnvironment = new SdkEnvironment();
                        sdkEnvironment.setSdkKey(args.getString(0));
                        sdkEnvironment.setSdkSecret(args.getString(1));
                        sdkEnvironment.setSdkEndpoint(args.getString(2));
                        sdkEnvironment.setAppId(appId);
                        sdkEnvironment.setLoggedUser(null);
                        boolean enabledNetworkLogs = args.getBoolean(3);

                        BlueGPSLib.Companion.getInstance().initSDK(sdkEnvironment, cordova.getActivity(), enabledNetworkLogs);
                        blueGPSinizialized = true;
                        callback.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                    } catch (JSONException e) {
                        callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Init Failed with error:" + e.getLocalizedMessage()));
                    }
                });

                status = true;
                result = null;
                break;

            case OPENMAP_BLOCK:

                navigation(args);

                Integer maxHeightJS =  args.getInt(3);
                Integer heightTopJS =  args.getInt(4);

                try{
                    originJSON =  new JSONObject(args.getString(5));
                    destinationJSON =  new JSONObject(args.getString(6));
                }catch(JSONException jsonE){

                }



                cordova.getActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    public void run() {
                        //Toast.makeText(webView.getContext(),"Set proxy fail!",Toast.LENGTH_LONG).show();
                        if (blueGPS == null) {
                            View mainView = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content)).getChildAt(0);

                            ViewGroup viewGroup = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content));


                            secondView = new FrameLayout(mainView.getContext());

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            blueGPS = new BlueGPSMapView(mainView.getContext());

                            blueGPS.initMap(BlueGPS.sdkEnvironment, setupConfigurationMap(false), null);

                            DisplayMetrics displayMetrics =  new DisplayMetrics();
                            cordova.getActivity().getWindowManager()
                                    .getDefaultDisplay()
                                    .getMetrics(displayMetrics);

                            Integer heightPixelsBLUGPS = (displayMetrics.heightPixels  * (maxHeightJS-heightTopJS))/maxHeightJS;


                            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    heightPixelsBLUGPS);
                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                            blueGPS.setZ(-2);


                            secondView.addView(blueGPS);
                            floorView = new TextView(mainView.getContext());
                            floorView.setText("");
                            floorView.setAllCaps(true);
                            floorView.setTextColor(R.color.black);

                            final ViewGroup.MarginLayoutParams textLayout = new ViewGroup.MarginLayoutParams(
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                                    ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                            textLayout.setMargins(130,0,0,20);
                            floorView.setLayoutParams(textLayout);

                            secondView.addView(floorView);

                            viewGroup.addView(secondView, params);

                            setListenerOnMapView();

                            mainLayoutBefore = mainView.getLayoutParams();

                            final FrameLayout.LayoutParams paramsMain = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    displayMetrics.heightPixels-heightPixelsBLUGPS);
                            paramsMain.gravity = Gravity.TOP | Gravity.CENTER;
                            mainView.setLayoutParams(paramsMain);

                            callback.success();
                        }else{
                            DisplayMetrics displayMetrics =  new DisplayMetrics();
                            cordova.getActivity().getWindowManager()
                                    .getDefaultDisplay()
                                    .getMetrics(displayMetrics);
                            Integer heightPixelsBLUGPS = (displayMetrics.heightPixels  * (maxHeightJS-heightTopJS))/maxHeightJS;
                            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    heightPixelsBLUGPS);
                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                            secondView.setLayoutParams(params);

                            View mainView = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content)).getChildAt(0);
                            final FrameLayout.LayoutParams paramsMain = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    displayMetrics.heightPixels-heightPixelsBLUGPS);
                            paramsMain.gravity = Gravity.TOP | Gravity.CENTER;
                            mainView.setLayoutParams(paramsMain);
                            showNavigationMapJSON();
                        }


                    }
                });

                status = true;
                result = new PluginResult(PluginResult.Status.OK);
                break;
            case GO_TO_FLOOR_BLOCK:
                Floor floor = new Floor();
                floor.setId(args.getInt(0));

                cordova.getActivity().runOnUiThread(()->{

                    blueGPS.gotoFloor(floor);
                });
                status = true;

                break;

            case CURR_FLOOR_BLOCK:
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        setCurrentFloor();
                    }
                });
                status = true;
                break;
            case CLOSE_BLOCK:
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if(blueGPS !=  null){
                            ViewGroup viewGroup = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content));

                            viewGroup.removeView(secondView);
                            View mainView = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content)).getChildAt(0);
                            mainView.setLayoutParams(mainLayoutBefore);


                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                            blueGPS = null;
                        }else{

                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "blueGPS not exist"));

                        }


                    }});

                status = true;
                break;
            case REFRESH_HEIGHT_BLOCK:

                Integer maxRefreshHeightJS =  args.getInt(0);
                Integer maxRefreshHeightTopJS =  args.getInt(1);
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                            DisplayMetrics displayMetrics =  new DisplayMetrics();
                            cordova.getActivity().getWindowManager()
                                    .getDefaultDisplay()
                                    .getMetrics(displayMetrics);

                            Integer heightPixelsBLUGPS = (displayMetrics.heightPixels  * (maxRefreshHeightJS-maxRefreshHeightTopJS))/maxRefreshHeightJS;

                            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    heightPixelsBLUGPS);
                            params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                            secondView.setLayoutParams(params);

                            View mainView = ((ViewGroup) cordova.getActivity().findViewById(android.R.id.content)).getChildAt(0);
                            final FrameLayout.LayoutParams paramsMain = new FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.WRAP_CONTENT,
                                    displayMetrics.heightPixels-heightPixelsBLUGPS);
                            paramsMain.gravity = Gravity.TOP | Gravity.CENTER;
                            mainView.setLayoutParams(paramsMain);

                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));



                    }
                });

                status = true;

                break;
            case REFRESH_BLOCK:
                status = true;
                break;

            case START_NAVIGATION_BLOCK:
                originJSON =  new JSONObject(args.getString(0));
                destinationJSON =  new JSONObject(args.getString(1));
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        showNavigationMapJSON();

                    }
                });
                status = true;

                break;

            default:
                result = new PluginResult(PluginResult.Status.ERROR, "Invalid Action!");
                break;
        }
        /*if (result != null) {
            callbackContext.sendPluginResult(result);
        }*/
        return status;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(cordova.getActivity(), BlueGPSAdvertisingService.class);
        cordova.getActivity().bindService(
                serviceIntent,
                advertisingServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        cordova.getActivity().registerReceiver(
                advertisingServiceReceiver,
                new IntentFilter(BlueGPSAdvertisingService.ACTION_ADV));
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        cordova.getActivity().unregisterReceiver(advertisingServiceReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        cordova.getActivity().unbindService(advertisingServiceConnection);
    }



    private final ServiceConnection advertisingServiceConnection = new ServiceConnection() {
        public void onServiceConnected(@NotNull ComponentName name, @NotNull IBinder service) {
            BlueGPSAdvertisingService.LocalBinder binder = (BlueGPSAdvertisingService.LocalBinder) service;
            blueGPSAdvertisingService = binder.getServiceBlueGPS();
        }

        public void onServiceDisconnected(@NotNull ComponentName name) {
            blueGPSAdvertisingService = (BlueGPSAdvertisingService) null;
        }
    };

    private final BroadcastReceiver advertisingServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BlueGPSAdvertisingService.ACTION_ADV) {
                AdvertisingStatus it = intent.getParcelableExtra(BlueGPSAdvertisingService.DATA_ADV);
                switch (it.getStatus()) {
                    case STARTED:
                    case STOPPED:
                    case ERROR:
                }
            }
        }
    };



    private void navigation(JSONArray args) throws JSONException {
        configurationMap = new ConfigurationMap();
        Map<String, String> credentialNav = new HashMap<>();
        credentialNav.put("sdkKey",sdkEnvironment.getSdkKey());
        credentialNav.put("sdkSecret",sdkEnvironment.getSdkSecret());
        AuthParameters authParametersNav;
        if (sdkEnvironment.getLoggedUser() != null) {

            Map<String, String> userNav = new HashMap<>();
            if (sdkEnvironment.getLoggedUser().getToken() != null){
                userNav.put("token",sdkEnvironment.getLoggedUser().getToken());
            }else{
                userNav.put("username",sdkEnvironment.getLoggedUser().getUsername());
                userNav.put("password",sdkEnvironment.getLoggedUser().getPassword());
            }
            authParametersNav = new AuthParameters(credentialNav,userNav,null,null,null);
        } else {
            authParametersNav = new AuthParameters(credentialNav,null,null,null,null);
        }
        configurationMap.setAuth(authParametersNav);

        MapStyle mapStyleNav = new MapStyle();
        JSONObject styleNav = new JSONObject(args.getString(1));
        Log.d("StyleNav", styleNav.toString());

        JSONObject iconsNav = styleNav.getJSONObject("icons");
        IconStyle iconsStyleNav = new IconStyle();
        if (iconsNav.has("opacity")) iconsStyleNav.setOpacity(iconsNav.getDouble("opacity"));
        if (iconsNav.has("name")) iconsStyleNav.setName(iconsNav.getString("name"));
        if (iconsNav.has("align")) iconsStyleNav.setAlign(iconsNav.getString("align"));
        if (iconsNav.has("vAlign")) iconsStyleNav.setVAlign(iconsNav.getString("vAlign"));
        if (iconsNav.has("followZoom")) iconsStyleNav.setFollowZoom(iconsNav.getBoolean("followZoom"));

        mapStyleNav.setIcons(iconsStyleNav);


//                JSONObject indicationNav = styleNav.getJSONObject("indication");
//                Log.d("IndicationNav", indicationNav.toString());
//                IndicationStyle indicationStyleNav = new IndicationStyle();
//                if (indicationNav.has("destColor"))
//                    indicationStyleNav.setDestColor(indicationNav.getString("destColor"));
//                if (indicationNav.has("followZoom"))
//                    indicationStyleNav.setFollowZoom(indicationNav.getBoolean("followZoom"));
//                if (indicationNav.has("iconDestination"))
//                    indicationStyleNav.setIconDestination(indicationNav.getString("iconDestination"));
//                if (indicationNav.has("iconHAlign"))
//                    indicationStyleNav.setIconHAlign(indicationNav.getString("iconHAlign"));
//                if (indicationNav.has("iconSource"))
//                    indicationStyleNav.setIconSource(indicationNav.getString("iconSource"));
//                if (indicationNav.has("iconVAlign"))
//                    indicationStyleNav.setIconVAlign(indicationNav.getString("iconVAlign"));
//                if (indicationNav.has("opacity"))
//                    indicationStyleNav.setOpacity(indicationNav.getDouble("opacity"));
//                if (indicationNav.has("radiusMeter"))
//                    indicationStyleNav.setRadiusMeter(indicationNav.getDouble("radiusMeter"));
//                mapStyleNav.setIndication(indicationStyleNav);

        JSONObject navigationNav = styleNav.getJSONObject("navigation");
        NavigationStyle navigationStyleNav = new NavigationStyle();

        if (navigationNav.has("animationTime"))
            navigationStyleNav.setAnimationTime(navigationNav.getDouble("animationTime"));
        if (navigationNav.has("autoZoom"))
            navigationStyleNav.setAutoZoom(navigationNav.getBoolean("autoZoom"));
        if (navigationNav.has("iconDestination"))
            navigationStyleNav.setIconDestination(navigationNav.getString("iconDestination"));
        if (navigationNav.has("iconSource"))
            navigationStyleNav.setIconSource(navigationNav.getString("iconSource"));
        if (navigationNav.has("jumpColor"))
            navigationStyleNav.setJumpColor(navigationNav.getString("jumpColor"));
        if (navigationNav.has("jumpOpacity"))
            navigationStyleNav.setJumpOpacity(navigationNav.getDouble("jumpOpacity"));
        if (navigationNav.has("jumpRadiusMeter"))
            navigationStyleNav.setJumpRadiusMeter(navigationNav.getDouble("jumpRadiusMeter"));
        if (navigationNav.has("navigationStep"))
            navigationStyleNav.setNavigationStep(navigationNav.getDouble("navigationStep"));
        if (navigationNav.has("showVoronoy"))
            navigationStyleNav.setShowVoronoy(navigationNav.getBoolean("showVoronoy"));
        if (navigationNav.has("stroke"))
            navigationStyleNav.setStroke(navigationNav.getString("stroke"));
        if (navigationNav.has("strokeLinecap"))
            navigationStyleNav.setStrokeLinecap(navigationNav.getString("strokeLinecap"));
        if (navigationNav.has("strokeLinejoin"))
            navigationStyleNav.setStrokeLinejoin(navigationNav.getString("strokeLinejoin"));
        if (navigationNav.has("strokeOpacity"))
            navigationStyleNav.setStrokeOpacity(navigationNav.getDouble("strokeOpacity"));
        if (navigationNav.has("strokeWidthMeter"))
            navigationStyleNav.setStrokeWidthMeter(navigationNav.getDouble("strokeWidthMeter"));
        if (navigationNav.has("velocityOptions")){
            Map<String,Double> velocityOptionsNav = new HashMap<>();
            JSONArray velocityArrayNav = navigationNav.getJSONArray("velocityOptions");
            for (int i = 0; i<velocityArrayNav.length(); i++){
                JSONObject velocityOptionNav = velocityArrayNav.getJSONObject(i);
                velocityOptionsNav.put(velocityOptionNav.getString("key"),velocityOptionNav.getDouble("value"));
            }
            navigationStyleNav.setVelocityOptions(velocityOptionsNav);
        }
        mapStyleNav.setNavigation(navigationStyleNav);

        JSONObject showmapNav = new JSONObject(args.getString(2));
        ShowMap mapNav = new ShowMap();

        if (showmapNav.has("all"))
            mapNav.setAll(showmapNav.getBoolean("all"));
        if (showmapNav.has("me"))
            mapNav.setMe(showmapNav.getBoolean("me"));
        if (showmapNav.has("room"))
            mapNav.setRoom(showmapNav.getBoolean("room"));
        if (showmapNav.has("park"))
            mapNav.setPark(showmapNav.getBoolean("park"));
        if (showmapNav.has("desk"))
            mapNav.setDesk(showmapNav.getBoolean("desk"));
        configurationMap.setShow(mapNav);

        configurationMap.setTagid(args.getString(0));

        Log.d("PoiArg", configurationMap.toString());

    }

    private ConfigurationMap setupConfigurationMap(boolean isNavigation) {
        ConfigurationMap configurationMap = new ConfigurationMap();

        MapStyle mapStyle = new MapStyle();

        NavigationStyle navigationStyle = new NavigationStyle();
        navigationStyle.setIconSource("/api/public/resource/icons/commons/start.svg");
        navigationStyle.setIconDestination("/api/public/resource/icons/commons/end.svg");

        navigationStyle.setStroke("#dc8731");
        IconStyle iconStyle = new IconStyle();
        iconStyle.setName("saipem");
        iconStyle.setAlign("center");
        iconStyle.setVAlign("center");
        iconStyle.setFollowZoom(true);

        iconStyle.setOpacity(0.0);
        //iconStyle.setRadiusMeter(5.0);
        mapStyle.setNavigation(navigationStyle);
        mapStyle.setIcons(iconStyle);

        ShowMap showMap = new ShowMap();
        showMap.setAll(true);
        showMap.setRoom(!isNavigation);
        showMap.setMe(true);

        configurationMap.setStyle(mapStyle);
        configurationMap.setShow(showMap);
        return configurationMap;
    }


    /**
     * Setup the listener for BlueGPSMapView in order to implement the code
     * to run when an event click on map occurs.
     */
    private void setListenerOnMapView() {
        blueGPS.setBlueGPSMapListener((data, typeMapCallback) -> {
            Type cType;
            switch (typeMapCallback){
                case INIT_SDK_COMPLETED:
                    cordova.getActivity().runOnUiThread(()->{
                        setCurrentFloor();
                        showNavigationMapJSON();
                    });
                    break;

                case FLOOR_CHANGE:
                    cType = new TypeToken<Floor>() {}.getType();
                    Floor floor = new Gson().fromJson(data.getPayload(),cType);
                    floorName = floor.getLabel();
                    floorView.setText(floor.getLabel());

                case PARK_CONF:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse payloadResponse = new Gson().fromJson(data.getPayload(),cType);
                    if (payloadResponse.getAvailableDateList() != null) {
                        if (!payloadResponse.getAvailableDateList().isEmpty()) {
                            //  Log.d(TAG, String.valueOf(payloadResponse.getAvailableDateList()));
                        }
                    }
                    break;
                //case ROOM_CLICK:
                case MAP_CLICK:

                case TAG_CLICK:
                    callback.sendPluginResult(getPoiTouchResult(PluginResult.Status.OK, data.getPayload()));
                    break;
                case BOOKING_CLICK:
                    cType = new TypeToken<ClickedObject>() {}.getType();
                    ClickedObject clickedObject = new Gson().fromJson(data.getPayload(),cType);
                    new MaterialAlertDialogBuilder(cordova.getActivity().getApplicationContext())
                            .setTitle("Type: "+typeMapCallback.name())
                            .setMessage(clickedObject.toString())
                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                    break;
                case NAV_STATS:
                   /* cType = new TypeToken<NavigationStats>() {}.getType();
                    NavigationStats navigationStats = new Gson().fromJson(data.getPayload(),cType);
                    //  Log.d(TAG, String.valueOf(navigationStats));
                    final String[] vehicles = {""};
                    navigationStats.getVehicles().forEach(vehicle -> vehicles[0] +=vehicle.getName()+": "+(Math.round(vehicle.getRemainingTimeSecond()*100)/100.f)+"s\n");*/
                    cordova.getActivity().runOnUiThread(()->{
                        // binding.tvRemaining.setText("Remaining distance: "+(Math.round(navigationStats.getRemainingDistance()*100)/100.f)+"m \n"+ vehicles[0]);
                        setCurrentFloor();

                    });
                    break;
                case NAV_INFO:
                    cType = new TypeToken<NavInfo>() {}.getType();
                    NavInfo navInfo = new Gson().fromJson(data.getPayload(),cType);
                    Snackbar.make(cordova.getActivity().findViewById(android.R.id.content),navInfo.getMessage(),Snackbar.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse response = new Gson().fromJson(data.getPayload(),cType);
                    // Log.d(TAG, response.getMessage());
                 //   callback.success(); // Thread-safe.

                    break;
                case ERROR:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse errorResp = new Gson().fromJson(data.getPayload(),cType);
                    //Log.e(TAG , TAG + errorResp.getMessage());
                    Snackbar.make(cordova.getActivity().findViewById(android.R.id.content),
                            errorResp.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                    break;

            }
        });
    }



    private PluginResult getPoiTouchResult(PluginResult.Status status, String payload){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();

        Type cType = new TypeToken<Position>() {}.getType();
        Position position = new Gson().fromJson(payload,cType);
        PoiField poiField = null;

        try {
            JSONObject jsonObject = new JSONObject(payload);
            Type poiType = new TypeToken<PoiField>() {}.getType();
            poiField= new Gson().fromJson(jsonObject.getString("data"), poiType);

        }catch (JSONException err){
            Log.d("Error", err.toString());
        }
        if(poiField == null){
            poiField = new PoiField();
        }
        poiField.setX(position.getX());
        poiField.setY(position.getY());
        poiField.setMapId(position.getMapId());
        poiField.setFloor(floorName);

        return new PluginResult(status, gson.toJson(poiField));
    }




    private void setCurrentFloor(){
        blueGPS.getCurrentFloor(new Function2<Floor, Error, Object>() {
            @Override
            public Object invoke(Floor floor, Error error) {
                if(floor!=null){
                    floorView.setText(floor.getName());
                    floorName = floor.getName();
                    callback.sendPluginResult(new PluginResult(PluginResult.Status.OK, floor.getName()));
                }
                return floor;
            }
        });

    }


    private void showNavigationMapJSON(){
        try {
            Position posSource = new Position();
            posSource.setMapId(originJSON.getInt("mapId"));
            posSource.setX(originJSON.getDouble("x"));
            posSource.setY(originJSON.getDouble("y"));

            Position posDestination = new Position();
            posDestination.setMapId(destinationJSON.getInt("mapId"));
            posDestination.setX(destinationJSON.getDouble("x"));
            posDestination.setY(destinationJSON.getDouble("y"));

            blueGPS.updateConfigurationMap(setupConfigurationMap(true));
            NavigationExtKt.moveTo(blueGPS, posSource, posDestination);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}