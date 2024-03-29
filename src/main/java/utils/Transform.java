package utils;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.dfki.mycbr.util.Pair;
import minesweeper.Case;
import minesweeper.Pattern;
import minesweeper.Solution;

/**
*
* Collection of transform functionalities.
*
* @author Marcel N&ouml;hre, 357775
*
*/
public class Transform {
	
	/**
	 * Transform the input (frontend) to a case instance.
	 * 
	 * @param pattern		The pattern of the case instance
	 * @param solveable		Wether the case is solveable
	 * @param solutionCells	The cell position for the solution keys
	 * @param solutionTypes	The solution keys
	 * @return	The created case instance
	 */
	public static Case apiInputToCase(String pattern, boolean solveable, String solutionCells, String solutionTypes) {
		return new Case(pattern, new Pattern(pattern.toCharArray()), new Solution(solveable, solutionCells, solutionTypes));
	}
	
	/**
	 * Transform a case instance to a string array.
	 * 
	 * @param caseObject The case instance
	 * @return	The created string array
	 */
	public static String[] caseToStringArray(Case caseObject) {
		String[] caseArray = new String[Constants.ATTRIBUTES_AMOUNT];
		caseArray[0] = caseObject.getPattern().getCenter();
		caseArray[1] = caseObject.getPattern().getInnerTopLeft();
		caseArray[2] = caseObject.getPattern().getInnerTop();
		caseArray[3] = caseObject.getPattern().getInnerTopRight();
		caseArray[4] = caseObject.getPattern().getInnerRight();
		caseArray[5] = caseObject.getPattern().getInnerBottomRight();
		caseArray[6] = caseObject.getPattern().getInnerBottom();
		caseArray[7] = caseObject.getPattern().getInnerBottomLeft();
		caseArray[8] = caseObject.getPattern().getInnerLeft();
		caseArray[9] = caseObject.getPattern().getOuterTopLeftCorner();
		caseArray[10] = caseObject.getPattern().getOuterTopLeft();
		caseArray[11] = caseObject.getPattern().getOuterTop();
		caseArray[12] = caseObject.getPattern().getOuterTopRight();
		caseArray[13] = caseObject.getPattern().getOuterTopRightCorner();
		caseArray[14] = caseObject.getPattern().getOuterRightTop();
		caseArray[15] = caseObject.getPattern().getOuterRight();
		caseArray[16] = caseObject.getPattern().getOuterRightBottom();
		caseArray[17] = caseObject.getPattern().getOuterBottomRightCorner();
		caseArray[18] = caseObject.getPattern().getOuterBottomRight();
		caseArray[19] = caseObject.getPattern().getOuterBottom();
		caseArray[20] = caseObject.getPattern().getOuterBottomLeft();
		caseArray[21] = caseObject.getPattern().getOuterBottomLeftCorner();
		caseArray[22] = caseObject.getPattern().getOuterLeftBottom();
		caseArray[23] = caseObject.getPattern().getOuterLeft();
		caseArray[24] = caseObject.getPattern().getOuterLeftTop();
		caseArray[25] = caseObject.getSolution().getSolvability() ? "True" : "False";
		caseArray[26] = stringArrayToSolutionString(caseObject.getSolution().getCells());
		caseArray[27] = stringArrayToSolutionString(caseObject.getSolution().getTypes());
		return caseArray;
	}
	
	/**
	 * Transform a string array to a solution string.
	 * 
	 * @param arr The string array
	 * @return	The created solution string
	 */
	public static String stringArrayToSolutionString(String[] arr) {
        String solution = "";
        if(arr.length > 0) {
	        for(String element : arr) {
	        	solution += element + Constants.SOLUTION_SEPERATOR;
	        }
	        solution = solution.substring(0, solution.length() - 1);
        }
        return solution;
	}
	
	/**
	 * Transform a case list to a JSON file.
	 * 
	 * @param caseList The case list
	 * @return	The created JSON file
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String caseListToJson(ArrayList<Pair<Case, Double>> caseList) {
		JSONObject jsonContainer = new JSONObject();
		JSONArray jsonCaseList = new JSONArray();
		for(Pair caseElement : caseList) {
			JSONObject jsonCase = new JSONObject();
			int i = 0;
			for(String attribute : Transform.caseToStringArray((Case)caseElement.getFirst())) {
				if(Constants.ATTRIBUTE_NAMES[i].equals("SolutionCells") || Constants.ATTRIBUTE_NAMES[i].equals("SolutionTypes")) {
					JSONArray jsonSolutionArray = new JSONArray();
					String[] solutionArray = attribute.split(Constants.SOLUTION_SEPERATOR);
					for(String solution : solutionArray) {
						jsonSolutionArray.add(solution);
					}
					jsonCase.put(Constants.ATTRIBUTE_NAMES[i], jsonSolutionArray);
				} else {
					jsonCase.put(Constants.ATTRIBUTE_NAMES[i], attribute);
				}
				i++;
			}
			jsonCase.put("Similarity", caseElement.getSecond());
			jsonCaseList.add(jsonCase);
		}
		jsonContainer.put("CaseList", jsonCaseList);
		return jsonContainer.toJSONString();
	}
	
	/**
	 * Transform wether a case is different to another one as a JSON file.
	 * 
	 * @param isDifferent Wether the case is different
	 * @return	The created JSON file
	 */
	@SuppressWarnings("unchecked")
	public static String differenceToJson(boolean isDifferent) {
		JSONObject jsonContainer = new JSONObject();
		jsonContainer.put("Difference", isDifferent? "True" : "False");
		return jsonContainer.toString();
	}
}
