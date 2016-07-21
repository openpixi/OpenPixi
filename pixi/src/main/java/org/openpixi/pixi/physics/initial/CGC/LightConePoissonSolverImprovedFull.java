package org.openpixi.pixi.physics.initial.CGC;

import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.physics.util.GridFunctions;

/**
 * This class solves the transverse Poisson equation for a three-dimensional (Lorenz gauge) charge density
 * 'sheet by sheet' in the longitudinal direction and then initializes the fields in the temporal gauge.
 * It implements an improved method of computing the Wilson lines which reduces spurious longitudinal fields.
 */
public class LightConePoissonSolverImprovedFull implements ICGCPoissonSolver {

	Simulation s;
	AlgebraElement[] gaussViolation;

	/**
	 * Initializes the LightConePoissonSolver. Used to solve the transverse Poisson equation 'sheer by sheet'.
	 * @param s Reference to the Simulation object
	 */
	public void initialize(Simulation s) {

		this.s = s;
	}

	/**
	 * Solves the Poisson equation in the transverse plane for a given 3D charge density distribution. Initializes all
	 * fields (U, Unext and E) and computes the Gauss constraint, which is used to spawn particles.
	 *
	 * @param chargeDensity Reference to an IInitialChargeDensity object.
	 */
	public void solve(IInitialChargeDensity chargeDensity) {
		AlgebraElement[] phi0;
		GroupElement[] V;

		int direction = chargeDensity.getDirection();
		int orientation = chargeDensity.getOrientation();

		int longitudinalNumCells = s.grid.getNumCells(direction);
		int[] transverseNumCells = GridFunctions.reduceGridPos(s.grid.getNumCells(), direction);
		int totalTransverseCells = GridFunctions.getTotalNumberOfCells(transverseNumCells);
		int numberOfColors = s.getNumberOfColors();
		int numberOfComponents = (numberOfColors > 1) ? numberOfColors * numberOfColors - 1 : 1;

		// Solve for phi at t = - at/2 'sheet by sheet'
		phi0 = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			phi0[i] = s.grid.getElementFactory().algebraZero();
		}

		for (int z = 0; z < longitudinalNumCells; z++) {
			for (int c = 0; c < numberOfComponents; c++) {
				// Prepare 2D charge density.
				double[] rho2D = new double[totalTransverseCells];
				for (int i = 0; i < totalTransverseCells; i++) {
					int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
					int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
					int index = s.grid.getCellIndex(gridPos);

					rho2D[i] = chargeDensity.getChargeDensity(index).get(c);
				}

				// Solve Poisson equation
				double[] phi2D = FourierFunctions.solvePoisson2D(rho2D, transverseNumCells, s.grid.getLatticeSpacing());

				// Put result into phi0.
				for (int i = 0; i < totalTransverseCells; i++) {
					int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
					int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
					int index = s.grid.getCellIndex(gridPos);

					// Already include gauge factor for Wilson line here.
					phi0[index].set(c, phi2D[i]);
				}
			}
		}

		// Compute V at t = - at/2 by constructing the Wilson line from gauge links.
		// New interpretation of the field phi: It sits in between two lattice points (staggered grid).
		// Convention: phi[n + 0.5] is stored at grid point n.
		V = new GroupElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			V[i] = s.grid.getElementFactory().groupIdentity();
		}

		int M = 16;
		for (int k = 0; k < longitudinalNumCells; k++) {
			int z = (orientation < 0) ? k : (longitudinalNumCells - k - 1);
			for (int i = 0; i < totalTransverseCells; i++) {
				// Current position
				int[] transGridPos = GridFunctions.getCellPos(i, transverseNumCells);
				int[] gridPos = GridFunctions.insertGridPos(transGridPos, direction, z);
				int index = s.grid.getCellIndex(gridPos);

				// Last position in longitudinal direction at same transverse position.
				int indexL = s.grid.shift(index, direction, orientation);

				// Compute V from V directly behind it in the longitudinal direction using phi between two grid points.
				// Staggered grid: for orientation +1, do not shift. For orientation -1, shift in 'backwards'.
				GroupElement gaugeLink = V[indexL].copy();

				GroupElement W;
				if(orientation == -1) {
					int i3 = index;
					int i2 = s.grid.shift(i3, direction, -1);
					int i1 = s.grid.shift(i2, direction, -1);

					AlgebraElement P1 = phi0[i1];
					AlgebraElement P2 = phi0[i2];
					AlgebraElement P3 = phi0[i3];

					double z1 = z - 1;
					double z2 = z - 0.5;
					double z3 = z;

					W = W2(z1, z2, M / 2, P1, P2);
					W.multAssign(W2(z2, z3, M / 2, P2, P3));
				} else {
					int i2 = index;
					int i3 = s.grid.shift(i2, direction, -1);
					int i1 = s.grid.shift(i2, direction, +1);

					AlgebraElement P1 = phi0[i1];
					AlgebraElement P2 = phi0[i2];
					AlgebraElement P3 = phi0[i3];

					double z1 = z + 1;
					double z2 = z + 0.5;
					double z3 = z;

					W = W2(z1, z2, M / 2, P1, P2);
					W.multAssign(W2(z2, z3, M / 2, P2, P3));
				}

				gaugeLink.multAssign(W);
				V[index] = gaugeLink;
			}
		}

		// Make a copy of the grid. Ugly, but needed for Gauss constraint calculation.
		Grid gridCopy = new Grid(s.grid);
		//gridCopy.createGrid();

		// Set gauge links at t = - at/2
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int d = 0; d < s.getNumberOfDimensions(); d++) {
				if(d != direction) {
					int is = s.grid.shift(i, d, 1);
					GroupElement V1 = V[i];
					GroupElement V2 = V[is];

					GroupElement U = s.grid.getU(i, d);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setU(i, d, V1.mult(U).mult(V2.adj()));
					// Also write to copy of the grid.
					gridCopy.setU(i, d, V1.mult(V2.adj()));
				}
			}
		}

		// Compute V at at/2 from V at -at/2 (improved using linear interpolation and path ordering).

		int Mfrac = (int) (M * s.grid.getTemporalSpacing() / s.grid.getLatticeSpacing() * 0.5);
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			// Compute time evolution operator using linear interpolation of the phi's and path ordering.
			int[] pos = s.grid.getCellPos(i);
			int zi = pos[direction];

			GroupElement W;
			int i1, i2;
			double z1, z2;
			if(orientation == -1) {
				i1 = s.grid.shift(i, direction, -1);
				i2 = i;

				z1 = zi;
				z2 = z1 + s.getTimeStep() / s.grid.getLatticeSpacing();
			} else {
				i1 = s.grid.shift(i, direction, -1);
				i2 = i;

				z1 = zi;
				z2 = z1 - s.getTimeStep() / s.grid.getLatticeSpacing();
			}

			AlgebraElement P1 = phi0[i1];
			AlgebraElement P2 = phi0[i2];

			W = W2(z1, z2, Mfrac, P1, P2);

			V[i].multAssign(W);
		}

		// Set gauge links at t = at/2
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int d = 0; d < s.getNumberOfDimensions(); d++) {
				if(d != direction) {
					int is = s.grid.shift(i, d, 1);
					GroupElement V1 = V[i];
					GroupElement V2 = V[is];

					GroupElement U = s.grid.getUnext(i, d);
					// U_x,i = V_x V_{x+i}^t
					s.grid.setUnext(i, d, V1.mult(U).mult(V2.adj()));
					// Also write to copy of the grid.
					gridCopy.setUnext(i, d, V1.mult(V2.adj()));
				}
			}
		}

		// Compute electric field at t = 0
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			for (int j = 0; j < s.getNumberOfDimensions(); j++) {
				s.grid.setE(i, j, s.grid.getEFromLinks(i, j));
				gridCopy.setE(i, j, gridCopy.getEFromLinks(i, j));
			}
		}

		// Compute Gauss constraint from grid copy
		gaussViolation = new AlgebraElement[s.grid.getTotalNumberOfCells()];
		for (int i = 0; i < s.grid.getTotalNumberOfCells(); i++) {
			if(gridCopy.isActive(i)) {
				this.gaussViolation[i] = gridCopy.getGaussConstraint(i);
			} else {
				this.gaussViolation[i] = gridCopy.getElementFactory().algebraZero();
			}
		}
	}

	private GroupElement W2(double z1, double z2, int M, AlgebraElement P1, AlgebraElement P2) {
		GroupElement W = s.grid.getElementFactory().groupIdentity();

		double dz = Math.abs(z2 - z1) / ((double) M) * s.grid.getLatticeSpacing();
		double gaugeFactor = - s.getCouplingConstant() * dz;
		double ui = Math.round((z1+z2) * 0.5);
		double u1 = z1 - ui;
		double u2 = z2 - ui;

		AlgebraElement PM = P1.add(P2).mult(0.5);
		AlgebraElement PD = P1.sub(P2);

		for (int m = 0; m < M; m++) {
			double u = u1 + (u2 - u1) * (m + 0.5) / ((double) M);
			AlgebraElement P = PM.add(PD.mult(u));
			W.multAssign(P.mult(gaugeFactor).getLink());
		}

		return W;
	}

	public AlgebraElement getGaussViolation(int index) {
		return this.gaussViolation[index];
	}

	public AlgebraElement[] getGaussViolation() {
		return this.gaussViolation;
	}
}
