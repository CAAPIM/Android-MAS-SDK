package ca.com.maspubsubsample;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;


public class QosSpinner extends AppCompatSpinner {

    public QosSpinner(Context context) {
        super(context);
    }

    public QosSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QosSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setAdapter(new ArrayAdapter<>(getContext(), R.layout.qos_spinner_item,
                getResources().getStringArray(R.array.qos_options)));
    }

    public Integer getSelectedQos(){
        return Integer.parseInt( (String) getSelectedItem());
    }
}
