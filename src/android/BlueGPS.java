package com.outsystems.bluegps;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import $appid.MapActivity;
import com.synapseslab.bluegps_sdk.core.BlueGPSLib;
import com.synapseslab.bluegps_sdk.data.model.advertising.AdvertisingStatus;
import com.synapseslab.bluegps_sdk.data.model.environment.SdkEnvironment;
import com.synapseslab.bluegps_sdk.data.model.environment.SdkEnvironmentLoggedUser;
import com.synapseslab.bluegps_sdk.data.model.map.*;
import com.synapseslab.bluegps_sdk.data.model.response.AuthResponse;
import com.synapseslab.bluegps_sdk.service.BlueGPSAdvertisingService;
import com.synapseslab.bluegps_sdk.utils.Resource;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kotlin.coroutines.CoroutineContext;

public class BlueGPS extends CordovaPlugin {

    private final String INIT = "initializeSDK";
    private final String LOGIN = "login";
    private final String OPENMAP = "openMap";
    private final String STARTADV = "startAdv";
    private final String STOPADV = "stopAdv";
    public static SdkEnvironment sdkEnvironment;
    public static ConfigurationMap configurationMap;
    private BlueGPSAdvertisingService blueGPSAdvertisingService = null;
    private CallbackContext callback;
    private final String appId = "$appid"; 
    String [] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

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
        PluginResult result = null;
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
                        callback.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                    } catch (JSONException e) {
                        callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Init Failed with error:" + e.getLocalizedMessage()));
                    }
                });
                status = true;
                result = null;
                break;
            case LOGIN:
                cordova.getThreadPool().execute(() -> {
                    try {
                        SdkEnvironmentLoggedUser loggedUser = new SdkEnvironmentLoggedUser();
                        loggedUser.setUsername(args.getString(0));
                        loggedUser.setPassword(args.getString(1));
                        loggedUser.setToken(args.getString(2));
                        sdkEnvironment.setLoggedUser(loggedUser);
                    } catch (JSONException e) {
                        callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Init Failed with error:" + e.getLocalizedMessage()));
                    }

                    BlueGPSLib.Companion.getInstance().registerSDK(sdkEnvironment, new Continuation<Resource<AuthResponse>>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            callback.sendPluginResult(new PluginResult(PluginResult.Status.OK));
                        }

                    });
                    /*
                    myscope.launch {
                        when (val result = BlueGPSLib.instance.registerSDK(sdkEnvironment)) {
                            is Resource.Success -> {
                                callback.sendPluginResult(PluginResult(PluginResult.Status.OK))
                            }
                            is Resource.Error -> {
                                callback.sendPluginResult(PluginResult(PluginResult.Status.ERROR,"SDK register Failed with error:"+result.message))
                            }
                        }
                    }*/
                });
                status = true;
                break;
            case OPENMAP:
                Intent mapIntent = new Intent(cordova.getActivity(), MapActivity.class);
                configurationMap = new ConfigurationMap();
                Map<String, String> credential = new HashMap<>();
                credential.put("sdkKey",sdkEnvironment.getSdkKey());
                credential.put("sdkSecret",sdkEnvironment.getSdkSecret());
                AuthParameters authParameters;
                if (sdkEnvironment.getLoggedUser() != null) {

                    Map<String, String> user = new HashMap<>();
                    if (sdkEnvironment.getLoggedUser().getToken() != null){
                        user.put("token",sdkEnvironment.getLoggedUser().getToken());
                    }else{
                        user.put("username",sdkEnvironment.getLoggedUser().getUsername());
                        user.put("password",sdkEnvironment.getLoggedUser().getPassword());
                    }
                    authParameters = new AuthParameters(credential,user,null,null,null);
                } else {
                    authParameters = new AuthParameters(credential,null,null,null,null);
                }
                configurationMap.setAuth(authParameters);

                MapStyle mapStyle = new MapStyle();
                JSONObject style = new JSONObject(args.getString(1));

                JSONObject icons = style.getJSONObject("icons");
                IconStyle iconsStyle = new IconStyle();
                if (icons.has("opacity")) iconsStyle.setOpacity(icons.getInt("opacity"));
                if (icons.has("name")) iconsStyle.setName(icons.getString("name"));
                if (icons.has("align")) iconsStyle.setAlign(icons.getString("align"));
                if (icons.has("vAlign")) iconsStyle.setVAlign(icons.getString("vAlign"));
                if (icons.has("followZoom")) iconsStyle.setFollowZoom(icons.getBoolean("followZoom"));

                mapStyle.setIcons(iconsStyle);


                JSONObject indication = style.getJSONObject("indication");
                IndicationStyle indicationStyle = new IndicationStyle();
                if (indication.has("destColor"))
                    indicationStyle.setDestColor(indication.getString("destColor"));
                if (indication.has("followZoom"))
                    indicationStyle.setFollowZoom(indication.getBoolean("followZoom"));
                if (indication.has("iconDestination"))
                    indicationStyle.setIconDestination(indication.getString("iconDestination"));
                if (indication.has("iconHAlign"))
                    indicationStyle.setIconHAlign(indication.getString("iconHAlign"));
                if (indication.has("iconSource"))
                    indicationStyle.setIconSource(indication.getString("iconSource"));
                if (indication.has("iconVAlign"))
                    indicationStyle.setIconVAlign(indication.getString("iconVAlign"));
                if (indication.has("opacity"))
                    indicationStyle.setOpacity(indication.getDouble("opacity"));
                if (indication.has("radiusMeter"))
                    indicationStyle.setRadiusMeter(indication.getDouble("radiusMeter"));
                mapStyle.setIndication(indicationStyle);

                JSONObject navigation = style.getJSONObject("navigation");
                NavigationStyle navigationStyle = new NavigationStyle();

                if (navigation.has("animationTime"))
                    navigationStyle.setAnimationTime(navigation.getDouble("animationTime"));
                if (navigation.has("autoZoom"))
                    navigationStyle.setAutoZoom(navigation.getBoolean("autoZoom"));
                if (navigation.has("iconDestination"))
                    navigationStyle.setIconDestination(navigation.getString("iconDestination"));
                if (navigation.has("iconSource"))
                    navigationStyle.setIconSource(navigation.getString("iconSource"));
                if (navigation.has("jumpColor"))
                    navigationStyle.setJumpColor(navigation.getString("jumpColor"));
                if (navigation.has("jumpOpacity"))
                    navigationStyle.setJumpOpacity(navigation.getDouble("jumpOpacity"));
                if (navigation.has("jumpRadiusMeter"))
                    navigationStyle.setJumpRadiusMeter(navigation.getDouble("jumpRadiusMeter"));
                if (navigation.has("navigationStep"))
                    navigationStyle.setNavigationStep(navigation.getDouble("navigationStep"));
                if (navigation.has("showVoronoy"))
                    navigationStyle.setShowVoronoy(navigation.getBoolean("showVoronoy"));
                if (navigation.has("stroke"))
                    navigationStyle.setStroke(navigation.getString("stroke"));
                if (navigation.has("strokeLinecap"))
                    navigationStyle.setStrokeLinecap(navigation.getString("strokeLinecap"));
                if (navigation.has("strokeLinejoin"))
                    navigationStyle.setStrokeLinejoin(navigation.getString("strokeLinejoin"));
                if (navigation.has("strokeOpacity"))
                    navigationStyle.setStrokeOpacity(navigation.getDouble("strokeOpacity"));
                if (navigation.has("strokeWidthMeter"))
                    navigationStyle.setStrokeWidthMeter(navigation.getDouble("strokeWidthMeter"));
                if (navigation.has("velocityOptions")){
                    Map<String,Double> velocityOptions = new HashMap<>();
                    JSONArray velocityArray = navigation.getJSONArray("velocityOptions");
                    for (int i = 0; i<velocityArray.length(); i++){
                        JSONObject velocityOption = velocityArray.getJSONObject(i);
                        velocityOptions.put(velocityOption.getString("key"),velocityOption.getDouble("value"));
                    }
                    navigationStyle.setVelocityOptions(velocityOptions);
                }
                mapStyle.setNavigation(navigationStyle);

                JSONObject showmap = new JSONObject(args.getString(2));
                ShowMap map = new ShowMap();

                if (showmap.has("all"))
                    map.setAll(showmap.getBoolean("all"));
                if (showmap.has("me"))
                    map.setMe(showmap.getBoolean("me"));
                if (showmap.has("room"))
                    map.setRoom(showmap.getBoolean("room"));
                if (showmap.has("park"))
                    map.setPark(showmap.getBoolean("park"));
                if (showmap.has("desk"))
                    map.setDesk(showmap.getBoolean("desk"));
                configurationMap.setShow(map);

                configurationMap.setTagid(args.getString(0));

                cordova.getActivity().startActivity(mapIntent);
                status = true;
                result = new PluginResult(PluginResult.Status.OK);
                break;
            case STARTADV:

                cordova.getThreadPool().execute(() -> blueGPSAdvertisingService.startAdv());
                status = true;
                result = new PluginResult(PluginResult.Status.OK);
                break;
            case STOPADV:
                cordova.getThreadPool().execute(() -> blueGPSAdvertisingService.stopAdv());
                status = true;
                result = new PluginResult(PluginResult.Status.OK);
                break;
            default:
                result = new PluginResult(PluginResult.Status.ERROR, "Invalid Action!");
                break;
        }
        if (result != null) {
            callbackContext.sendPluginResult(result);
        }
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

    private void checkStatusBluetooth() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}