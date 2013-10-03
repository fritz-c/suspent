package com.example.suspent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

//import android.widget.EditText;

public class MainActivity extends Activity {

	Handler mHandler = new Handler();
	private static final int MYDELAYTIME = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (checkCameraHardware(this)) {
			mHandler.postDelayed(mLaunchCamera, MYDELAYTIME);
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.error_camera_not_found_title));

			// set dialog message
			alertDialogBuilder.setMessage(getResources().getString(R.string.error_camera_not_found)).setPositiveButton(
					getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// if this button is clicked, close current activity
							MainActivity.this.finish();
						}
					});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
	}

	private Runnable mLaunchCamera = new Runnable() {
		public void run() {
			Intent i = new Intent(getApplicationContext(), CameraActivity.class);
			startActivity(i);
		}
	};

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}
}
