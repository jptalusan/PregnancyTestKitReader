package com.talusan.pregnancytestkitreader.Analyzer;

import android.util.Log;

import com.talusan.pregnancytestkitreader.Utils.Constants;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by talusan on 7/27/2016.
 */
public class ImageProcessingAlgorithm {
	private final static String TAG = "IPA";

	public static ArrayList<Integer> findListOfIndicesFromMat(Mat inputMatrix, boolean isPortrait) {
		ArrayList<Double> basePixelLine = getPixelLineFromMat(inputMatrix, isPortrait);

		ArrayList<Double> smoothedPixelLine =
				performMovingAverage(basePixelLine,
						Constants.MOVING_AVERAGE,
						Constants.NUMBER_OF_PASSES);

		ArrayList<PixelDerivative> pixelDxList = getDerivativeAndAssignToPixelDx(smoothedPixelLine);

		getMinimaFromPixelDxList(pixelDxList, Constants.VALIDITY_THRESHOLD);

		Collections.sort(pixelDxList, new PixelDerivative.PixelDerivativeComparator());

		ArrayList<Integer> foundIndices = new ArrayList<>();
		boolean firstIndexFound = false;

		for (int i = 0; i < pixelDxList.size(); ++i) {
			boolean isCurrentPixelDxALocalMinima = pixelDxList.get(i).isLocalMinima;
			int currentPixelDxIndex = pixelDxList.get(i).index;
			boolean isCurrentPixelDxAGlobalMinima = true;

			if (!firstIndexFound) {
				if (isCurrentPixelDxALocalMinima) {
					foundIndices.add(currentPixelDxIndex);
				}
				firstIndexFound = true;
			} else {
				if (isCurrentPixelDxALocalMinima) {
					for (int j = 0; j < foundIndices.size(); ++j) {
						if ((currentPixelDxIndex < (foundIndices.get(j) + Constants.STRIP_THICKNESS)) &&
								(currentPixelDxIndex > (foundIndices.get(j) - Constants.STRIP_THICKNESS))) {
							isCurrentPixelDxAGlobalMinima = false;
						}
					}
					if (isCurrentPixelDxAGlobalMinima) {
						foundIndices.add(currentPixelDxIndex);
					}
				}
			}
		}

		Log.d(TAG, "foundIndices: " + foundIndices.size());
		for(int i : foundIndices)
			Log.d(TAG, i + "");

		return foundIndices;
	}

	private static void getMinimaFromPixelDxList(
			ArrayList<PixelDerivative> pixelDxList,
			double pixelDxAverage) {
		for (int i = 1; i < pixelDxList.size() - 1; ++i) {
			if (pixelDxList.get(i).pixelDxValue < pixelDxList.get(i - 1).pixelDxValue &&
					pixelDxList.get(i).pixelDxValue < pixelDxList.get(i + 1).pixelDxValue &&
					pixelDxList.get(i).pixelDxValue < pixelDxAverage) {
				pixelDxList.get(i).isLocalMinima = true;
			}
		}
	}

	private static ArrayList<PixelDerivative> getDerivativeAndAssignToPixelDx(
			ArrayList<Double> smoothedLine) {
		ArrayList<PixelDerivative> output = new ArrayList<PixelDerivative>();
		output.add(new PixelDerivative(0, 0, false)); //start

		for (int i = 1; i < smoothedLine.size(); ++i) {
			output.add(new PixelDerivative(
					i,
					smoothedLine.get(i) - smoothedLine.get(i - 1),
					false));
		}
		output.add(new PixelDerivative(smoothedLine.size(), 0, false)); //end
		return output;
	}

	private static ArrayList<Double> performMovingAverage(
			ArrayList<Double> pixelLine, int window, int runs) {
		ArrayList<Double> feedback = pixelLine;
		double temp = 0.0;
		Log.d(TAG, "performMovingAverage: " + pixelLine.get(5));
		for (int i = window / 2; i < pixelLine.size() - (window / 2); ++i) {
			for (int j = -(window / 2); j <= (window / 2); ++j) {
				temp += pixelLine.get(i + j);
			}
			feedback.set(i, temp / window);
			temp = 0.0;
		}

		if (0 < runs) {
			feedback = performMovingAverage(feedback, window, --runs);
		}
		return feedback;
	}

	private static ArrayList<Double> getPixelLineFromMat(Mat inputMatrix, boolean isPortrait) {
		Log.d(TAG, "getPixelLineFromMat: " + isPortrait);
		if (null == inputMatrix) throw new NullPointerException("InputMatrix is null");

		ArrayList<Double> pixelValues = new ArrayList<>();
		int cols = inputMatrix.cols(); //width
		int rows = inputMatrix.rows(); //height

		if (isPortrait) {
			for (int i = Constants.OFFSET; i < (rows - Constants.OFFSET); ++i) {
				pixelValues.add(inputMatrix.get(i, cols / 2)[0]);
			}
		} else {
			for (int i = Constants.OFFSET; i < (cols - Constants.OFFSET); ++i) {
				pixelValues.add(inputMatrix.get(rows / 2, i)[0]);
			}
		}
		return pixelValues;
	}
}
