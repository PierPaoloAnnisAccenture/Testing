package $appid;

import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.webView.initMap(BlueGPS.sdkEnvironment, BlueGPS.configurationMap, null);
        startNavigation(view);
    }

    void startNavigation(View view){
        Position source = new Position();
        source.setMapId(11);
        source.setX(-11.05);
        source.setY(-3.12);
        Position destination = new Position();
        destination.setMapId(11);
        destination.setX(32.64);
        destination.setY(6.3);
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
