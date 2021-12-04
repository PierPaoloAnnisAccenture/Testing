package $appid;

import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synapseslab.bluegps_sdk.data.model.map.*;
import com.synapseslab.bluegps_sdk.data.model.stats.NavInfo;
import com.synapseslab.bluegps_sdk.data.model.stats.NavigationStats;

import $appid.databinding.ActivityMapBinding;
import com.outsystems.bluegps.BlueGPS;

import java.lang.reflect.Type;


public class MapActivity extends AppCompatActivity {
private String TAG = "MapActivity";

private ActivityMapBinding binding;
private View toolbarView = null;

private Boolean hideRoomLayer = false;

private Boolean navigationMode = false;

private Position source;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        getSupportActionBar().setTitle("Map View");
        tapViewClickListener();

        //Receive Map Config and sdk Config also login info

        /**
         * The BlueGPSMapView component expose an initMap method for initialize the web view
         * with the required parameters and load the start url. [ *baseURL* + api/public/resource/sdk/mobile.html]
         */
        binding.webView.initMap(BlueGPS.sdkEnvironment,BlueGPS.configurationMap);

        setListenerOnMapView();
        setOnNavigationModeButtonListener();
        setOnGoToClickListener();
    }

    /**
 * Setup the listener for BlueGPSMapView in order to implement the code
 * to run when an event click on map occurs.
 */
private void setListenerOnMapView() {
        binding.webView.setBlueGPSMapListener((data, typeMapCallback) -> {
            Type cType;
            switch (typeMapCallback){
                case INIT_SDK_END:
                    Log.d(TAG,"INIT_SDK_END");
                    break;
                case PARK_CONF:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse payloadResponse = new Gson().fromJson(data.getPayload(),cType);
                    if (payloadResponse.getAvailableDateList() != null) {
                        if (!payloadResponse.getAvailableDateList().isEmpty()) {
                            Log.d(TAG, String.valueOf(payloadResponse.getAvailableDateList()));
                        }
                    }
                    break;
                case ROOM_CLICK:
                case MAP_CLICK:
                case TAG_CLICK:
                    cType = new TypeToken<Position>() {}.getType();
                    Position position = new Gson().fromJson(data.getPayload(),cType);
                    if (navigationMode) {
                        runOnUiThread(() -> {
                            source = position;
                            binding.tvDestination.setText("Destination: ("+String.format("%.3f", position.getX())+"), "+String.format("%.3f", position.getY())+")");
                            showHideLayoutDestination(true);
                        });
                    } else {
                        new MaterialAlertDialogBuilder(getApplicationContext())
                                .setTitle("Type: "+typeMapCallback.name())
                                .setMessage(position.toString())
                                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                    }
                    break;
                case BOOKING_CLICK:
                    cType = new TypeToken<ClickedObject>() {}.getType();
                    ClickedObject clickedObject = new Gson().fromJson(data.getPayload(),cType);
                    new MaterialAlertDialogBuilder(getApplicationContext())
                            .setTitle("Type: "+typeMapCallback.name())
                            .setMessage(clickedObject.toString())
                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                    break;
                case NAV_STATS:
                    cType = new TypeToken<NavigationStats>() {}.getType();
                    NavigationStats navigationStats = new Gson().fromJson(data.getPayload(),cType);
                    Log.d(TAG, String.valueOf(navigationStats));
                    final String[] vehicles = {""};
                    navigationStats.getVehicles().forEach(vehicle -> vehicles[0] +=vehicle.getName()+": "+(Math.round(vehicle.getRemainingTimeSecond()*100)/100.f)+"s\n");
                    runOnUiThread(()->{
                        binding.tvRemaining.setText("Remaining distance: "+(Math.round(navigationStats.getRemainingDistance()*100)/100.f)+"m \n"+ vehicles[0]);
                    });
                    break;
                case NAV_INFO:
                    cType = new TypeToken<NavInfo>() {}.getType();
                    NavInfo navInfo = new Gson().fromJson(data.getPayload(),cType);
                    Snackbar.make(findViewById(android.R.id.content),navInfo.getMessage(),Snackbar.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse response = new Gson().fromJson(data.getPayload(),cType);
                    Log.d(TAG, response.getMessage());
                    break;
                case ERROR:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse errorResp = new Gson().fromJson(data.getPayload(),cType);
                    Log.e(TAG , TAG + errorResp.getMessage());
                    Snackbar.make(findViewById(android.R.id.content),
                            errorResp.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                    break;

            }
        });
}


/**
 * Toolbox GUI for configure and change the map control layer.
 * This demo show only some functions. Look at the documentation for all available methods.
 */
    private void showToolbarView() {
        if (toolbarView != null && toolbarView.isShown()) {
            return;
        }
        toolbarView =
        LayoutInflater.from(this).inflate(R.layout.toolbar_map_view, binding.mapView, false);
        TransitionManager.beginDelayedTransition(binding.mapView, new Fade());
        binding.mapView.addView(toolbarView);
        binding.tapView.setVisibility(View.VISIBLE);


        SwitchMaterial switchStatus = toolbarView.findViewById(R.id.switchStatus);

        ToolboxMapParameter tmpMapControl = null;
        if(BlueGPS.configurationMap.getToolbox() != null){
            tmpMapControl = BlueGPS.configurationMap.getToolbox().getMapControl();
        }
        final ToolboxMapParameter mapControl = tmpMapControl;
        if (mapControl != null){
            if (mapControl.getEnabled() != null){
                switchStatus.setChecked(mapControl.getEnabled());
            }else{
                switchStatus.setChecked(true);
            }
        }
        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mapControl != null){
                    if (mapControl.getEnabled() != null){
                        switchStatus.setChecked(mapControl.getEnabled());
                    }
                }
                binding.webView.updateConfigurationMap(BlueGPS.configurationMap);
            }
        });

        Button btnHorizontal = toolbarView.findViewById(R.id.btnHorizontal);
        Button btnVertical = toolbarView.findViewById(R.id.btnVertical);

        if (mapControl != null){
            if (mapControl.getOrientation() != null){
                if (mapControl.getOrientation() == OrientationType.horizontal){
                    btnHorizontal.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500));
                    btnVertical.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.grey));
                }else{
                    btnHorizontal.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.grey));
                    btnVertical.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500));
                }
            }
        }

        btnHorizontal.setOnClickListener(view->{
            btnHorizontal.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500));
            btnVertical.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.grey));
            if (mapControl != null){
                mapControl.setOrientation(OrientationType.horizontal);
            }
            binding.webView.updateConfigurationMap(BlueGPS.configurationMap);
        });

        btnVertical.setOnClickListener(view-> {
            btnVertical.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500));
            btnHorizontal.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.grey));
            if (mapControl != null){
                mapControl.setOrientation(OrientationType.vertical);
            }
            binding.webView.updateConfigurationMap(BlueGPS.configurationMap);
        });


        Slider sliderButtonWidth = toolbarView.findViewById(R.id.sliderButtonWidth);
        if (mapControl != null){
            if (mapControl.getButtonWidth() != null){
                sliderButtonWidth.setValue(mapControl.getButtonWidth().floatValue());
            }
        }
        sliderButtonWidth.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (mapControl != null){
                    mapControl.setButtonWidth((int) slider.getValue());
                }
                binding.webView.updateConfigurationMap(BlueGPS.configurationMap);
            }
        });

        Slider sliderButtonHeight = toolbarView.findViewById(R.id.sliderButtonHeight);
        if (mapControl != null){
            if (mapControl.getButtonHeight() != null){
                sliderButtonHeight.setValue(mapControl.getButtonHeight().floatValue());
            }
        }
        sliderButtonHeight.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (mapControl != null){
                    mapControl.setButtonHeight((int) slider.getValue());
                }
                binding.webView.updateConfigurationMap(BlueGPS.configurationMap);
            }
        });

        ImageButton actionNextFloor = toolbarView.findViewById(R.id.actionNextFloor);
        actionNextFloor.setOnClickListener(view -> binding.webView.nextFloor());

        ImageButton actionResetView = toolbarView.findViewById(R.id.actionResetView);
        actionResetView.setOnClickListener(view -> binding.webView.resetView());

        ImageButton actionHideRoomLayer = toolbarView.findViewById(R.id.actionHideRoomLayer);
        if (!hideRoomLayer) actionHideRoomLayer.setImageResource(R.drawable.ic_baseline_layers_24);
        else actionHideRoomLayer.setImageResource(R.drawable.ic_baseline_layers_clear_24);
        actionHideRoomLayer.setOnClickListener(view -> {
            hideRoomLayer = !hideRoomLayer;
            binding.webView.hideRoomLayer(hideRoomLayer);

            if (!hideRoomLayer) actionHideRoomLayer.setImageResource(R.drawable.ic_baseline_layers_24);
            else actionHideRoomLayer.setImageResource(R.drawable.ic_baseline_layers_clear_24);
        });

        ImageButton actionGetFloor = toolbarView.findViewById(R.id.actionGetFloor);
        actionGetFloor.setOnClickListener(
            view-> binding.webView.getFloor((result, error)-> {
                if (error != null){
                    Log.e(TAG, String.valueOf(error));
                }else {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Floor list")
                            .setMessage(result.toString())
                            .setPositiveButton("Ok", (dialog, i) -> dialog.dismiss()).show();
                }
                return result;
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu){
        //TODO check usability in this context
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                showToolbarView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tapViewClickListener() {
        binding.tapView.setOnClickListener(view -> {
        if (toolbarView != null && toolbarView.isShown()) {
        binding.mapView.removeView(toolbarView);
        }

        binding.tapView.setVisibility(View.GONE);
        });
    }

    private void setOnNavigationModeButtonListener() {
        binding.btnNavigationMode.setOnClickListener(view -> {
            navigationMode = !navigationMode;

            if (navigationMode) {
                binding.btnNavigationMode.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue_500));

            } else {
                binding.btnNavigationMode.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.grey));

                showHideLayoutDestination(false);
                binding.tvDestination.setText("");
                binding.tvRemaining.setText("");
                binding.webView.removeNavigation();
            }
        });
    }

    private void setOnGoToClickListener() {
        binding.btnGoTo.setOnClickListener(view -> binding.webView.gotoFromMe(source,true));
    }

    private void showHideLayoutDestination(Boolean visibility) {
        if (visibility) binding.layoutDestination.setVisibility(View.VISIBLE); else binding.layoutDestination.setVisibility(View.GONE);
    }
}
