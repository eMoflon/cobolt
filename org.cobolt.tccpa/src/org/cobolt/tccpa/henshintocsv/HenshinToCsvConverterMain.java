package org.cobolt.tccpa.henshintocsv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.cobolt.tccpa.CsvUtils;
import org.cobolt.tccpa.stabilizationanalysis.RuleNames;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * This class converts a Henshin results folder into a CSV-based interaction list file
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class HenshinToCsvConverterMain
{
   /**
    * Usage: HenshinToCsvConverterMain [Henshin-results-folder]
    *
    * @param args expected length: 1, content: Henshin results folder
    * @throws IOException if writing the CSV file fails
    */
   public static void main(String[] args) throws IOException
   {
      final File henshinResultsFolder = extractResultsFolder(args);

      final File outputCsvFile = new File(henshinResultsFolder, "Interactions.csv");

      FileUtils.writeLines(outputCsvFile, Arrays.asList(formatCsvLine(getInteractionsCsvHeaderEntries())), false);

      for (final File dateBasedFolder : henshinResultsFolder.listFiles(new HenshinDateFolderFilenameFilter()))
      {
         processDateBasedFolder(outputCsvFile, dateBasedFolder);
      }
   }

   private static File extractResultsFolder(String[] args)
   {
      if (args.length != 1)
         throw new IllegalArgumentException("Usage: HenshinToCsvConverterMain [Henshin-results-folder]");

      final File henshinResultsFolder = new File(args[0]);
      if (!henshinResultsFolder.isDirectory())
         throw new IllegalArgumentException("Provided path must point to an existing directory.");
      return henshinResultsFolder;
   }

   private static void processDateBasedFolder(final File outputCsvFile, final File dateBasedFolder) throws IOException
   {
      for (final File interactionSetFolder : dateBasedFolder.listFiles(new HenshinInteractionSetFolderFilenameFilter()))
      {
         processInteractionSetFolder(outputCsvFile, interactionSetFolder);
      }
   }

   private static void processInteractionSetFolder(final File outputCsvFile, final File interactionSetFolder) throws IOException
   {
      final Pattern interactionSetFolderPattern = Pattern.compile(HenshinInteractionSetFolderFilenameFilter.REGEX);
      final String folderName = interactionSetFolder.getName();
      Matcher interactionSetMatcher = interactionSetFolderPattern.matcher(folderName);
      interactionSetMatcher.matches();
      final String lhsRule = interactionSetMatcher.group(1);
      final String rhsRule = interactionSetMatcher.group(2);
      for (final File interactionFolder : interactionSetFolder.listFiles(new HenshinInteractionFolderFilenameFilter()))
      {
         processInteractionFolder(interactionFolder, lhsRule, rhsRule, outputCsvFile);
      }
   }

   private static void processInteractionFolder(final File interactionFolder, final String lhsRuleName, final String rhsRuleName, final File outputCsvFile)
         throws IOException
   {

      final String lhsTextualRuleName = mapToTextualRuleName(lhsRuleName);
      final String rhsTextualRuleName = mapToTextualRuleName(rhsRuleName);
      final Pattern interactionFolderPattern = Pattern.compile(HenshinInteractionFolderFilenameFilter.REGEX);
      final String interactionFolderName = interactionFolder.getName();
      Matcher interactionMatcher = interactionFolderPattern.matcher(interactionFolderName);
      interactionMatcher.matches();
      final String interactionReason = interactionMatcher.group(2);
      final String interactionType = interactionMatcher.group(3);
      final String interactionLocality;
      final String interactionTypeShort = mapToTextualInteractionType(interactionType);
      final String reasonString = ("Reason: " + interactionReason + ", Origin:" + interactionFolder.getName()).replaceAll(Pattern.quote(CsvUtils.CSV_SEP), ",");

      final HenshinResourceSet resourceSet = new HenshinResourceSet(interactionFolder.getAbsolutePath());
      final Resource leftResource = resourceSet.getResource(String.format("(1)%s.henshin", lhsRuleName));
      final Resource rightResource = resourceSet.getResource(String.format("(2)%s.henshin", rhsRuleName));
      switch (interactionType)
      {
      case "conflict":
      {
         final List<Node> leftSelfNodeVariables = determineSelfNodeVariables(leftResource, "LHS");
         final List<Node> rightSelfNodeVariables = determineSelfNodeVariables(rightResource, "LHS");

         interactionLocality = calculateInteractionLocality(leftSelfNodeVariables, rightSelfNodeVariables);

         break;
      }
      case "dependency":
      {
         final List<Node> leftSelfNodeVariables = determineSelfNodeVariables(leftResource, "RHS");
         final List<Node> rightSelfNodeVariables = determineSelfNodeVariables(rightResource, "LHS");

         interactionLocality = calculateInteractionLocality(leftSelfNodeVariables, rightSelfNodeVariables);

         break;
      }
      default:
         throw new IllegalArgumentException("Cannot handle " + interactionType);
      }

      final List<String> csvData = Arrays.asList(lhsTextualRuleName, rhsTextualRuleName, interactionTypeShort + interactionLocality, reasonString);
      final String csvLine = formatCsvLine(csvData);
      FileUtils.writeLines(outputCsvFile, Arrays.asList(csvLine), true);
   }

   private static String calculateInteractionLocality(final List<Node> leftSelfNodeVariables, final List<Node> rightSelfNodeVariables)
   {
      String interactionLocality;
      if (!leftSelfNodeVariables.isEmpty() && !rightSelfNodeVariables.isEmpty())
      {
         final Node leftSelfNodeVariable = leftSelfNodeVariables.get(0);
         final Node rightSelfNodeVariable = rightSelfNodeVariables.get(0);
         if (leftSelfNodeVariable.getName().equals(rightSelfNodeVariable.getName()))
         {
            interactionLocality = "l";
         } else
         {
            interactionLocality = "r";
         }
         //TODO@rkluge: How to determine self conflicts?
      } else
      {
         interactionLocality = "r";
      }
      return interactionLocality;
   }

   private static List<Node> determineSelfNodeVariables(final Resource resource, String side)
   {
      final Rule rule = (Rule) resource.getContents().get(0);
      final Graph pattern = "LHS".equals(side) ? rule.getLhs() : rule.getRhs();
      final List<Node> leftSelfNodeVariables = extractSelfNodeVariable(pattern);
      return leftSelfNodeVariables;
   }

   private static List<Node> extractSelfNodeVariable(final Graph pattern)
   {
      final EList<Node> leftNodesList = pattern.getNodes();
      final List<Node> leftSelfNodeVariables = leftNodesList.stream()//
            .filter(node -> node.getType().getName().equals("Node"))//
            .filter(node -> node.getAttributes().stream()
                  .anyMatch(attribute -> attribute.getType().getName().equals("isSelf") && attribute.getValue().equals("true")))
            .collect(Collectors.toList());
      return leftSelfNodeVariables;
   }

   /**
    * Joins the given entries using the default CSV separator.
    * @param entries the entries
    * @return the formatted CSV line
    */
   private static String formatCsvLine(final List<String> entries)
   {
      return StringUtils.join(entries, CsvUtils.CSV_SEP);
   }

   /**
    * Returns the CSV header entries for the textual interaction file format
    * @return the header entries
    */
   private static List<String> getInteractionsCsvHeaderEntries()
   {
      return Arrays.asList("lhsRule", "rhsRule", "interaction", "reason");
   }

   /**
    * Abbreviates the given interaction type (as used by Henshin)
    * @param interactionType the Henshin interaction type
    * @return the textual interaction type
    */
   private static String mapToTextualInteractionType(final String interactionType)
   {
      switch (interactionType)
      {
      case "dependency":
         return "d";
      case "conflict":
         return "c";
      default:
         throw new IllegalArgumentException("Cannot map " + interactionType);
      }
   }

   /**
    * Maps a Henshin rule name to a textual rule name
    * @param rule the Henshin rule name
    * @return the textual rule name
    */
   private static String mapToTextualRuleName(final String rule)
   {
      switch (rule)
      {
      case "activateLink":
         return RuleNames.R_A;
      case "inactivateLink":
         return RuleNames.R_I;
      case "findUnmarkedLink":
         return RuleNames.R_FIND_U;
      case "removeLink":
         return RuleNames.R_MINUS_E;
      case "handleLinkRemoval1":
         return RuleNames.R_MINUS_EH1;
      case "handleLinkRemoval2":
         return RuleNames.R_MINUS_EH2;
      case "addLink":
         return RuleNames.R_PLUS_E;
      case "handleLinkAddition1":
         return RuleNames.R_PLUS_EH1;
      case "handleLinkAddition2":
         return RuleNames.R_PLUS_EH2;
      case "modifyLinkWeight":
         return RuleNames.R_MOD_W;
      case "handleLinkWeightModification1":
         return RuleNames.R_MOD_WH1;
      case "handleLinkWeightModification2":
         return RuleNames.R_MOD_WH2;
      case "handleLinkWeightModification3":
         return RuleNames.R_MOD_WH2;
      case "handleLinkWeightModification4":
         return RuleNames.R_MOD_WH3;
      case "addNode":
         return RuleNames.R_PLUS_N;
      case "removeNode":
         return RuleNames.R_MINUS_N;
      case "unlock":
         return RuleNames.R_UNLOCK;
      case "dellock":
         return RuleNames.R_DELETE_LOCK;
      default:
         throw new IllegalArgumentException("Cannot map " + rule);
      }
   }
}
