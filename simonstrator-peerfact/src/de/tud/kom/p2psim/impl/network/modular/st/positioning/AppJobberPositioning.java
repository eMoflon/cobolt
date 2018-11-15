package de.tud.kom.p2psim.impl.network.modular.st.positioning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.util.positioning.GeoSpherePosition;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

public class AppJobberPositioning implements PositioningStrategy {

	private String path = "appjobber.csv";

	private ArrayList<GeoSpherePosition> positions = null;

	private Random rnd = Randoms.getRandom(AppJobberPositioning.class);

	@Override
	public Location getPosition(
			SimHost host,
			NetMeasurementDB db,
			NetMeasurementDB.Host hostMeta) {

		if (db != null || hostMeta != null) {
			throw new IllegalArgumentException("FootprintPositioning is incompatible with the NetMeasurementDB");
		}

		if (positions == null)
			readDb();

		if (positions.isEmpty())
			throw new AssertionError("used all avilable locations");

		int index = rnd.nextInt(positions.size());
		return positions.remove(index);
	}

	private void readDb() {
		positions = new ArrayList<GeoSpherePosition>();
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String parts[] = line.split(";");
				if (parts.length != 2)
					throw new RuntimeException("database broken");

				// coordinates in database seem to be in "wrong" order (longitude, latitude)
				String lat = parts[1].replace('"', ' ').trim();
				String lon = parts[0].replace('"', ' ').trim();
				positions.add(new GeoSpherePosition(Double.parseDouble(lat), Double.parseDouble(lon)));
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("could not read appjobber database: file not found");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("could not read appjobber database: encoding troubles");
		} catch (IOException e) {
			throw new RuntimeException("could not read appjobber database: IO error");
		}
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("path", path);
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}
