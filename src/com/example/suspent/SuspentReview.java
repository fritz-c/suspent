package com.example.suspent;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

public class SuspentReview extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suspent_review);

		Intent intent = getIntent();
		String imageFilepath = intent.getStringExtra(CameraActivity.IMAGE_FILEPATH_KEY);
		// String audioFilepath =
		// intent.getStringExtra(CameraActivity.AUDIO_FILEPATH_KEY);

		// Fetch captured image and audio files from file system
		File imgFile = new File(imageFilepath);
		// File audioFile = new File(audioFilepath);
		if (imgFile.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			ImageView myImage = (ImageView) findViewById(R.id.image_preview);
			myImage.setImageBitmap(myBitmap);
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.suspent_review, menu);
		return true;
	}

}
