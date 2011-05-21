package com.anteboth.agrisys.mobile;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class ImageHelper {
	
	
	/**
	 * Returns the image bytes of a downscaled image instances from the source image.
	 * Aspect ration will be preserved.
	 * Returning image is converted to JPEG and compressed to qualitiy 70 jpeg image.
	 * @param data the source image data
	 * @param maxWidth the max width of the down scaled image
	 * @return downscaled and compressed jpeg image bytes
	 */
	public static byte[] getDownScaledAndCompressedJpegImage(byte[] data, int maxWidth) {
		//create bitmap of source image data
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		bitmap.getWidth();
		bitmap.getHeight();
	
		//resize image so a smaller image
		int dstWidth = Math.min(maxWidth, bitmap.getWidth());
		double scaleFactor = (double) bitmap.getWidth() / (double)dstWidth;
		int dstHeight = (int) (bitmap.getHeight() / scaleFactor); 
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
		scaledBitmap.getWidth();
		scaledBitmap.getHeight();
		
		//compress scaledImage and store to byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		scaledBitmap.compress(CompressFormat.JPEG, 70, baos);
		data = baos.toByteArray();
		return data;
	}
}