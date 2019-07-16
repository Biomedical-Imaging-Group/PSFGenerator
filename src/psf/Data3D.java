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

package psf;

public class Data3D {

	public double[][]	data;

	public int			nx;
	public int			ny;
	public int			nz;
	private int			nxy;

	public Point3D		max		= new Point3D();
	public Point3D		fwhm	= new Point3D();

	public double		energy;

	public Data3D(int nx, int ny, int nz) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.nxy = nx * ny;
		data = new double[nz][nx * ny];
	}

	public byte[] createAsByte(int z) {
		byte[] p = new byte[nxy];
		for (int k = 0; k < nx * ny; k++) {
			double v = data[z][k] * 255;
			p[k] = (byte) (v > 255 ? 255 : (v < 0 ? 0 : v));
		}
		return p;
	}

	public short[] createAsShort(int z) {
		short[] p = new short[nxy];
		for (int k = 0; k < nx * ny; k++) {
			double v = data[z][k] * 65535;
			p[k] = (short) (v > 65535 ? 65535 : (v < 0 ? 0 : v));
		}
		return p;
	}

	public float[] createAsFloat(int z) {
		float[] p = new float[nxy];
		for (int k = 0; k < nx * ny; k++) {
			double v = data[z][k];
			p[k] = (float) (v);
		}
		return p;
	}

	public double[] createAsDouble(int z) {
		double[] p = new double[nxy];
		System.arraycopy(data[z], 0, p, 0, nxy);
		return p;
	}

	public int[] getHistogram(int nbins) {
		int histo[] = new int[nbins];
		for (int i = 0; i < nx; i++)
			for (int j = 0; j < ny; j++)
				for (int k = 0; k < nz; k++) {
					int v = (int) (data[k][i + nx * j] * nbins);
					if (v >= 0)
						histo[v]++;
				}
		return histo;
	}

	public double[] getPlane(int z) {
		return data[z];
	}

	public void setPlane(int z, double plane[]) {
		data[z] = plane;
	}

	public void putXY(int z, double plane[]) {
		System.arraycopy(plane, 0, data[z], 0, nxy);
	}

	public void getXY(int z, double plane[]) {
		System.arraycopy(data[z], 0, plane, 0, nxy);
	}

	public void determineMaximumAndEnergy() {
		max.value = -Double.MAX_VALUE;
		energy = 0;
		for (int z = 0; z < nz; z++) {
			double[] slice = ((double[]) data[z]);
			for (int k = 0; k < nxy; k++) {
				energy += slice[k] * slice[k];
				if (max.value < slice[k]) {
					max.value = slice[k];
					max.x = k % nx;
					max.y = k / nx;
					max.z = z;
				}
			}
		}
	}

	public double getMaximum(int z) {
		double max = -Double.MAX_VALUE;
		double[] slice = ((double[]) data[z]);
		for (int k = 0; k < nxy; k++)
			if (max < slice[k])
				max = slice[k];
		return max;
	}

	public void multiply(double num) {
		double[] slice;
		for (int z = 0; z < nz; z++) {
			slice = ((double[]) data[z]);
			for (int k = 0; k < nxy; k++)
				slice[k] *= num;
		}
	}

	public void clip(double lower, double upper) {
		double[] slice;
		for (int z = 0; z < nz; z++) {
			slice = ((double[]) data[z]);
			for (int k = 0; k < nxy; k++) {
				if (slice[k] > upper)
					slice[k] = upper;
				if (slice[k] < lower)
					slice[k] = lower;
			}

		}
	}

	/**
	 * Scale the intensity PSF. Scale (scale==0, linear scale, do nothing)
	 */
	public void rescale(int scale, double max) {
		int nxy = nx * ny;
		if (scale == 0) {
			for (int z = 0; z < nz; z++)
				for (int k = 0; k < nxy; k++)
					data[z][k] /= max;
		}
		else if (scale == 1) {
			for (int z = 0; z < nz; z++)
				for (int k = 0; k < nxy; k++)
					data[z][k] = Math.log(data[z][k] <= 1e-6 ? 1e-6 : data[z][k] / max);
		}
		else if (scale == 2) {
			for (int z = 0; z < nz; z++)
				for (int k = 0; k < nxy; k++)
					data[z][k] = Math.sqrt(data[z][k] <= 1e-6 ? 1e-6 : data[z][k] / max);
		}
		else if (scale == 3) {
			for (int z = 0; z < nz; z++)
				for (int k = 0; k < nxy; k++)
					data[z][k] = 20 * Math.log10(data[z][k] <= 1e-6 ? 1e-6 : data[z][k] / max);
		}
	}

	public double getNorm2() {
		double norm = 0.0;
		double[] slice;
		for (int z = 0; z < nz; z++) {
			slice = ((double[]) data[z]);
			for (int k = 0; k < nxy; k++)
				norm += slice[k] * slice[k];
		}
		return norm;
	}

	public double getNorm2(int z) {
		double norm = 0.0;
		double[] slice = ((double[]) data[z]);
		for (int k = 0; k < nxy; k++)
			norm += slice[k] * slice[k];
		return norm;
	}

	/**
	 * Compute the lateral parameters for every plane.
	 */
	public double[][] getPlaneInformation() {
		double sliceMaxValue;
		double sliceEnergy;

		double p[][] = new double[nz][4];
		for (int z = 0; z < nz; z++) {
			sliceMaxValue = getMaximum(z);
			sliceEnergy = getNorm2(z);
			p[z][0] = z;
			p[z][1] = sliceMaxValue / max.value;
			p[z][2] = sliceEnergy / energy;

			double x0 = (nx - 1) / 2.0, y0 = (ny - 1) / 2.0;
			double sum = 0.0, sigma2 = 0.0, value = 0.0;

			for (int x = 0; x < nx; x++)
			for (int y = 0; y < ny; y++) {
				value = data[z][x + nx * y];
				sum += value;
				sigma2 += value * ((x - x0) * (x - x0) + (y - y0) * (y - y0));
			}

			p[z][3] = Math.sqrt(sigma2 / sum);
		}
		return p;
	}

	// Finding the lateral FWHM parameter in units of [pixels]
	// The psf is assumed to have radial symmetry in the (x,y) plane
	// Finding the axial FWHM parameter
	// The psf is not assumed to have axial symmetry
	public void estimateFWHM() {

		double value;
		int x = 0, y=0, z=0;
		
		// X
		value = max.value;
		for(x=max.x; x<nx; x++) {
			value = data[max.z][x + nx * max.y] ;
			if (value < max.value * 0.5) {
				break;
			}
		}
		int x2 = x;
		
		value = max.value;
		for(x=max.x; x<0; x--) {
			value = data[max.z][x + nx * max.y] ;
			if (value < max.value * 0.5) {
				break;
			}
		}
		int x1 = x;

		// Y
		value = max.value;
		for(y=max.y; y<ny; y++) {
			value = data[max.z][max.x + nx * y] ;
			if (value < max.value * 0.5) {
				break;
			}
		}
		int y2 = y;
		
		value = max.value;
		for(y=max.y; y<0; y--) {
			value = data[max.z][max.x + nx * y] ;
			if (value < max.value * 0.5) {
				break;
			}
		}
		int y1 = y;

		// Z
		value = max.value;
		for(z=max.z; z<nz; z++) {
			value = data[z][max.x + nx * max.y] ;
			if (value < max.value * 0.5) {
				break;
			}
			
		}
		int z2 = z;
		
		value = max.value;
		for(z=max.z; z<0; z--) {
			value = data[z][x + nx * max.y] ;
			if (value < max.value * 0.5) {
				break;
			}
		}
		int z1 = z;
		
		fwhm.x = (x2-x1);
		fwhm.y = (y2-y1);
		fwhm.z = (z2-z1);
	
		fwhm.value = 0;
		for(z=z1; z<=Math.min(z2, nz-1); z++)
		for(x=x1; x<=Math.min(x2, nx-1); x++)
		for(y=y1; y<=Math.min(y2, ny-1); y++)
			fwhm.value += data[z][x + nx * y];
	}
}
