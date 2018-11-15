package de.tudarmstadt.maki.simonstrator.tc.weighting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class CsvEdgeWeightingFunction implements EdgeWeightProvider {

	private final int weightColumn;
	private final int distanceColumn;
	private final Pattern numberPattern = Pattern.compile("\\d+(.\\d+)?");
	private Map<Double, Double> distanceToWeightMap;
	private List<Double> distances;

	@XMLConfigurableConstructor({ "file", "distanceColumn", "weightColumn" })
	public CsvEdgeWeightingFunction(final File file, final int distanceColumn, final int weightColumn)
			throws IOException {
		this(FileUtils.readFileToString(file), distanceColumn, weightColumn);
	}

	public CsvEdgeWeightingFunction(final String content, final int distanceColumn, final int weightColumn)
			throws IOException {
		this.distanceColumn = distanceColumn;
		this.weightColumn = weightColumn;
		this.distanceToWeightMap = new HashMap<>();
		this.distances = new ArrayList<>();
		this.parseFile(content);
	}

	private void parseFile(final String content) throws IOException {
		for (final String line : content.split("\\n")) {
			final String[] fields = line.split(";");
			if (fields.length < Math.max(this.distanceColumn, weightColumn) + 1) {
				throw new IllegalArgumentException(String.format("Line has fewer columns than required: %s", line));
			}
			final String distanceString = fields[this.distanceColumn];
			final String weightString = fields[this.weightColumn];
			if (isNumber(distanceString) && isNumber(weightString)) {
				final double distance = Double.parseDouble(distanceString);
				final double weight = Double.parseDouble(weightString);
				this.addMapping(distance, weight);
			}
		}

		if (this.distances.isEmpty())
			throw new IllegalArgumentException(
					String.format("No entries could be parsed from the given CSV specification: %s", content));

		if (new HashSet<Double>(this.distances).size() != this.distances.size())
			throw new IllegalArgumentException("CSV specification contains duplicates.");

		Collections.sort(this.distances);
	}

	private void addMapping(double distance, double weight) {
		this.distanceToWeightMap.put(distance, weight);
		this.distances.add(distance);
	}

	private boolean isNumber(final String string) {
		return numberPattern.matcher(string).matches();
	}

	@Override
	public double calculateWeight(IEdge edge, Graph graph) {
		if (edge == null)
			throw new IllegalArgumentException("edge is null");

		final Double distance = edge.getProperty(UnderlayTopologyProperties.DISTANCE);
		if (distance == null)
			throw new IllegalArgumentException(
					String.format("Edge %s is missing required property %s", edge, UnderlayTopologyProperties.DISTANCE));

		final Double result = this.distanceToWeightMap.get(distance);
		if (result == null) {
			final int i = Collections.binarySearch(this.distances, distance);
			final int insertionPoint = -(i + 1);
			if (insertionPoint == 0)
				return this.distanceToWeightMap.get(this.distances.get(0));
			else if (insertionPoint == this.distances.size())
				return this.distanceToWeightMap.get(this.distances.get(this.distances.size() - 1));
			else {
				final double previousDistance = this.distances.get(insertionPoint - 1);
				final double nextDistance = this.distances.get(insertionPoint);
				final double previousWeight = this.distanceToWeightMap.get(previousDistance);
				final double nextWeight = this.distanceToWeightMap.get(nextDistance);
				final double weightedMean = ((distance - previousDistance) * previousWeight
						+ (nextDistance - distance) * nextWeight)
						/ (nextDistance - previousDistance);
				return weightedMean;
			}
		} else {
			return result;
		}
	}

}
