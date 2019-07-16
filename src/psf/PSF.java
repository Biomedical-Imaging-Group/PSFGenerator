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

import javax.swing.JPanel;

import bilib.commons.job.ExecutionMode;
import bilib.commons.job.JobEvent;
import bilib.commons.job.runnable.Job;
import bilib.commons.job.runnable.Pool;
import bilib.commons.job.runnable.PoolResponder;
import bilib.commons.settings.Settings;

abstract public class PSF extends Job implements PoolResponder {

	protected Data3D	data;
	protected String	fullname	= "Untitled";
	protected String	shortname	= "...";
	public int			nx;
	public int			ny;
	public int			nz;
	protected int		type;
	private int			scale;

	public double		resLateral;				// in nm
	public double		resAxial;				// in nm
	public double		NA;
	public double		lambda;					// in nm
	private Pool		pool;

	public Data3D getData() {
		return data;
	}

	public void setOpticsParameters(double NA, double lambdaNM) {
		this.NA = NA;
		this.lambda = lambdaNM * 1E-9;
	}

	public void setResolutionParameters(double resLateral, double resAxial) {
		this.resLateral = resLateral;
		this.resAxial = resAxial;
	}

	public void setOutputParameters(int nx, int ny, int nz, int type, int scale) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.type = type;
		this.scale = scale;
	}

	@Override
	public void abort() {
		if (pool != null)
			pool.die();
		super.abort();
	}

	@Override
	public void process() {
		progress(1, "Starting " + getShortname() + "...");
		rewind();
		String errorSize = checkSize(nx, ny, nz);
		if (!errorSize.equals("")) {
			abort(errorSize);
			print(errorSize);
			return;
		}
		fetchParameters();

		data = new Data3D(nx, ny, nz);
		progress(4, "Init " + getShortname() + "...");
		pool = new Pool(shortname, this);
		generate(pool);

		progress(5, "Executing " + getShortname() + "...");
		pool.execute(ExecutionMode.MULTITHREAD_SYNCHRONIZED);

		data.determineMaximumAndEnergy();
		data.estimateFWHM();
		data.rescale(scale, data.max.value);
	}

	public double[] getPlane(int z) {
		return data.getPlane(z);
	}

	public void setPlane(int z, double plane[]) {
		data.setPlane(z, plane);
	}

	public String getShortname() {
		return shortname;
	}

	public String getFullname() {
		return fullname;
	}

	abstract public void generate(Pool pool);

	abstract public String checkSize(int nx, int ny, int nz);

	abstract public JPanel buildPanel(Settings settings);

	abstract public String getDescription();

	abstract public void resetParameters();

	abstract public void fetchParameters();

	@Override
	public void onFailure(Pool pool, JobEvent event) {
		getPool().fire(event);
	}

	@Override
	public void onEvent(Pool pool, JobEvent event) {
		getPool().fire(event);
	}

	@Override
	public void onSuccess(Pool pool, JobEvent event) {
		getPool().fire(event);
	}

}
