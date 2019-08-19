package com.ca.mas.foundation;

import android.util.Pair;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that forms the Form Part, containing the form parameters, in Multipart request.
 */
public class FormPart {

   private Map<String, String> formFields = new HashMap<>();

    public void addFormField(String key, String value){
       formFields.put(key,value);
    }

   public Map<String, String> getFormFields() {
      return formFields;
   }

   public void clear(){
        formFields.clear();
   }
}
