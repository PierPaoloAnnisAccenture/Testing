package $appid;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Spinner;

import $appid.PoiField;
import $appid.NavigationAdapter;

import com.outsystems.bluegps.BlueGPS;
import com.mobilecop.bluegps.NavigationExtKt;
import com.synapseslab.bluegps_sdk.data.model.map.GenericResource;
import $appid.databinding.ActivityNavigationBinding;

import com.synapseslab.bluegps_sdk.data.model.map.ConfigurationMap;
import com.synapseslab.bluegps_sdk.data.model.map.IconStyle;
import com.synapseslab.bluegps_sdk.data.model.map.MapStyle;
import com.synapseslab.bluegps_sdk.data.model.map.Position;
import com.synapseslab.bluegps_sdk.data.model.map.NavigationStyle;
import com.synapseslab.bluegps_sdk.data.model.map.ShowMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class NavigationActivity extends AppCompatActivity {

    private ActivityNavigationBinding binding;

    private ArrayList<PoiField> list;
    private PoiField origin;
    private PoiField destination;
    private int selectOrigin = 0;
    private int selectDest = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        list = (ArrayList<PoiField>) getIntent().getExtras().getSerializable("list");
        selectOrigin = getIntent().getExtras().getInt("origin");
        selectDest = getIntent().getExtras().getInt("destination");

        Spinner originSpinner = binding.spinnerFrom;
        Spinner destinationSpinner = binding.spinnerTo;

        NavigationAdapter adapter = new NavigationAdapter(this, R.layout.item_spinner, R.id.tvName, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        originSpinner.setAdapter(adapter);
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
        originSpinner.setSelection(selectOrigin);

        destinationSpinner.setAdapter(adapter);
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
        destinationSpinner.setSelection(selectDest);

        Log.d("Info", "SDK Env: " + BlueGPS.sdkEnvironment.toString());

        ConfigurationMap configurationMap = getIntent().getExtras().getParcelable("configurationMap");

        binding.webView.initMap(BlueGPS.sdkEnvironment, configurationMap, new Function1<String, Unit>() {
            @Override
            public Unit invoke(String s) {
                if (origin != null && destination != null)
                    startNavigation(view, origin, destination);
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

    void startNavigation(View view, PoiField o, PoiField d) {
        Position source = new Position();
        source.setMapId(o.getMapId());
        source.setX(o.getX());
        source.setY(o.getY());

        Position destination = new Position();
        destination.setMapId(d.getMapId());
        destination.setX(d.getX());
        destination.setY(d.getY());

        NavigationExtKt.moveTo(binding.webView, source, destination);
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
