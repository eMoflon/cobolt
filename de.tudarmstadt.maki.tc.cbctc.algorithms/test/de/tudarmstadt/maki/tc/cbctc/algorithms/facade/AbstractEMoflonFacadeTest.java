package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import org.junit.Before;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.tc.cbctc.algorithms.io.GraphTFileReader;

public abstract class AbstractEMoflonFacadeTest
{
   protected EMoflonFacade facade;
   protected GraphTFileReader reader;

   @Before
   public void setup() {

      this.facade = (EMoflonFacade) TopologyControlFacadeFactory
            .create("de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade");
      this.facade.setOperationMode(TopologyControlOperationMode.INCREMENTAL);
      this.facade.configureAlgorithm(getAlgorithmID());
      this.reader = new GraphTFileReader();
   }

   protected abstract TopologyControlAlgorithmID getAlgorithmID();
}
