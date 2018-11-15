package de.tudarmstadt.maki.simonstrator.tc.io;

/**
 * Common super class for all readers of the custom GraphT file format
 * 
 * Main properties of the format:
 * * Comment lines start with #
 * * Whitespace lines are ignored
 * * There are magic comments that start with #!
 *    * #!auto-create-reverse-edges automatically set every 2i'th link as the reverse link of the 2i+1'th link
 * * Apart from comment and whitespace lines, the file should have the following format:
 * 1. A line of the form [nodeCount] [edgeCount]
 * 2. [nodeCount] lines of the form [nodeId] [attributeName1=attributeValue1] [attributeName2=attributeValue2] ...
 * 3. [edgeCount] lines of the form [edgeId] [fromId] [toId] [attributeName1=attributeValue1] [attributeName2=attributeValue2] ...
 * 
 * The respective subclass defines the possible attribute names (and attribute value types).
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class GraphTReader
{

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
      return !line.startsWith(GraphTConstants.COMMENT_PREFIX) && !line.trim().isEmpty();
   }

   /**
    * Similar to the BASH, a comment line is a magic comment if it starts with a she-bang (#!)
    * @param line
    * @return
    */
   protected boolean isMagicComment(String line)
   {
      return line.startsWith(GraphTConstants.MAGIC_COMMENT_PREFIX);
   }

   protected void handleMagicComment(String line)
   {
      final String command = line.replace(GraphTConstants.MAGIC_COMMENT_PREFIX, "").trim();
      switch (command)
      {
      case GraphTConstants.AUTO_CREATE_REVERSE_EDGES_COMMAND:
         this.isAutoReverseLinkingActive = true;
      }
   }
}
