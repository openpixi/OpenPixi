package org.openpixi.pixi.physics.fields;

import org.openpixi.pixi.parallel.cellaccess.CellAction;
import org.openpixi.pixi.physics.grid.Grid;
import org.openpixi.pixi.math.AlgebraElement;
import org.openpixi.pixi.math.GroupElement;

public class TemporalYangMillsSolver extends FieldSolver
{

	private double timeStep;
	private UpdateFields fieldUpdater = new UpdateFields();
	private UpdateLinks linkUpdater = new UpdateLinks();

	@Override
	public FieldSolver clone() {
		TemporalYangMillsSolver clone = new TemporalYangMillsSolver();
		clone.copyBaseClassFields(this);
		clone.timeStep = timeStep;
		clone.fieldUpdater = fieldUpdater;
		clone.linkUpdater = linkUpdater;
		return clone;
	}

	@Override
	public void step(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		fieldUpdater.at = timeStep;
		fieldUpdater.as = grid.getLatticeSpacing();
		fieldUpdater.g = grid.getGaugeCoupling();
		linkUpdater.at = timeStep;
		linkUpdater.as = grid.getLatticeSpacing();
		cellIterator.execute(grid, fieldUpdater);
		cellIterator.execute(grid, linkUpdater);
	}

	@Override
	public void stepLinks(Grid grid, double timeStep) {
		this.timeStep = timeStep;
		linkUpdater.at = timeStep;
		linkUpdater.as = grid.getLatticeSpacing();
		cellIterator.execute(grid, linkUpdater);
	}


	private class UpdateFields implements CellAction
	{
		private double as;
		private double at;
		private double g;

		/**
		 * Updates the electric fields at a given coordinate
		 *
		 * @param index  Lattice index
		 */
		public void execute(Grid grid, int index) {
			for(int i = 0; i < grid.getNumberOfDimensions(); i++)
			{
				GroupElement temp = grid.getElementFactory().groupZero();
				for(int j = 0; j < grid.getNumberOfDimensions(); j++)
				{
					if(j != i)
					{
						temp.addAssign(grid.getPlaquette(index, i, j, 1 , 1, 0));
						temp.addAssign(grid.getPlaquette(index, i, j, 1 , -1, 0));
					}
				}

				AlgebraElement currentE = grid.getE(index, i).add(temp.proj().mult(at / (as * as )));
				//currentE.addAssign(grid.getJ(index, i).mult(-at / as));  // TODO: check factor 1/as !!!
				currentE.addAssign(grid.getJ(index, i).mult(-at));
				grid.setE(index, i, currentE);
			}
		}
	}

	private class UpdateLinks implements CellAction {

		private double as;
		private double at;

		/**
		 * Updates the links matrices in a given cell.
		 * @param grid	Reference to the grid
		 * @param index	Cell index
		 */
		public void execute(Grid grid, int index) {

			GroupElement V;

			for(int k=0;k<grid.getNumberOfDimensions();k++) {

				V = grid.getE(index, k).mult(-at).getLink();	//minus sign takes take of conjugation
				grid.setUnext( index, k, V.mult(grid.getU(index, k)) );

			}
		}
	}
}