package com.ssns.falldetectionsystem;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;


public class Helper extends DialogFragment {

    SmsManager smsManager = SmsManager.getDefault();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you fine?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String textMsg = "Hello, I have fallen down here-> " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()) + "need help immediately!!";
                        smsManager.sendTextMessage("01593652599", null, textMsg, null, null);

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
