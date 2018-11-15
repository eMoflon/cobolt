package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.analyzer.writer;

import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

public class FossaPropertyWriter implements PropertyWriter {
	
	@Override
	public void writeProperty(String k, double value) {
		try {
		System.out.println("Writing metric to Fossa DB. Property: " + k + "; Value: " + (double) value);
			
		//ApplicationMetricHelper.getInstance().storeMetric(kTCParameterK, (double) value);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void writeProperty(String k, boolean value) {
		// TODO MS
	}

	@Override
	public void writeComment(String comment) {
		// not supported
	}

}
