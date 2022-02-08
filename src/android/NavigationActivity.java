package $appid;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Spinner;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import $appid.PoiField;
import $appid.NavigationAdapter;

import com.outsystems.bluegps.BlueGPS;
import com.synapseslab.bluegps_sdk.core.BlueGPSLib;
import com.mobilecop.bluegps.NavigationExtKt;
import com.synapseslab.bluegps_sdk.data.model.map.ClickedObject;
import com.synapseslab.bluegps_sdk.data.model.map.GenericResource;
import $appid.databinding.ActivityNavigationBinding;

import com.synapseslab.bluegps_sdk.data.model.map.ConfigurationMap;
import com.synapseslab.bluegps_sdk.data.model.map.IconStyle;
import com.synapseslab.bluegps_sdk.data.model.map.MapStyle;
import com.synapseslab.bluegps_sdk.data.model.map.PayloadResponse;
import com.synapseslab.bluegps_sdk.data.model.map.Position;
import com.synapseslab.bluegps_sdk.data.model.map.NavigationStyle;
import com.synapseslab.bluegps_sdk.data.model.map.ShowMap;
import com.synapseslab.bluegps_sdk.data.model.stats.NavInfo;
import com.synapseslab.bluegps_sdk.data.model.stats.NavigationStats;
import com.synapseslab.bluegps_sdk.utils.Resource;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.Dispatchers;


public class NavigationActivity extends AppCompatActivity {

    private ActivityNavigationBinding binding;

    private ArrayList<GenericResource> list =  new ArrayList<GenericResource>();
    private GenericResource origin;
    private GenericResource destination;
    private Boolean navigationMode = false;

    private PoiField destinationJson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        destinationJson = (PoiField) getIntent().getExtras().getSerializable("destination");

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Spinner originSpinner = binding.spinnerFrom;
        Spinner destinationSpinner = binding.spinnerTo;

        originSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                origin = list.get(position);
                if (origin != null && destination != null)
                    startNavigation(view, origin, destination);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Continuation<Resource<List<GenericResource>>> continuation = new Continuation<Resource<List<GenericResource>>>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return (CoroutineContext) Dispatchers.getMain();
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                Log.d("Coroutine", "Test");
                if(o instanceof Resource.Error){
                    Log.d("Coroutine", "Errore");
                    Log.d("Coroutine", ((Resource.Error<?>) o).getMessage());
                }
                else {
                    Log.d("Coroutine", "Success");
                    List<GenericResource> resourceList = ((Resource.Success<List<GenericResource>>) o).getData();
                    list = new ArrayList<>(resourceList);

                   int destinationIndex = 1;
                    int originIndex = 0;

                    int i=0;
                    for( GenericResource genericResource : resourceList){
                        if(genericResource.getName().equals(destinationJson.getName())){
                            destinationIndex = i;
                        }
                        if(genericResource.getName().contains("Elevator")){
                            originIndex = i;
                        }

                        i++;
                    }




                    NavigationAdapter adapter = new NavigationAdapter(NavigationActivity.this, R.layout.item_spinner, R.id.tvName, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    originSpinner.setAdapter(adapter);
                    destinationSpinner.setAdapter(adapter);


                    originSpinner.setSelection(originIndex);
                    destinationSpinner.setSelection(destinationIndex);
                }
            }
        };


        BlueGPSLib.Companion.getInstance().findResources(false, null, "name", null, null, null, null, null, continuation);

        destinationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destination = list.get(position);
                if (origin != null && destination != null)
                    startNavigation(view, origin, destination);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        destinationSpinner.setSelection(selectDest);

        Log.d("Info", "SDK Env: " + BlueGPS.sdkEnvironment.toString());

//        ConfigurationMap configurationMap = getIntent().getExtras().getParcelable("configurationMap");

        binding.webView.initMap(BlueGPS.sdkEnvironment, setupConfigurationMap(), new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                return null;
            }
        });

        binding.btnGuestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (origin != null && destination != null)
                    startNavigation(view, origin, destination);
            }
        });


        setListenerOnMapView();
    }

    void startNavigation(View view, GenericResource source, GenericResource destination) {
        NavigationExtKt.moveTo(binding.webView, source.getPosition(), destination.getPosition());
    }

    private ConfigurationMap setupConfigurationMap() {
        ConfigurationMap configurationMap = new ConfigurationMap();

        MapStyle mapStyle = new MapStyle();

        NavigationStyle navigationStyle = new NavigationStyle();
        navigationStyle.setIconSource("/api/public/resource/icons/commons/start.svg");
        navigationStyle.setIconDestination("/api/public/resource/icons/commons/end.svg");

        IconStyle iconStyle = new IconStyle();
        iconStyle.setName("saipem");
        iconStyle.setAlign("center");
        iconStyle.setVAlign("center");
        iconStyle.setFollowZoom(false);

        mapStyle.setNavigation(navigationStyle);
        mapStyle.setIcons(iconStyle);

        ShowMap showMap = new ShowMap();
        showMap.setAll(true);
        showMap.setRoom(false);
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
        binding.webView.setBlueGPSMapListener((data, typeMapCallback) -> {
            Type cType;
            switch (typeMapCallback){
                /*case INIT_SDK_END:
                    Log.d(TAG,"INIT_SDK_END");
                    break;
                 */
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
               /* case RESORUCE:
                    cType = new TypeToken<ClickedObject>() {}.getType();
                    GenericResource resource = new Gson().fromJson(data.getPayload(),cType);
                    Snackbar.make(findViewById(android.R.id.content),
                            typeMapCallback.name() + resource.getName() + resource.getType(),
                            Snackbar.LENGTH_LONG).show();
*/
                case TAG_CLICK:
                    cType = new TypeToken<Position>() {}.getType();
                    Position position = new Gson().fromJson(data.getPayload(),cType);
                    if (navigationMode) {
                        runOnUiThread(() -> {
                            /*source = position;
                            binding.tvDestination.setText("Destination: ("+String.format("%.3f", position.getX())+"), "+String.format("%.3f", position.getY())+")");
                            showHideLayoutDestination(true);*/

                        });
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                typeMapCallback.name() + position.toString(),
                                Snackbar.LENGTH_LONG).show();

                       /* new MaterialAlertDialogBuilder(getApplicationContext())
                                .setTitle("Type: "+typeMapCallback.name())
                                .setMessage(position.toString())
                                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();*/
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
                  //  Log.d(TAG, String.valueOf(navigationStats));
                    final String[] vehicles = {""};
                    navigationStats.getVehicles().forEach(vehicle -> vehicles[0] +=vehicle.getName()+": "+(Math.round(vehicle.getRemainingTimeSecond()*100)/100.f)+"s\n");
                    runOnUiThread(()->{
                       // binding.tvRemaining.setText("Remaining distance: "+(Math.round(navigationStats.getRemainingDistance()*100)/100.f)+"m \n"+ vehicles[0]);
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
                   // Log.d(TAG, response.getMessage());
                    break;
                case ERROR:
                    cType = new TypeToken<PayloadResponse>() {}.getType();
                    PayloadResponse errorResp = new Gson().fromJson(data.getPayload(),cType);
                    //Log.e(TAG , TAG + errorResp.getMessage());
                    Snackbar.make(findViewById(android.R.id.content),
                            errorResp.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                    break;

            }
        });
    }
}
