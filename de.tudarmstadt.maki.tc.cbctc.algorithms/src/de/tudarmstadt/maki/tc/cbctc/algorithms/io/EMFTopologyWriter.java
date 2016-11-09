package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.moflon.core.utilities.eMoflonEMFUtil;

import de.tudarmstadt.maki.tc.cbctc.model.Topology;

public class EMFTopologyWriter {

	public static void saveGraph(final Topology graph, final String filename) throws IOException {
		final ResourceSet set = eMoflonEMFUtil.createDefaultResourceSet();
		final Resource resource = set.createResource(eMoflonEMFUtil.createFileURI(filename, false));
		resource.getContents().add(graph);
		resource.save(null);
	}

}
