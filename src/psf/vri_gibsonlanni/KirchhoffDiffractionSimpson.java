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

package psf.vri_gibsonlanni;

import bilib.commons.math.bessel.Bessel;

public class KirchhoffDiffractionSimpson {

	// Gibson & Lanni parameters of the acquisition
	private VRIGLParameters	p;
	// Stopping conditions:
	// Difference between consecutive Riemann approximations.
	private double			TOL	= 1E-1;
	// The number of consecutive approximations that meet the TOL criterion
	private int				K	= 1;
	private double resLateral = 100;
	private double resAxial = 250;
	
	// Constructor
	public KirchhoffDiffractionSimpson(VRIGLParameters p, int accuracy, int z, double resLateral, double resAxial) {
		this.p = p;
		this.resLateral = resLateral;
		this.resAxial = resAxial;
		if (accuracy == 0)
			K = 5;
		else if (accuracy == 1)
			K = 7;
		else if (accuracy == 2)
			K = 9;
		else
			K = 3;
	}

	// calculate()
	// Simpson approximation for the Kirchhoff diffraction integral
	// 'r' is the radial distance of the detector relative to the optical axis.
	double calculate(double r) {

		double a = 0.0; // Lower and upper limits of the integral
		double b = Math.min(1, p.ni / p.NA); // 1.0

		// IJ.log("(p.ti0, p.ti) = (" + p.ti0 + ", " + p.ti + ")");
		int N; // number of sub-intervals
		int k; // number of consecutive successful approximations
		double del; // integration interval
		int iteration; // number of iterations.
		double curDifference; // Stopping criterion

		double realSum, imagSum, rho;
		double[] sumOddIndex = new double[2], sumEvenIndex = new double[2];
		double[] valueX0 = new double[2], valueXn = new double[2];
		double[] value = new double[2];

		double curI = 0.0, prevI = 0.0;

		// Initialization of the Simpson sum (first iteration)
		N = 2;
		del = (b - a) / 2.0;
		k = 0;
		iteration = 1;
		rho = (b - a) / 2.0;
		sumOddIndex = this.integrand(rho, r);
		sumEvenIndex[0] = 0.0;
		sumEvenIndex[1] = 0.0;

		valueX0 = this.integrand(a, r);
		valueXn = this.integrand(b, r);

		realSum = valueX0[0] + 2.0 * sumEvenIndex[0] + 4.0 * sumOddIndex[0] + valueXn[0];
		imagSum = valueX0[1] + 2.0 * sumEvenIndex[1] + 4.0 * sumOddIndex[1] + valueXn[1];
		curI = (realSum * realSum + imagSum * imagSum) * del * del;

		prevI = curI;
		curDifference = TOL;

		// Finer sampling grid until we meet the TOL value with the specified
		// number of repetitions, K
		while (k < K && iteration < 10000) {
			iteration++;
			N *= 2;
			del = del / 2;
			sumEvenIndex[0] = sumEvenIndex[0] + sumOddIndex[0];
			sumEvenIndex[1] = sumEvenIndex[1] + sumOddIndex[1];
			sumOddIndex[0] = 0.0;
			sumOddIndex[1] = 0.0;
			for (int n = 1; n < N; n = n + 2) {
				rho = n * del;
				value = this.integrand(rho, r);
				sumOddIndex[0] += value[0];
				sumOddIndex[1] += value[1];
			}
			realSum = valueX0[0] + 2.0 * sumEvenIndex[0] + 4.0 * sumOddIndex[0] + valueXn[0];
			imagSum = valueX0[1] + 2.0 * sumEvenIndex[1] + 4.0 * sumOddIndex[1] + valueXn[1];
			curI = (realSum * realSum + imagSum * imagSum) * del * del;

			// Relative error between consecutive approximations
			if (prevI == 0.0)
				curDifference = Math.abs((prevI - curI) / 1E-5);
			else
				curDifference = Math.abs((prevI - curI) / curI);

			if (curDifference <= TOL)
				k++;
			else
				k = 0;

			prevI = curI;
		}

		return curI;
	}

	double[] integrand(double rho, double r) {

		// 'rho' is the integration parameter.
		// 'r' is the radial distance of the detector relative to the optical
		// axis.
		// NA is assumed to be less than 1.0, i.e. it assumed to be already
		// normalized by the refractive index of the immersion layer, ni.
		// The return value is a complex number.

		double k0 = 2 * Math.PI / p.lambda;
		double BesselValue = Bessel.J0(k0 * p.NA * r * rho);

		double OPD, OPD1, OPD3; // Optical path differences
		int zint = (int) Math.ceil(p.particleAxialPosition / (resAxial*1E-9));
		double[] I = new double[2];

		// OPD1 =
		// p.ns*p.particleAxialPosition*Math.sqrt(1-(p.NA*rho/p.ns)*(p.NA*rho/p.ns));
		// OPD2 = p.ng*p.tg*Math.sqrt(1-(p.NA*rho/p.ng)*(p.NA*rho/p.ng));
		// OPD3 = p.ni*p.ti*Math.sqrt(1-(p.NA*rho/p.ni)*(p.NA*rho/p.ni));
		// OPD4 = -p.ng0*p.tg0*Math.sqrt(1-(p.NA*rho/p.ng0)*(p.NA*rho/p.ng0));
		// OPD5 = -p.ni0*p.ti0*Math.sqrt(1-(p.NA*rho/p.ni0)*(p.NA*rho/p.ni0));
		// OPD = OPD1+OPD2+OPD3+OPD4+OPD5;

		// Saving some computation time
		// OPD1 =
		// p.ns1*p.particleAxialPosition*Math.sqrt(1-(p.NA*rho/p.ns1)*(p.NA*rho/p.ns1));
		// OPD3 =
		// p.ni*(p.ti-p.ti0)*Math.sqrt(1-(p.NA*rho/p.ni)*(p.NA*rho/p.ni));
		// OPD = OPD1+OPD3;

		// Modified G&L
		OPD1 = (resLateral*1E-9) * angleinterm(zint, p.ns1, p.ns2, p.NA, rho, p.type);
		OPD3 = p.ni * (p.ti - p.ti0) * Math.sqrt(1 - (p.NA * rho / p.ni) * (p.NA * rho / p.ni));
		OPD = OPD1 + OPD3;

		double W = k0 * OPD;

		// The real part
		I[0] = BesselValue * Math.cos(W) * rho;
		// The imaginary part
		I[1] = BesselValue * Math.sin(W) * rho;

		return I;
	}

	// Variantions in refractive index within the specimen
	private double angleinterm(int zin, double ns1, double ns2, double NA, double rho, int type) {
		double sumaRI = 0;
		if (type == 0) {
			// Linear
			double b = ns1;
			double a = (ns2 - b) / zin;
			for (int l = 0; l < zin; l++) {
				// Linear
				sumaRI += Math.sqrt(Math.abs(a * l + b - NA * rho));
			}
		}
		else if (type == 1) {
			// Lograthmic
			double b = Math.exp(ns1);
			double a = (Math.exp(ns2) - b) / zin;
			for (int l = 0; l < zin; l++) {
				// Logrithm
				sumaRI += Math.sqrt(Math.abs(Math.log(a * l + b) - NA * rho));
			}
		}
		else if (type == 2) {
			// Exponential
			double b = Math.log(ns1);
			double a = (Math.log(ns2) - b) / zin;
			for (int l = 0; l < zin; l++) {
				// Exponential
				sumaRI += Math.sqrt(Math.abs(Math.exp(a * l + b) - NA * rho));
			}
		}
		return sumaRI;
	}

}
