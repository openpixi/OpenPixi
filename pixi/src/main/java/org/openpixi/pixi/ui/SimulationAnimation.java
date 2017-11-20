package org.openpixi.pixi.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.openpixi.pixi.physics.Settings;
import org.openpixi.pixi.physics.Simulation;
import org.openpixi.pixi.ui.panel.Particle2DPanel;
import org.openpixi.pixi.ui.util.FrameRateDetector;

/**
 * Wrapper for the simulation class in GUI applications.
 */
public class SimulationAnimation {

	protected Simulation s;
	private MainControlApplet mainControlApplet;

	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	private Timer timer;

	private FrameRateDetector frameratedetector;

	private ArrayList<SimulationAnimationListener> listeners = new ArrayList<SimulationAnimationListener>();

	/** Constructor */
	public SimulationAnimation(MainControlApplet mainControlApplet) {
		this.mainControlApplet = mainControlApplet;
		timer = new Timer(interval, new TimerListener());
		frameratedetector = new FrameRateDetector(500);
		Settings settings = new Settings();
		s = new Simulation(settings);
	}

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			doSimulationStep();
		}
	}

	public void startAnimation() {
		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
	}

	/**
	 * Perform a single animation step
	 */
	public void stepAnimation() {
		// stop animation first
		stopAnimation();

		doSimulationStep();
	}

	public Simulation getSimulation() {
		return s;
	}

	public MainControlApplet getMainControlApplet() { return mainControlApplet; }

	public FrameRateDetector getFrameRateDetector() {
		return frameratedetector;
	}

	public Timer getTimer() {
		return timer;
	}

	/**
	 * Add Listener for repaint() event.
	 * @param listener
	 */
	public void addListener(SimulationAnimationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes Listener for repaint() event.
	 * @param listener
	 */
	public void removeListener(SimulationAnimationListener listener) {
		listeners.remove(listener);
	}

	public void repaint() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.repaint();
		}
	}

	private void clear() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.clear();
		}
	}

	private void doSimulationStep() {
		try {
			s.step();
			if (s.totalSimulationSteps == s.getIterations()) {
				// Stop simulation (the user can continue by hand)
				stopAnimation();
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex2) {
			Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex2);
		}
		frameratedetector.update();
		repaint();
	}

	/**
	 * Reset animation according to settings
	 *
	 * @param settings New settings for animation.
	 */
	public void resetAnimation(Settings settings) {
		// timer.restart();
		timer.stop();
		s = new Simulation(settings);
		mainControlApplet.defaultLabeledGrid.grid = s.grid;
		clear();
		repaint();
	}

}
