package org.openpixi.pixi.distributed.ibis;

import ibis.ipl.ReadMessage;
import ibis.ipl.ReceivePort;
import ibis.ipl.SendPort;
import ibis.ipl.WriteMessage;
import org.openpixi.pixi.distributed.ResultsHolder;
import org.openpixi.pixi.physics.Particle;
import org.openpixi.pixi.physics.grid.Cell;
import org.openpixi.pixi.physics.util.IntBox;

import java.io.IOException;
import java.util.List;

/**
 * Handles the communication connected with problem distribution and results collection
 * on the side of the Master.
 */
public class MasterCommunicator {

	private IbisRegistry registry;


	/**
	 * Creates ports for communication.
	 * To avoid deadlock the receive ports have to be created first.
	 */
	public MasterCommunicator(IbisRegistry registry) throws Exception {
		this.registry = registry;
	}


	/**
	 * The ports for problem distribution are closed right after they are used to minimize
	 * number of open connections.
	 */
	public void distributeProblem(IntBox[] partitions, int[] assignment,
	                              List<List<Particle>> particlePartitions,
	                              Cell[][][] gridPartitions) throws IOException {
		assert assignment.length >= registry.getWorkers().size();

		for (int i = 0; i < assignment.length; ++i) {

			SendPort distributePort = registry.getIbis().createSendPort(PixiPorts.ONE_TO_ONE_PORT);
			distributePort.connect(
					registry.convertNodeIDToIbisID(assignment[i]),
					PixiPorts.DISTRIBUTE_PORT_ID);

			WriteMessage wm = distributePort.newMessage();
			wm.writeObject(partitions);
			wm.writeObject(assignment);
			wm.writeObject(particlePartitions.get(assignment[i]));
			wm.writeObject(gridPartitions[assignment[i]]);
			wm.finish();

			distributePort.close();
		}
	}


	public ResultsHolder collectResults() throws Exception {
		ReceivePort recvPort = registry.getIbis().createReceivePort(
				PixiPorts.GATHER_PORT, PixiPorts.GATHER_PORT_ID);
		recvPort.enableConnections();

		ResultsHolder resultsHolder = new ResultsHolder(registry.getWorkers().size());
		for (int i = 0; i < registry.getWorkers().size(); ++i) {

			ReadMessage rm = recvPort.receive();
			List<Particle> particles = (List<Particle>)rm.readObject();
			Cell[][] cells = (Cell[][])rm.readObject();
			rm.finish();

			int nodeID = registry.convertIbisIDToNodeID(registry.getWorkers().get(i));
			resultsHolder.nodeIDs[i] = nodeID;
			resultsHolder.gridPartitions[i] = cells;
			resultsHolder.particlePartitions.set(i, particles);
		}

		recvPort.close();
		return  resultsHolder;
	}
}