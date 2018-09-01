package com.mr_abdali.mychat;

import android.content.Context;
import android.content.SharedPreferences;

class MyPrefrences {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    // TODO: 8/7/2018  Constructor imolementation...
    public MyPrefrences(Context context) {
        sharedPref = context.getSharedPreferences("asasdasd", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    void setID(String id){
        editor.putString("id",id);
        editor.commit();
    }

    String getID(){
        return sharedPref.getString("id", null);
    }
}
