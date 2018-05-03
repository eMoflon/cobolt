package org.cobolt.tccpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;

public final class HenshinRules
{
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
}
