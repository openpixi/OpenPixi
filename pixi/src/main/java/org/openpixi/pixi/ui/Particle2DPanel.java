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
package org.openpixi.pixi.ui;

import org.openpixi.pixi.physics.*;
import org.openpixi.pixi.physics.boundary.HardWallBoundary;
import org.openpixi.pixi.physics.boundary.PeriodicBoundary;
import org.openpixi.pixi.physics.collision.algorithms.*;
import org.openpixi.pixi.physics.collision.detectors.*;
import org.openpixi.pixi.physics.force.*;
import org.openpixi.pixi.physics.force.relativistic.*;
import org.openpixi.pixi.physics.solver.*;
import org.openpixi.pixi.physics.solver.relativistic.*;
import org.openpixi.pixi.ui.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.*;
import java.util.ArrayList;

import org.openpixi.pixi.physics.grid.*;


/**
 * Displays 2D particles.
 */
public class Particle2DPanel extends JPanel {
	
	public Simulation s;

	public String fileName;
	
	public String fileDirectory;
	
	private WriteFile file = new WriteFile();
	
	private boolean relativistic = false;

	private boolean reset_trace;
	
	private boolean test = false;
	
	private boolean drawCurrentGrid = false;
	
	private boolean drawFields = false;
	
	private boolean calculateFields = false;
	
	private boolean writePosition = false;
	
	/** Scaling factor for the displayed panel in x-direction*/
	double sx;
	/** Scaling factor for the displayed panel in y-direction*/
	double sy;	
	
	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	public Timer timer;
	
	public boolean showinfo = false;
	private FrameRateDetector frameratedetector;

	/** A state for the trace */
	public boolean paint_trace = false;

	Color darkGreen = new Color(0x00, 0x80, 0x00);

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {

			s.step();
			frameratedetector.update();
			sx = getWidth() / s.width;
			sy = getHeight() / s.height;
			repaint();
			if(writePosition)
			{
				Particle par = (Particle) s.particles.get(0);
				System.out.println(par.getX() + " " + par.getY());
				file.writeFile(fileName, fileDirectory, par.getX() + " " + par.getY());
			}
		}
	}

	/** Constructor */
	public Particle2DPanel() {
		
		timer = new Timer(interval, new TimerListener());
		this.setVisible(true);
		frameratedetector = new FrameRateDetector(500);
		
		s = InitialConditions.initRandomParticles(10, 1);

	}

	public void startAnimation() {
		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
		//test = false;
	}

	public void resetAnimation(int id) {
		// timer.restart();
		timer.stop();
		reset_trace = true;
		switch(id) {
		case 0:
			s = InitialConditions.initRandomParticles(10, 1);
			break;
		case 1:
			s = InitialConditions.initRandomParticles(100, 1);
			break;
		case 2:
			s = InitialConditions.initRandomParticles(1000, 0.5);
			break;
		case 3:
			s = InitialConditions.initRandomParticles(10000, 0.01);
			break;
		case 4:
			s = InitialConditions.initGravity(1, 1);
			break;
		case 5:
			s = InitialConditions.initElectric(1, 1);
			break;
		case 6:
			s = InitialConditions.initMagnetic(3, 1);
			break;
		case 7:
			s = InitialConditions.initSpring(1, 1);
			break;
		}
		updateFieldForce();
		s.prepareAllParticles();
		timer.start();
	}	

	public void checkTrace() {
		paint_trace =! paint_trace;
		startAnimation();
	}
	
	public void drawCurrentGrid() {
		drawCurrentGrid =! drawCurrentGrid;
	}
	
	public void drawFields() {
		drawFields =! drawFields;
	}
	
	public void calculateFields() {
		calculateFields =! calculateFields;
		updateFieldForce();
	}

	private void updateFieldForce() {
		
		if(calculateFields) {
			s.grid = null;
			s.grid = new YeeGrid(s);
			s.boundary = new PeriodicBoundary(s);
		}
		else {
			s.grid = null;
			s.grid = new Grid(s);
			//clears forces ArrayList of all GridForces
			if (s.f instanceof CombinedForce) {
				ArrayList<Force> forces = ((CombinedForce) s.f).forces;
				for (int i = 0; i < forces.size(); i++) {
					if (forces.get(i) instanceof SimpleGridForce){
						forces.remove(i);
					}
				}
			}			
		}
	}
	
	public void writePosition() {
		writePosition =! writePosition;
		if(writePosition)
		{
			s.f.clear();
			ConstantForce force = new ConstantForce();
			force.bz = - 0.23; // -ConstantsSI.g;
			//force.gy = -1;
			//force.drag = 0.08;
			s.f.add(force);
			InitialConditions.createRandomParticles(s.width, s.height, s.c, 1, 10);
			Particle par = (Particle) s.particles.get(0);
			par.setX(this.getWidth() * 0.5);
			par.setY(this.getHeight() * 0.5);
			//System.out.println(this.getWidth() * 0.5 + " x0");
			//System.out.println(this.getHeight() * 0.5 + " y0");
			par.setVx(10);
			par.setVy(10);
			par.setMass(1);
			par.setCharge(1);
		}
		else {
			resetAnimation(0);
		}
		s.prepareAllParticles();
/*		ConstantForce force = new ConstantForce();
		force.bz = - 0.23;
		s.f.add(force);
*/	}
	
	public void algorithmChange(int id)
	{
		s.completeAllParticles();
		
		switch(id) {
		case 0:
			s.psolver = new EulerRichardson();
			break;
		case 1:
			s.psolver = new LeapFrog();
			break;
		case 2:
			s.psolver = new LeapFrogDamped();
			break;
		case 3:
			s.psolver = new LeapFrogHalfStep();
			break;
		case 4:
			s.psolver = new Boris();
			break;
		case 5:
			s.psolver = new BorisDamped();
			break;
		case 6:
			s.psolver = new SemiImplicitEuler();
			break;
		case 7:
			s.psolver = new Euler();
			break;
			}

		s.prepareAllParticles();
	}
	
	public void relativisticEffects(int i) {
		relativistic =! relativistic;
		
		if(relativistic == false) {
			if (s.f instanceof CombinedForce) {
				ArrayList<Force> forces = ((CombinedForce) s.f).forces;
				for (int j = 0; j < forces.size(); j++) {
					if (forces.get(j) instanceof ConstantForceRelativistic){
						forces.set(j, new ConstantForce());
					}
					if (forces.get(j) instanceof SimpleGridForceRelativistic){
						forces.set(j, new SimpleGridForce());
					}
					if (forces.get(j) instanceof SpringForceRelativistic){
						forces.set(j, new SpringForce());
					}
				}
			}
			switch(i) {
			case 1:
				s.psolver = new LeapFrog();
			case 4:
				s.psolver = new Boris();
				break;
			case 6:
				s.psolver = new SemiImplicitEuler();
				break;
			}
		}
		
		if(relativistic == true) {
			//System.out.println("relativistic version on");
			if (s.f instanceof CombinedForce) {
				ArrayList<Force> forces = ((CombinedForce) s.f).forces;
				for (int j = 0; j < forces.size(); j++) {
					if (forces.get(j) instanceof ConstantForce){
						forces.set(j, new ConstantForceRelativistic());
					}
					if (forces.get(j) instanceof SimpleGridForce){
						forces.set(j, new SimpleGridForceRelativistic(s));
					}
					if (forces.get(j) instanceof SpringForce){
						forces.set(j, new SpringForceRelativistic());
					}
				}
			}
			switch(i) {
			case 1:
				s.psolver = new LeapFrogRelativistic();
			case 4:
				s.psolver = new BorisRelativistic();
				break;
			case 6:
				s.psolver = new SemiImplicitEulerRelativistic();
				break;
			}
		}
		
	}
	
	public void collisionChange(int i) {
		switch(i) {
		case 0:
			s.collisionBoolean = false;
			s.detector = new Detector();
			s.collisionalgorithm = new CollisionAlgorithm();
			break;
		case 1:
			s.collisionBoolean = true;
			s.detector = new AllParticles(s.particles);
			s.collisionalgorithm = new SimpleCollision();
			break;
		}
	}
	
	public void detectorChange(int i) {
		switch(i) {
		case 0:
			s.detector = new AllParticles(s.particles);
			break;
		case 1:
			s.detector = new SweepAndPrune(s.particles);
			break;
		}
	}
	
	public void algorithmCollisionChange(int i) {
		switch(i) {
		case 0:
			s.collisionalgorithm = new SimpleCollision();
			break;
		case 1:
			s.collisionalgorithm = new VectorTransformation();
			break;
		case 2:
			s.collisionalgorithm = new MatrixTransformation();
			break;
		}
	}
	
	public void boundariesChange(int i) {
		switch(i) {
		case 0:
			s.boundary = new HardWallBoundary(s);
			break;
		case 1:
			s.boundary = new PeriodicBoundary(s);
		}
		
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		if(!paint_trace)
		{
			super.paintComponent(graph1);
		}
		if(reset_trace)
		{
			super.paintComponent(graph1);
			reset_trace = false;
		}
		
		for (int i = 0; i < s.particles.size(); i++) {
			Particle par = (Particle) s.particles.get(i);
			if (par.getCharge() > 0) {
				graph.setColor(Color.red);
			} else {
				graph.setColor(Color.blue);
			}
			double radius = par.getRadius();
			int width = (int) (2*sx*radius);
			int height = (int) (2*sy*radius);
			if(width > 2 && height > 2 && !paint_trace) {
				graph.fillOval((int) (par.getX()*sx) - width/2, (int) (par.getY()*sy) - height/2,  width,  height);
			}
			else {
				graph.drawRect((int) (par.getX()*sx), (int) (par.getY()*sy), 0, 0);
			}
		}
		
		if(drawCurrentGrid)
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.numCellsX; i++)
				for(int k = 0; k < s.grid.numCellsY; k++)
				{
					int xstart = (int) (s.grid.cellWidth * (i + 0.5) * sx);
					int ystart = (int) (s.grid.cellHeight * (k + 0.5) * sy);
					drawArrow(graph, xstart, ystart, (int) Math.round(s.grid.jx[i][k]*sx + xstart), (int) Math.round(s.grid.jy[i][k]*sy + ystart));
				}
			//return;
		}
		
		if(drawFields)
		{
			graph.setColor(Color.black);
			for(int i = 0; i < s.grid.numCellsX; i++)
				for(int k = 0; k < s.grid.numCellsY; k++)
				{
					int xstart = (int) (s.grid.cellWidth * (i + 0.5) * sx);
					int ystart = (int) (s.grid.cellHeight * (k + 0.5) * sy);
					drawArrow(graph, xstart, ystart, (int) Math.round(s.grid.Ex[i][k]*sx + xstart), (int) Math.round(s.grid.Ey[i][k]*sy + ystart));
				}
			//return;
		}

		if (showinfo) {
			graph.translate(0.0, this.getHeight());
			graph.scale(1.0, -1.0);
			graph.setColor(darkGreen);
			graph.drawString("Frame rate: " + frameratedetector.getRateString() + " fps", 30, 30);
			graph.drawString("Time step: " + (float) s.tstep, 30, 50);

			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			int bottom = getHeight();
			graph.drawString("free memory: " + freeMemory / 1024, 30, bottom - 90);
			graph.drawString("allocated memory: " + allocatedMemory / 1024, 30, bottom - 70);
			graph.drawString("max memory: " + maxMemory /1024, 30, bottom - 50);
			graph.drawString("total free memory: " +
				(freeMemory + (maxMemory - allocatedMemory)) / 1024, 30, bottom - 30);
		}		
	}
	
	
	private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
		
		int ARR_SIZE = 5;

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        // get the old transform matrix
        AffineTransform old = g.getTransform();
        AffineTransform at = getTranslateInstance(x1, y1);
        at.concatenate(getRotateInstance(angle));
        g.transform(at);
        //g.setTransform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, (int) len, 0);
        if(Math.abs(x2 - x1) > 0 || Math.abs(y2 - y1) > 0)
        	g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len},
        				  new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        
        // reset transformationmatrix
        g.setTransform(old);
     }

}
