package com.klinger.shmuli.cloud_kit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class DeepLinkActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getAction() == Intent.ACTION_VIEW) {
            Uri data = getIntent().getData();

            if (data != null) {

                // show an alert with the "custom" param
                new AlertDialog.Builder(this)
                        .setTitle("An example of a Deeplink")
                        .setMessage("Found custom param: " +data.getQueryParameter("custom"))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }
}