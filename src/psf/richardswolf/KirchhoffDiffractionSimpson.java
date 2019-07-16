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

package psf.richardswolf;

import bilib.commons.math.bessel.Bessel;


/**
 * @author Hagai Kirshner
 */
public class KirchhoffDiffractionSimpson {

	// Richards & Wolf parameters of the acquisition
	private double defocus = 1;
	private double ni = 1.5;

	// Stopping conditions:
	// Difference between consecutive approximations.
	private double					TOL		= 1E-1;
	// The number of consecutive approximations that meet the TOL criterion
	private int						K;
	private double					NA		= 1.4;
	private double					lambda	= 610;

	// Constructor
	public KirchhoffDiffractionSimpson(double defocus, double ni, int accuracy, double NA, double lambda) {
		this.NA = NA;
		this.lambda = lambda;
		this.ni = ni;
		this.defocus = defocus;
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

		int N; // number of sub-intervals
		int k; // number of consecutive successful approximations
		double del; // integration interval
		int iteration; // number of iterations.
		double curDifference; // Stopping criterion

		double realSumI0, realSumI1, realSumI2;
		double imagSumI0, imagSumI1, imagSumI2;
		double rho;

		// The real part of I9 is in value[0][0], the imaginary part of I0 is in
		// value[0][1]
		// The real part of I9 is in value[1][0], the imaginary part of I0 is in
		// value[1][1]
		// The real part of I9 is in value[2][0], the imaginary part of I0 is in
		// value[2][1]
		// The same holds for the intermediate sums sumOddIndex and sumEvenIndex
		double[][] value = new double[3][2];
		double[][] valuea = new double[3][2], valueb = new double[3][2];
		double[][] sumOddIndex = new double[3][2], sumEvenIndex = new double[3][2];

		double curI = 0.0, prevI = 0.0;
		double a, b; // Integration limits

		// Initialization of the three Simpson sums for I0, I1 and I2
		a = 0.0;
		b = Math.asin(NA / ni); // This is alpha in equation (3) of the paper
		N = 2;
		del = b / 2.0;
		k = 0;
		iteration = 1;
		rho = b / 2.0;
		sumOddIndex = this.integrand(rho, r);
		for (int m = 0; m < 3; m++)
			for (int n = 0; n < 2; n++)
				sumEvenIndex[m][n] = 0.0;
		valuea = this.integrand(a, r);
		valueb = this.integrand(b, r);
		realSumI0 = valuea[0][0] + 2.0 * sumEvenIndex[0][0] + 4.0 * sumOddIndex[0][0] + valueb[0][0];
		imagSumI0 = valuea[0][1] + 2.0 * sumEvenIndex[0][1] + 4.0 * sumOddIndex[0][1] + valueb[0][1];
		realSumI1 = valuea[1][0] + 2.0 * sumEvenIndex[1][0] + 4.0 * sumOddIndex[1][0] + valueb[1][0];
		imagSumI1 = valuea[1][1] + 2.0 * sumEvenIndex[1][1] + 4.0 * sumOddIndex[1][1] + valueb[1][1];
		realSumI2 = valuea[2][0] + 2.0 * sumEvenIndex[2][0] + 4.0 * sumOddIndex[2][0] + valueb[2][0];
		imagSumI2 = valuea[2][1] + 2.0 * sumEvenIndex[2][1] + 4.0 * sumOddIndex[2][1] + valueb[2][1];
		curI = (realSumI0 * realSumI0 + imagSumI0 * imagSumI0 + 2 * (realSumI1 * realSumI1 + imagSumI1 * imagSumI1) + realSumI2 * realSumI2 + imagSumI2 * imagSumI2) * del * del;

		prevI = curI;
		curDifference = TOL;

		// Finer sampling grid until we meet the TOL value with the specified
		// number of repetitions, K
		while (k < K && iteration < 10000) {
			iteration++;
			N *= 2;
			del = del / 2;
			for (int m = 0; m < 3; m++) {
				for (int n = 0; n < 2; n++) {
					sumEvenIndex[m][n] += sumOddIndex[m][n];
					sumOddIndex[m][n] = 0.0;
				}
			}
			// sumEvenIndex[0] = sumEvenIndex[0] + sumOddIndex[0];
			// sumEvenIndex[1] = sumEvenIndex[1] + sumOddIndex[1];
			// sumOddIndex[0] = 0.0;
			// sumOddIndex[1] = 0.0;
			for (int n = 1; n < N; n = n + 2) {
				rho = n * del;
				value = this.integrand(rho, r);
				for (int mm = 0; mm < 3; mm++)
					for (int nn = 0; nn < 2; nn++)
						sumOddIndex[mm][nn] += value[mm][nn];

				// sumOddIndex[0] += value[0];
				// sumOddIndex[1] += value[1];
			}
			// realSum = valueX0[0] + 2.0*sumEvenIndex[0] + 4.0*sumOddIndex[0] +
			// valueXn[0];
			// imagSum = valueX0[1] + 2.0*sumEvenIndex[1] + 4.0*sumOddIndex[1] +
			// valueXn[1];
			// curI = (realSum*realSum+imagSum*imagSum)*del*del;
			realSumI0 = valuea[0][0] + 2.0 * sumEvenIndex[0][0] + 4.0 * sumOddIndex[0][0] + valueb[0][0];
			imagSumI0 = valuea[0][1] + 2.0 * sumEvenIndex[0][1] + 4.0 * sumOddIndex[0][1] + valueb[0][1];
			realSumI1 = valuea[1][0] + 2.0 * sumEvenIndex[1][0] + 4.0 * sumOddIndex[1][0] + valueb[1][0];
			imagSumI1 = valuea[1][1] + 2.0 * sumEvenIndex[1][1] + 4.0 * sumOddIndex[1][1] + valueb[1][1];
			realSumI2 = valuea[2][0] + 2.0 * sumEvenIndex[2][0] + 4.0 * sumOddIndex[2][0] + valueb[2][0];
			imagSumI2 = valuea[2][1] + 2.0 * sumEvenIndex[2][1] + 4.0 * sumOddIndex[2][1] + valueb[2][1];
			curI = (realSumI0 * realSumI0 + imagSumI0 * imagSumI0 + 2 * (realSumI1 * realSumI1 + imagSumI1 * imagSumI1) + realSumI2 * realSumI2 + imagSumI2 * imagSumI2) * del * del;

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

	double[][] integrand(double theta, double r) {

		// 'theta' is the integration parameter.
		// 'r' is the radial distance of the detector relative to the optical
		// axis.
		// NA is assumed to be less than 1.0, i.e. it assumed to be already
		// normalized by the refractive index of the immersion layer, ni.
		// The return value is a complex number.

		double sinTheta = Math.sin(theta);
		double cosTheta = Math.cos(theta);
		// double sinThetaSinTheta = sinTheta*sinTheta;
		double sqrtCosThetaSinTheta = Math.sqrt(cosTheta) * sinTheta;
		// double niNiSinThetaSinTheta = p.ni*p.ni*sinThetaSinTheta;

		double k_tag = (2 * Math.PI * ni / lambda);
		double x = k_tag * sinTheta * r;
		double J0 = Bessel.J0(x);
		double J1 = Bessel.J1(x);
		double J2 = (x == 0) ? 0 : 2.0 * J1 / x + J0;

		double B0 = sqrtCosThetaSinTheta * (1 + cosTheta) * J0;
		double B1 = sqrtCosThetaSinTheta * sinTheta * J1;
		double B2 = sqrtCosThetaSinTheta * (1 - cosTheta) * J2;

		double OPD = defocus * cosTheta;
		double W = k_tag * OPD;
		double cosW = Math.cos(W);
		double sinW = Math.sin(W);

		double[][] I = new double[3][2];
		// The real part
		I[0][0] = B0 * cosW;
		I[1][0] = B1 * cosW;
		I[2][0] = B2 * cosW;
		// The imaginary part
		I[0][1] = B0 * sinW;
		I[1][1] = B1 * sinW;
		I[2][1] = B2 * sinW;

		return I;
	}
}
