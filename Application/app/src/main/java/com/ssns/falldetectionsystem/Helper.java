package com.ssns.falldetectionsystem;

import android.content.DialogInterface;
import android.widget.Toast;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class Helper  {

  public static boolean userConfirmation= false;
 public static boolean createAlert(AlertDialog.Builder alertDialog3,  Context context ) {

     final Context applicationContext = context;
//// Setting Dialog Title
      alertDialog3.setTitle("Alert");
//
//// Setting Dialog Message
    alertDialog3.setMessage("Are you fine?");

//// Setting Positive Yes Button
  alertDialog3.setPositiveButton("YES",
             new DialogInterface.OnClickListener() {

                  public void onClick(DialogInterface dialog, int which) {
//                        // User pressed Cancel button. Write Logic Here
                      Toast.makeText(applicationContext,
                              " Thank you for your Response", Toast.LENGTH_SHORT)
                            .show();
                      userConfirmation= true;
                 }
                });
//// Setting Positive Yes Btn
  /*    alertDialog3.setNeutralButton("NO",
              new DialogInterface.OnClickListener() {

                  public void onClick(DialogInterface dialog, int which) {
//                        // User pressed No button. Write Logic Here
                     Toast.makeText(applicationContext,
                     "Thanks for your response, will send your location to emergency contact.", Toast.LENGTH_SHORT)
                         .show();
                 }
            });
//// Setting Positive "Cancel" Btn
     alertDialog3.setNegativeButton("Cancel",
               new DialogInterface.OnClickListener() {

                   public void onClick(DialogInterface dialog, int which) {
//                        // User pressed Cancel button. Write Logic Here
                       Toast.makeText(applicationContext,
                                "You clicked on Cancel", Toast.LENGTH_SHORT)
                             .show();
                }
             });*/
//// Showing Alert Dialog
      alertDialog3.show();
      return userConfirmation;

 }

 public void saveContact() {

 }

 public void retrieveContact() {

 }

}
