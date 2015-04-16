package sg.edu.nus.taptask.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import sg.edu.nus.taptask.R;

/**
 * Created by Yiwen on 16/4/2015.
 */
public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    private List<Drawable> images;

    public ArrayAdapterWithIcon(Context context, List<String> items, List<Drawable> images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = images;
    }

    public ArrayAdapterWithIcon(Context context, String[] items, Drawable[] images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = Arrays.asList(images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        try {
            System.out.println(images.get(position));
            textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), null, null, null);
        } catch(Exception e){
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.reject, 0, 0, 0);
        }
        textView.setCompoundDrawablePadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
        return view;
    }

}