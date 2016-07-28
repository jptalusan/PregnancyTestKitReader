package com.talusan.pregnancytestkitreader.Analyzer;

import java.util.Comparator;

/**
 * Created by talusan on 7/27/2016.
 */
public class PixelDerivative {
	double pixelDxValue;
	int index;
	boolean isLocalMinima;

	public PixelDerivative(int index, double pixelDxValue, boolean isLocalMinima) {
		this.index = index;
		this.pixelDxValue = pixelDxValue;
		this.isLocalMinima = isLocalMinima;
	}

	public static class PixelDerivativeComparator implements Comparator<PixelDerivative> {
		@Override
		public int compare(PixelDerivative p1, PixelDerivative p2) {
			if (p1.pixelDxValue < p2.pixelDxValue)
				return -1;
			if (p1.pixelDxValue > p2.pixelDxValue)
				return 1;
			return 0;
		}
	}
}
