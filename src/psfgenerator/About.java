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

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import bilib.commons.components.HTMLPane;

public class About {

	public static String title() {
		return "PSF Generator";
	}

	public static String version() {
		return "1.1.1.2 (18.12.2017)";
	}

	public static String url() {
		return "http://bigwww.epfl.ch/algorithms/psfgenerator/";
	}

	public static String copyright() {
		return " 2017 EPFL BIG \u2022 " + version();
	}

	public static HTMLPane getPanel(int w, int h) {
		HTMLPane html = new HTMLPane(w, h);

		html.append("h1", title());
		html.append("p", "<i>Daniel Sage and Hagai Kirshner</i>");
		html.append("p", "Biomedical Imaging Group (BIG)");
		html.append("p", "EPFL, Lausanne, Switzerland");
		html.append("<hr>");
		html.append("h2", "Version: " + version());
		html.append("p", "http://bigwww.epfl.ch/algorithms/psfgenerator/");
		html.append("<hr>");
		html.append("h2", "Reference:");
		html.append("p", "H. Kirshner, F. Aguet, D. Sage, M. Unser, " + "3-D PSF Fitting for Fluorescence Microscopy: Implementation and Localization Application, "
				+ "Journal of Microscopy, vol. 249, no. 1, 2013.");
		html.append("p", "A. Griffa, N, Garin, D. Sage, " + "Comparison of Deconvolution Software in 3D Microscopy: A User Point of View"
				+ "G.I.T. Imaging & Microscopy, vol. 12, no. 1, pp. 43-45, March 2010.");
		html.append("<hr>");

		html.append("p", "PSF Generator is a Java software package that allows one to generate " + "and visualize various 3D models of a microscope PSF.");
		html.append("p", "The current version has 15 different models including true 3D optical " + "models Born & Wolf, Gibson & Lanni, and the vectorial-based model Richards & Wolf.");

		return html;
	}

	public static void show() {
		HTMLPane pane = getPanel(400, 400);
		pane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		JFrame frame = new JFrame("About " + title());
		frame.getContentPane().add(pane);
		frame.pack();
		frame.setVisible(true);
	}
}