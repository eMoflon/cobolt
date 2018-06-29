package org.cobolt.ngctoac;

public final class HenshinRules
{
   public static final String MINIMAL_MODEL_FILENAME = "minimal-model.ecore";

   private HenshinRules()
   {
      throw new UnsupportedOperationException("Utility class");
   }

   /**
    * Returns the project-relative path to the folder containing models and metamodels
    */
   public static String getRulesDirectory()
   {
      return "src/org/cobolt/ngctoac";
   }

   public static String getMetamodelFileName()
   {
      return "tccpa.ecore";
   }


   public static String getHenshinRulesFilename()
   {
      return "tccpa.henshin";
   }

}
