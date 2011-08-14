package com.anteboth.agrisys.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class App extends Activity {

//	private static final String BASE_URL = "http://192.168.178.29:8888";
	private static final String BASE_URL = "http://agri-sys.appspot.com";
	private static final String LOGIN_URL = BASE_URL + "/mobile/loginstatus.jsp";

	public static final String APP_URL = "file:///android_asset/www/mobile/index.html";
	private static final String JS_INTERFACE_NAME = "AGRISYS";
	private static final String LOGGED_IN_KEY = "logged_in";
	private WebView appView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);        

		appView = (WebView) findViewById(R.id.webview);

		appView.setWebChromeClient(new MyWebChromeClient());
		appView.setWebViewClient(new MyWebViewClient());
		appView.addJavascriptInterface(new MyJSClient(), JS_INTERFACE_NAME);
		appView.getSettings().setJavaScriptEnabled(true);
		appView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		appView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		appView.getSettings().setAppCacheEnabled(false);
		appView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		//load the login url
		appView.loadUrl(LOGIN_URL);
		
//		Intent intent = new Intent(App.this, CameraPreview.class);
//        startActivity(intent);
	}

	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			Log.d("App", message);
			
			Toast.makeText(
					getBaseContext(), 
					message, 
					Toast.LENGTH_LONG).show();
			
			result.confirm();
			return true;
		}
	}

	//listen on AGRISYS.loginStatus(...)
	class MyJSClient {
		public void loginStatus(String status) {
			Log.d("App", "Login Status: " + status);
			if (status != null && status.equals(LOGGED_IN_KEY)) {
				loggedIn();
			}
		}
		
		public void takePicture(String id, String uploadUrl) {
			onTakePicture(id, uploadUrl);
		}
		
		public void showBusyIndicator() {
			showProgressDialog();
		}

		public void hideBusyIndicator() {
			hideProgressDialog();
		}
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//don't reload the current page when the orientation is changed
		super.onConfigurationChanged(newConfig);
	} 

	public void onTakePicture(String id, String uploadUrl) {
		//set required parameters which will be needed to perform a later image upload
		CameraPreview.baseUrl = BASE_URL;
		CameraPreview.uploadUrl = uploadUrl;
		CameraPreview.refId = id;
		
		//take the picture
		handler.sendEmptyMessage(TAKE_PICTURE);
	}
	public void loggedIn() {
		//load app url after logged in
		handler.sendEmptyMessage(LOAD_APP_URL);
	}
	public void showProgressDialog() {
		handler.sendEmptyMessage(SHOW_PROGRESS);
	}
	public void hideProgressDialog() {
		handler.sendEmptyMessage(HIDE_PROGRESS);
	}
	
	private int progressCounter = 0;
	private ProgressDialog progressDialog;
	private static final int LOAD_APP_URL = 0;
	private static final int SHOW_PROGRESS = 1;
	private static final int HIDE_PROGRESS = 2;
	private static final int TAKE_PICTURE = 3;
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
				case LOAD_APP_URL:
					/* LOAD APP_URL*/
					appView.loadUrl(APP_URL);
					break;
				case SHOW_PROGRESS:
					progressCounter++;
					if (progressDialog == null) {
						progressDialog = ProgressDialog.show(App.this, "", 
	                        "Bitte warten...", true);
					} else {
						if (!progressDialog.isShowing()) {
							progressDialog.show();
						}
					}
					
					break;
				case HIDE_PROGRESS:
					progressCounter --;
					if (progressDialog != null && progressCounter == 0) {
						progressDialog.dismiss();
					}
					break;
				case TAKE_PICTURE:
					onTakePicture();
			        break;
			}
		}
	};
	
	protected void onTakePicture() {
		Intent intent = new Intent(App.this, CameraPreview.class);
		startActivity(intent);
	}

	/**
	 * when back pressed, go back in web view
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && appView.canGoBack()) {
	        appView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}


	boolean loadingFinished = true;
	boolean redirect = false;

	class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
			if (!loadingFinished) {
				redirect = true;
			}
			loadingFinished = false;
			view.loadUrl(urlNewString);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			loadingFinished = false;
			//SHOW LOADING IF IT ISNT ALREADY VISIBLE  
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d("App", "page loaded: " + url);
			if(!redirect){
				loadingFinished = true;
			}

			if(loadingFinished && !redirect){
				//HIDE LOADING IT HAS FINISHED
			} else{
				redirect = false; 
			}
		}
	}
}