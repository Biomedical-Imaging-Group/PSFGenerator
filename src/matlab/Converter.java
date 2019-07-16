/**
 * PSFGenerator
 * 
 * Authors: Daniel Sage and Hagai Kirshner
 * Organization: Biomedical Imaging Group (BIG), Ecole Polytechnique Federale de Lausanne
 * Address: EPFL-STI-IMT-LIB, 1015 Lausanne, Switzerland
 * Information: http://bigwww.epfl.ch/algorithms/psfgenerator/
 *
 * References:
 * [1] H. Kirshner, F. Aguet, D. Sage, M. Unser
 * 3-D PSF Fitting for Fluorescence Microscopy: Implementation and Localization Application 
 * Journal of Microscopy, vol. 249, no. 1, pp. 13-25, January 2013.
 * Available at: http://bigwww.epfl.ch/publications/kirshner1301.html
 * 
 * [2] A. Griffa, N. Garin, D. Sage
 * Comparison of Deconvolution Software in 3D Microscopy: A User Point of View
 * G.I.T. Imaging & Microscopy, vol. 12, no. 1, pp. 43-45, March 2010.
 * Available at: http://bigwww.epfl.ch/publications/griffa1001.html
 *
 * Conditions of use:
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 */

/**
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of PSFGenerator.
 * 
 * PSFGenerator is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * PSFGenerator is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PSFGenerator. If not, see <http://www.gnu.org/licenses/>.
 */

package matlab;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

public class Converter {

	/**
	 * Get an image.
	 *
	 * @param imageplus
	 *            image
	 * @return an N x M array representing the input image
	 */
	public static Object get(ImagePlus imageplus) {
		if (imageplus == null)
			return null;
		int width = imageplus.getWidth();
		int height = imageplus.getHeight();
		int stackSize = imageplus.getStackSize();
		int counter = 0;
		ImageStack imagestack = imageplus.getStack();
		switch (imageplus.getType()) {

		case ImagePlus.COLOR_256: {
			;
		}
		case ImagePlus.GRAY8: {
			short[][][] is = new short[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				ByteProcessor byteprocessor = (ByteProcessor) imagestack.getProcessor(sz + 1);
				byte[] pixels = (byte[]) byteprocessor.getPixels();
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz] = (short) (pixels[counter] & 0xff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		case ImagePlus.GRAY16: {
			int[][][] is = new int[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				counter = 0;
				ShortProcessor shortprocessor = (ShortProcessor) imagestack.getProcessor(sz + 1);
				short[] spixels = (short[]) shortprocessor.getPixels();
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz] = (int) (spixels[counter] & 0xffff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		case ImagePlus.GRAY32: {
			double[][][] fs = new double[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				FloatProcessor floatprocessor = (FloatProcessor) imagestack.getProcessor(sz + 1);
				float[] fpixels = (float[]) floatprocessor.getPixels();
				counter = 0;
				int i = 0;
				while (i < height) {
					int j = 0;
					while (j < width) {
						fs[i][j][sz] = (double) fpixels[counter];
						j++;
						counter++;
					}
					counter = ++i * width;
				}
			}
			return fs;
		}
		case ImagePlus.COLOR_RGB: {
			if (stackSize == 1) {
				short[][][] is = new short[height][width][3];
				ColorProcessor colorprocessor = (ColorProcessor) imagestack.getProcessor(1);
				byte[] red = new byte[width * height];
				byte[] green = new byte[width * height];
				byte[] blue = new byte[width * height];
				colorprocessor.getRGB(red, green, blue);
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][0] = (short) (red[counter] & 0xff);
						is[h][w][1] = (short) (green[counter] & 0xff);
						is[h][w][2] = (short) (blue[counter] & 0xff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
				return is;
			}
			short[][][][] is = new short[height][width][stackSize][3];
			for (int sz = 0; sz < stackSize; sz++) {
				ColorProcessor colorprocessor = (ColorProcessor) imagestack.getProcessor(sz + 1);
				byte[] red = new byte[width * height];
				byte[] green = new byte[width * height];
				byte[] blue = new byte[width * height];
				colorprocessor.getRGB(red, green, blue);
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz][0] = (short) red[counter];
						is[h][w][sz][1] = (short) green[counter];
						is[h][w][sz][2] = (short) blue[counter];
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		default:
			System.out.println("MIJ Error message: Unknow type of volumes.");
			return null;
		}
	}
}
