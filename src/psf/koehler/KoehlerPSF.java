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

package psf.koehler;

import javax.swing.JPanel;

import bilib.commons.components.GridToolbar;
import bilib.commons.components.SpinnerRangeDouble;
import bilib.commons.fft.BasicFFT;
import bilib.commons.job.runnable.Job;
import bilib.commons.job.runnable.Pool;
import bilib.commons.settings.Settings;
import psf.PSF;

/**
 * @author Daniel Sage
 */
public class KoehlerPSF extends PSF {

	private double			dTop_Default	= 6;
	private double			dMid_Default	= 3;
	private double			dBot_Default	= 6;
	private double			n0_Default		= 1.5;
	private double			n1_Default		= 1.0;

	private double			dTop			= dTop_Default;
	private double			dMid			= dMid_Default;
	private double			dBot			= dBot_Default;
	private double			n0				= n0_Default;
	private double			n1				= n1_Default;

	private SpinnerRangeDouble	spnN0			= new SpinnerRangeDouble(n0_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnN1			= new SpinnerRangeDouble(n1_Default, 0, 10000, 1);
	private SpinnerRangeDouble	spnDTop			= new SpinnerRangeDouble(dTop_Default, 0, 1000000, 1);
	private SpinnerRangeDouble	spnDMid			= new SpinnerRangeDouble(dMid_Default, 0, 1000000, 1);
	private SpinnerRangeDouble	spnDBot			= new SpinnerRangeDouble(dBot_Default, 0, 1000000, 1);

	public KoehlerPSF() {;
		shortname = "Koehler";
		fullname = "Koehler Illumination - OTF Definition";
	}

	@Override
	public void resetParameters() {
		spnN0.set(n0_Default);
		spnN1.set(n1_Default);
		spnDTop.set(dTop_Default);
		spnDMid.set(dMid_Default);
		spnDBot.set(dBot_Default);
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

	@Override
	public void fetchParameters() {
		this.n0 = spnN0.get();
		this.n1 = spnN1.get();
		this.dTop = spnDTop.get();
		this.dMid = spnDMid.get();
		this.dBot = spnDBot.get();
	}

	@Override
	public JPanel buildPanel(Settings settings) {
		GridToolbar pn = new GridToolbar(false, 1);
		pn.place(01, 0, "<html>Refractive index &nu;<sub>0</sub></html>");
		pn.place(02, 0, "<html>Refractive index &nu;<sub>1</sub></html>");
		pn.place(03, 0, "<html>Out-of-focus - top</html>");
		pn.place(04, 0, "<html>Out-of-focus - middle</html>");
		pn.place(05, 0, "<html>Out-of-focus - bottom</html>");
		pn.place(01, 1, spnN0);
		pn.place(02, 1, spnN1);
		pn.place(03, 1, spnDTop);
		pn.place(04, 1, spnDMid);
		pn.place(05, 1, spnDBot);
		pn.place(01, 2, "");
		pn.place(02, 2, "");
		pn.place(03, 2, "<html>[&mu;m]</html>");
		pn.place(04, 2, "<html>[&mu;m]</html>");
		pn.place(05, 2, "<html>[&mu;m]</html>");
		JPanel panel = new JPanel();
		panel.add(pn);
		settings.record("psf-" + shortname + "-n0", spnN0, "" + n0_Default);
		settings.record("psf-" + shortname + "-n1", spnN1, "" + n1_Default);
		settings.record("psf-" + shortname + "-dTop", spnDTop, "" + n0_Default);
		settings.record("psf-" + shortname + "-dMid", spnDMid, "" + dMid_Default);
		settings.record("psf-" + shortname + "-dBot", spnDBot, "" + dBot_Default);
		return panel;
	}

	@Override
	public String getDescription() {
		String desc = "<h1>" + fullname + "</h1>";
		desc += "<p>Simulates a defocussing effect due to the K&ouml;hler ";
		desc += "illumination in a brightfield microscope. The ";
		desc += "optical transfer function in the Fourier domain is ";
		desc += "OTF(&omega;) = exp(-&omega;<sup>2</sup>&sigma;<sup>2</sup>/2),";
		desc += "where &sigma; = &nu;<sub>0</sub> + &nu;<sub>1</sub>.(d+z)</p>";
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
		private int	z;
		
		public Plane(int z) {
			this.z = z;
		}
		
		@Override
		public void process() {
			double z0 = (nz - 1) / 2.0;

			double d = 0;
			if (z < z0) {
				double r = (z0 - z) / z0;
				d = dMid * (1.0 - r) + dTop * r;
			}
			if (z >= z0) {
				double r = (z - z0) / z0;
				d = dMid * (1.0 - r) + dBot * r;
			}
			
			if (!live)
				return;
			double slice[] = create(d + z);
			
			if (!live)
				return;

			(new BasicFFT()).shift2D(slice, nx, ny);
			if (!live)
				return;

			data.data[z] = slice;
			increment(90.0 / nz, "" + z + " / " + nz );
		}

		private double[] create(double d) {
			double sigma = n0 + n1 * Math.abs(d);
			int xsize = nx / 2 + 1;
			int ysize = ny / 2 + 1;
			double ampl = 1.0;
			double function[][] = new double[xsize][ysize];
			double wx, wy, wr;
			for (int y = 0; y < xsize; y++)
				for (int x = 0; x < ysize; x++) {
					wx = Math.PI * x / xsize;
					wy = Math.PI * y / ysize;
					wr = Math.sqrt(wx * wx + wy * wy);
					function[x][y] = ampl * Math.exp(-wr * wr * sigma * sigma / 2.0);
				}
			double[] real = (new BasicFFT()).fillHermitian2D(function);
			double signal[][] = (new BasicFFT()).inverse2D(real, new double[real.length], nx, ny);
			return signal[0];
		}
	}


}
