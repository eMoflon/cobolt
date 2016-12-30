package de.tudarmstadt.maki.tc.cbctc.algorithms.facade.preprocessing;

public final class NodePreprocessingRegistry
{

   private static final NodePreprocessingRegistry INSTANCE = new NodePreprocessingRegistry();
   
   private INodePreprocessor nodePreprocessor;
   
   /**
    * Returns the singleton instance of this class
    * @return
    */
   public static NodePreprocessingRegistry getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Returns the stored {@link INodePreprocessor}
    */
   public INodePreprocessor getNodePreprocessor()
   {
      return nodePreprocessor;
   }
   
   /**
    * Sets the {@link INodePreprocessor} to return when {@link #getNodePreprocessor()} is called
    * @param nodePreprocessor the node preprocessor
    */
   public void setNodePreprocessor(INodePreprocessor nodePreprocessor)
   {
      this.nodePreprocessor = nodePreprocessor;
   }
}
