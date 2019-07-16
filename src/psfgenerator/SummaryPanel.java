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

import bilib.commons.components.HTMLPane;
import bilib.commons.table.CustomizedColumn;
import bilib.commons.table.CustomizedTable;
import bilib.commons.utils.NumFormat;
import psf.PSF;
import psf.Point3D;

public class SummaryPanel extends JPanel {

	public SummaryPanel(PSF psf) {

		Point3D max = psf.getData().max;
		Point3D fwhm = psf.getData().fwhm;

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Feature", String.class, 40, false));
		columns.add(new CustomizedColumn("Value in nm", String.class, 40, false));
		columns.add(new CustomizedColumn("Value in pixel", String.class, 40, false));

		CustomizedTable table = new CustomizedTable(columns, true);

		table.append(new String[] { "Numerical Aperture", NumFormat.sci(psf.NA), "" });
		table.append(new String[] { "Wavelength", NumFormat.sci(psf.lambda), "" });
		table.append(new String[] { "Energy", NumFormat.sci(psf.getData().energy), "" });

		table.append(new String[] { "Size X", NumFormat.sci(psf.nx * psf.resLateral), "" + psf.nx });
		table.append(new String[] { "Size Y", NumFormat.sci(psf.ny * psf.resLateral), "" + psf.ny });
		table.append(new String[] { "Size Z", NumFormat.sci(psf.nz * psf.resAxial), "" + psf.nz });

		table.append(new String[] { "Pixelsize X", NumFormat.sci(psf.resLateral) });
		table.append(new String[] { "Pixelsize Y", NumFormat.sci(psf.resLateral) });
		table.append(new Object[] { "Axial Z-step", NumFormat.sci(psf.resAxial) });

		table.append(new String[] { "FWHM Lateral X", NumFormat.sci(fwhm.x * psf.resLateral), NumFormat.sci(fwhm.x) });
		table.append(new String[] { "FWHM Lateral Y", NumFormat.sci(fwhm.y * psf.resLateral), NumFormat.sci(fwhm.y) });
		table.append(new String[] { "FWHM Axial Z", NumFormat.sci(fwhm.z * psf.resAxial), NumFormat.sci(fwhm.z) });
		table.append(new String[] { "Energy under FWHM", NumFormat.sci(fwhm.value), "" });

		table.append(new String[] { "Max Lateral X", NumFormat.sci(max.x * psf.resLateral), NumFormat.sci(max.x) });
		table.append(new String[] { "Max Lateral Y", NumFormat.sci(max.y * psf.resLateral), NumFormat.sci(max.y) });
		table.append(new String[] { "Max Axial Z", NumFormat.sci(max.z * psf.resAxial), NumFormat.sci(max.z) });
		table.append(new String[] { "Max Value", NumFormat.sci(max.value), "" });

		HTMLPane panel = new HTMLPane();
		panel.append("<h1>" + psf.getFullname() + "</h1>");
		setLayout(new BorderLayout());
		add(panel.getPane(), BorderLayout.NORTH);
		add(table.getPane(200, 200), BorderLayout.CENTER);
	}
}
