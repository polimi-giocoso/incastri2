package it.gbresciani.poligame.customUIElements;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.squareup.otto.Bus;

import it.gbresciani.poligame.R;
import it.gbresciani.poligame.events.NoEmailEvent;
import it.gbresciani.poligame.helper.BusProvider;

public class DataCollectPreference extends CheckBoxPreference {

    private Context mContext;
    private Bus BUS;

    public DataCollectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        BUS = BusProvider.getInstance();
        mContext = context;
    }



    @Override protected void onClick() {
        String email = getSharedPreferences().getString(mContext.getResources().getString(R.string.setting_email_key),"");
        if("".equals(email)){
            BUS.post(new NoEmailEvent());
        }else {
            super.onClick();
        }
    }
}
