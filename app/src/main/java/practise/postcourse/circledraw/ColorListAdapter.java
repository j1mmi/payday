package practise.postcourse.circledraw;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Jim on 15/01/2016.
 */
public class ColorListAdapter extends ArrayAdapter<String> {

//    String[] colorNameList = { "MAGENTA", "BRICK RED", "DARK RED", "ORANGE", "DARK YELLOW", "TURQUOISE", "GREEN", "DARK GREEN",
//            "AQUAMARINE", "BLUE", "DARK BLUE", "DARKER BLUE", "DARKEST BLUE", "PURPLE" };
//
//    String[] colorHexList = { "#CC0099", "#CC0033", "#CC3300", "#FFC20A", "#CC9900", "#00CC99", "#99CC00", "#33CC00",
//            "#4775FF", "#0099CC", "#0A47FF", "#0033CC", "#3300CC", "#9900CC" };

    String[] colorHexList;

    public ColorListAdapter(Context ctx, int txtViewResourceId, String[] objects) {
        super(ctx, txtViewResourceId, objects);
        this.colorHexList = objects;
    }

    @Override public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        return getCustomView(position, cnvtView, prnt);
    }

    @Override public View getView(int pos, View cnvtView, ViewGroup prnt) {
        return getCustomView(pos, cnvtView, prnt);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View mySpinner = inflater.inflate(R.layout.color_spinner, parent, false);
        TextView main_text = (TextView) mySpinner.findViewById(R.id.item);
        main_text.setBackgroundColor(Color.parseColor(colorHexList[position]));
        return mySpinner;
    }

}
