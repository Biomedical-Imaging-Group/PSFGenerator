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

package psfgenerator;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import bilib.commons.table.CustomizedColumn;
import bilib.commons.table.CustomizedTable;
import bilib.commons.utils.NumFormat;
import psf.Data3D;
import psf.PSF;

public class ResultPlanesTable extends JPanel {

	public ResultPlanesTable(PSF psf) {
		super();
		Data3D data = psf.getData();
		double plane[][] = data.getPlaneInformation();
		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Z-Plane", String.class, 40, false));
		columns.add(new CustomizedColumn("Max Value", String.class, 40, false));
		columns.add(new CustomizedColumn("Energy Value", String.class, 40, false));
		columns.add(new CustomizedColumn("Efficiency Radius", String.class, 40, false));

		CustomizedTable table = new CustomizedTable(columns, true);
		for (int i = 0; i < plane.length; i++) {
			String z = String.format("%05d", (int) plane[i][0]);
			String m = NumFormat.sci(plane[i][1]);
			String e = NumFormat.sci(plane[i][2] * 100);
			String r = NumFormat.sci(plane[i][3] * psf.resLateral);
			table.append(new String[] { z, m, e, r });
		}

		setLayout(new BorderLayout());
		add(table.getPane(200, 200));
	}

}
