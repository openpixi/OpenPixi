package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.gauge.DoubleFFTWrapper;
import edu.emory.mathcs.jtransforms.fft.*;
import org.openpixi.pixi.physics.grid.SU2Field;
import org.openpixi.pixi.physics.grid.YMField;

public class LightConePoissonSolver {

	//private DoubleFFTWrapper fft;
	private int dir;
	private int[] pos;

	public LightConePoissonSolver(int[] position, int direction) {
		this.pos = position;
		this.dir = direction;
	}

	public void solve(Grid g) {

		int truesize = 0;
		for(int i = 0; i < g.getNumberOfDimensions(); i++) {
			if( (i != dir) && (g.getNumCells(i) > 1) ) {
				truesize++;
			} else {}
		}
		int[] size = new int[truesize];
		int[] signature = new int[truesize];
		int k = 0;
		for(int i = 0; i < g.getNumberOfDimensions(); i++) {
			if( (i != dir) && (g.getNumCells(i) > 1) ) {
				size[k] = g.getNumCells(i);
				signature[k] = i;
				k++;
			} else {}
		}
		//fft = new DoubleFFTWrapper(size);
		int numberOfComponents = g.getNumberOfColors() * g.getNumberOfColors() - 1;

		double norm = Math.pow(g.getLatticeSpacing(), g.getNumberOfDimensions() - 1);

		if (size.length != 1) {
			double[][] charge = new double[size[0]][2 * size[1]];
			double[][] current = new double[size[0]][2 * size[1]];
			YMField[][] gaugeList = new YMField[size[0]][size[1]];
			YMField[][] E0List = new YMField[size[0]][size[1]];
			YMField[][] E1List = new YMField[size[0]][size[1]];
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					gaugeList[j][w] = new SU2Field();
					E0List[j][w] = new SU2Field();
					E1List[j][w] = new SU2Field();
				}
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						charge[j][2*w] = g.getRho(g.getCellIndex(pos)).get(i);
						charge[j][2*w + 1] = 0.0;
						current[j][2*w] = g.getJ(g.getCellIndex(pos), dir).get(i);
						current[j][2*w + 1] = 0.0;
					}
				}
				//perform Fourier transformation
				DoubleFFT_2D fft = new DoubleFFT_2D(size[0], size[1]);
				fft.complexForward(charge);
				fft.complexForward(current);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						double psqr = (4 - 2 * Math.cos((2 * Math.PI * j) / size[0]) - 2 * Math.cos((2 * Math.PI * w) / size[1]))/norm;
						if( (j+w) != 0 ) {
							charge[j][2*w] = charge[j][2*w]/psqr;
							charge[j][2*w + 1] = charge[j][2*w + 1]/psqr;
							current[j][2*w] = current[j][2*w]/psqr;
							current[j][2*w + 1] = current[j][2*w + 1]/psqr;
						}
					}
				}
				charge[0][0] = 0.0;
				charge[0][1] = 0.0;
				current[0][0] = 0.0;
				current[0][1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				fft.complexInverse(current, true);
				//compute the values of the gauge field in the direction of the current and the values of the electric field
				for(int j = 0; j < size[0]; j++) {
					for (int w = 0; w < size[1]; w++) {
						pos[signature[0]] = j;
						pos[signature[1]] = w;
						gaugeList[j][w].set(i, current[j][2*w]);
						E0List[j][w].set(i, -(charge[(j + 1) % size[0]][2 * w] - charge[j][2*w]) / g.getLatticeSpacing());
						E1List[j][w].set(i, -(charge[j][ 2 * ((w + 1) % size[1]) ] - charge[j][2*w]) / g.getLatticeSpacing());
					}
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			for(int j = 0; j < size[0]; j++) {
				for (int w = 0; w < size[1]; w++) {
					pos[signature[0]] = j;
					pos[signature[1]] = w;
					g.setU(g.getCellIndex(pos), dir, gaugeList[j][w].getLinkExact());
					g.setE(g.getCellIndex(pos), signature[0], E0List[j][w]);
					g.setE(g.getCellIndex(pos), signature[1], E1List[j][w]);
				}
			}

		} else {
			double[] charge = new double[2 * size[0]];
			double[] current = new double[2 * size[0]];
			YMField[] gaugeList = new YMField[size[0]];
			YMField[] E0List = new YMField[size[0]];
			for(int j = 0; j < size[0]; j++) {
				gaugeList[j] = new SU2Field();
				E0List[j] = new SU2Field();
			}

			for(int i = 0; i < numberOfComponents; i++) {
				//prepare input for fft
				for(int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;
					charge[2*j] = g.getRho(g.getCellIndex(pos)).get(i);
					charge[2*j + 1] = 0.0;
					current[2*j] = g.getJ(g.getCellIndex(pos), dir).get(i);
					current[2*j + 1] = 0.0;
				}
				//perform Fourier transformation
				DoubleFFT_1D fft = new DoubleFFT_1D(size[0]);
				fft.complexForward(charge);
				fft.complexForward(current);
				//perform computation in Fourier space
				for(int j = 0; j < size[0]; j++) {
					double psqr = (2 - 2 * Math.cos((2 * Math.PI * j) / size[0]))/norm;
						if( j != 0 ) {
							charge[2*j] = charge[2*j]/psqr;
							charge[2*j + 1] = charge[2*j + 1]/psqr;
							current[2*j] = current[2*j]/psqr;
							current[2*j + 1] = current[2*j + 1]/psqr;
						}
				}
				charge[0] = 0.0;
				charge[1] = 0.0;
				current[0] = 0.0;
				current[1] = 0.0;
				//perform inverse Fourier transform
				fft.complexInverse(charge, true);
				fft.complexInverse(current, true);
				//compute the values of the gauge field in the direction of the current and the values of the electric field
				for (int j = 0; j < size[0]; j++) {
					pos[signature[0]] = j;
					gaugeList[j].set(i, current[2*j]);
					E0List[j].set(i, -(charge[ 2*((j + 1) % size[0]) ] - charge[2*j]) / g.getLatticeSpacing());
				}
			}
			//set the values of the gauge field in the direction of the current and the values of the electric field
			for(int j = 0; j < size[0]; j++) {
				pos[signature[0]] = j;
				g.setU(g.getCellIndex(pos), dir, gaugeList[j].getLinkExact());
				g.setE(g.getCellIndex(pos), signature[0], E0List[j]);
			}
		}


	}

}
