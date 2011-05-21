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
			Worker w = new Worker(data);
			Thread t = new Thread(w);
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
//				FileOutputStream outStream = null;
//				String fName = "tmpImageFile.jpeg";
//				// write to local sandbox file system
//				outStream = CameraPreview.this.openFileOutput(fName, MODE_WORLD_READABLE);
//				// Or write to sdcard
//				//outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
//				outStream.write(data);
//				outStream.close();
				
				String id = refId;
				new FileUploader().upload(data, id, baseUrl, uploadUrl, getApplicationContext());
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
				
				//go back and close image preview
				closePreview();
			} catch (Exception e) {
				e.printStackTrace();
				//Log.e(TAG, e.getLocalizedMessage());
				Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
			} finally {
			}
		}
		
	}
	

	protected void closePreview() {
		//TODO close preview
	}

}
