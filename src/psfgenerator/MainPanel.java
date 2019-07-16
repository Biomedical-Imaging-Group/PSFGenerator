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

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bilib.commons.components.DoubleScrollablePanel;
import bilib.commons.components.GridPanel;
import bilib.commons.components.GridToolbar;
import bilib.commons.components.HTMLPane;
import bilib.commons.components.SpinnerRangeDouble;
import bilib.commons.components.SpinnerRangeInteger;
import bilib.commons.job.ExecutionMode;
import bilib.commons.job.MonitorTimedProgressBar;
import bilib.commons.job.runnable.Pool;
import bilib.commons.job.runnable.PoolResponder;
import bilib.commons.settings.Settings;
import bilib.commons.settings.SettingsFileDialog;
import bilib.commons.utils.WebBrowser;
import psf.PSF;

public class MainPanel extends JPanel implements ListSelectionListener, ActionListener, ChangeListener {

	private MonitorTimedProgressBar				progress		= new MonitorTimedProgressBar();

	private SpinnerRangeInteger					spnNX			= new SpinnerRangeInteger(256, 1, 9999, 1, 3);
	private SpinnerRangeInteger					spnNY			= new SpinnerRangeInteger(256, 1, 9999, 1, 3);
	private SpinnerRangeInteger					spnNZ			= new SpinnerRangeInteger(32, 1, 9999, 1, 3);
	private SpinnerRangeDouble					spnResLateral	= new SpinnerRangeDouble(100, 1, 99999, 1, 3);
	private SpinnerRangeDouble					spnResAxial		= new SpinnerRangeDouble(250, 1, 999999, 1, 3);
	private SpinnerRangeDouble					spnNA			= new SpinnerRangeDouble(1.4, 0.1, 3, 0.1, 3);
	private SpinnerRangeDouble					spnLambda		= new SpinnerRangeDouble(610, 10, 9999, 10, 3);

	private JLabel								lblResolutionXY	= new JLabel("FWHM", JLabel.CENTER);
	private JLabel								lblResolutionZ	= new JLabel("FWHM", JLabel.CENTER);

	private JComboBox							cmbType;
	private JComboBox							cmbLUT;
	private JComboBox							cmbScale		= new JComboBox(new String[] { "Linear", "Log", "Sqrt", "Decibel" });

	private JPanel								pnPSF;
	private JList						list[]			= new JList[3];
	private ArrayList<DoubleScrollablePanel>	cards			= new ArrayList<DoubleScrollablePanel>();
	private JTabbedPane							tab				= new JTabbedPane();
	private Settings							settings;
	private ArrayList<PSF>						psfs;
	private PSF									psf;
	private PoolResponder						responder;
	private HashMap<String, JButton>			buttons;
	private JPanel								pnApplication;

	public MainPanel(Settings settingsExt, HashMap<String, JButton> buttons, ArrayList<PSF> psfs, String luts[], String types[], PoolResponder responder) {
		this.settings = (settingsExt == null ? new Settings("PSFGenerator", "config.txt") : settingsExt);
		this.psfs = psfs;
		this.buttons = buttons;
		for (PSF psf : psfs)
			psf.addMonitor(progress);
		this.responder = responder;
		progress.setString(About.copyright());
		lblResolutionXY.setBorder(BorderFactory.createEtchedBorder());
		lblResolutionZ.setBorder(BorderFactory.createEtchedBorder());

		if (luts != null)
			cmbLUT = new JComboBox(luts);

		if (types != null)
			cmbType = new JComboBox(types);

		// List Panel
		JPanel[] pn = new JPanel[3];
		DefaultListModel model[] = new DefaultListModel[3];
		model[0] = new DefaultListModel();
		model[1] = new DefaultListModel();
		model[2] = new DefaultListModel();
		for (int i = 0; i < 5; i++)
			model[0].addElement(psfs.get(i).getFullname());
		for (int i = 5; i < 13; i++)
			model[1].addElement(psfs.get(i).getFullname());
		for (int i = 13; i < 15; i++)
			model[2].addElement(psfs.get(i).getFullname());

		for (int i = 0; i < 3; i++) {
			list[i] = new JList(model[i]);
			list[i].setVisibleRowCount(4);
			list[i].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			Font font = list[i].getFont();
			list[i].setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()));
			list[i].setSelectedIndex(0);

			JScrollPane scrollList = new JScrollPane(list[i]);
			pn[i] = new JPanel();
			scrollList.setBorder(BorderFactory.createEmptyBorder());
			pn[i].setLayout(new BoxLayout(pn[i], BoxLayout.Y_AXIS));
			pn[i].setBorder(BorderFactory.createEmptyBorder());
			pn[i].add(scrollList);
		}

		pnPSF = new JPanel(new CardLayout());

		for (PSF psf : psfs) {
			JPanel panel = psf.buildPanel(settings);
			HTMLPane info = new HTMLPane();
			info.append(psf.getDescription());
			DoubleScrollablePanel card = new DoubleScrollablePanel(105, info, 80, panel);
			card.setName(psf.getShortname());
			cards.add(card);
			pnPSF.add(card, psf.getShortname());
		}

		tab.setBorder(BorderFactory.createEmptyBorder());
		tab.addTab("Optical Model", pn[0]);
		tab.addTab("Lateral/Axial", pn[1]);
		tab.addTab("OTF", pn[2]);
		tab.addChangeListener(this);

		GridPanel pnOptics = new GridPanel(2);
		pnOptics.place(0, 0, "Wavelength");
		pnOptics.place(0, 1, spnLambda);
		pnOptics.place(0, 2, "nm");
		pnOptics.place(0, 3, "  ");
		pnOptics.place(0, 4, "NA");
		pnOptics.place(0, 5, spnNA);

		pnOptics.place(2, 0, "Pixelsize XY");
		pnOptics.place(2, 1, spnResLateral);
		pnOptics.place(2, 2, "nm");
		pnOptics.place(2, 3, "  ");
		pnOptics.place(2, 4, "Z-step");
		pnOptics.place(2, 5, spnResAxial);
		pnOptics.place(2, 6, "nm");

		pnOptics.place(3, 0, "FWHM XY");
		pnOptics.place(3, 1, lblResolutionXY);
		pnOptics.place(3, 2, "nm");
		pnOptics.place(3, 3, "  ");
		pnOptics.place(3, 4, "FWHM Z");
		pnOptics.place(3, 5, lblResolutionZ);
		pnOptics.place(3, 6, "nm");

		// Output Panel
		GridPanel pnOut = new GridPanel(2);
		pnOut.place(1, 0, "Size XYZ");
		pnOut.place(1, 1, spnNX);
		pnOut.place(1, 2, spnNY);
		pnOut.place(1, 3, spnNZ);

		pnOut.place(3, 0, new JLabel("Display"));
		pnOut.place(3, 1, cmbScale);
		if (cmbType != null)
			pnOut.place(3, 2, cmbType);

		if (cmbLUT != null)
			pnOut.place(3, 3, cmbLUT);

		JPanel pnButtons = new JPanel(new GridLayout(1, 7));
		if (buttons.get("run") != null)
			pnButtons.add(buttons.get("run"));
		if (buttons.get("stop") != null)
			pnButtons.add(buttons.get("stop"));
		if (buttons.get("settings") != null)
			pnButtons.add(buttons.get("settings"));
		if (buttons.get("save") != null)
			pnButtons.add(buttons.get("save"));
		if (buttons.get("export") != null)
			pnButtons.add(buttons.get("export"));
		if (buttons.get("help") != null)
			pnButtons.add(buttons.get("help"));

		GridPanel pnMain = new GridPanel(false, 6);
		pnMain.place(0, 0, tab);
		pnMain.place(1, 0, pnPSF);
		pnMain.place(2, 0, pnOptics);
		pnMain.place(4, 0, pnOut);
		pnMain.place(5, 0, pnButtons);

		GridToolbar pnBase = new GridToolbar(false, 0);
		pnBase.setLayout(new BoxLayout(pnBase, BoxLayout.X_AXIS));
		if (buttons.get("about") != null)
			pnBase.place(0, 0, buttons.get("about"));
		pnBase.place(0, 1, progress);
		if (buttons.get("close") != null)
			pnBase.place(0, 2, buttons.get("close"));

		pnApplication = new JPanel(new CardLayout());
		pnApplication.add(pnMain, "Back");
		pnApplication.add(About.getPanel(pnMain.getWidth(), pnMain.getHeight()), "About");
		pnBase.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, progress.getBackground()));
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(pnApplication);
		add(pnBase);

		// Add Listeners
		for (String key : buttons.keySet())
			buttons.get(key).addActionListener(this);

		spnNA.addChangeListener(this);
		spnLambda.addChangeListener(this);
		spnResLateral.addChangeListener(this);
		spnResAxial.addChangeListener(this);
		list[0].addListSelectionListener(this);
		list[1].addListSelectionListener(this);
		list[2].addListSelectionListener(this);

		settings.record("NX", spnNX, "256");
		settings.record("NY", spnNY, "256");
		settings.record("NZ", spnNZ, "65");
		settings.record("ResLateral", spnResLateral, "100");
		settings.record("ResAxial", spnResAxial, "250");
		settings.record("NA", spnNA, "1.4");
		settings.record("Lambda", spnLambda, "610");
		if (cmbType != null)
			settings.record("Type", cmbType, "" + cmbType.getItemAt(0));
		if (cmbLUT != null)
			settings.record("LUT", cmbLUT, "Special PSF");
		settings.record("Scale", cmbScale, "Linear");
		settings.loadRecordedItems();
		this.setSelectedPSF(settings.loadValue("PSF-shortname", "BW"));
		updateInterface();
		setEnabledRun(true);
	}

	public PSF getLastPSF() {
		return psf;
	}

	public void success() {
		progress.progress(100, "End of PSF");
	}

	public void setEnabledRun(boolean enabled) {
		if (buttons.get("run") != null)
			buttons.get("run").setEnabled(enabled);
		if (buttons.get("stop") != null)
			buttons.get("stop").setEnabled(!enabled);
	}

	public void onClosed() {
		list[0].removeListSelectionListener(this);
		list[1].removeListSelectionListener(this);
		list[2].removeListSelectionListener(this);
		for (String key : buttons.keySet())
			buttons.get(key).removeActionListener(this);
	}

	public int getSelectedType() {
		return (cmbType == null ? 0 : cmbType.getSelectedIndex());
	}

	public int getSelectedLUT() {
		return (cmbLUT == null ? 0 : cmbLUT.getSelectedIndex());
	}

	public int getSelectedPSF() {
		int itab = tab.getSelectedIndex();
		int selection = list[itab].getSelectedIndex();
		return (itab == 0 ? selection : (itab == 1 ? selection + 5 : selection + 13));
	}

	public String getSelectedPSFShortname() {
		return psfs.get(getSelectedPSF()).getShortname();
	}

	public void setSelectedPSF(String shortname) {
		int index = 0;
		for (int i = 0; i < psfs.size(); i++)
			if (psfs.get(i).getShortname().equals(shortname)) {
				index = i;
				break;
			}
		if (index < 5) {
			tab.setSelectedIndex(0);
			list[0].setSelectedIndex(index);
		}
		else if (index < 14) {
			tab.setSelectedIndex(1);
			list[1].setSelectedIndex(index - 5);
		}
		else {
			tab.setSelectedIndex(2);
			list[2].setSelectedIndex(index - 13);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		CardLayout cl = (CardLayout) (pnPSF.getLayout());
		cl.show(pnPSF, cards.get(getSelectedPSF()).getName());
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		JButton bn = (JButton) e.getSource();
		if (bn == buttons.get("stop")) {
			if (psf != null) {
				psf.abort();
				progress.print("" + psf.getShortname() + " stopped");
			}
		}
		else if (bn == buttons.get("about")) {
			CardLayout cl = (CardLayout) (pnApplication.getLayout());
			cl.show(pnApplication, bn.getText());
			if (bn.getText().equals("About")) {
				bn.setText("Back");
				bn.setToolTipText("Back to " + About.title());
			}
			else {
				bn.setText("About");
				bn.setToolTipText("Back to the application");
			}
		}

		else if (bn == buttons.get("help"))
			WebBrowser.open(About.url());

		else if (bn == buttons.get("settings"))
			new SettingsFileDialog(settings);

		else if (bn == buttons.get("run")) {
			compute(ExecutionMode.MULTITHREAD_ASYNCHRONIZED);
		}
	}

	public void compute(ExecutionMode mode) {
		int index = getSelectedPSF();
		if (index < 0)
			return;
		int nx = spnNX.get();
		int ny = spnNY.get();
		int nz = spnNZ.get();
		int type = (cmbType == null ? 0 : cmbType.getSelectedIndex());
		int scale = (cmbScale == null ? 0 : cmbScale.getSelectedIndex());
		setEnabledRun(false);

		psf = psfs.get(index);

		psf.setOpticsParameters(spnNA.get(), spnLambda.get());
		psf.setResolutionParameters(spnResLateral.get(), spnResAxial.get());
		psf.setOutputParameters(nx, ny, nz, type, scale);
		Pool pool = new Pool("Main", responder);
		pool.register(psf);
		pool.execute(mode);
	}

	public void finish() {
		progress.progress(100, "End of " + psf.getShortname());
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == tab) {
			CardLayout cl = (CardLayout) (pnPSF.getLayout());
			cl.show(pnPSF, cards.get(getSelectedPSF()).getName());
		}
		updateInterface();
	}

	private void updateInterface() {
		double z = 2 * spnLambda.get() / (spnNA.get() * spnNA.get());
		String znm = (new DecimalFormat("###.#")).format(z);
		double xy = 0.61 * spnLambda.get() / spnNA.get();
		String snm = (new DecimalFormat("###.#")).format(xy);
		lblResolutionXY.setText(snm);
		lblResolutionZ.setText(znm);
	}
}
