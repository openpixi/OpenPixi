/*
 * OpenPixi - Open Particle-In-Cell (PIC) Simulator
 * Copyright (C) 2012  OpenPixi.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openpixi.pixi.ui.panel.gl;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.Box;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.panel.properties.ComboBoxProperties;
import org.openpixi.pixi.ui.panel.properties.CoordinateProperties;
import org.openpixi.pixi.ui.panel.properties.ScaleProperties;


/**
 * Displays 2D energy density.
 */
public class EnergyDensity2DGLPanel extends AnimationGLPanel {

	public static final int INDEX_ENERGY_DENSITY = 0;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE = 1;
	public static final int INDEX_NABLA_POYNTING = 2;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE_NABLA_POYNTING = 3;
	public static final int INDEX_CURRENT_ELECTRIC_FIELD = 4;
	public static final int INDEX_ENERGY_DENSITY_DERIVATIVE_NABLA_POYNTING_CURRENT = 5;

	String[] dataLabel = new String[] {
			"Energy density",
			"dE/dt",
			"nabla S",
			"dE/dt + nabla S",
			"j*E",
			"dE/dt + nabla S + j*E"
	};

	public static final int RED = 0;
	public static final int GREEN = 1;
	public static final int BLUE = 2;

	public ComboBoxProperties dataProperties;
	public ScaleProperties scaleProperties;
	public CoordinateProperties showCoordinateProperties;

	private double[] oldEnergyDensity;
	private int[] oldTime;
	private double[] currentEnergyDensity;
	private int[] currentTime;

	/** Constructor */
	public EnergyDensity2DGLPanel(SimulationAnimation simulationAnimation) {
		super(simulationAnimation);
		dataProperties = new ComboBoxProperties(simulationAnimation, "Data", dataLabel, 0);
		scaleProperties = new ScaleProperties(simulationAnimation);
		scaleProperties.setAutomaticScaling(true);
		showCoordinateProperties = new CoordinateProperties(simulationAnimation, CoordinateProperties.Mode.MODE_2D);
	}

	@Override
	public void display(GLAutoDrawable glautodrawable) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		int width = glautodrawable.getWidth();
		int height = glautodrawable.getHeight();
		gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
		gl2.glLoadIdentity();

		double scale = scaleProperties.getScale();
		scaleProperties.resetAutomaticScale();
		Simulation s = getSimulationAnimation().getSimulation();

		int xAxisIndex = showCoordinateProperties.getXAxisIndex();
		int yAxisIndex = showCoordinateProperties.getYAxisIndex();
		int pos[] = showCoordinateProperties.getPositions();

		/** Scaling factor for the displayed panel in x-direction*/
		double sx = width / s.getSimulationBoxSize(xAxisIndex);
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = height / s.getSimulationBoxSize(yAxisIndex);

		double[] color = new double[3];

		int dataIndex = dataProperties.getIndex();

		for(int i = 0; i < s.grid.getNumCells(xAxisIndex); i++) {

			gl2.glBegin( GL2.GL_QUAD_STRIP );
			for(int k = 0; k < s.grid.getNumCells(yAxisIndex); k++)
			{
				//float xstart = (float) (s.grid.getLatticeSpacing() * (i + 0.5) * sx);
				float xstart2 = (float)(s.grid.getLatticeSpacing() * i * sx);
				float xstart3 = (float)(s.grid.getLatticeSpacing() * (i + 1) * sx);
				//float ystart = (float) (s.grid.getLatticeSpacing() * (k + 0.5) * sy);
				float ystart2 = (float) (s.grid.getLatticeSpacing() * k * sy);

				pos[xAxisIndex] = i;
				pos[yAxisIndex] = k;
				int index = s.grid.getCellIndex(pos);

				double value = 0;
				color[RED] = 0;
				color[GREEN] = 0;
				color[BLUE] = 0;
				if(s.grid.isEvaluatable(index)) {
					switch(dataIndex) {
					case INDEX_ENERGY_DENSITY:
						value = getEnergyDensity(s, index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE:
						value = getEnergyDensityDerivative(s, index);
						break;
					case INDEX_NABLA_POYNTING:
						value = getNablaPoyntingVector2(s, index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE_NABLA_POYNTING:
						value = getEnergyDensityDerivative(s, index)
							+ getNablaPoyntingVector2(s, index);
						break;
					case INDEX_CURRENT_ELECTRIC_FIELD:
						value = getCurrentElectricField(s, index);
						break;
					case INDEX_ENERGY_DENSITY_DERIVATIVE_NABLA_POYNTING_CURRENT:
						value = getEnergyDensityDerivative(s, index)
							+ getNablaPoyntingVector2(s, index)
							+ getCurrentElectricField(s, index);
						break;
					}
					getColorFromEField(s, index, color);
				}
				// Normalize
				double norm = Math.max(color[RED] + color[GREEN] + color[BLUE], 10E-20);
				double limitedValue = Math.min(1, scale * Math.abs(value));

				// Set color according to E-field, and brightness according
				// to total energy density:
				color[RED] = Math.sqrt(color[RED] / norm) * limitedValue;
				color[GREEN] = Math.sqrt(color[GREEN] / norm) * limitedValue;
				color[BLUE] = Math.sqrt(color[BLUE] / norm) * limitedValue;

				scaleProperties.putValue(value);

				gl2.glColor3d( color[RED], color[GREEN], color[BLUE] );
				gl2.glVertex2f( xstart2, ystart2 );
				gl2.glVertex2f( xstart3, ystart2 );
			}
			gl2.glEnd();
		}
		scaleProperties.calculateAutomaticScale(1.0);
	}

	private double getEnergyDensity(Simulation s, int index) {
		double value = 0;

		// Lattice spacing and coupling constant
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			value += s.grid.getEsquaredFromLinks(index, w);
			// Time averaging for B field.
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 0);
			value += 0.5 * s.grid.getBsquaredFromLinks(index, w, 1);
		};
		return value / (as * g * as * g) / 2;
	}

	private double getEnergyDensityDerivative(Simulation s, int index) {
		double value = 0;
		if (oldEnergyDensity == null
				|| index > oldEnergyDensity.length) {
			// Initialize arrays
			int cells = s.grid.getTotalNumberOfCells();
			oldEnergyDensity = new double[cells];
			oldTime = new int[cells];
			currentEnergyDensity = new double[cells];
			currentTime = new int[cells];
		}
		if (currentTime[index] > s.totalSimulationSteps) {
			// Reset values (in case of a simulation reset)
			oldEnergyDensity[index] = 0;
			oldTime[index] = 0;
			currentEnergyDensity[index] = getEnergyDensity(s, index);
			currentTime[index] = s.totalSimulationSteps;
		}
		if (currentTime[index] < s.totalSimulationSteps) {
			// Update values
			oldEnergyDensity[index] = currentEnergyDensity[index];
			oldTime[index] = currentTime[index];
			currentEnergyDensity[index] = getEnergyDensity(s, index);
			currentTime[index] = s.totalSimulationSteps;
		}
		if (oldTime[index] != 0) {
			// Calculate derivative
			double deltaTime = (currentTime[index] - oldTime[index]) * s.tstep;
			value = (currentEnergyDensity[index] - oldEnergyDensity[index]) / deltaTime;
		}
		return value;
	}

	private double getNablaPoyntingVector(Simulation s, int index) {
		double as = s.grid.getLatticeSpacing();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			int indexShifted1 = s.grid.shift(index, direction, 1);
			if (!s.grid.isEvaluatable(indexShifted1)) {
				return 0;
			}
			int indexShifted2 = s.grid.shift(index, direction, -1);
			if (!s.grid.isEvaluatable(indexShifted2)) {
				return 0;
			}
			value += getPoyntingVector(s, indexShifted1, direction)
					- getPoyntingVector(s, indexShifted2, direction);
		}
		return value / (2*as);
	}

	private double getPoyntingVector(Simulation s, int index, int direction) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		// fields at same time:
		AlgebraElement E1 = s.grid.getE(index, dir1);
		AlgebraElement E2 = s.grid.getE(index, dir2);
		// time averaged B-field:
		AlgebraElement B1 = s.grid.getB(index, dir1, 0).add(s.grid.getB(index, dir1, 1)).mult(0.5);
		AlgebraElement B2 = s.grid.getB(index, dir2, 0).add(s.grid.getB(index, dir2, 1)).mult(0.5);
		double S = E1.mult(B2) - E2.mult(B1);
		return S / (as * g * as * g);
	}

	private double getNablaPoyntingVector2(Simulation s, int index) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;
		if (s.getNumberOfDimensions() != 3) {
			throw new RuntimeException("Dimension other than 3 has not been implemented yet.");
			// TODO: Implement for arbitrary dimensions
			// return 0;
		}
		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement rotE = rotE(s, index, direction);
			AlgebraElement rotB0 = rotB(s, index, direction, 0);
			AlgebraElement rotB1 = rotB(s, index, direction, 1);

			if (rotE == null || rotB0 == null || rotB1 == null) {
				// One of the neighbouring cells is not evaluatable.
				return 0;
			}

			AlgebraElement E = s.grid.getE(index, direction);
			// time averaged B-fields:
			AlgebraElement B = (s.grid.getB(index, direction, 0).add(s.grid.getB(index, direction, 0))).mult(0.5);
			AlgebraElement rotB = (rotB0.add(rotB1)).mult(0.5);

			value += E.mult(rotB) - B.mult(rotE);
		}
		return value / (as * g);
	}

	private AlgebraElement rotB(Simulation s, int index, int direction, int timeIndex) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		int indexShifted1 = s.grid.shift(index, dir1, -1);
		if (!s.grid.isEvaluatable(indexShifted1)) {
			return null;
		}

		int indexShifted2 = s.grid.shift(index, dir2, -1);
		if (!s.grid.isEvaluatable(indexShifted2)) {
			return null;
		}

		AlgebraElement By1 = s.grid.getB(index, dir1, timeIndex);
		AlgebraElement By2 = s.grid.getB(indexShifted2, dir1, timeIndex);
		AlgebraElement Bz1 = s.grid.getB(index, dir2, timeIndex);
		AlgebraElement Bz2 = s.grid.getB(indexShifted1, dir2, timeIndex);

		// By2 - By1
		AlgebraElement dBy = (By2.add(By1.mult(-1)));

		// Bz2 - Bz1
		AlgebraElement dBz = (Bz2.add(Bz1.mult(-1)));

		// dBy - dBz
		return (dBy.add(dBz.mult(-1))).mult(-1 / (as * as * g));
	}

	private AlgebraElement rotE(Simulation s, int index, int direction) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		// Indices for cross product:
		int dir1 = (direction + 1) % 3;
		int dir2 = (direction + 2) % 3;

		int indexShifted1 = s.grid.shift(index, dir1, 1);
		if (!s.grid.isEvaluatable(indexShifted1)) {
			return null;
		}

		int indexShifted2 = s.grid.shift(index, dir2, 1);
		if (!s.grid.isEvaluatable(indexShifted2)) {
			return null;
		}

		AlgebraElement Ey1 = s.grid.getE(index, dir1);
		AlgebraElement Ey2 = s.grid.getE(indexShifted2, dir1);
		AlgebraElement Ez1 = s.grid.getE(index, dir2);
		AlgebraElement Ez2 = s.grid.getE(indexShifted1, dir2);

		// Ey2 - Ey1
		AlgebraElement dEy = (Ey2.add(Ey1.mult(-1)));

		// Ez2 - Ez1
		AlgebraElement dEz = (Ez2.add(Ez1.mult(-1)));

		// dEy - dEz
		return (dEy.add(dEz.mult(-1))).mult(1 / (as * as * g));
	}

	private double getCurrentElectricField(Simulation s, int index) {
		double as = s.grid.getLatticeSpacing();
		double g = s.getCouplingConstant();

		double value = 0;

		for (int direction = 0; direction < s.grid.getNumberOfDimensions(); direction++) {
			AlgebraElement J = s.grid.getJ(index, direction);
			AlgebraElement E = s.grid.getE(index, direction);
			value += J.mult(E);
		}
		return value / (as * g * as * g);
	}

	private void getColorFromEField(Simulation s, int index,
			double[] color) {
		int colors = s.grid.getNumberOfColors();
		for (int w = 0; w < s.getNumberOfDimensions(); w++) {
			// get color:
			double c;
			for (int n = 0; n < colors * colors - 1; n++) {
				c = s.grid.getE(index, w).get(n);
				// cycle through colors if there are more than three
				switch (n % 3) {
					case 0:
						color[RED] += c * c;
						break;
					case 1:
						color[GREEN] += c * c;
						break;
					case 2:
						color[BLUE] += c * c;
						break;
				}
			}
		}
	}

	public void addPropertyComponents(Box box) {
		addLabel(box, "Energy density 2D (OpenGL) panel");
		dataProperties.addComponents(box);
		scaleProperties.addComponents(box);
		showCoordinateProperties.addComponents(box);
	}
}