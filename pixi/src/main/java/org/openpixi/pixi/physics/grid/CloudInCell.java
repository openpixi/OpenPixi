package org.openpixi.pixi.physics.grid;

import org.openpixi.pixi.physics.Debug;
import org.openpixi.pixi.physics.Particle;

public class CloudInCell implements InterpolatorAlgorithm {

	@Override
	public void interpolateToGrid(Particle p, Grid g, double tstep) {
		double cellArea = g.getCellWidth() * g.getCellHeight();

		//Determine the nearest lower left grid point
		//THIS CAN BE NEGATIVE if particle is behind the left or the lower boundary
		int i = (int) Math.floor(p.getX() / g.getCellWidth());
		int j = (int) Math.floor(p.getY() / g.getCellHeight());
		
		/**Distance to the left cell boundary*/
		double a;
		/**Distance to the right cell boundary*/
		double b;
		/**Distance to the lower cell boundary*/
		double c;
		/**Distance to the upper cell boundary*/
		double d;
		
		a = p.getX() - i * g.getCellWidth();
		b = g.getCellWidth() - a;
		c = p.getY() -  j * g.getCellHeight();
		d = g.getCellHeight() - c;
		
		//The Jx-field is located in the middle of the left cell boundary.
		//This means that the Jx-field-grid is shifted upwards by half a cell height.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the vertical plane. All changes are reversed
		//after the calculation.
		if( c < g.getCellHeight()/2 ){
			j -= 1;
			c += g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			
			g.addJx(i, j, p.getCharge() * p.getVx() * b * d / cellArea);
			g.addJx(i,j+1, p.getCharge() * p.getVx() * b * c / cellArea);
			g.addJx(i + 1,j + 1, p.getCharge() * p.getVx() * a * c / cellArea);
			g.addJx(i+1, j, p.getCharge() * p.getVx() * a * d / cellArea);
			
			c -= g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			j += 1;
		} else {
			c -= g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			
			g.addJx(i, j, p.getCharge() * p.getVx() * b * d / cellArea);
			g.addJx(i,j+1, p.getCharge() * p.getVx() * b * c / cellArea);
			g.addJx(i + 1,j + 1, p.getCharge() * p.getVx() * a * c / cellArea);
			g.addJx(i+1, j, p.getCharge() * p.getVx() * a * d / cellArea);
			
			c += g.getCellHeight()/2;
			d = g.getCellHeight() - c;
		}

		//The Jy-field is located in the middle of the lower cell boundary.
		//This means that the Jy-field-grid is shifted to the right by half a cell width.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the horizontal plane. All changes are reversed
		//after the calculation.
		if( a < g.getCellWidth()/2 ){
			i -= 1;
			a += g.getCellWidth()/2;
			b = g.getCellWidth() - a;
			
			g.addJy(i, j, p.getCharge() * p.getVy() * b * d / cellArea);
			g.addJy(i,j+1, p.getCharge() * p.getVy() * b * c / cellArea);
			g.addJy(i + 1,j + 1, p.getCharge() * p.getVy() * a * c / cellArea);
			g.addJy(i+1, j, p.getCharge() * p.getVy() * a * d / cellArea);
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.
		} else {
			a -= g.getCellWidth()/2;
			b = g.getCellWidth() - a;
			
			g.addJy(i, j, p.getCharge() * p.getVy() * b * d / cellArea);
			g.addJy(i,j+1, p.getCharge() * p.getVy() * b * c / cellArea);
			g.addJy(i + 1,j + 1, p.getCharge() * p.getVy() * a * c / cellArea);
			g.addJy(i+1, j, p.getCharge() * p.getVy() * a * d / cellArea);
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.			
		}
	}

	@Override
	public void interpolateChargedensity(Particle p, Grid g) {
		double cellArea = g.getCellWidth() * g.getCellHeight();

		//Determine the nearest lower left grid point on a grid that is shifted
		//upward and to the right by half a cell height and width respectively.
		//THIS CAN BE NEGATIVE if particle is behind the left or the lower boundary
		int i = (int) (Math.floor( p.getX() / g.getCellWidth() - 0.5));
		int j = (int) (Math.floor( p.getY() / g.getCellHeight() - 0.5));
		
		/**Distance to the left cell boundary*/
		double a;
		/**Distance to the right cell boundary*/
		double b;
		/**Distance to the lower cell boundary*/
		double c;
		/**Distance to the upper cell boundary*/
		double d;
		
		//The -0.5*cellWidth is there to shift the grid
		a = p.getX() - (i + 0.5) * g.getCellWidth();
		b = g.getCellWidth() - a;

		//The -0.5*cellWidth is there to shift the grid
		c = p.getY() -  (j + 0.5) * g.getCellHeight();
		d = g.getCellHeight() - c;

		//Assign a portion of the charge to the four surrounding points depending on
		//the distance.
		g.addRho(i, j, p.getCharge() * b * d / cellArea);
		g.addRho(i,j+1, p.getCharge() * b * c / cellArea);
		g.addRho(i + 1,j + 1, p.getCharge() * a * c / cellArea);
		g.addRho(i+1, j, p.getCharge() * a * d / cellArea);
	}
	
	@Override
	public void interpolateToParticle(Particle p, Grid g) {

		//Determine the nearest lower left grid point
		//THIS CAN BE NEGATIVE if particle is behind the left or the lower boundary
		int i = (int) Math.floor(p.getX() / g.getCellWidth());
		int j = (int) Math.floor(p.getY() / g.getCellHeight());
		
		/**Distance to the left cell boundary*/
		double a;
		/**Distance to the right cell boundary*/
		double b;
		/**Distance to the lower cell boundary*/
		double c;
		/**Distance to the upper cell boundary*/
		double d;
		
		a = p.getX() - i * g.getCellWidth();
		b = g.getCellWidth() - a;
		c = p.getY() -  j * g.getCellHeight();
		d = g.getCellHeight() - c;
		
		//Bz as given by the FDTD field solver is defined half a timestep ahead of particle
		//time. Therefore we have to average over the old Bz (that is half a timestep behind)
		//and the current Bz. The magnetic field is located at the grid points. 
		//No adjustments to the grid are necessary.
		p.setBz((formFactor(
				g.getBzo(i, j), g.getBzo(i, j+1), g.getBzo(i+1, j+1), g.getBzo(i+1, j),
				a, b, c, d) + 
				formFactor(g.getBz(i, j), g.getBz(i, j+1), g.getBz(i+1, j+1), g.getBz(i+1, j),
				a, b, c, d)) / 2);
		
		//The Ex-field is located in the middle of the left cell boundary.
		//This means that the Ex-field-grid is shifted upwards by half a cell height.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the vertical plane. All changes are reversed
		//after the calculation.
		if( c < g.getCellHeight()/2 ){
			j -= 1;
			c += g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			
			p.setEx(formFactor(
					g.getEx(i, j), g.getEx(i, j+1), g.getEx(i+1, j+1), g.getEx(i+1, j),
					a, b, c, d));
			
			c -= g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			j += 1;
		} else {
			c -= g.getCellHeight()/2;
			d = g.getCellHeight() - c;
			
			p.setEx(formFactor(
					g.getEx(i, j), g.getEx(i, j+1), g.getEx(i+1, j+1), g.getEx(i+1, j),
					a, b, c, d));
			
			c += g.getCellHeight()/2;
			d = g.getCellHeight() - c;
		}
		
		//The Ey-field is located in the middle of the lower cell boundary.
		//This means that the Ey-field-grid is shifted to the right by half a cell width.
		//The adjustments are made to calculate the distance to the shifted grid. The
		//only changes to be made are in the horizontal plane. All changes are reversed
		//after the calculation.
		if( a < g.getCellWidth()/2 ){
			i -= 1;
			a += g.getCellWidth()/2;
			b = g.getCellWidth() - a;
			
			p.setEy(formFactor(
					g.getEy(i, j), g.getEy(i, j+1), g.getEy(i+1, j+1), g.getEy(i+1, j),
					a, b, c, d));
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.
		} else {
			a -= g.getCellWidth()/2;
			b = g.getCellWidth() - a;
			
			p.setEy(formFactor(
					g.getEy(i, j), g.getEy(i, j+1), g.getEy(i+1, j+1), g.getEy(i+1, j),
					a, b, c, d));
			
			//No need to return the values to their previous state because they are
			//not going to be used anymore.		
		}
	}

	/**Defines how grid values are weighted. Characteristic of interpolation algorithm.
	 * <p>This is linear interpolation.</p>
	 * @param A value at the lower left grid point
	 * @param B value at the upper left grid point
	 * @param C value at the upper right grid point
	 * @param D value at the lower right grid point
	 * @param a distance to the left cell boundary
	 * @param b distance to the right cell boundary
	 * @param c distance to the lower cell boundary
	 * @param d distance to the upper cell boundary
	 * @return Total value at the particle position
	 */
	private double formFactor(double A, double B, double C, double D,
			double a, double b, double c, double d){
		return A*b*d + B*b*c + C*a*c + D*a*d;
	}

}
