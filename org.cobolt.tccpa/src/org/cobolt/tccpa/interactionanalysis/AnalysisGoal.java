package org.cobolt.tccpa.interactionanalysis;

import java.util.Arrays;
import java.util.Optional;

/**
 * The type of interaction to determine
 *
 * @author Roland Kluge - Initial implementation
 */
enum AnalysisGoal {
	CONFLICT, DEPENDENCY;

   static Optional<AnalysisGoal> getAnalysisGoalByName(final String analysisGoalStr) {
   	return Arrays.asList(values()).stream()//
   			.filter(goal -> goal.toString().equals(analysisGoalStr))//
   			.findAny();
   }
}