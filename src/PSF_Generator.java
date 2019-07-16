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
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import bilib.commons.buttons.ButtonFactory;
import bilib.commons.job.JobEvent;
import bilib.commons.job.runnable.Pool;
import bilib.commons.job.runnable.PoolResponder;
import bilib.commons.settings.Settings;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GUI;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import psf.Data3D;
import psf.PSF;
import psfgenerator.About;
import psfgenerator.CollectionPSF;
import psfgenerator.MainPanel;
import psfgenerator.ResultPlanesTable;
import psfgenerator.SummaryPanel;

public class PSF_Generator implements PoolResponder, PlugIn, ActionListener {

	private Settings					settings	= new Settings("PSFGenerator", IJ.getDirectory("plugins") + "PSFGenerator.txt");
	private String[]					types		= new String[] { "32-bits", "8-bits", "16-bits" };
	private String[]					luts		= new String[] { "Fire", "Spectrum", "Grays", "Green" };
	private MainPanel					panel;
	private JFrame						frame;

	private HashMap<String, JButton>	buttons		= new HashMap<String, JButton>();

	@Override
	public void run(String arg) {
		ArrayList<PSF> psfs = CollectionPSF.getStandardCollection();
		buildButtons();
		panel = new MainPanel(settings, buttons, psfs, luts, types, this);
		frame = new JFrame(About.title());
		frame.getContentPane().add(panel);
		frame.setResizable(false);
		frame.pack();
		GUI.center(frame);
		frame.setVisible(true);
	}

	private void buildButtons() {
		buttons.clear();
		buttons.put("run", ButtonFactory.run(false));
		buttons.put("stop", ButtonFactory.stop(false));
		buttons.put("settings", ButtonFactory.prefs(false));
		buttons.put("help", ButtonFactory.help(false));
		buttons.put("about", ButtonFactory.about(false));
		buttons.put("close", ButtonFactory.close(false));

		buttons.get("close").addActionListener(this);
	}

	public void close() {
		if (panel != null) {
			settings.storeValue("PSF-shortname", panel.getSelectedPSFShortname());
			panel.onClosed();
		}
		if (frame != null)
			frame.dispose();
		settings.storeRecordedItems();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton bn = (JButton) event.getSource();
		if (bn == buttons.get("close"))
			close();
	}

	@Override
	public void onFailure(Pool pool, JobEvent event) {
		panel.setEnabledRun(true);
		Exception ex = event.getException();
		if (ex != null) {
			IJ.log(" POOL " + pool.getName() + "  " + pool.size());
			// event.getJob().getPool().die();
			StackTraceElement elements[] = ex.getStackTrace();
			IJ.log("" + event);
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
			int lut = panel.getSelectedLUT();
			PSF psf = (PSF) event.getSource();
			panel.finish();

			ImagePlus imp = createImagePlus(psf, type);
			display(psf, imp, lut);
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

	public ImagePlus createImagePlus(PSF psf, int type) {
		String name = psf.getShortname();
		Data3D data = psf.getData();
		int nx = data.nx;
		int ny = data.ny;
		int nz = data.nz;

		ImageStack stack = new ImageStack(nx, ny);
		for (int z = 0; z < nz; z++) {
			if (type == 0)
				stack.addSlice(new FloatProcessor(nx, ny, data.createAsFloat(z)));
			else if (type == 2)
				stack.addSlice(new ShortProcessor(nx, ny, data.createAsShort(z), null));
			else if (type == 1)
				stack.addSlice(new ByteProcessor(nx, ny, data.createAsByte(z)));
		}
		return new ImagePlus("PSF " + name, stack);
	}

	private void display(PSF psf, ImagePlus imp, int lut) {
		imp.show();
		imp.setSlice(imp.getStackSize() / 2);
		imp.getCalibration().pixelHeight = psf.resLateral;
		imp.getCalibration().pixelWidth = psf.resLateral;
		imp.getCalibration().pixelDepth = psf.resAxial;
		imp.getCalibration().setUnit("nm");
		try {
			IJ.run((String) luts[lut]);
		}
		catch (Exception e) {
			IJ.error("Unknown LUT " + (String) luts[lut]);
		}
	}

}
