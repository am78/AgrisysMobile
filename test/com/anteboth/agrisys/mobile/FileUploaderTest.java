package com.anteboth.agrisys.mobile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.test.AndroidTestCase;

public class FileUploaderTest extends AndroidTestCase {

	private static boolean DEV = true; 
	private byte[] data;
	private String id;
	private String baseUrl;
	private String uploadUrl;
	private Context context;


	protected void setUp() throws Exception {
		super.setUp();
		
		this.data = getBytesFromFile(
				super.mContext.getResources().openRawResource(R.drawable.test_img_large));

		if (DEV) {
			this.id = "504";
			this.baseUrl = "http://192.168.178.28:8888";
		} 
		else {
			this.baseUrl = "http://agri-sys.appspot.com";
			this.id = "41001";
		}
		
		this.uploadUrl = "/upload";
		this.context = super.mContext;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUpload() {
		//upload the image
		try {
			data = ImageHelper.getDownScaledAndCompressedJpegImage(data, 1000);
			FileUploader fu = new FileUploader();
			fu.upload(data, id, baseUrl, uploadUrl, context);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
	public static byte[] getBytesFromFile(InputStream is) throws IOException {
	    // Get the size of the file
	    long length = is.available();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file");
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}

}
