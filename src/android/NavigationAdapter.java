package $appid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.synapseslab.bluegps_sdk.data.model.map.GenericResource;

import java.util.ArrayList;

public class NavigationAdapter extends ArrayAdapter<GenericResource> {
    private Context context;
    private int layoutId;
    private int textId;
    private ArrayList<GenericResource> list;

    public NavigationAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull ArrayList<GenericResource> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.layoutId = resource;
        this.textId = textViewResourceId;
        this.list = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent){
        GenericResource current = list.get(position);
        View v = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        TextView tv = v.findViewById(textId);

        String resourceName = current.getName();

        String result = resourceName;

        if(current.getBuildingPosition() != null){
            String floorName = current.getBuildingPosition().getFloorName();
            if( floorName != null && !floorName.isEmpty())
            {
                result = result.concat(String.format(", %s", floorName));
            }
            String roomName = current.getBuildingPosition().getRoomName();
            if(roomName != null && !roomName.isEmpty()){
                result = result.concat(String.format(", %s", roomName));
            }
        }

        tv.setText(result);
        return v;
    }
}
