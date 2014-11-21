package org.openpixi.pixi.ui.panel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.physics.particles.Particle;
import org.openpixi.pixi.ui.SimulationAnimation;
import org.openpixi.pixi.ui.SimulationAnimationListener;

/**
 * This panel shows the one-dimensional electric field along the x-direction.
 * Several field lines for various grid positions along the y-direction are
 * superimposed.
 */
public class ElectricFieldPanel extends JPanel {

	private SimulationAnimation simulationAnimation;

	/** Constructor */
	public ElectricFieldPanel(SimulationAnimation simulationAnimation) {
		this.simulationAnimation = simulationAnimation;
		this.simulationAnimation.addListener(new MyAnimationListener());
		this.setVisible(true);
	}

	/** Listener for timer */
	public class MyAnimationListener implements SimulationAnimationListener {

		public void repaint() {
			ElectricFieldPanel.this.repaint();
		}

		public void clear() {
		}
	}

	/** Display the particles */
	public void paintComponent(Graphics graph1) {
		Graphics2D graph = (Graphics2D) graph1;
		setBackground(Color.white);
		graph.translate(0, this.getHeight());
		graph.scale(1, -1);

		super.paintComponent(graph1);

		Simulation s = simulationAnimation.getSimulation();
		/** Scaling factor for the displayed panel in x-direction*/
		double sx = getWidth() / s.getWidth();
		/** Scaling factor for the displayed panel in y-direction*/
		double sy = getHeight() / s.getHeight();

		double panelHeight = getHeight();

		// Draw particles on a central line:
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
			if(width > 2 && height > 2) {
				graph.fillOval((int) (par.getX()*sx) - width/2, (int) (panelHeight/2 - height/2),  width,  height);
			}
			else {
				graph.drawRect((int) (par.getX()*sx), (int) panelHeight/2, 0, 0);
			}
		}

		// scale factor for electric field
		double scaleE = 1;

		// Draw electrid field:
		graph.setColor(Color.black);
		for(int k = 0; k < s.grid.getNumCellsY(); k++)
		{
			int newPosition = 0;
			int newValue = 0;
			for(int i = 0; i < s.grid.getNumCellsX(); i++)
			{

				int oldPosition = newPosition;
				int oldValue = newValue;
				newPosition = (int) (s.grid.getCellWidth() * (i + 0.5) * sx);
				newValue = (int) (((0.5 - scaleE * s.grid.getEx(i,k)) * panelHeight));

				if (i > 0) {
					graph.drawLine(oldPosition, oldValue,newPosition, newValue);
				}
			}
		}

	}

}
