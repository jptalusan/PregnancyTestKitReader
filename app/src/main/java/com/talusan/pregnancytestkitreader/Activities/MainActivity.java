package com.talusan.pregnancytestkitreader.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.talusan.pregnancytestkitreader.Analyzer.Processor;
import com.talusan.pregnancytestkitreader.R;
import com.talusan.pregnancytestkitreader.Utils.Constants;
import com.talusan.pregnancytestkitreader.Utils.Utilities;
import com.theartofdev.edmodo.cropper.CropImage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String TAG = "MainActivity";
	private ImageView outputImageView;
	private ProgressDialog progressDialog;
	private TextView resultTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button selectImageButton = (Button) findViewById(R.id.selectImage);
		selectImageButton.setOnClickListener(this);

		outputImageView = (ImageView) findViewById(R.id.outputImageView);
		resultTextView = (TextView) findViewById(R.id.resultTextView);
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (status != LoaderCallbackInterface.SUCCESS) {
				super.onManagerConnected(status);
			}
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.selectImage:
				CropImage.startPickImageActivity(this);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String imagePath = "";

		if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Uri imageUri = CropImage.getPickImageResultUri(this, data);
			startCropImageActivity(imageUri);
		} else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);
			if (activityResult.isSuccessful()) {
				Uri imageUri = activityResult.getUri();
				Rect rect = activityResult.getCropRect();
				Log.d(TAG, "rect: W:" + rect.width() + " x H:" + rect.height());

				if ((rect.width()) <= Constants.OFFSET || (rect.height()) <= Constants.OFFSET) {
					Log.d(TAG, "startCropImageActivity: too small");
					Toast.makeText(MainActivity.this, "Selected rectangle is too small, please select again.", Toast.LENGTH_SHORT).show();
				} else {
					File myFile = new File(imageUri.getPath());
					imagePath = getDestinationImagePathString();
					try {
						copyFile(myFile, new File(imagePath));
					} catch (IOException e) {
						Log.e(TAG, e + "");
					}
				}

				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("Please Wait...");
				progressDialog.setTitle("Processing Image");
				progressDialog.setIndeterminate(true);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(false);
				progressDialog.setCanceledOnTouchOutside(false);

				processImage(imagePath);
			}
		}
	}

	private String getDestinationImagePathString() {
		File savePath = Utilities.generateDirectory(this);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss", Locale.ENGLISH);
		Date date = new Date();
		return savePath.toString() + File.separator + dateFormat.format(date) + ".jpg";
	}

	private void processImage(String imagePath) {
		ProcessorHelperAsyncTask processorHelper =
				new ProcessorHelperAsyncTask(getApplicationContext(), progressDialog, outputImageView);
		processorHelper.execute(imagePath);
	}

	private class ProcessorHelperAsyncTask extends Processor {
		private static final String TAG = "ProcHelperAsyncTask";
		private ProgressDialog progressDialog;

		public ProcessorHelperAsyncTask(Context context, ProgressDialog progressDialog, ImageView outputImageView) {
			super(context, outputImageView);
			this.progressDialog = progressDialog;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}

		@Override
		protected Bitmap doInBackground(String... strings) {
			super.doInBackground(strings);
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			String result = (getResult() == Constants.NUMBER_OF_LINES_FOR_POSITIVE) ? "Positive" : "Negative";
			resultTextView.setText(result);
			progressDialog.dismiss();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (null != progressDialog) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void startCropImageActivity(Uri imageUri) {
		CropImage.activity(imageUri).start(this);
	}

	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (!sourceFile.exists()) {
			return;
		}

		FileChannel source = new FileInputStream(sourceFile).getChannel();
		FileChannel destination = new FileOutputStream(destFile).getChannel();

		if (source != null) {
			destination.transferFrom(source, 0, source.size());
		}
		if (source != null) {
			source.close();
		}
		destination.close();
	}
}
