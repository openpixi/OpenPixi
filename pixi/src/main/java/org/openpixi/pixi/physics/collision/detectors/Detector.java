package org.openpixi.pixi.physics.collision.detectors;

import java.util.ArrayList;

import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.collision.util.Pair;

public class Detector {
	
	public ArrayList<Pair<Particle, Particle>> overlappedPairs;// = new ArrayList<Pair<Particle2D, Particle2D>>();
	
	public Detector() {

		
	}
	
	public void run() {
		
	}
	
	public ArrayList<Pair<Particle, Particle>> getOverlappedPairs() {
		return overlappedPairs;
	}

}
