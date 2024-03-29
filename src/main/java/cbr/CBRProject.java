package cbr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;
import de.dfki.mycbr.core.similarity.config.StringConfig;
import de.dfki.mycbr.util.Pair;
import minesweeper.Case;
import utils.Constants;
import utils.Exports;
import utils.Imports;
import utils.Transform;

/**
*
* Collection of CBR actions.
*
* @author Marcel N&ouml;hre, 357775
*
*/
public class CBRProject {
	private Project project;
	private Concept minesweeperPatternConcept;
	private AmalgamationFct minesweeperPatternSim;
	private ICaseBase casebase;
	private StringDesc[] attributes = new StringDesc[Constants.ATTRIBUTES_AMOUNT];
	
	/**
	 * Imports a project or creates a new one.
	 */
	protected CBRProject() {
		try {
			System.out.println("Importing Project...");
			importProject();
			System.out.println("The project " + project.getName() + " from " + project.getAuthor() + " was imported!");
		} catch(Exception importing) {
			try {
				System.out.println("No project found!");
				System.out.print("Creating new Project...");
				initProjectInformation();
				initSpecialSimilarity();
				initConceptAndAmalgation();
				initAttributes();
				initCaseBase();
				addCase(Constants.DEFAULT_CASE);
				Exports.exportProject(project);
				System.out.println(" Success!");
			} catch(Exception initializing) {
				System.out.println(" Failed!");
			}
		}
	}
	
	/**
	 * Import a existing project.
	 * 
	 * @throws Exception
	 */
	protected void importProject() throws Exception {
		project = Imports.importProject();
		minesweeperPatternConcept = project.getConceptByID("MinesweeperPatternConcept");
		minesweeperPatternSim = project.getActiveAmalgamFct();
		importAttributes();
		casebase = project.getCB("MinesweeperPatternCasebase");
	}
	
	/**
	 * Import attributes for a case instance.
	 * 
	 * @throws Exception
	 */
	private void importAttributes() throws Exception {
		for(int i = 0; i < Constants.ATTRIBUTES_AMOUNT; i++) {
			attributes[i] = (StringDesc) project.getAttDescsByName(Constants.ATTRIBUTE_NAMES[i]).getFirst();
		}
	}
	
	/**
	 * Initialize the project information.
	 * 
	 * @throws Exception
	 */
	private void initProjectInformation() throws Exception {
		project = new Project();
		project.setName("MinesweeperBackend");
		project.setAuthor("Jannis Kehrhahn 275136 and Marcel Nöhre 357775");
	}
	
	/**
	 * Initialize special similarity.
	 * 
	 * @throws Exception
	 */
	private void initSpecialSimilarity() throws Exception {
		SymbolFct sym = project.getSpecialFct();
		sym.setSimilarity("_unknown_", "_undefined_", 1);
		sym.setSimilarity("_undefined_", "_unknown_", 1);
		sym.setSimilarity("_others_", "_unknown_", 0);
		sym.setSimilarity("_others_", "_undefined_", 0);
		sym.setSimilarity("_unknown_", "_others_", 0);
		sym.setSimilarity("_undefined_", "_others_", 0);
	}
	
	/**
	 * Initialize the concept and amalgamation function.
	 * 
	 * @throws Exception
	 */
	private void initConceptAndAmalgation() throws Exception {
		minesweeperPatternConcept = project.createTopConcept("MinesweeperPatternConcept");
		minesweeperPatternSim = minesweeperPatternConcept.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "MinesweeperPatternSimFct", true);
	}
	
	/**
	 * Initialize case attributes.
	 * 
	 * @throws Exception
	 */
	private void initAttributes() throws Exception {
		for(int i = 0; i < Constants.ATTRIBUTES_AMOUNT; i++) {
			if(i == 0) {
				attributes[i] = configureAttribute(Constants.ATTRIBUTE_NAMES[i], 10);
			} else if(i < 9) {
				attributes[i] = configureAttribute(Constants.ATTRIBUTE_NAMES[i], 5);
			} else if (i >= 9 && i < 25) {
				attributes[i] = configureAttribute(Constants.ATTRIBUTE_NAMES[i], 1);
			} else {
				attributes[i] = configureAttribute(Constants.ATTRIBUTE_NAMES[i], 0);
			}
			
		}
	}
	
	/**
	 * Initialize the case base.
	 * 
	 * @throws Exception
	 */
	private void initCaseBase() throws Exception {
		casebase = project.createDefaultCB("MinesweeperPatternCasebase");
	}
	
	/**
	 * Configure the case attributes.
	 */
	private StringDesc configureAttribute(String descName, int weight) throws Exception {
		StringDesc attribute = new StringDesc(minesweeperPatternConcept, descName);
		attribute.addStringFct(StringConfig.LEVENSHTEIN, descName + "Fct", true);
		minesweeperPatternSim.setWeight(descName, weight);
		return attribute;
	}
	
	/**
	 * Extract the case from a instance object.
	 * 
	 * @param instance The instance object containing a case
	 * @return	The extracted case
	 */
	private Case getCaseFromInstance(Instance instance) {
		int attributeCounter = 0;
		String[] caseValues = new String[Constants.ATTRIBUTES_AMOUNT];
		for(StringDesc attribute : attributes) {
			caseValues[attributeCounter] = instance.getAttForDesc(attribute).getValueAsString();
			attributeCounter++;
		}	
		return new Case(caseValues);
		
	}
	
	/**
	 * Get a case from the case base.
	 * 
	 * @param pattern The pattern to search for.
	 * @return	The case with the given pattern
	 */
	protected Case getCase(String pattern) {
		return getCaseFromInstance(casebase.containsCase(pattern));
	}
	
	/**
	 * Add a list of cases to the case base.
	 * 
	 * @param caseList The list of cases
	 */
	protected void addCaseList(ArrayList<Case> caseList) {
		for(Case newCase : caseList) {
			try {
				if(!checkForCase(newCase.getName())) {
					addCase(newCase);
					System.out.println("Case " + newCase.getName() + " added to casebase!");
				} else {
					System.out.println("Case " + newCase.getName() + " already exists!");	
				}
			} catch (Exception e) {
				System.out.println("Invalid Case " + newCase.getName() + " detected!");
			}
        }
		System.out.println("");
	}
	
	/**
	 * Add a case to the case base.
	 * 
	 * @param newCase	The new case
	 * @throws Exception
	 */
	protected void addCase(Case newCase) throws Exception {
		if(newCase.getSolution().getSolvability()) {	
			Instance instance = minesweeperPatternConcept.addInstance(newCase.getName());
			instance.addAttribute(attributes[0], newCase.getPattern().getCenter());
			instance.addAttribute(attributes[1], newCase.getPattern().getInnerTopLeft());
			instance.addAttribute(attributes[2], newCase.getPattern().getInnerTop());
			instance.addAttribute(attributes[3], newCase.getPattern().getInnerTopRight());
			instance.addAttribute(attributes[4], newCase.getPattern().getInnerRight());
			instance.addAttribute(attributes[5], newCase.getPattern().getInnerBottomRight());
			instance.addAttribute(attributes[6], newCase.getPattern().getInnerBottom());
			instance.addAttribute(attributes[7], newCase.getPattern().getInnerBottomLeft());
			instance.addAttribute(attributes[8], newCase.getPattern().getInnerLeft());
			instance.addAttribute(attributes[9], newCase.getPattern().getOuterTopLeftCorner());
			instance.addAttribute(attributes[10], newCase.getPattern().getOuterTopLeft());
			instance.addAttribute(attributes[11], newCase.getPattern().getOuterTop());
			instance.addAttribute(attributes[12], newCase.getPattern().getOuterTopRight());
			instance.addAttribute(attributes[13], newCase.getPattern().getOuterTopRightCorner());
			instance.addAttribute(attributes[14], newCase.getPattern().getOuterRightTop());
			instance.addAttribute(attributes[15], newCase.getPattern().getOuterRight());
			instance.addAttribute(attributes[16], newCase.getPattern().getOuterRightBottom());
			instance.addAttribute(attributes[17], newCase.getPattern().getOuterBottomRightCorner());
			instance.addAttribute(attributes[18], newCase.getPattern().getOuterBottomRight());
			instance.addAttribute(attributes[19], newCase.getPattern().getOuterBottom());
			instance.addAttribute(attributes[20], newCase.getPattern().getOuterBottomLeft());
			instance.addAttribute(attributes[21], newCase.getPattern().getOuterBottomLeftCorner());
			instance.addAttribute(attributes[22], newCase.getPattern().getOuterLeftBottom());
			instance.addAttribute(attributes[23], newCase.getPattern().getOuterLeft());
			instance.addAttribute(attributes[24], newCase.getPattern().getOuterLeftTop());
			instance.addAttribute(attributes[25], newCase.getSolution().getSolvability() ? "True": "False");
			instance.addAttribute(attributes[26], Transform.stringArrayToSolutionString(newCase.getSolution().getCells()));
			instance.addAttribute(attributes[27], Transform.stringArrayToSolutionString(newCase.getSolution().getTypes()));
			casebase.addCase(instance);
		}
	}
	
	/**
	 * Remove a case from the case base.
	 * 
	 * @param pattern	THe pattern of the case
	 * @return	Wether the case was removed
	 */
	protected boolean removeCase(String pattern) {
		if(checkForCase(pattern)) {
			casebase.removeCase(pattern);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Check wether a case exists in the case base.
	 * 
	 * @param pattern	The pattern of the case
	 * @return	Wether the case exists in the case base
	 */
	protected boolean checkForCase(String pattern) {
		boolean available = false;
		if(casebase.containsCase(pattern) != null) {
			available = true;
		};
		return available;
	}
	
	/**
	 * Find the most similar case for a given problem.
	 * 
	 * @param problemCase	The active problem
	 * @return	The most similar case found for the given problem
	 */
	protected String caseQuery(Case problemCase) {
		System.out.println("Query starts... ");
		System.out.println("Input: " + problemCase.getName());
		Retrieval retrieve = new Retrieval(minesweeperPatternConcept, casebase);
		retrieve.setRetrievalMethod(RetrievalMethod.RETRIEVE_SORTED);
		String[] problemValues = Transform.caseToStringArray(problemCase);
		int attributeCounter = 0;
		Instance query = retrieve.getQueryInstance();
		for(StringDesc attribute : attributes) {
			try {
				query.addAttribute(attribute , attribute.getAttribute(problemValues[attributeCounter]));
				attributeCounter++;
			} catch (ParseException e) {
				System.out.println("Failed!");
			}
		}
		retrieve.start();
		List<Pair<Instance, Similarity>> result = retrieve.getResult();
		System.out.println("Retrieved result:");
		
		ArrayList<Pair<Case, Double>> resultList= new ArrayList<Pair<Case, Double>>();
		int caseAmount = result.size() < Constants.RESULT_ATTRIBTUES_AMOUNT ? result.size() : Constants.RESULT_ATTRIBTUES_AMOUNT;
		for (int i = 0; i < caseAmount; i++) {
			Case retrievedCase = getCaseFromInstance(minesweeperPatternConcept.getInstance(result.get(i).getFirst().getName()));
			double similarity = result.get(i).getSecond().getValue();
			if(similarity > Constants.MINIMUM_SIMILARITY) {
				resultList.add(new Pair<Case, Double>(retrievedCase, similarity));
				System.out.print("(+) ");
			} else {
				System.out.print("(-) ");
			}
			System.out.println("Case " + retrievedCase.getName() + 
					" fits with a probability of " + Math.floor(similarity * 100) / 100);
		}
		System.out.println("");
		return Transform.caseListToJson(resultList);
	}
}