package de.tudarmstadt.maki.simonstrator.tc.io;

public interface GraphTConstants
{
   /**
    * Lines starting with this token are comment lines
    */
   String COMMENT_PREFIX = "#";
   
   /**
    * Lines starting with this token are magic comments, i.e., they may be interpreted by the reader
    */
   String MAGIC_COMMENT_PREFIX = "#!";
   
   /**
    * Newline character
    */
   String NL = "\n";
   
   /**
    * If this command appears as a magic comment inside a grapht file, subsequent edges are automatically liked together as pairs of reverse edges
    * 
    * For instance, if the first four edges are eA, eB, eC, eD, then eA and eB will be each other's reverse edge. The same holds for eC and eD.
    */
   String AUTO_CREATE_REVERSE_EDGES_COMMAND = "auto-create-reverse-edges";

}
