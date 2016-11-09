package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

public class GraphTFileReader {

	public void read(final Topology topology, final String filename) throws FileNotFoundException {
		this.read(topology, new File(filename));
	}

	public void read(final Topology topology, final File inputFile) throws FileNotFoundException {
		this.read(topology, new FileInputStream(inputFile));
	}

	public void read(final Topology topology, final FileInputStream stream) {
		Scanner scanner = null;

		try {
			scanner = new Scanner(stream);
			String nmLine = scanner.nextLine();
			while (!isValidLine(nmLine)) {
				nmLine = scanner.nextLine();
			}
			final int n = Integer.parseInt(nmLine.split("\\s+")[0]);
			final int m = Integer.parseInt(nmLine.split("\\s+")[1]);
			final Map<String, Node> idToNode = new HashMap<>();
			int readNodeLines = 0;
			while (readNodeLines < n) {
				final String line = scanner.nextLine();
				if (isValidLine(line)) {
					final String[] splitLine = line.split("\\s+");
					final String nodeId = splitLine[0];
					final Double remainingEnergy;
					if (splitLine.length > 1) {
						remainingEnergy = Double.parseDouble(splitLine[1]);
					} else {
						remainingEnergy = Double.NaN;
					}
					final Node node = TopologyUtils.addNode(topology, nodeId, remainingEnergy);
					idToNode.put(nodeId, node);
					readNodeLines++;
				}
			}
			int readEdgeLines = 0;
			while (readEdgeLines < m) {
				final String line = scanner.nextLine();
				if (isValidLine(line)) {
					final String[] splitLine = line.split("\\s+");
					final String edgeIdFwd = splitLine[0];
					final String edgeIdBwd = splitLine[1];
					final String sourceId = splitLine[2];
					final String targetId = splitLine[3];
					final Double distance = Double.parseDouble(splitLine[4]);
					final Double requiredTransmissionPower;
					if (splitLine.length > 5) {
						requiredTransmissionPower = Double.parseDouble(splitLine[5]);
					} else {
						requiredTransmissionPower = Double.NaN;
					}
					TopologyUtils.addUndirectedEdge(topology, edgeIdFwd, edgeIdBwd, idToNode.get(sourceId), idToNode.get(targetId),
							distance, requiredTransmissionPower);

					readEdgeLines++;
				}
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		} finally {
			scanner.close();
		}
	}

	private static boolean isValidLine(final String line) {
		return !line.startsWith("#") && !line.trim().isEmpty();
	}

	/**
	 * Utility function that creates a reader and reads a topology from the file
	 * described by the given filename.
	 */
	public static void readTopology(final Topology topology, final String filename) throws FileNotFoundException {
		final GraphTFileReader reader = new GraphTFileReader();
		reader.read(topology, new FileInputStream(filename));
	}

	public void read(final EMoflonFacade facade, final FileInputStream stream) {
		Scanner scanner = null;

		try {
			scanner = new Scanner(stream);
			String nmLine = scanner.nextLine();
			while (!isValidLine(nmLine)) {
				nmLine = scanner.nextLine();
			}
			final int n = Integer.parseInt(nmLine.split("\\s+")[0]);
			final int m = Integer.parseInt(nmLine.split("\\s+")[1]);
			int readNodeLines = 0;
			while (readNodeLines < n) {
				final String line = scanner.nextLine();
				if (isValidLine(line)) {
					final String[] splitLine = line.split("\\s+");
					final String nodeId = splitLine[0];
					final Double remainingEnergy;
					if (splitLine.length > 1) {
						remainingEnergy = Double.parseDouble(splitLine[1]);
					} else {
						remainingEnergy = Double.NaN;
					}
					INode node = Graphs.createNode(nodeId);
					node.setProperty(UnderlayTopologyProperties.REMAINING_ENERGY, remainingEnergy);
					facade.addNode(node);
					readNodeLines++;
				}
			}
			int readEdgeLines = 0;
			while (readEdgeLines < m) {
				final String line = scanner.nextLine();
				if (isValidLine(line)) {
					final String[] splitLine = line.split("\\s+");
					final String edgeIdFwd = splitLine[0];
					final String edgeIdBwd = splitLine[1];
					final String sourceIdStr = splitLine[2];
					final String targetIdStr = splitLine[3];
					final Double distance = Double.parseDouble(splitLine[4]);
					final Double requiredTransmissionPower;
					if (splitLine.length > 5) {
						requiredTransmissionPower = Double.parseDouble(splitLine[5]);
					} else {
						requiredTransmissionPower = Double.NaN;
					}
					final INodeID srcId = INodeID.get(sourceIdStr);
					final INode source = facade.getGraph().getNode(srcId);
					final INodeID targetId = INodeID.get(targetIdStr);
					final INode target = facade.getGraph().getNode(targetId);
					final IEdge forwardPrototype = new DirectedEdge(srcId, targetId, EdgeID.get(edgeIdFwd));
					final IEdge backwardPrototype = new DirectedEdge(targetId, srcId, EdgeID.get(edgeIdBwd));
					forwardPrototype.setProperty(UnderlayTopologyProperties.DISTANCE, distance);
					backwardPrototype.setProperty(UnderlayTopologyProperties.DISTANCE, distance);
					forwardPrototype.setProperty(UnderlayTopologyProperties.WEIGHT, distance);
					backwardPrototype.setProperty(UnderlayTopologyProperties.WEIGHT, distance);
					forwardPrototype.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER,
							requiredTransmissionPower);
					backwardPrototype.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER,
							requiredTransmissionPower);
					forwardPrototype.setProperty(UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE,
							source.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY)
									/ requiredTransmissionPower);
					backwardPrototype.setProperty(UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE,
							target.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY)
									/ requiredTransmissionPower);

					final IEdge fwdEdge = facade.addEdge(forwardPrototype);
					final IEdge bwdEdge = facade.addEdge(backwardPrototype);
					facade.connectOppositeEdges(fwdEdge, bwdEdge);

					readEdgeLines++;
				}
			}
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		} finally {
			scanner.close();
		}
	}
}
