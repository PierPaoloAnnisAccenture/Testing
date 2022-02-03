package $appid;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Spinner;

import $appid.PoiField;
import $appid.NavigationAdapter;

import com.outsystems.bluegps.BlueGPS;
import com.synapseslab.bluegps_sdk.core.BlueGPSLib;
import com.mobilecop.bluegps.NavigationExtKt;
import com.synapseslab.bluegps_sdk.data.model.map.GenericResource;
import $appid.databinding.ActivityNavigationBinding;

import com.synapseslab.bluegps_sdk.data.model.map.ConfigurationMap;
import com.synapseslab.bluegps_sdk.data.model.map.IconStyle;
import com.synapseslab.bluegps_sdk.data.model.map.MapStyle;
import com.synapseslab.bluegps_sdk.data.model.map.Position;
import com.synapseslab.bluegps_sdk.data.model.map.NavigationStyle;
import com.synapseslab.bluegps_sdk.data.model.map.ShowMap;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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
                return (CoroutineContext) Dispatchers.getDefault();
            }

            @Override
            public void resumeWith(@NonNull Object o) {
                Log.d("Coroutine", "Test");
                if(o instanceof Resource.Error){
                    Log.d("Coroutine", "Test");
                }
                else {
                    List<GenericResource> resourceList = ((Resource.Success<List<GenericResource>>) o).getData();
                    list = new ArrayList<>(resourceList);
                    NavigationAdapter adapter = new NavigationAdapter(NavigationActivity.this, R.layout.item_spinner, R.id.tvName, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    originSpinner.setAdapter(adapter);
                    originSpinner.setSelection(0);
                    destinationSpinner.setAdapter(adapter);
                    destinationSpinner.setSelection(1);
                }
            }
        };


        BlueGPSLib.Companion.getInstance().findResources(false, null, null, "name", null, null, null, null, continuation);

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
        showMap.setAll(false);
        showMap.setRoom(true);
        showMap.setMe(true);

        configurationMap.setStyle(mapStyle);
        configurationMap.setShow(showMap);
        return configurationMap;
    }
}
