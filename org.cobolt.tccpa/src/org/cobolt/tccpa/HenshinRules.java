package org.cobolt.tccpa;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;

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
      return "src/org/cobolt/tccpa/rules";
   }

   public static List<Rule> collectAllRules(final Module module)
   {
      final List<Rule> rules = module.getUnits().stream() //
   			.filter(unit -> unit instanceof Rule) //
   			.map(unit -> (Rule) unit).collect(Collectors.toList());
      return rules;
   }

   public static String getMetamodelFileName()
   {
      return "tccpa.ecore";
   }

   public static Optional<Rule> getRuleByName(Module module, String ruleName) {
   	return module.getUnits().stream() //
   			.filter(unit -> unit instanceof Rule) //
   			.map(unit -> (Rule) unit) //
   			.filter(rule -> rule.getName().equals(ruleName))//
   			.findAny();
   }

   public static String getHenshinRulesFilename()
   {
      return "tccpa.henshin";
   }

   public static boolean containsConflictMetamodel(final Path directory)
   {
      final File[] minimalEcoreFiles = directory.toFile().listFiles(new FilenameFilter() {
   
         @Override
         public boolean accept(File dir, String name)
         {
            return MINIMAL_MODEL_FILENAME.equals(name);
         }
      });
      final boolean containsConflictMetamodel = minimalEcoreFiles.length != 0;
      return containsConflictMetamodel;
   }

   public static boolean containsMetamodel(final Path directory)
   {
      final File[] metamodelFiles = directory.toFile().listFiles(new FilenameFilter() {
   
         @Override
         public boolean accept(File dir, String name)
         {
            return getMetamodelFileName().equals(name);
         }
      });
      final boolean containsMetamodel = metamodelFiles.length != 0;
      return containsMetamodel;
   }
}
