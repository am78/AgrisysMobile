package com.anteboth.agrisys.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class FileUploader {

	private static final String TAG = "FileUploader";

	public void upload(byte[] data, String id, String baseUrl, String uploadUrl, Context context) 
	throws Exception {
			
		//String baseUri = "https://android-gae-http-test.appspot.com";
		//"/authenticated/get

	    try {
	    	//get the upload URL
	    	AndroidHttpClient client = AndroidHttpClient.newInstance( "IntegrationTestAgent");//, context);
	    	String url = baseUrl + "/service/imageUploadUrl";
	    	HttpGet get = new HttpGet(url);
	    	
	        // Execute HTTP Post Request
	    	boolean isDevMode = baseUrl.contains("192.168.178");
	    	HttpResponse response = null;
	    	if (!isDevMode) {
	    		HttpContext httpContext = AuthenticatedAppEngineContext.newInstance(context, baseUrl);
	    		response = client.execute(get, httpContext);
	    	} else {
	    		response = client.execute(get);
	    	}
	    	String theUrl = EntityUtils.toString(response.getEntity());

	    	
	    	//perform the upload via HTTP POST
	    	HttpPost post = new HttpPost(theUrl);

	    	ByteArrayBody bab = new ByteArrayBody(data, "image");
	    	MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    	//add the "refId" value
	    	reqEntity.addPart(new FormBodyPart("refId", new StringBody(id)));
	    	//add the binary data for value "myFile"
	        reqEntity.addPart("myFile", bab);
	        post.setEntity(reqEntity);
	    	
	        
	        // Execute HTTP Post Request
	        response = client.execute(post);
	        System.out.println("POST reponse: " + response.getStatusLine());
	        
	    } catch (ClientProtocolException e) {
//	    	Log.w(TAG, e.getLocalizedMessage());
	    	throw e;
	    } catch (IOException e) {
//	    	Log.w(TAG, e.getLocalizedMessage());
	    	throw e;
	    }
	}

}
