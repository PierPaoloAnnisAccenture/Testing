package $appid;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.outsystems.bluegps.BlueGPS;
import com.synapseslab.bluegps_sdk.data.model.map.GenericResource;
import $appid.databinding.ActivityNavigationBinding;

public class NavigationActivity extends AppCompatActivity {


    private String TAG = "NavigationActivity";
    private ActivityNavigationBinding binding;
    private View toolbarView;
    private GenericResource source;
    private GenericResource destination;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("Navigation View");
        binding.webView.initMap(BlueGPS.sdkEnvironment, BlueGPS.configurationMap, null);
    }
}
