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

package psf.defocusplane;

public class ZFunction {

	final static public int	ZFUNC_LINEAR	= 0;
	final static public int	ZFUNC_EXPO		= 1;
	final static public int	ZFUNC_PARABOLIC	= 2;
	final static public int	ZFUNC_CONSTANT	= 3;

	static public String[]	names			= new String[] { "Linear", "Exponential","Parabolic", "Constant" };

	private int				func1D			= 1;
	private double			zdefocus		= 1.0;
	private double			zfocal			= 1.0;

	public ZFunction(int func1D, double zdefocus, double zfocal) {
		this.func1D = func1D;
		this.zdefocus = zdefocus;
		this.zfocal = zfocal;
	}

	public double getDefocusFactor(double z) {
		double zf = z - zfocal;
		double za = (zf < 0 ? -zf : zf);
		switch (func1D) {
		case ZFUNC_EXPO:
			double K = 0.69314718056; // log(0.5)
			return Math.exp(-za * K / (zdefocus-zfocal));

		case ZFUNC_PARABOLIC:
			return  1.0 + (za*za) / ((zdefocus - zfocal) * (zdefocus - zfocal));

		case ZFUNC_CONSTANT:
			return 1.0;

		case ZFUNC_LINEAR:
			return 1.0 + za / Math.abs(zdefocus - zfocal);
		}
		return 1.0;
	}

	public String getName() {
		return names[func1D];
	}

	@Override
	public String toString() {
		return names[func1D] + " " + zfocal + " " + zdefocus;
	}
}
