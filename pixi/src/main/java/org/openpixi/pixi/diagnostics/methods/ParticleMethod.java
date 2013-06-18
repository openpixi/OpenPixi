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

package org.openpixi.pixi.diagnostics.methods;

import java.util.ArrayList;

import org.openpixi.pixi.diagnostics.ParticleDataOutput;
import org.openpixi.pixi.physics.Particle;

/**
 * Every diagnostics method that needs the particles should implement this.
 */
public interface ParticleMethod {
	
	/** Performes the desired diagnostics*/
	public void calculate(ArrayList<Particle> particles);
	
	/** Provides a way how other classes can access its information*/
	public void getData(ParticleDataOutput out);

}
