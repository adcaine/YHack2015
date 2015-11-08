package com.example.ehc.myapplication;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Help_Contact_Fragment extends Fragment implements View.OnClickListener{
    Button callBtn,textBtn;
    EditText mEdit;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.help_contact_fragment,container,false);
        Button callBtn = (Button) v.findViewById(R.id.contact_Btn1);
        callBtn.setOnClickListener(this);
        Button textBtn = (Button) v.findViewById(R.id.contact_Btn2);
        textBtn.setOnClickListener(this);
        mEdit   = (EditText) v.findViewById(R.id.text_input);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_Btn1:
                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:" + getContactNumber()));
                startActivity(phoneIntent);
                break;
            case R.id.contact_Btn2:

                String message = mEdit.getText().toString();
        SmsManager.getDefault().sendTextMessage(getContactNumber(), null, message, null, null);break;
        }
    }

    private String getContactNumber(){
        return "11111111111";
    }

}