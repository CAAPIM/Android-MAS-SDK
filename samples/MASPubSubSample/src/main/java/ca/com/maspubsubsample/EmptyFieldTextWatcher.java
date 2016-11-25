package ca.com.maspubsubsample;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Disables views when provided {@link EditText}s are empty.
 * Enables them when some text is entered.
 */
public class EmptyFieldTextWatcher {

    private ArrayList<View> viewsToDisable;
    private ArrayList<EditText> editTexts;
    private TextWatcher textWatcher;

    public EmptyFieldTextWatcher(View[] viewsToDisable, EditText[] editTexts) {
        this.editTexts = new ArrayList<>();
        this.viewsToDisable = new ArrayList<>();
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s;
                for( EditText editText : EmptyFieldTextWatcher.this.editTexts ){
                    s = editText.getText().toString();
                    if(TextUtils.isEmpty(s.trim())){
                        disableViews();
                        return;
                    }
                }
                enableViews();
            }
        };
        setViewsToDisable(viewsToDisable);
        setEditTexts(editTexts);
        disableViews();
    }

    private void setViewsToDisable(View... disableViews){
        for( View v : disableViews ){
            viewsToDisable.add(v);
        }
    }

    private void setEditTexts( EditText... editTexts ){
        for( EditText editText : editTexts){
            this.editTexts.add(editText);
            editText.addTextChangedListener(textWatcher);
        }
    }

    private void disableViews(){
        setViewsEnabled(false);
    }

    private void enableViews(){
        setViewsEnabled(true);
    }

    private void setViewsEnabled(boolean enable){
        for( View v : viewsToDisable ){
            v.setEnabled(enable);
        }
    }
}
