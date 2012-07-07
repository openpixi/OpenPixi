package org.openpixi.pixi.distributed;

import org.openpixi.pixi.distributed.ibis.IbisRegistry;
import org.openpixi.pixi.distributed.ibis.WorkerCommunicator;
import org.openpixi.pixi.physics.Settings;

import java.io.IOException;

/**
 * Receives the problem, calculates the problem, sends back results.
 */
public class Worker implements Runnable {

	private WorkerCommunicator communicator;
	private Settings settings;


	public Worker(IbisRegistry registry, Settings settings) throws Exception {
		communicator = new WorkerCommunicator(registry);
	}


	public void run() {
		try {
			communicator.receiveProblem();

			// TODO build the simulation together with boundaries
			// TODO run the simulation

			communicator.sendResults(
					communicator.getParticles(),
					communicator.getCells());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}