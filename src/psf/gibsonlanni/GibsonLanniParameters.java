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

package psf.gibsonlanni;

public class GibsonLanniParameters {

	/**
	 * Working distance of the objective (design value). This is also the width
	 * of the immersion layer.
	 */
	public double	ti0;

	/**
	 * Working distance of the objective (experimental value). influenced by the
	 * stage displacement.
	 */
	public double	ti;

	/** Immersion medium refractive index (design value). */
	public double	ni0;

	/** Immersion medium refractive index (experimental value). */
	public double	ni;

	/** Coverslip thickness (design value). */
	public double	tg0;

	/** Coverslip thickness (experimental value). */
	public double	tg;

	/** Coverslip refractive index (design value). */
	public double	ng0	= 1.5;

	/** Coverslip refractive index (experimental value). */
	public double	ng	= 1.5;

	/** Sample refractive index. */
	public double	ns;

	/** Axial position of the particle. */
	public double	particleAxialPosition;

	public GibsonLanniParameters() {
	}

	public GibsonLanniParameters(GibsonLanniParameters p) {
		this.ng = p.ng;
		this.ng0 = p.ng0;
		this.ni = p.ni;
		this.ni0 = p.ni0;
		this.ns = p.ns;
		this.particleAxialPosition = p.particleAxialPosition;
		this.tg = p.tg;
		this.tg0 = p.tg0;
		this.ti = p.ti;
		this.ti0 = p.ti0;
	}
}