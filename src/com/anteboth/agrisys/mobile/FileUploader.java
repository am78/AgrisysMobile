package com.anteboth.agrisys.mobile;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class FileUploader {

	private static final String TAG = "FileUploader";

	public void upload(byte[] data, String id, String baseUrl, String uploadUrl, Context context) 
	throws Exception {
	    try {
	    	//get the upload URL
	    	String imgUploadUrl;
	    	AndroidHttpClient client = 
	    		AndroidHttpClient.newInstance( "IntegrationTestAgent");//, context);
	    	String url = baseUrl + "/upload";
	    	HttpGet get = new HttpGet(url);
	    	HttpResponse response = null;
	    	
	        // Execute HTTP Get Request which returns the upload URL
	    	boolean isDevMode = baseUrl.contains("192.168.");
	    	if (!isDevMode) {
	    		HttpContext httpContext = AuthenticatedAppEngineContext.newInstance(context, baseUrl);
	    		response = client.execute(get, httpContext);
	    		imgUploadUrl = EntityUtils.toString(response.getEntity());
	    	} else {
	    		response = client.execute(get);
	    		//for localhost, add baseUrl as prefix
	    		imgUploadUrl = baseUrl + EntityUtils.toString(response.getEntity());
	    	}
	    	
	    	//perform the upload via HTTP POST
	    	HttpPost post = new HttpPost(imgUploadUrl);

	    	ByteArrayBody bab = new ByteArrayBody(data, "image");
	    	MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    	//add the "refId" value
	    	reqEntity.addPart(new FormBodyPart("refId", new StringBody(id)));
	    	//add the binary data for value "myFile"
	        reqEntity.addPart("image", bab);
	        post.setEntity(reqEntity);
	    	
	        
	        // Execute HTTP Post Request
	        response = client.execute(post);
	        Log.d(TAG, "POST reponse: " + response.getStatusLine());
	        
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	//an ERROR occurred
	        	Log.w(TAG, "Error during upload");
	        	throw new Exception("Error during file upload.");
	        } else {
	        	//upload completed successfully
	        	Log.d(TAG, "Upload successfull!");
	        }
	        
	    } catch (ClientProtocolException e) {
	    	Log.w(TAG, e.getLocalizedMessage());
	    	throw e;
	    } catch (IOException e) {
	    	Log.w(TAG, e.getLocalizedMessage());
	    	throw e;
	    }
	}
}
