package org.cobolt.tccpa.henshintocsv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.cobolt.tccpa.CsvUtils;
import org.cobolt.tccpa.stabilizationanalysis.InteractionCategory;
import org.cobolt.tccpa.stabilizationanalysis.InteractionType;
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
   private static final String IS_SELF_ATTRIBUTE_NAME = "isSelf";

   private static final String NODE_CLASS_NAME = "Node";

   private static final String RHS = "RHS";

   private static final String LHS = "LHS";

   private static final String HENSHIN_DEPENDENCY = "dependency";

   private static final String HENSHIN_CONFLICT = "conflict";

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

      FileUtils.writeLines(outputCsvFile, Arrays.asList(CsvUtils.formatCsvLine(getInteractionsCsvHeaderEntries())), false);

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
      final Matcher interactionMatcher = interactionFolderPattern.matcher(interactionFolderName);
      interactionMatcher.matches();
      final String interactionReason = interactionMatcher.group(2);
      final String henshinInteractionType = interactionMatcher.group(3);
      final InteractionType interactionTypeShort = mapToTextualInteractionType(henshinInteractionType);
      final String reasonString = CsvUtils.makeCsvSafe("Reason: " + interactionReason + ", Origin:" + formatOrigin(interactionFolder));

      final HenshinResourceSet resourceSet = new HenshinResourceSet(interactionFolder.getAbsolutePath());
      final Resource leftResource = resourceSet.getResource(String.format("(1)%s.henshin", lhsRuleName));
      final Resource rightResource = resourceSet.getResource(String.format("(2)%s.henshin", rhsRuleName));
      final InteractionCategory interactionLocality = getLocality(henshinInteractionType, leftResource, rightResource);

      final List<String> csvData = Arrays.asList(lhsTextualRuleName, rhsTextualRuleName, interactionTypeShort.getMnemonic() + interactionLocality.getMnemonic(),
            reasonString);
      final String csvLine = CsvUtils.formatCsvLine(csvData);
      FileUtils.writeLines(outputCsvFile, Arrays.asList(csvLine), true);
   }

   /**
    * Formats the entire path relative to the result folder root
    * @param interactionFolder the folder containing a contrete interaction
    * @return
    */
   private static String formatOrigin(final File interactionFolder)
   {
      final File interactionSetFolder = interactionFolder.getParentFile();
      final File dateFolder = interactionSetFolder.getParentFile();
      return String.format("%s/%s/%s", dateFolder.getName(), interactionSetFolder.getName(), interactionFolder.getName());
   }

   private static InteractionCategory getLocality(final String henshinInteractionType, final Resource leftResource, final Resource rightResource)
   {
      final InteractionCategory interactionLocality;
      if (isSameMatchConfict(henshinInteractionType, leftResource, rightResource))
         interactionLocality = InteractionCategory.SAME_MATCH;
      else
         interactionLocality = determineWhetherRemoteOrLocalInteraction(henshinInteractionType, leftResource, rightResource);
      return interactionLocality;
   }

   /**
    * Returns the set of all {@link Node} names in the given {@link Graph}
    * @param graph the graph
    * @return the set of node names
    */
   private static Set<String> collectNodeNames(final Graph graph)
   {
      return graph.getNodes().stream().map(Node::getName).collect(Collectors.toSet());
   }

   /**
    * Determines whether the given left and right resources of an interaction is a same-match conflict
    *
    * This is the case if the involved rules have the same names and if the names of the variables in both rules are identical (because they are prefixed with the unique ID of the elements in the minimal model)
    * @param henshinInteractionType the Henshin interaction type
    * @param leftResource the resource containing the left {@link Rule}
    * @param rightResource the resource containing the right {@link Rule}
    * @return true if the given resources are part of a same-match conflict
    */
   private static boolean isSameMatchConfict(final String henshinInteractionType, final Resource leftResource, final Resource rightResource)
   {
      if (!HENSHIN_CONFLICT.equals(henshinInteractionType))
         return false;

      final Rule leftRule = (Rule) leftResource.getContents().get(0);
      final Rule rightRule = (Rule) rightResource.getContents().get(0);
      if (!leftRule.getName().equals(rightRule.getName()))
         return false;

      final Set<String> leftNames = collectNodeNames(leftRule.getLhs());
      final Set<String> rightNames = collectNodeNames(rightRule.getLhs());
      if (!leftNames.equals(rightNames))
         return false;

      return true;
   }

   /**
    * Determines the {@link Node}s that represents a node variable and has the constraint isSelf=true
    * @param resource the resource containing the pattern
    * @param side the specification from which side to extract the variables (LHS or RHS)
    * @return the list of self-node variables
    */
   private static List<Node> getSelfNodeVariables(final Resource resource, final String side)
   {
      final Rule rule = (Rule) resource.getContents().get(0);
      final Graph pattern;
      switch (side)
      {
      case LHS:
         pattern = rule.getLhs();
         break;
      case RHS:
         pattern = rule.getRhs();
         break;
      default:
         throw new IllegalArgumentException("Invalid rule side specification " + side);
      }
      final List<Node> leftSelfNodeVariables = extractSelfNodeVariable(pattern);
      return leftSelfNodeVariables;
   }

   private static InteractionCategory determineWhetherRemoteOrLocalInteraction(final String henshinInteractionType, final Resource leftResource,
         final Resource rightResource)
   {
      final InteractionCategory interactionLocality;
      final List<Node> leftSelfNodeVariables;
      switch (henshinInteractionType)
      {
      case HENSHIN_CONFLICT:
      {
         leftSelfNodeVariables = getSelfNodeVariables(leftResource, LHS);
         break;
      }
      case HENSHIN_DEPENDENCY:
      {
         leftSelfNodeVariables = getSelfNodeVariables(leftResource, RHS);
         break;
      }
      default:
         throw new IllegalArgumentException("Cannot handle " + henshinInteractionType);
      }
      final List<Node> rightSelfNodeVariables = getSelfNodeVariables(rightResource, LHS);

      interactionLocality = determineWhetherRemoteOrLocalInteraction(leftSelfNodeVariables, rightSelfNodeVariables);
      return interactionLocality;
   }

   private static InteractionCategory determineWhetherRemoteOrLocalInteraction(final List<Node> leftSelfNodeVariables, final List<Node> rightSelfNodeVariables)
   {
      final InteractionCategory interactionLocality;
      if (!leftSelfNodeVariables.isEmpty() && !rightSelfNodeVariables.isEmpty())
      {
         final Node leftSelfNodeVariable = leftSelfNodeVariables.get(0);
         final Node rightSelfNodeVariable = rightSelfNodeVariables.get(0);
         if (leftSelfNodeVariable.getName().equals(rightSelfNodeVariable.getName()))
         {
            interactionLocality = InteractionCategory.LOCAL;
         } else
         {
            interactionLocality = InteractionCategory.REMOTE;
         }
      } else
      {
         interactionLocality = InteractionCategory.REMOTE;
      }
      return interactionLocality;
   }

   /**
    * Determines the {@link Node}s that represents a node variable and has the constraint isSelf=true
    * @param pattern the pattern to analyze
    * @return the list of self-node variables
    */
   private static List<Node> extractSelfNodeVariable(final Graph pattern)
   {
      final EList<Node> leftNodesList = pattern.getNodes();
      final List<Node> leftSelfNodeVariables = leftNodesList.stream()//
            .filter(node -> node.getType().getName().equals(NODE_CLASS_NAME))//
            .filter(node -> node.getAttributes().stream()
                  .anyMatch(attribute -> attribute.getType().getName().equals(IS_SELF_ATTRIBUTE_NAME) && attribute.getValue().equals("true")))
            .collect(Collectors.toList());
      return leftSelfNodeVariables;
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
   private static InteractionType mapToTextualInteractionType(final String interactionType)
   {
      switch (interactionType)
      {
      case HENSHIN_DEPENDENCY:
         return InteractionType.DEPENDENCY;
      case HENSHIN_CONFLICT:
         return InteractionType.CONFLICT;
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
