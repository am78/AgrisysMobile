/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anteboth.agrisys.mobile;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CameraPreview extends Activity {

	private static final String TAG = "CameraPreview";
	private Preview preview;
	private Button buttonClick;
	
	protected static String refId;
	protected static String baseUrl;
	protected static String uploadUrl;
	private ProgressDialog progressDialog;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camerapreview);

		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//take picture and stop preview
				preview.camera.takePicture(shutterCallback, null, jpegCallback);
			}
		});

		Log.d(TAG, "onCreate'd");
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};


	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			//display a progess dialog
			progressDialog = ProgressDialog.show(
					CameraPreview.this, "Bild-Upload", "Bitte Warten...", true);
			
			//start upload in separate thread
			Thread t = new Thread(new Worker(data));
			t.start();
		}
	};
	
	class Worker implements Runnable {
		private byte[] data;

		public Worker(byte[] data) {
			this.data = data;
		}

		@Override
		public void run() {
			Log.d(TAG, "onPictureTaken - jpeg");
			try {
				String id = refId;
				//downscale image to max width of 1000 px
				data = ImageHelper.getDownScaledAndCompressedJpegImage(data, 1000);
				//start the upload
				new FileUploader().upload(data, id, baseUrl, uploadUrl, getApplicationContext());
				//if no exception occurred, the upload was successfull
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				displayUploadNotification();
			} catch (Exception e) {
				Log.w(TAG, e.getLocalizedMessage());
				Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
			} finally {
				//close progress dialog
				progressDialog.dismiss();
				//go back and close image preview
				closePreview();
			}
		}
	}
	

	protected void closePreview() {
		//close preview activity
		this.finish();
	}


	public void displayUploadNotification() {
		Context context = getApplicationContext();
		CharSequence contentTitle = "Agrisys";
		CharSequence contentText = "Bild-Upload erfolgreich abgeschlossen.";
		Intent notificationIntent = new Intent(this, CameraPreview.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		int icon = R.drawable.icon;
		CharSequence tickerText = "Bild-Upload erfolgreich!";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	}
}
