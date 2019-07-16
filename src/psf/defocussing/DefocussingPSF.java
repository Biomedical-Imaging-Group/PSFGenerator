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

package psf.defocussing;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bilib.commons.components.GridToolbar;
import bilib.commons.components.SpinnerRangeDouble;
import bilib.commons.fft.BasicFFT;
import bilib.commons.job.runnable.Job;
import bilib.commons.job.runnable.Pool;
import bilib.commons.settings.Settings;
import psf.PSF;

public class DefocussingPSF extends PSF {

	private double				zi_Default		= 2000;
	private double				K_Default		= 275;
	private double				dTop_Default	= 30;
	private double				dMid_Default	= 1;
	private double				dBot_Default	= 30;

	private double				zi				= zi_Default;
	private double				K				= K_Default;
	private double				dTop			= dTop_Default;
	private double				dMid			= dMid_Default;
	private double				dBot			= dBot_Default;

	private SpinnerRangeDouble	spnZI			= new SpinnerRangeDouble(zi_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnK			= new SpinnerRangeDouble(K_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnDTop			= new SpinnerRangeDouble(dTop_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnDMid			= new SpinnerRangeDouble(dMid_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnDBot			= new SpinnerRangeDouble(dBot_Default, 0, 10000, 1);

	public DefocussingPSF() {
		shortname = "Defocus";
		fullname = "Simulation of Lens Defocussing";
	}

	@Override
	public String getDescription() {
		String html = "<h1>Simulation of the lens defocussing</h1>";
		html += "<p>Simulation the defocussing of a microscope lens.";
		html += "It is defined by its optical transfer function (OTF) in";
		html += "the Fourier domain: OTF(&omega;) = exp(-&omega;<sup>2</sup>&sigma;<sup>2</sup>).&#124;sin(&xi)/&xi&#124;";
		html += "where &xi; = (d.&omega;.(1-&omega;)) / (K.(z<sub>i</sub>-d) and &sigma; = sqrt(3)";
		html += "d is the defocusing distance<p>";
		return html;
	}

	@Override
	public String checkSize(int nx, int ny, int nz) {
		if (nz < 3)
			return ("nz should be greater than 3.");
		int mx = 1;
		while (mx < nx)
			mx *= 2;
		if (mx != nx)
			return ("nx should be a power of 2.");
		int my = 1;
		while (my < ny)
			my *= 2;
		if (my != ny)
			return ("ny should be a power of 2.");
		return "";
	}

	public void setParameters(double zi, double K, double dTop, double dMid, double dBot) {
		this.zi = zi;
		this.K = K;
		this.dTop = dTop;
		this.dMid = dMid;
		this.dBot = dBot;
	}

	@Override
	public void resetParameters() {
		spnZI.set(zi_Default);
		spnK.set(K_Default);
		spnDTop.set(dTop_Default);
		spnDMid.set(dMid_Default);
		spnDBot.set(dBot_Default);
	}

	@Override
	public void fetchParameters() {
		this.zi = spnZI.get();
		this.K = spnK.get();
		this.dTop = spnDTop.get();
		this.dMid = spnDMid.get();
		this.dBot = spnDBot.get();
	}

	@Override
	public JPanel buildPanel(Settings settings) {
		GridToolbar pn = new GridToolbar(false, 1);
		pn.place(01, 0, new JLabel("<html>z<sub>i</sub></html>"));
		pn.place(02, 0, new JLabel("<html>K (x 10<sup>-6</sup>)</html>"));
		pn.place(03, 0, new JLabel("<html>Out-of-focus - top</html>"));
		pn.place(04, 0, new JLabel("<html>Out-of-focus - middle</html>"));
		pn.place(05, 0, new JLabel("<html>Out-of-focus - bottom</html>"));
		pn.place(01, 1, spnZI);
		pn.place(02, 1, spnK);
		pn.place(03, 1, spnDTop);
		pn.place(04, 1, spnDMid);
		pn.place(05, 1, spnDBot);
		pn.place(01, 2, new JLabel("<html>[&mu;m]</html>"));
		pn.place(02, 2, new JLabel(""));
		pn.place(03, 2, new JLabel("<html>[&mu;m]</html>"));
		pn.place(04, 2, new JLabel("<html>[&mu;m]</html>"));
		pn.place(05, 2, new JLabel("<html>[&mu;m]</html>"));
		JPanel panel = new JPanel();
		panel.add(pn);
		settings.record("psf-" + shortname + "-ZI", spnZI, "" + zi_Default);
		settings.record("psf-" + shortname + "-K", spnK, "" + K_Default);
		settings.record("psf-" + shortname + "-DTop", spnDTop, "" + dTop_Default);
		settings.record("psf-" + shortname + "-DMid", spnDMid, "" + dMid_Default);
		settings.record("psf-" + shortname + "-DBot", spnDBot, "" + dBot_Default);
		return panel;
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
			int n = nz / 2;
			double d = dMid;
			if (z < n) {
				double r = (n - z) / (double) n;
				d = dMid * (1.0 - r) + dTop * r;
			}
			if (z == n)
				d = dMid;
			if (z > n) {
				double r = (z - n) / (double) n;
				d = dMid * (1.0 - r) + dBot * r;
			}
			if (!live)
				return;

			double[] slice = create(d);
			if (!live)
				return;
			(new BasicFFT()).shift2D(slice, nx, ny);
			if (!live)
				return;
			data.putXY(z, slice);

			increment(90.0 / nz, "" + z + " / " + nz);
		}

		private double[] create(double d) {
			double d_um = d * 1e-6;
			double zi_um = zi * 1e-6;
			double K_um = K * 1e-6;
			if (d_um == zi_um)
				return new double[nx * ny];

			double wm = (d_um / (zi_um - d_um)) / K_um;
			double sigma = Math.sqrt(3);
			int xsize = nx / 2;
			int ysize = ny / 2;
			double s, sinc;
			double function[][] = new double[xsize + 1][ysize + 1];
			double wx, wy, wr;
			for (int y = 0; y <= xsize; y++)
				for (int x = 0; x <= xsize; x++) {
					wx = Math.PI * x / xsize;
					wy = Math.PI * y / ysize;
					wr = Math.sqrt(wx * wx + wy * wy);
					s = wm * wr * (1.0 - wr);
					if (s == 0.0)
						sinc = 1.0;
					else
						sinc = Math.sin(s) / s;
					if (sinc < 0)
						sinc = -sinc;
					function[x][y] = Math.exp(-sigma * sigma * wr * wr) * sinc;
				}
			double[] real = (new BasicFFT()).fillHermitian2D(function);
			double signal[][] = (new BasicFFT()).inverse2D(real, new double[real.length], nx, ny);
			return signal[0];
		}
	}

}
