PSFGenerator
============

[**A Java software package to generate realistic 3D microscope Point-Spread Function (PSF)**](http://bigwww.epfl.ch/algorithms/psfgenerator/)

_Written by Hagai Kirshner and Daniel Sage at the Biomedical Image Group (BIG), EPFL, Switzerland_

## Outline
PSF Generator is a Java software package that allows one to generate and visualize various 3D models of a microscope PSF. The current version has more than fifteen different models:
* 3D diffractive models: scalar-based diffraction model Born & Wolf, scalar-based diffraction model with 3 layers Gibson & Lanni, and vectorial-based model Richards & Wolf, and Variable Refractive Index Gibson & Lanni model.
* Defocussing a 2D lateral function with 1D axial function: the available lateral functions are: "Gaussian", "Lorentz", "Cardinale-Sine", "Cosine", "Circular-Pupil", "Astigmatism", "Oriented-Gaussian", "Double-Helix".
* Optical Transfer Function generated in the Fourier domain: Koehler simulation, defocus simulation.

PSF Generator is provided for several environments: as ImageJ/Fiji plugin, as an Icy plugin, and as a Java standalone application. The program requires only few parameters which are readily-available for microscopy practitioners. Our Java implementation achieves fast execution times, as it is based on multi-threading the computational tasks and on a numerical method that adapts to the oscillatory nature of the required integrands. Potential applications are 3D deconvolution, 3D particle localization and tracking, and extended depth of field estimation to name a few.

## References

* H. Kirshner, F. Aguet, D. Sage, M. Unser [3-D PSF Fitting for Fluorescence Microscopy: Implementation and Localization Application](http://bigwww.epfl.ch/publications/kirshner1301.html) Journal of Microscopy, 2013.
* A. Griffa, N. Garin and D. Sage, [Comparison of Deconvolution Software in 3D Microscopy. A User Point of View](http://bigwww.epfl.ch/publications/griffa1001.html), G.I.T. Imaging & Microscopy, 2010.
* D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz, R. Guiet, C. Vonesch, M. Unser [DeconvolutionLab2: An Open-Source Software for Deconvolution Microscopy](http://bigwww.epfl.ch/publications/sage1701.html) Methods—Image Processing for Biologists, 2017.