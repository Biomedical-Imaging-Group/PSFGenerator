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

import javax.swing.JComboBox;
import javax.swing.JPanel;

import bilib.commons.components.GridToolbar;
import bilib.commons.components.SpinnerRangeDouble;
import bilib.commons.job.runnable.Job;
import bilib.commons.job.runnable.Pool;
import bilib.commons.settings.Settings;
import psf.PSF;
import psf.defocusplane.lateral.Astigmatism;
import psf.defocusplane.lateral.CardinalSin;
import psf.defocusplane.lateral.CircularPupil;
import psf.defocusplane.lateral.Cosine;
import psf.defocusplane.lateral.DoubleHelix;
import psf.defocusplane.lateral.Gaussian;
import psf.defocusplane.lateral.LateralFunction;
import psf.defocusplane.lateral.Lorentz;
import psf.defocusplane.lateral.OrientedGaussian;

public class DefocusPlanePSF extends PSF {

	static public int			GAUSSIAN			= 0;
	static public int			LORENTZ				= 1;
	static public int			SINC				= 2;
	static public int			COSINE				= 3;
	static public int			CIRCULAR			= 4;
	static public int			ASTIGMATISM			= 5;
	static public int			ROTATED_GAUSSIAN	= 6;
	static public int			DOUBLE_HELIX		= 7;

	static public String[]		namesXY				= new String[] { "Gaussian", "Lorentz", "Cardinale-Sine", "Cosine", "Circular-Pupil", "Astigmatism", "Oriented-Gaussian", "Double-Helix" };

	private double				zFocus_Default		= 0;
	private double				zDefocus_Default	= 100;
	private int					zfunction			= 10;
	private int					xyfunction			= 0;

	private double				zFocus				= zFocus_Default;
	private double				zDefocus			= zDefocus_Default;

	private SpinnerRangeDouble	spnFocus			= new SpinnerRangeDouble(zFocus_Default, 0, 1000000, 1, 3);
	private SpinnerRangeDouble	spnDefocus		= new SpinnerRangeDouble(zDefocus_Default, 0, 1000000, 1, 3);
	private JComboBox		cmbAxial				= new JComboBox(ZFunction.names);

	public DefocusPlanePSF(int xyfunction) {
		fullname = "Lateral (" + namesXY[xyfunction] + ") and Axial Definition";
		shortname = namesXY[xyfunction];
		this.xyfunction = xyfunction;
	}

	@Override
	public String checkSize(int nx, int ny, int nz) {
		if (nz < 3)
			return ("nz should be greater than 3.");
		if (nx < 4)
			return ("nx should be greater than 4.");
		if (ny < 4)
			return ("ny should be greater than 4.");
		return "";
	}

	@Override
	public void resetParameters() {
		spnFocus.set(zFocus_Default);
		spnDefocus.set(zDefocus_Default);
		cmbAxial.setSelectedIndex(0);
	}

	@Override
	public void fetchParameters() {
		this.zFocus = spnFocus.get();
		this.zDefocus = spnDefocus.get();
		this.zfunction = cmbAxial.getSelectedIndex();
	}

	@Override
	public JPanel buildPanel(Settings settings) {
		GridToolbar pn = new GridToolbar(false);
		pn.place(03, 0, "<html>Z function</sub></html>");
		pn.place(04, 0, "<html>Z focal plane</sub></html>");
		pn.place(05, 0, "<html>Z defocused plane (x2)</html>");

		pn.place(03, 1, 2, 1, cmbAxial);
		pn.place(04, 1, spnFocus);
		pn.place(05, 1, spnDefocus);

		pn.place(04, 2, "[nm]");
		pn.place(05, 2, "[nm]");

		JPanel panel = new JPanel();
		panel.add(pn);
		settings.record("psf-" + shortname + "-focus", spnFocus, "" + zFocus_Default);
		settings.record("psf-" + shortname + "-defocus", spnDefocus, "" + zDefocus_Default);
		settings.record("psf-" + shortname + "-axial", cmbAxial, "" + cmbAxial.getItemAt(0));
		return panel;
	}

	@Override
	public String getDescription() {
		String desc = "<h1>" + fullname + "</h1>";
		desc += "<p>These synthetic PSFs are defined by the tensor product of 2 functions, ";
		desc += "the lateral 2D function and the axial Z-function. ";
		desc += "At the Z defocussed plane the 2D lateral function is two times larger than ";
		desc += "the focal plane.</p>";
		return desc;
	}

	@Override
	public void generate(Pool pool) {
		for (int z = 0; z < nz; z++) {
			Plane p = new Plane(z);
			p.addMonitor(this);
			pool.register(p);
		}
	}

	public class Plane extends Job {
		private int z;

		public Plane(int z) {
			this.z = z;
		}

		@Override
		public void process() {

			ZFunction zfunc = new ZFunction(zfunction, zDefocus / resAxial, zFocus / resAxial);
			// XY, 2*sqrt(2*ln(2)) = 2.35482005, fwmh = 2*sqrt(2*ln(2)) * sigma
			double fwhm = 0.5 * lambda / NA; // in nm
			double radiusPix = fwhm / 2.35482005 / (resLateral * 1E-9);
			double array[] = new double[nx * ny];

			double defocusFactor = zfunc.getDefocusFactor(z);
			LateralFunction func = null;
			if (xyfunction == GAUSSIAN)
				func = new Gaussian(radiusPix, defocusFactor);
			else if (xyfunction == LORENTZ)
				func = new Lorentz(radiusPix, defocusFactor);
			else if (xyfunction == SINC)
				func = new CardinalSin(radiusPix, defocusFactor);
			else if (xyfunction == COSINE)
				func = new Cosine(radiusPix, defocusFactor);
			else if (xyfunction == CIRCULAR)
				func = new CircularPupil(radiusPix, defocusFactor);
			else if (xyfunction == ASTIGMATISM)
				func = new Astigmatism(radiusPix, defocusFactor);
			else if (xyfunction == ROTATED_GAUSSIAN)
				func = new OrientedGaussian(radiusPix, defocusFactor);
			else if (xyfunction == DOUBLE_HELIX)
				func = new DoubleHelix(radiusPix, defocusFactor);
			else
				return;

			double xo = nx * 0.5;
			double yo = ny * 0.5;
			double v = 0, integral = 0;
			for (int x = 0; x < nx & live; x++) {
				for (int y = 0; y < ny; y++) {
					v = func.eval(x - xo, y - yo);
					array[x + nx * y] = v;
					integral += v;
				}
				if (!live)
					return;
			}

			for (int x = 0; x < nx & live; x++) {
				for (int y = 0; y < ny; y++) {
					array[x + nx * y] /= integral;
				}
				if (!live)
					return;
			}

			increment(90.0 / nz, "" + z + "/" + nz);
			data.putXY(z, array);
		}
	}
}
