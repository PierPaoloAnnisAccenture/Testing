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

import java.util.List;

public class NavigationAdapter extends ArrayAdapter<PoiField> {
    private Context context;
    private int layoutId;
    private int textId;
    private List<PoiField> list;

    public NavigationAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<PoiField> objects) {
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
        View v = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        TextView tv = v.findViewById(textId);
        tv.setText(list.get(position).getName());
        return v;
    }
}
