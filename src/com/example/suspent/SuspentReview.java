package com.example.suspent;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SuspentReview extends Activity {
	private static final String TAG = "SuspentReview";
	Handler mHandler = new Handler();

	/** The photograph */
	private Bitmap mBitmap;

	/** The audio player */
	private MediaPlayer mPlayer = null;

	private String mImageFilepath;
	private String mAudioFilepath;

	/** Spot in which to put the photograph */
	private ImageView mImageFrame;

	private Button mReplayButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suspent_review);

		Intent intent = getIntent();
		mImageFilepath = intent.getStringExtra(CameraActivity.IMAGE_FILEPATH_KEY);
		mAudioFilepath = intent.getStringExtra(CameraActivity.AUDIO_FILEPATH_KEY);

		// Fetch captured image and audio files from file system
		File imgFile = new File(mImageFilepath);
		File audioFile = new File(mAudioFilepath);

		mReplayButton = (Button) findViewById(R.id.button_replay);
		mReplayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				replay();
			}
		});
		mImageFrame = (ImageView) findViewById(R.id.image_preview);
		// mImageFrame.setVisibility(View.INVISIBLE);

		if (imgFile.exists() && audioFile.exists()) {
			mBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);
			mImageFrame.setImageBitmap(mBitmap);
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(mAudioFilepath);
				mPlayer.prepare();
			} catch (Exception e) {
				Log.e(TAG, "prepare() failed");
			}
			replay();
		} else {
			// throw file read error
		}
	}

	private void replay() {
		// Stop the audio if it is already playing
		if (mPlayer.isPlaying()) {
			mPlayer.stop();
			try {
				mPlayer.prepare();
			} catch (Exception e) {
				Log.e(TAG, "mPlayer prepare failed");
				e.printStackTrace();
			}
		}
		// Hide the picture if it is already drawn
		mImageFrame.setVisibility(View.INVISIBLE);
		mReplayButton.setVisibility(View.INVISIBLE);
		mPlayer.start();
		mHandler.postDelayed(new Runnable() {
			public void run() {
				// display picture
				mImageFrame.setVisibility(View.VISIBLE);
				mHandler.postDelayed(new Runnable() {
					public void run() {
						// display replay button
						mReplayButton.setVisibility(View.VISIBLE);
					}
				}, CameraActivity.WAIT_DURATION_AFTER_PICTURE);
			}
		}, CameraActivity.WAIT_DURATION_BEFORE_PICTURE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}
}
