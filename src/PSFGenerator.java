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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import bilib.commons.buttons.ButtonFactory;
import bilib.commons.job.ExecutionMode;
import bilib.commons.job.JobEvent;
import bilib.commons.job.runnable.Pool;
import bilib.commons.job.runnable.PoolResponder;
import bilib.commons.settings.Settings;
import bilib.commons.utils.WebBrowser;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import matlab.Converter;
import psf.PSF;
import psfgenerator.About;
import psfgenerator.CollectionPSF;
import psfgenerator.MainPanel;
import psfgenerator.ResultPlanesTable;
import psfgenerator.SummaryPanel;

public class PSFGenerator implements ActionListener, PoolResponder {

	private static String[]				types	= new String[] { "32-bits", "8-bits", "16-bits" };
	private static MainPanel			panel;
	private HashMap<String, JButton>	buttons	= new HashMap<String, JButton>();

	// -----------------------------------------------------------------------------------
	// Standalone with GUI: java -cp PSFGenerator.jar PSFGenerator
	// Standalone without GUI: java -cp PSFGenerator.jar PSFGenerator
	// config_filename.txt
	// -----------------------------------------------------------------------------------
	public static void main(String args[]) {

		if (args.length > 1) {
			System.out.println("Run with a GUI: no expected argument.");
			System.out.println("Run without GUI: the argument has to be the configuration file which defines the parameters.");
			System.exit(0);
		}

		if (args.length == 0) {
			PSFGenerator generator = new PSFGenerator();
			generator.showFrame(true, null);
		}

		if (args.length == 1) {
			ImagePlus imp = PSFGenerator.computeImagePlus(args[0]);
			if (imp != null) {
				System.out.println("Save " + imp.getTitle());
				IJ.saveAsTiff(imp, imp.getTitle());
			}
		}
	}

	// ------------------------------------------------------------------
	// Matlab with GUI
	// javaaddpath /Applications/MATLAB_R2013b.app/java/PSFGenerator.jar
	// PSFGenerator.gui
	// psf = PSFGenerator.get;
	// ------------------------------------------------------------------
	public static void gui() {
		PSFGenerator generator = new PSFGenerator();
		generator.showFrame(false, null);
	}

	// ------------------------------------------------------------------
	// Matlab wit GUI
	// javaaddpath /Applications/MATLAB_R2013b.app/java/PSFGenerator.jar
	// psf = PSFGenerator.get();
	// ------------------------------------------------------------------
	public static Object get() {
		if (panel == null) {
			System.out.println("No open GUI");
			return "No open GUI";
		}
		panel.compute(ExecutionMode.MULTITHREAD_NO);
		PSF psf = panel.getLastPSF();
		int type = panel.getSelectedType();
		ImagePlus imp = new PSF_Generator().createImagePlus(psf, type);
		if (imp != null)
			return Converter.get(imp);
		return "ERROR";
	}

	// ------------------------------------------------------------------
	// Matlab without GUI
	// javaaddpath /Applications/MATLAB_R2013b.app/java/PSF_Generator.jar
	// psf = PSFGenerator.compute(config);
	// ------------------------------------------------------------------
	public static Object compute(String config) {
		ArrayList<PSF> psfs = CollectionPSF.getStandardCollection();
		MainPanel panel = new MainPanel(new Settings("PSFGenerator", config), new HashMap<String, JButton>(), psfs, null, types, null);
		panel.compute(ExecutionMode.MULTITHREAD_NO);
		PSF psf = panel.getLastPSF();
		int type = panel.getSelectedType();
		ImagePlus imp = new PSF_Generator().createImagePlus(psf, type);
		if (imp != null)
			return Converter.get(imp);
		return "ERROR";
	}

	public static ImagePlus computeImagePlus(String config) {
		return computeImagePlus(new Settings("PSFGenerator", config));
	}

	private static ImagePlus computeImagePlus(Settings settings) {
		ArrayList<PSF> psfs = CollectionPSF.getStandardCollection();
		MainPanel panel = new MainPanel(settings, new HashMap<String, JButton>(), psfs, null, types, null);
		System.out.println("Computing " + panel.getSelectedPSFShortname());
		panel.compute(ExecutionMode.MULTITHREAD_NO);
		PSF psf = panel.getLastPSF();
		return new PSF_Generator().createImagePlus(psf, panel.getSelectedType());
	}

	public void close() {
		if (panel == null)
			return;
		panel.onClosed();
		panel = null;
	}

	public void showFrame(boolean withRun, String config) {
		if (config == null)
			buildPanel(withRun, new Settings("PSFGenerator", null));
		else
			buildPanel(withRun, new Settings("PSFGenerator", config));
		JFrame frame = new JFrame(About.title());
		frame.getContentPane().add(panel);
		frame.setResizable(false);
		frame.pack();
		GUI.center(frame);
		frame.setVisible(true);
	}

	private void buildPanel(boolean withRunStop, Settings settings) {
		ArrayList<PSF> psfs = CollectionPSF.getStandardCollection();
		if (withRunStop) {
			buttons.put("run", ButtonFactory.run(true));
			buttons.put("stop", ButtonFactory.stop(true));
			buttons.put("about", ButtonFactory.about(false));
			buttons.put("close", new JButton("Exit"));
			buttons.get("close").addActionListener(this);
			buttons.get("about").addActionListener(this);
		}

		panel = new MainPanel(settings, buttons, psfs, null, types, this);
	}

	@Override
	public void onFailure(Pool pool, JobEvent event) {
		panel.setEnabledRun(true);
		Exception ex = event.getException();
		if (ex != null) {
			StackTraceElement elements[] = ex.getStackTrace();
			System.out.println("Exception " + ex.getMessage());
			for (StackTraceElement element : elements)
				IJ.log(element.toString());

		}
	}

	@Override
	public void onEvent(Pool pool, JobEvent event) {
	}

	@Override
	public void onSuccess(Pool pool, JobEvent event) {
		if (event.getJob() instanceof PSF) {
			int type = panel.getSelectedType();
			PSF psf = (PSF) event.getSource();
			panel.finish();

			ImagePlus imp = new PSF_Generator().createImagePlus(psf, type);
			imp.show();
			JFrame frame = new JFrame("Characterization of PSF");
			JTabbedPane tab = new JTabbedPane();
			tab.add("Summary", new SummaryPanel(psf));
			tab.add("Plane by plane", new ResultPlanesTable(psf));
			frame.getContentPane().add(tab);
			frame.pack();
			frame.setVisible(true);
		}
		panel.setEnabledRun(true);
	}

	public void exit() {
		if (panel != null)
			panel.onClosed();
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton bn = (JButton) event.getSource();
		if (bn == buttons.get("close"))
			exit();

		if (bn == buttons.get("help"))
			WebBrowser.open(About.url());

		if (bn == buttons.get("save")) {
			PSF psf = panel.getLastPSF();
			if (psf == null)
				return;
			int type = panel.getSelectedType();
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter ext = new FileNameExtensionFilter("Stack TIF", "tif");
			// add filters
			fc.addChoosableFileFilter(ext);
			fc.setFileFilter(ext);
			fc.setSelectedFile(new File(psf.getShortname() + ".tif"));
			int ret = fc.showSaveDialog(new JFrame());
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				ImagePlus imp = new PSF_Generator().createImagePlus(psf, type);
				IJ.saveAsTiff(imp, file.getPath());
			}
		}
	}

}
