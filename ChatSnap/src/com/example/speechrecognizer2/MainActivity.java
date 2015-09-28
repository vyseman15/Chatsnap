package com.example.speechrecognizer2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

public class MainActivity extends Activity
{

	private SpeechRecognizer sr;
	private static final String TAG = "ChatSnap";
	boolean killCommanded = false;

	TextView responseText;

	FrameLayout frameLayout;

	private Camera cameraObject;
	private ShowCamera showCamera;


	protected static final int MEDIA_TYPE_IMAGE = 0;
	private static final String LOG_TAG = null;
	
	public static final int RESULT_GALLERY = 0;

	// commands
	private static final String[] VALID_COMMANDS = { 
			
			"picture", // 0
			"flash", // 1
			"disable", // 2
			"gallery",	//3
			"exit" // 4
	};
	private static final int VALID_COMMANDS_SIZE = VALID_COMMANDS.length;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main2);

		
		showCamera = new ShowCamera(this, (SurfaceView)findViewById(R.id.surfaceView));
		showCamera.setCameraFirst(cameraObject);
		showCamera.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((FrameLayout) findViewById(R.id.camera_preview)).addView(showCamera);
		showCamera.setKeepScreenOn(true);
		
		RelativeLayout relativeLayoutControls = (RelativeLayout) findViewById(R.id.controls_layout);
		relativeLayoutControls.bringToFront();

		sr = SpeechRecognizer.createSpeechRecognizer(this);
		sr.setRecognitionListener(new listener());

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				"com.example.speechrecognizer2");

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);
		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		
		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		
		getSharedPreferences("PREFERENCE", MODE_PRIVATE)
		.edit()
		.putBoolean("firstrun", false)
		.commit();
		
        if (firstrun){
        	new AlertDialog.Builder(this)
			.setTitle("Welcome!") //set the Title text
			.setIcon(R.drawable.ic_launcher) //Set the picture in the top left of the popup
			.setMessage("Hello and welcome to ChatSnap, the voice controlled camera! \n" +
			" To use ChatSnap, you say 'picture' to take a picture, 'flash' to enable flash, " +
			"'flash off' to disable flash, 'gallery' to access the gallery ")
			.setNeutralButton("OK", null).show(); //Sets the button type
			
		}
		
		
		sr.startListening(intent);

		Log.i("111111", "11111111");

	}


	
	private String getResponse(int command) {

		switch (command) {
		
		case 0:
			Log.d(TAG, "case 0");
			cameraObject.takePicture(shutterCallback, null, capturedIt);
			Log.d(TAG, "voice take");
			//cameraObject.takePicture(null, null, capturedIt);
			cameraObject.startPreview();
			break;

		case 1:
			Log.d(TAG, "case 1");
			Toast.makeText(getApplicationContext(), "Flash Enabled",
					Toast.LENGTH_SHORT).show();
			showCamera.setCameraFlash(cameraObject);
			break;

		case 2:
			Toast.makeText(getApplicationContext(), "Flash Disabled",
					Toast.LENGTH_SHORT).show();
			showCamera.setCameraFirst(cameraObject);
			break;

		case 3:
			Intent galleryIntent = new Intent(
	                Intent.ACTION_PICK,
	                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(galleryIntent , RESULT_GALLERY );
			break;
			
		case 4:
			killCommanded = true;
			break;

		default:
			break;
		}
		return null;
	}

	private void processCommand(ArrayList<String> matchStrings) {
		String response = "I'm sorry, Dave. I'm afraid I can't do that.";
		int maxStrings = matchStrings.size();
		boolean resultFound = false;
		Log.d(TAG, "for");
		for (int i = 0; i < VALID_COMMANDS_SIZE && !resultFound; i++) {
			for (int j = 0; j < maxStrings && !resultFound; j++) {
				if (StringUtils.getLevenshteinDistance(matchStrings.get(j),
						VALID_COMMANDS[i]) < (VALID_COMMANDS[i].length() / 3)) {
					response = getResponse(i);
					Log.d(TAG, "leven res");
					break;
				}
			}
			
		}

	}

	class listener implements RecognitionListener {
		public void onReadyForSpeech(Bundle params) {
			// Log.d(TAG, "onReadyForSpeech");
		}

		public void onBeginningOfSpeech() {
			// Log.d(TAG, "onBeginningOfSpeech");
		}

		public void onRmsChanged(float rmsdB) {
			// Log.d(TAG, "onRmsChanged");
		}

		public void onBufferReceived(byte[] buffer) {
			// Log.d(TAG, "onBufferReceived");
		}

		public void onEndOfSpeech() {
			// Log.d(TAG, "onEndofSpeech");
		}

		public void onError(int error) {
			Log.d(TAG, "error " + error);
			// mText.setText("error " + error);

			// if critical error then exit
			if (error == SpeechRecognizer.ERROR_CLIENT
					|| error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
				Log.d(TAG, "client error");
			}
			// else ask to repeats
			else {
				Log.d(TAG, "other error");
				sr.startListening(new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
			}
		}

		public void onResults(Bundle results) {
			Log.d(TAG, "on results");
			ArrayList<String> matches = null;
			if (results != null) {
				matches = results
						.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				if (matches != null) {
					Log.d(TAG, "results are " + matches.toString());
					final ArrayList<String> matchesStrings = matches;
					processCommand(matchesStrings);
					if (!killCommanded) {
						sr.startListening(new Intent(
								RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
						Log.d(TAG, "on results inner ");
					} else
						finish();

				}
			}
		}

		public void onPartialResults(Bundle partialResults) {
			// Log.d(TAG, "onPartialResults");
		}

		public void onEvent(int eventType, Bundle params) {
			// Log.d(TAG, "onEvent " + eventType);
		}
	}

	public Camera isCameraAvailable() {
		Camera object = null;
		try {
			object = Camera.open();
			Log.d(MainActivity.LOG_TAG, "getCameraInstance()open:: " + object);
		} catch (Exception e) {
			object.release();
		}
		return object;
	}
	
	private PictureCallback capturedIt = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);

			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (bitmap == null) {
				Toast.makeText(getApplicationContext(), "not taken",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "taken",
						Toast.LENGTH_SHORT).show();
			}

			//cameraObject.startPreview();
			Log.d(TAG, "onPictureTaken");
			resetCam();
		}
	};

	class SavePhotoTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(byte[]... data) {

			File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (picFile == null) {
				Log.e(TAG,
						"Couldn't create media file; check storage permissions?");
				return null;
			}

			byte[] photoData = data[0];
			try {
				FileOutputStream fos = new FileOutputStream(picFile);
				fos.write(photoData);
				fos.flush();
				fos.close();

			} catch (FileNotFoundException e) {
				Log.e(TAG, "File not found: " + e.getMessage());
				e.getStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "I/O error writing file: " + e.getMessage());
				e.getStackTrace();
			}

			refreshGallery(picFile);
			 //cameraObject.release();
			return null;
		}

	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		sendBroadcast(mediaScanIntent);
	}

	
	public void snapIt(View view) {
		cameraObject.takePicture(shutterCallback, null, capturedIt);
		//cameraObject.takePicture(null, null, capturedIt);
		cameraObject.startPreview();

	}

	
	public void galleryIntent(View view){
		Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent , RESULT_GALLERY );
	}

	public void helpDialog(View view){
    	new AlertDialog.Builder(this)
		.setTitle("Welcome!") //set the Title text
		.setIcon(R.drawable.ic_launcher) //Set the picture in the top left of the popup
		.setMessage(
		" To use ChatSnap, you say 'picture' to take a picture, 'flash' to enable flash, " +
		"'flash off' to disable flash, 'gallery' to access the gallery ")
		.setNeutralButton("OK", null).show(); //Sets the button type
	}
	
	private File getOutputMediaFile(int type) {
		File dir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				getPackageName());

		
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log.e(TAG, "Failed to create storage directory.");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US)
				.format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {

			return new File(dir.getPath() + File.separator + "IMG_" + timeStamp
					+ ".jpg");

		} else {
			return null;
		}

	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	@Override
	protected void onPause() {
		Log.d(TAG, "on pause");
		if (cameraObject != null) {
			cameraObject.stopPreview();
			Log.d(TAG, "on pause stopPre");
			showCamera.setCameraDefaults(null);
			Log.d(TAG, "on pause null");
			showCamera.setCameraFlash(null);
			Log.d(TAG, "on pause flash null");
			cameraObject.release();
			Log.d(TAG, "on pause release");
			cameraObject = null;
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "on resume");
		if (cameraObject == null) {
			cameraObject = isCameraAvailable();
			Log.d(TAG, "resume isCameraAvailable");
			cameraObject.startPreview();
			Log.d(TAG, "resume startpreview");
			showCamera.setCameraDefaults(cameraObject);
			Log.d(TAG, "resume set");
		}
		if (sr == null)
		{
			sr = SpeechRecognizer.createSpeechRecognizer(this);
			sr.setRecognitionListener(new listener());
			
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
					"com.example.speechrecognizer2");

			intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);
			intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

			sr.startListening(intent);
		}
	}
	
	@Override
	public void onDestroy(){
		Log.d(TAG, "on destroy");
	    sr.destroy();
	    
		if (cameraObject != null) {
			cameraObject.stopPreview();
			Log.d(TAG, "on pause stopPre");
			showCamera.setCameraDefaults(null);
			Log.d(TAG, "on pause null");
			showCamera.setCameraFlash(null);
			Log.d(TAG, "on pause flash null");
			cameraObject.release();
			Log.d(TAG, "on pause release");
			cameraObject = null;
		}
	    super.onDestroy();
	}

	private void resetCam() {
		//cameraObject.release();
		Log.d(TAG, "on reset");
		cameraObject.startPreview();
		Log.d(TAG, "on reset startpreview");
		showCamera.setCameraDefaults(cameraObject);
		Log.d(TAG, "on reset set camera");
	}

}
