package com.example.suspent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

	protected static final String TAG = "Camera Capture";
	public static final String IMAGE_FILEPATH_KEY = "image_filepath";
	public static final String AUDIO_FILEPATH_KEY = "audio_filepath";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int AUDIO_LENGTH = 7000;
	public static final int WAIT_DURATION_BEFORE_PICTURE = 5000;
	public static final int WAIT_DURATION_AFTER_PICTURE = 2000;

	private Camera mCamera;
	private CameraPreview mPreview;
	private Button mCaptureButton;

	private String mUniqueStamp;
	private String mImageFilepath;
	private String mAudioFilepath;

	private static final String LOG_TAG = "AudioRecordTest";
	private MyRecorder mRecorder = null;
	private MyCountdown mTimerCount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_activity);

		mCaptureButton = (Button) findViewById(R.id.button_capture);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		// Create an instance of Camera
		mCamera = getCameraInstance();
		mTimerCount = new MyCountdown(WAIT_DURATION_BEFORE_PICTURE, 1000);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		preview.addView(mPreview);

		// Add a listener to the Capture button
		mCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCaptureButton.setClickable(false);
				mCaptureButton.setText("" + WAIT_DURATION_BEFORE_PICTURE / 1000);
				startRecording();
				mTimerCount.start();
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
			// c.setDisplayOrientation(90); // Set camera to portrait mode
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d(TAG, "Error creating media file, check storage permissions");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};

	private void startRecording() {
		mRecorder = new MyRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setMaxDuration(AUDIO_LENGTH);
		mRecorder.setOnInfoListener(mRecorder);
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/suspent/audio");
		dir.mkdirs();
		mUniqueStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		mAudioFilepath = dir.getAbsolutePath() + "/AUD_" + mUniqueStamp + ".3gp";

		mRecorder.setOutputFile(mAudioFilepath);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	/** Create a File for saving an image or video */
	private File getOutputMediaFile(int type) {

		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			// We can't read or write to the media, so return null
			return null;
		}

		// For the save to sd card info:
		// http://stackoverflow.com/a/3551906/1601953
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/suspent/images");
		dir.mkdirs();
		File mediaFile;
		mImageFilepath = dir + "/IMG_" + mUniqueStamp + ".jpg";

		if (type == MEDIA_TYPE_IMAGE) {
			// Create a media file name
			mediaFile = new File(mImageFilepath);
		} else {
			return null;
		}
		return mediaFile;
	}

	public class MyCountdown extends CountDownTimer {
		public MyCountdown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			mCamera.setPreviewCallback(null);
			mCamera.takePicture(shutterCallback, null, mPicture);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mCaptureButton.setText("" + millisUntilFinished / 1000);
			// some script here
		}
	}

	class MyRecorder extends MediaRecorder implements MediaRecorder.OnInfoListener {

		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			switch (what) {
			case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
				stopRecording();
				final Intent intent = new Intent(getApplicationContext(), SuspentReview.class);
				intent.putExtra(IMAGE_FILEPATH_KEY, mImageFilepath);
				intent.putExtra(AUDIO_FILEPATH_KEY, mAudioFilepath);
				startActivity(intent);
				mCaptureButton.setClickable(true);
				break;
			case MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
			case MEDIA_RECORDER_INFO_UNKNOWN:
				break;
			default:
				break;
			}
		}
	}

	private final ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// AudioManager mgr = (AudioManager)
			// getSystemService(Context.AUDIO_SERVICE);
			// mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
		}
	};
}
