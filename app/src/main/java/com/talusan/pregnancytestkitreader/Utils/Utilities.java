package com.talusan.pregnancytestkitreader.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.view.Surface;
import android.view.WindowManager;

import com.talusan.pregnancytestkitreader.R;

import java.io.File;

/**
 * Created by talusan on 7/26/2016.
 */
public class Utilities {
	public static File generateDirectory(Context ctx) {
		String appname = ctx.getResources().getString(R.string.app_name);
		String pathName = Environment.getExternalStorageDirectory() + File.separator + appname + File.separator;
		File savePath = new File(pathName);
		if (!savePath.exists())
			savePath.mkdirs();
		return savePath;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		return BitmapFactory.decodeFile(path, generateResizedBitmapOptions(path, reqWidth, reqHeight));
	}

	private static BitmapFactory.Options generateResizedBitmapOptions(String path, int width, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inDither = true;
		return options;
	}
}
