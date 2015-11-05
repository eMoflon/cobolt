package de.tudarmstadt.maki.modeling.jvlc.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

public class JvlcTopologyFromTextFileReader {

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
			final Map<String, KTCNode> idToNode = new HashMap<>();
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
					final KTCNode node = topology.addKTCNode(nodeId, remainingEnergy);
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
					topology.addUndirectedKTCLink(edgeIdFwd, edgeIdBwd, idToNode.get(sourceId), idToNode.get(targetId), distance,
							requiredTransmissionPower);

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
	 * Utility function that creates a reader and reads a topology from the file described by the given filename.
	 */
	public static void readTopology(final Topology topology, final String filename) throws FileNotFoundException {
		final JvlcTopologyFromTextFileReader reader = new JvlcTopologyFromTextFileReader();
		reader.read(topology, new FileInputStream(filename));
	}

	public void read(final JVLCFacade facade, final FileInputStream stream) {
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
					facade.addNode(INodeID.get(nodeId), remainingEnergy);
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
					facade.addEdge(EdgeID.get(edgeIdFwd), EdgeID.get(edgeIdBwd), INodeID.get(sourceId), INodeID.get(targetId), distance,
							requiredTransmissionPower);

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
