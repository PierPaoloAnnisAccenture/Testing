package $appid;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import $appid.PoiField;

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

public class NavigationActivity extends AppCompatActivity {

    private ActivityNavigationBinding binding;
    private ArrayList<GenericResource> genericResourceList = new ArrayList<>();
    private ConfigurationMap configurationMap = setupConfigurationMap();

    private PoiField origin;
    private PoiField destination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        origin = (PoiField) getIntent().getExtras().getSerializable("origin");
        destination = (PoiField) getIntent().getExtras().getSerializable("destination");

        Log.d("Extras", origin.toString());
        Log.d("Extras", destination.toString());

        binding.webView.initMap(BlueGPS.sdkEnvironment, BlueGPS.configurationMap, null);
        binding.btnGuestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNavigation(view, origin, destination);
            }
        });
    }

    void startNavigation(View view, PoiField o, PoiField d){
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
