package de.tudarmstadt.maki.modeling.jvlc.io;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.moflon.core.utilities.eMoflonEMFUtil;

import de.tudarmstadt.maki.modeling.graphmodel.Graph;

public class EMFTopologyWriter {

	public static void saveGraph(final Graph graph, final String filename) throws IOException {
		final ResourceSet set = eMoflonEMFUtil.createDefaultResourceSet();
		final Resource resource = set.createResource(eMoflonEMFUtil.createFileURI(filename, false));
		resource.getContents().add(graph);
		resource.save(null);
	}

}
