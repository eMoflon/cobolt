package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

public class GraphTReader
{

   private static final String COMMENT_PREFIX = "#";

   private static final String MAGIC_COMMENT_PREFIX = "#!";

   /**
    * If this command appears as a magic comment inside a grapht file, subsequent edges are automatically liked together as pairs of reverse edges
    * 
    * For instance, if the first four edges are eA, eB, eC, eD, then eA and eB will be each other's reverse edge. The same holds for eC and eD.
    */
   public static final String AUTO_CREATE_REVERSE_EDGES_COMMAND = "auto-create-reverse-edges";

   protected boolean isAutoReverseLinkingActive;

   public GraphTReader()
   {
      configureDefaults();
   }
   
   protected void configureDefaults()
   {
      isAutoReverseLinkingActive = false;
   }

   protected static boolean isDataLine(final String line)
   {
      return !line.startsWith(COMMENT_PREFIX) && !line.trim().isEmpty();
   }

   /**
    * Similar to the BASH, a comment line is a magic comment if it starts with a she-bang (#!)
    * @param line
    * @return
    */
   protected boolean isMagicComment(String line)
   {
      return line.startsWith(MAGIC_COMMENT_PREFIX);
   }

   protected void handleMagicComment(String line)
   {
      final String command = line.replace(MAGIC_COMMENT_PREFIX, "").trim();
      switch (command)
      {
      case AUTO_CREATE_REVERSE_EDGES_COMMAND:
         this.isAutoReverseLinkingActive = true;
      }
   }
}
