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

package plugins.sage.psfgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import icy.file.FileUtil;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;
import icy.image.IcyBufferedImage;
import icy.image.colormap.FireColorMap;
import icy.image.colormap.HSVColorMap;
import icy.image.colormap.IceColorMap;
import icy.image.colormap.IcyColorMap;
import icy.image.colormap.JETColorMap;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import psf.Data3D;
import psf.PSF;
import psfgenerator.About;
import psfgenerator.CollectionPSF;
import psfgenerator.MainPanel;
import psfgenerator.ResultPlanesTable;
import psfgenerator.SummaryPanel;

public class PSFGeneratorIcyFrame extends IcyFrame implements IcyFrameListener, PoolResponder, ActionListener {

	private MainPanel	panel;
	private String		path		= FileUtil.getApplicationDirectory() + File.separator + "plugins" + File.separator + "sage" + File.separator + "psfgenerator" + File.separator;
	private Settings	settings	= new Settings("PSF Generator", path + "config.txt");
	private String[]	types		= new String[] { "double", "float", "ubyte", "short" };
	private String[]	luts		= new String[] { "Fire", "HSV", "JET", "Glow", "Gray" };
	
	private HashMap<String, JButton> buttons = new HashMap<String, JButton>();

	public PSFGeneratorIcyFrame() {
		super(About.title());

		ArrayList<PSF> psfs = CollectionPSF.getStandardCollection();
		buildButtons();
		
		panel = new MainPanel(settings, buttons, psfs, luts, types, this);

		getContentPane().add(panel);
		pack();
		addFrameListener(this);
		setVisible(true);
		toFront();
		addToDesktopPane();
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

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton bn = (JButton) event.getSource();
		if (bn == buttons.get("close")) {
			settings.storeRecordedItems();
			settings.storeValue("PSF-shortname", panel.getSelectedPSFShortname());
			close();
		}
	}
	
	@Override
	public void onClosed() {
		super.onClosed();
		panel.onClosed();
	}

	@Override
	public void onFailure(Pool pool, JobEvent event) {
		panel.setEnabledRun(true);
		Exception ex = event.getException();
		if (ex != null) {
			System.out.println(event);
			ex.printStackTrace();
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
			display(psf, type, psf.getShortname(), lut);
			JFrame frame = new JFrame("Characterization of PSF");
			JTabbedPane tab = new JTabbedPane();
			tab.add("Summary", new SummaryPanel(psf));
			tab.add("Plane by plane", new ResultPlanesTable(psf));
			frame.getContentPane().add(tab);
			frame.pack();
			frame.setVisible(true);
			panel.finish();
		}
		panel.setEnabledRun(true);
	}

	private void display(PSF psf, int type, String name, int lut) {
		Sequence sequence = new Sequence();
		Data3D data = psf.getData();
		sequence.setName("PSF " + name);
		IcyColorMap colormap = null;
		switch (lut) {
		case 0:
			colormap = new FireColorMap();
			break;
		case 1:
			colormap = new HSVColorMap();
			break;
		case 2:
			colormap = new JETColorMap();
			break;
		case 3:
			colormap = new IceColorMap();
			break;
		}

		for (int z = 0; z < data.nz; z++) {
			if (type == 0) {
				double[] plane = data.createAsDouble(z);
				IcyBufferedImage image = new IcyBufferedImage(data.nx, data.ny, 1, DataType.DOUBLE);
				if (colormap != null)
					image.setColorMap(0, colormap, true);
				Array1DUtil.doubleArrayToSafeArray(plane, image.getDataXY(0), image.isSignedDataType());
				image.dataChanged();
				sequence.setImage(0, z, image);
			}
			if (type == 1) {
				float[] plane = data.createAsFloat(z);
				IcyBufferedImage image = new IcyBufferedImage(data.nx, data.ny, 1, DataType.FLOAT);
				if (colormap != null)
					image.setColorMap(0, colormap, true);
				Array1DUtil.floatArrayToSafeArray(plane, image.getDataXY(0), image.isSignedDataType());
				image.dataChanged();
				sequence.setImage(0, z, image);
			}
			if (type == 2) {
				byte[] plane = data.createAsByte(z);
				IcyBufferedImage image = new IcyBufferedImage(data.nx, data.ny, 1, DataType.UBYTE);
				if (colormap != null)
					image.setColorMap(0, colormap, true);
				Array1DUtil.byteArrayToArray(plane, image.getDataXY(0), image.isSignedDataType());
				image.dataChanged();
				sequence.setImage(0, z, image);
			}
			if (type == 3) {
				short[] plane = data.createAsShort(z);
				IcyBufferedImage image = new IcyBufferedImage(data.nx, data.ny, 1, DataType.SHORT);
				if (colormap != null)
					image.setColorMap(0, colormap, true);
				Array1DUtil.shortArrayToSafeArray(plane, image.getDataXY(0), image.isSignedDataType());
				image.dataChanged();
				sequence.setImage(0, z, image);
			}

		}
		sequence.setPixelSizeX(psf.resLateral * 0.001);
		sequence.setPixelSizeY(psf.resLateral * 0.001);
		sequence.setPixelSizeZ(psf.resAxial * 0.001);
		Icy.getMainInterface().addSequence(sequence);
	}

	@Override
	public void icyFrameOpened(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosing(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosed(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameIconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeiconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameActivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeactivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameInternalized(IcyFrameEvent e) {
		pack();
	}

	@Override
	public void icyFrameExternalized(IcyFrameEvent e) {
		pack();
	}
}