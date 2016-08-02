package com.talusan.pregnancytestkitreader.Analyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.talusan.pregnancytestkitreader.R;
import com.talusan.pregnancytestkitreader.Utils.Constants;
import com.talusan.pregnancytestkitreader.Utils.Utilities;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by talusan on 7/27/2016.
 */
public class Processor extends AsyncTask<String, Void, Bitmap> {
	private static final String TAG = "Processor";
//	private final WeakReference<ImageView> imageViewReference;
	private Context context;
	private SharedPreferences editor;
	private String imagePath = "";
	private File file;

	private Mat matCopyOfOriginalImage;
	private Mat matCopyOfMatToBeProcessed;
	private Mat matCroppedToBeProcessed;
	private Scalar mBlack = new Scalar(0, 0, 0, 0);

	private Bitmap outputImage;

	private int roiHeight;
	private int roiWidth;
	private int originalImageHeight;
	private int originalImageWidth;

	private boolean isPortrait;

	private int result = 0;

	public Processor(Context context) { //, ImageView imageView) {
		this.context = context;
//		imageViewReference = new WeakReference<>(imageView);
		editor = context.getSharedPreferences(
				context.getResources().getString(R.string.app_name),
				Context.MODE_PRIVATE);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(String... strings) {
		imagePath = strings[0];
		Log.d(TAG, "doInBackground");
		if (imagePath.isEmpty()) {
			return null;
		} else {
			file = new File(imagePath);

			Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);

			//TODO: May have to use this instead if i encounter outOfMemoryExceptions
//			Bitmap imageBitmap = Utilities.decodeSampledBitmapFromResource(
//					this.imagePath,
//					Constants.PROCESSING_RESIZE,
//					Constants.PROCESSING_RESIZE);

			originalImageHeight = imageBitmap.getHeight();
			originalImageWidth = imageBitmap.getWidth();

			isPortrait = originalImageHeight > originalImageWidth;

			matCopyOfOriginalImage = new Mat(originalImageHeight, originalImageWidth, CvType.CV_8UC4, new Scalar(4));
			Utils.bitmapToMat(imageBitmap, matCopyOfOriginalImage);

			matCroppedToBeProcessed = new Mat(originalImageHeight, originalImageWidth, CvType.CV_8UC4, new Scalar(4));
			matCopyOfOriginalImage.copyTo(matCroppedToBeProcessed);

			roiHeight = matCroppedToBeProcessed.rows();
			roiWidth = matCroppedToBeProcessed.cols();

			Log.d(TAG, roiWidth + "," + roiHeight);

			matCopyOfMatToBeProcessed = new Mat(roiHeight, roiWidth, CvType.CV_8UC4);
			Mat matBlack = new Mat(roiHeight, roiWidth, CvType.CV_8UC1);
			Mat matGray = new Mat(roiHeight, roiWidth, CvType.CV_8UC1, new Scalar(255));
			Mat matGrayBlurred = new Mat(roiHeight, roiWidth, CvType.CV_8UC1);

			matBlack.setTo(mBlack);
			matCroppedToBeProcessed.copyTo(matCopyOfMatToBeProcessed);
			Imgproc.cvtColor(matCopyOfMatToBeProcessed, matGray, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(matGray, matGrayBlurred, new Size(7, 7), 8);

			ArrayList<Integer> foundIndices = ImageProcessingAlgorithm.findListOfIndicesFromMat(
					matGrayBlurred,
					isPortrait);
			result = foundIndices.size();

			drawBoxesOnTheResults(matBlack, foundIndices, Constants.RECT_FILLED);
			matCopyOfMatToBeProcessed.copyTo(matBlack, matBlack);
			drawBoxesOnTheResults(matCopyOfMatToBeProcessed, foundIndices, Constants.RECT_NOT_FILLED);

			outputImage = Bitmap.createBitmap(roiWidth, roiHeight, Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(matCopyOfMatToBeProcessed, outputImage);
			return outputImage;
		}
	}

	private void drawBoxesOnTheResults(Mat m, ArrayList<Integer> indices, int thickness) {
		if(!isPortrait) {
			for (int i = 0; i < indices.size(); ++i) {
				Core.rectangle(m,
						new Point(indices.get(i), 0),
						new Point(indices.get(i) + Constants.STRIP_THICKNESS, originalImageHeight),
						new Scalar(0, 0, 0, 255),
						thickness);
			}
		} else {
			for (int i = 0; i < indices.size(); ++i) {
				Core.rectangle(m,
						new Point(0, indices.get(i)),
						new Point(originalImageWidth, indices.get(i) + Constants.STRIP_THICKNESS),
						new Scalar(0, 0, 0, 255),
						thickness);
			}
		}
	}

	@Override
	protected void onPostExecute(Bitmap b) {
//		if (null != imageViewReference && null != b) {
//			final ImageView imageView = imageViewReference.get();
//			if (null != imageView) {
//				Log.d(TAG, "onPostExecute: imageview is not null");
//				imageView.setImageBitmap(b);
//			}
//		} else {
//			Log.d(TAG, "onPostExecute: imageview is null");
//		}
		this.imagePath = "";
	}

	public int getResult() {
		return result;
	}

	public Bitmap getBitmap() {
		return outputImage;
	}
}
