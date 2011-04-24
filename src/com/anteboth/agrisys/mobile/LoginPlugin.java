package com.anteboth.agrisys.mobile;

import org.json.JSONArray;

import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class LoginPlugin extends Plugin {

	public static final String ACTION_LOGGED_IN = "loggedIn";
	public static final String ACTION_LOGGED_OUT = "loggedOut";
	
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {	
		System.out.println("Plugin Called");
		PluginResult result = null;

		if (ACTION_LOGGED_IN.equals(action)) {
			System.out.println("logged in!");
			webView.loadUrl(App.APP_URL);
			result = new PluginResult(Status.OK);
		}
		else if (ACTION_LOGGED_OUT.equals(action)) {
			System.out.println("logged out!");
			result = new PluginResult(Status.OK);
		}
		return result;
	}

}
