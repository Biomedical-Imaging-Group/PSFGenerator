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

import javax.swing.JComboBox;
import javax.swing.JPanel;

import bilib.commons.components.GridToolbar;
import bilib.commons.components.SpinnerRangeDouble;
import bilib.commons.job.runnable.Job;
import bilib.commons.job.runnable.Pool;
import bilib.commons.settings.Settings;
import psf.PSF;

/**
 * @author Sameer Hiware
 */
public class VRIGLPSF extends PSF {

	private double				ni_Default		= 1.5;
	private double				ng_Default		= 1.5;
	private double				ns1_Default		= 1.33;
	private double				ns2_Default		= 1.4;
	private double				ti_Default		= 150;
	private double				tg_Default		= 170;
	private double				zpos_Default	= 2000;

	private SpinnerRangeDouble	spnNI			= new SpinnerRangeDouble(ni_Default, 0, 3, 0.1);
	private SpinnerRangeDouble	spnNG			= new SpinnerRangeDouble(ng_Default, 0, 3, 0.1);
	private SpinnerRangeDouble	spnNS1			= new SpinnerRangeDouble(ns1_Default, 0, 3, 0.1);
	private SpinnerRangeDouble	spnNS2			= new SpinnerRangeDouble(ns2_Default, 0, 3, 0.1);
	private JComboBox			cmbRIvary		= new JComboBox(new String[] { "Linear", "Logrithmic", "Exponential" });	// Line
																															// added
	private SpinnerRangeDouble	spnTI			= new SpinnerRangeDouble(ti_Default, 0, 999999.0, 100);
	private SpinnerRangeDouble	spnTG			= new SpinnerRangeDouble(tg_Default, 0, 999999.0, 100);
	private SpinnerRangeDouble	spnZPos			= new SpinnerRangeDouble(zpos_Default, -99999999.0, 99999999.0, 10);
	private JComboBox			cmbAccuracy		= new JComboBox(new String[] { "Good", "Better", "Best" });

	protected VRIGLParameters	p;
	VRIGL[]						gb;
	public int					accuracy		= 0;

	public VRIGLPSF() {
		fullname = "Variable Refractive Index Gibson & Lanni";
		shortname = "VRIGL";
		p = new VRIGLParameters();
	}

	@Override
	public void resetParameters() {
		spnNI.set(ni_Default);
		spnNG.set(ng_Default);
		spnNS1.set(ns1_Default);
		spnNS2.set(ns2_Default);
		// VariType.set(ri_variation_default); // Line added
		spnTI.set(ti_Default);
		spnTG.set(tg_Default);
		spnZPos.set(zpos_Default);
		cmbAccuracy.setSelectedIndex(0);
	}

	@Override
	public void fetchParameters() {
		p.ni = spnNI.get();
		p.ng = spnNG.get();
		p.ns1 = spnNS1.get();
		p.ns2 = spnNS2.get();
		p.type = cmbRIvary.getSelectedIndex(); // Line added
		// p.type = VariType.get(); // Line added
		p.ti0 = spnTI.get() * 1E-6;
		p.tg0 = spnTG.get() * 1E-6;
		p.particleAxialPosition = spnZPos.get() * 1E-9;
		accuracy = cmbAccuracy.getSelectedIndex();
	}

	@Override
	public JPanel buildPanel(Settings settings) {
		GridToolbar pn = new GridToolbar(false, 1);
		pn.place(02, 0, "Refr. index immersion");
		pn.place(03, 0, "Refr. index sample at 0");
		pn.place(04, 0, "Refr. index sample at Zp");
		pn.place(05, 0, "Refr. index variation"); // Line added
		pn.place(06, 0, "Working distance (ti)");
		pn.place(11, 0, "Particle position Zp");
		pn.place(14, 0, "Accuracy computation");

		pn.place(02, 1, spnNI);
		pn.place(03, 1, spnNS1);
		pn.place(04, 1, spnNS2);
		pn.place(05, 1, cmbRIvary);
		pn.place(06, 1, spnTI);
		pn.place(11, 1, spnZPos);
		pn.place(14, 1, cmbAccuracy);

		pn.place(01, 2, "<html>[nm]</html>");
		pn.place(02, 2, "<html>n<sub>i</sub></html>");
		pn.place(03, 2, "<html>n<sub>s0</sub></html>");
		pn.place(04, 2, "<html>n<sub>sz</sub></html>");
		pn.place(06, 2, "<html>[&mu;m]</html>");
		pn.place(11, 2, "<html>[nm]</html>");
		JPanel panel = new JPanel();
		panel.add(pn);
		settings.record("psf-" + shortname + "-NI", spnNI, "" + ni_Default);
		settings.record("psf-" + shortname + "-NG", spnNG, "" + ng_Default);
		settings.record("psf-" + shortname + "-NS1", spnNS1, "" + ns1_Default);
		settings.record("psf-" + shortname + "-NS2", spnNS2, "" + ns2_Default);
		settings.record("psf-" + shortname + "-RIvary", cmbRIvary, "Linear");
		settings.record("psf-" + shortname + "-TI", spnTI, "" + ti_Default);
		settings.record("psf-" + shortname + "-TG", spnTG, "" + tg_Default);
		settings.record("psf-" + shortname + "-ZPos", spnZPos, "" + zpos_Default);
		settings.record("psf-" + shortname + "-accuracy", cmbAccuracy, (String) cmbAccuracy.getItemAt(0));
		return panel;
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
	public void generate(Pool pool) {
		for (int z = 0; z < nz; z++) {
			VRIGLParameters param = new VRIGLParameters(p);
			param.ti = p.ti0 + resAxial * 1E-9 * (z - (nz - 1.0) / 2.0);
			VRIGL plane = new VRIGL(param, z);
			plane.addMonitor(this);
			pool.register(plane);
		}
	}

	public class VRIGL extends Job {

		private int				OVER_SAMPLING	= 2;
		private VRIGLParameters	p;
		private int				z;

		public VRIGL(VRIGLParameters p, int z) {
			this.z = z;
			this.p = p;
		}

		@Override
		public void process() {

			// The center of the image in units of [pixels]
			double x0 = (nx - 1) / 2.0;
			double y0 = (ny - 1) / 2.0;

			// Lateral particle position in units of [pixels]
			double xp = x0;// 0.0/p.pixelSize;
			double yp = y0;// 0.0/p.pixelSize;

			// Radial locations.
			// double xpAbs = Math.abs(xp), ypAbs = Math.abs(yp);
			// double maxRadialDistanceInPixels =
			// Math.round(Math.sqrt((xpAbs+nx-x0)*(xpAbs+nx-x0)+(ypAbs+ny-y0)*(ypAbs+ny-y0)))+1;
			int maxRadius = ((int) Math.round(Math.sqrt((nx - x0) * (nx - x0) + (ny - y0) * (ny - y0)))) + 1;
			double[] r = new double[maxRadius * OVER_SAMPLING];
			double[] h = new double[r.length];

			KirchhoffDiffractionSimpson I = new KirchhoffDiffractionSimpson(p, accuracy, z, resLateral, resAxial);
			for (int n = 0; n < r.length; n++) {
				r[n] = ((double) n) / ((double) OVER_SAMPLING);
				h[n] = I.calculate(r[n] * resLateral * 1E-9);
				if (!live)
					return;
			}

			// Linear interpolation of the pixels values
			double slice[] = new double[nx * ny];
			double rPixel, value;
			int index;
			for (int x = 0; x < nx; x++) {
				for (int y = 0; y < ny; y++) {
					// radius of the current pixel in units of [pixels]
					rPixel = Math.sqrt((x - xp) * (x - xp) + (y - yp) * (y - yp));
					// Index of nearest coordinate from bellow
					index = (int) Math.floor(rPixel * OVER_SAMPLING);
					// Interpolated value.
					value = h[index] + (h[index + 1] - h[index]) * (rPixel - r[index]) * OVER_SAMPLING;
					slice[x + nx * y] = value;
				}
				if (!live)
					return;
			}
			setPlane(z, slice);
			increment(90.0 / nz, "" + z + " / " + nz);
		}
	}

	@Override
	public String getDescription() {
		return "<h1>Variable Refractive Index G&L</h1>" + "<p>Author:Sameer Hiware, IIT Bombay, India<p>";
	}
}
