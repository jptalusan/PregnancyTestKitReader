package com.talusan.pregnancytestkitreader.Analyzer;

/**
 * Created by talusan on 7/27/2016.
 */
public class Pixel {
	double pixelValue;
	int index;
	boolean isLocalMinima;

	public Pixel(int index, double pixelValue, boolean isLocalMinima) {
		this.index = index;
		this.pixelValue = pixelValue;
		this.isLocalMinima = isLocalMinima;
	}
}
