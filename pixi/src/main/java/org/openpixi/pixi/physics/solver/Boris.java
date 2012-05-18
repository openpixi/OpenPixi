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

package org.openpixi.pixi.physics.solver;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.force.Force;

/**The calculation is due to Boris and the equations((7) - (10)) can be found here:
 * http://ptsg.eecs.berkeley.edu/publications/Verboncoeur2005IOP.pdf
 */
public class Boris implements Solver{
	

	public Boris()
	{
		super();
	}
	
	/**
	 * Boris algorithm for implementing the electric and magnetic field.
	 * The damping is implemented with an linear error O(dt).
	 * Warning: the velocity is stored half a time step before of the position.
	 * @param p before the update: x(t), v(t-dt/2);
	 *                 after the update: x(t+dt), v(t+dt/2)
	 */
	public void step(Particle p, Force f, double step) {

		// remember for complete()
		//a(t) = F(v(t), x(t)) / m
		//p.ax = f.getForceX(p) / p.mass;
		//p.ay = f.getForceY(p) / p.mass;
		
		double vxminus = p.getVx() + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass());
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass());
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * f.getBz(p) * step / (2.0 * p.getMass());   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * step / (2.0 * p.mass), 1);   //t vector
		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + f.getPositionComponentofForceX(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * step / p.getMass());
		p.setVy(vyplus + f.getPositionComponentofForceY(p) * step / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * step / p.getMass());
		
		p.setX(p.getX() + p.getVx() * step);
		p.setY(p.getY() + p.getVy() * step);
	}	
	/**
	 * prepare method for bringing the velocity in the desired half step
	 * @param p before the update: v(t);
	 *                 after the update: v(t-dt/2)
	 */
	public void prepare(Particle p, Force f, double dt)
	{
		/*//a(t) = F(v(t), x(t)) / m
		p.ax = f.getForceX(p) / p.mass;
		p.ay = f.getForceY(p) / p.mass;

		//v(t - dt / 2) = v(t) - a(t)*dt / 2
		p.vx -= p.ax * dt / 2;
		p.vy -= p.ay * dt / 2;*/
		
	//	for(int i = 0; i < 100; i++) {
		dt = - dt * 0.5;
		double vxminus = p.getVx() + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass());
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass());
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * p.getMass());   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * dt / (2.0 * p.mass), 1);   //t vector

		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * dt / p.getMass());
		p.setVy(vyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * dt / p.getMass());
		
		//v(t - dt / 2) = 2 * v(t) - v(t + dt / 2 )
		//p.vx = 2 * p.vx - vx;
		//p.vy = 2 * p.vy - vy;

		//}
		/*dt = dt / 200;
		for(int i = 0; i < 100; i++) {
			p.ax = f.getForceX(p) / p.mass;
			p.ay = f.getForceY(p) / p.mass;
			p.vx -= p.ax * dt;
			p.vy -= p.ay * dt;
		}*/
		
	}

	/**
	 * complete method for bringing the velocity in the desired half step
	 * @param p before the update: v(t-dt/2);
	 *                 after the update: v(t)
	 */
	public void complete(Particle p, Force f, double dt)
	{
//		//v(t) = v(t - dt / 2) + a(t)*dt / 2
//		p.vx += p.ax * dt / 2;
//		p.vy += p.ay * dt / 2;
		
		//for(int i = 0; i < 100; i++) {
		dt = dt * 0.5;
		double vxminus = p.getVx() + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass());
		double vxplus;
		double vxprime;
		
		double vyminus = p.getVy() + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass());
		double vyplus;
		double vyprime;
		
		double t_z = p.getCharge() * f.getBz(p) * dt / (2.0 * p.getMass());   //t vector
		//double t_z = Math.atan2(p.charge * f.getBz(p) * dt / (2.0 * p.mass), 1);   //t vector

		
		double s_z = 2 * t_z / (1 + t_z * t_z);               //s vector
		
		vxprime = vxminus + vyminus * t_z;
		vyprime = vyminus - vxminus * t_z;
		
		vxplus = vxminus + vyprime * s_z;
		vyplus = vyminus - vxprime * s_z;
		
		p.setVx(vxplus + f.getPositionComponentofForceX(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceX(p) * dt / p.getMass());
		p.setVy(vyplus + f.getPositionComponentofForceY(p) * dt / (2.0 * p.getMass()) + f.getTangentVelocityComponentOfForceY(p) * dt / p.getMass());
		
		//}
	}
}
