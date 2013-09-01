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
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CameraActivity extends Activity {

	protected static final String TAG = "Camera Capture";
	public static final String IMAGE_FILEPATH_KEY = "image_filepath";
	public static final String AUDIO_FILEPATH_KEY = "audio_filepath";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int AUDIO_LENGTH = 7000;
	public static final int WAIT_DURATION_BEFORE_PICTURE = 5000;

	private Camera mCamera;
	private CameraPreview mPreview;
	private Button captureButton;
	private TextView mTextField;

	private String uniqueStamp;
	private String imageFilepath;
	private String audioFilepath;

	private static final String LOG_TAG = "AudioRecordTest";
	private MyRecorder mRecorder = null;
	MyCountdown timerCount;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_activity);

		mTextField = (TextView) findViewById(R.id.countdown_clock);
		// LinearLayout ll = new LinearLayout(this);
		// mRecordButton = new RecordButton(this);
		// ll.addView(mRecordButton, new
		// LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT, 0));
		// mPlayButton = new PlayButton(this);
		// ll.addView(mPlayButton, new
		// LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT, 0));
		// setContentView(ll);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		timerCount =  new MyCountdown(WAIT_DURATION_BEFORE_PICTURE, 1000);
		// Add a listener to the Capture button
		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				captureButton.setClickable(false);
				startRecording();
				timerCount.start();
			}
		});
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
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
		imageFilepath = dir + "/IMG_" + uniqueStamp + ".jpg";

		if (type == MEDIA_TYPE_IMAGE) {
			// Create a media file name
			mediaFile = new File(imageFilepath);
		} else {
			return null;
		}
		return mediaFile;
	}

	
	
	
	// / Begin recording part

	
	
	
	public class MyCountdown extends CountDownTimer {
		public MyCountdown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onFinish() {
			mCamera.setPreviewCallback(null);
			mCamera.takePicture(null, null, mPicture);
		}
		@Override
		public void onTick(long millisUntilFinished) {
//			mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
			mTextField.setText("" + millisUntilFinished / 1000);
			// some script here
		}
	}

	private void startRecording() {
		mRecorder = new MyRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setMaxDuration(AUDIO_LENGTH);
		mRecorder.setOnInfoListener(mRecorder);
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/suspent/audio");
		dir.mkdirs();
		uniqueStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		audioFilepath = dir.getAbsolutePath() + "/AUD_" + uniqueStamp + ".3gp";

		mRecorder.setOutputFile(audioFilepath);
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

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		// if (mPlayer != null) {
		// mPlayer.release();
		// mPlayer = null;
		// }
	}

	class MyRecorder extends MediaRecorder implements MediaRecorder.OnInfoListener {
		
		
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			switch (what) {
			case MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
				stopRecording();
				final Intent intent = new Intent(getApplicationContext(), SuspentReview.class);
				intent.putExtra(IMAGE_FILEPATH_KEY, imageFilepath);
				intent.putExtra(AUDIO_FILEPATH_KEY, audioFilepath);
				startActivity(intent);
				captureButton.setClickable(true);
				// ctodo change screen
				break;
			case MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
			case MEDIA_RECORDER_INFO_UNKNOWN:
				break;
			default:
				break;
			}
		}
	}

	// private static String mFileName = null;
	//
	// private CaptureButton mRecordButton = null;
	//
	// private PlayButton mPlayButton = null;
	// private MediaPlayer mPlayer = null;

	// private void onRecord(boolean start) {
	// if (start) {
	// startRecording();
	// } else {
	// stopRecording();
	// }
	// }

	// class CaptureButton extends Button {
	// boolean mStartRecording = true;
	//
	// OnClickListener clicker = new OnClickListener() {
	// public void onClick(View v) {
	// onRecord(mStartRecording);
	// // if (mStartRecording) {
	// // setText("Stop recording");
	// // } else {
	// // setText("Start recording");
	// // }
	// mStartRecording = !mStartRecording;
	// }
	// };
	//
	// public CaptureButton(Context context) {
	// super(context);
	// setText("Start recording");
	// setOnClickListener(clicker);
	// }
	// }

	// private void onPlay(boolean start) {
	// if (start) {
	// startPlaying();
	// } else {
	// stopPlaying();
	// }
	// }

	// private void startPlaying() {
	// mPlayer = new MediaPlayer();
	// try {
	// mPlayer.setDataSource(mFileName);
	// mPlayer.prepare();
	// mPlayer.start();
	// } catch (IOException e) {
	// Log.e(LOG_TAG, "prepare() failed");
	// }
	// }

	// private void stopPlaying() {
	// mPlayer.release();
	// mPlayer = null;
	// }

	// class PlayButton extends Button {
	// boolean mStartPlaying = true;
	//
	// OnClickListener clicker = new OnClickListener() {
	// public void onClick(View v) {
	// onPlay(mStartPlaying);
	// if (mStartPlaying) {
	// setText("Stop playing");
	// } else {
	// setText("Start playing");
	// }
	// mStartPlaying = !mStartPlaying;
	// }
	// };
	//
	// public PlayButton(Context ctx) {
	// super(ctx);
	// setText("Start playing");
	// setOnClickListener(clicker);
	// }
	// }

}
