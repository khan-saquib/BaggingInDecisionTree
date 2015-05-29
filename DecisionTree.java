/**
 * DecisionTree class can be used to create objects. Each object represents its own decision tree.
 * Node class represent each node in the tree.
 * 
 * @author Saquib
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DecisionTree {
	
	
	
	
	
	

	//Root node of the decision tree
	Node root;
	
	//List of all the attribute names saved in order present in the train.dat and test.dat.
	List<String> attributeList;
	
	//Map of "all the possible values stored as a list" against "attribute name" as key.
	Map<String, List<Integer>> attributeValuesMap;
	
	//List of all examples from the training data during decision tree creation and testing data during test.
	List<List<Integer>> examples;
	
	//Most frequent class in the entire training data set.
	Integer mostFrequentClass;

	
	/**
	 * Initialize a decision tree object to relevant values
	 */
	public DecisionTree() {
		attributeList = new ArrayList<String>();
		attributeValuesMap = new LinkedHashMap<String, List<Integer>>();
		examples = new ArrayList<List<Integer>>();
	}

	/**
	 * Represents each node in the decision tree. 
	 * List<Node> children contains the list of all its children from left to right.
	 * classValue represents the class of this node. If the classValue is present, then children is null.
	 * 
	 * @author Saquib
	 *
	 */
	public class Node {

		String attribute;
		Integer classValue;
		List<Node> children;

	}

	
	/**
	 * 
	 *	builds the decision tree on the entire training data.
	 */
	public void buildDecisionTree() {
		this.root = new Node();
		List<String> attributeListRelevant = new ArrayList<String>(
				this.attributeList);
		buildDecisionTreeRecursion(this.root, attributeListRelevant,
				this.examples);
	}

	/**
	 * Builds the decision tree on the training data from startIndex to stopIndex.
	 * Ignores the training examples before the startIndex and after stopIndex
	 * 
	 * @param startIndex
	 * @param stopIndex
	 */
	private void buildDecisionTree(int startIndex, int stopIndex) {
		this.root = new Node();
		List<String> attributeListRelevant = new ArrayList<String>(
				this.attributeList);
		buildDecisionTreeRecursion(this.root, attributeListRelevant,
				this.examples.subList(startIndex, stopIndex));
	}

	/**
	 * This method creates the decision tree by calling itself recursively
	 * 
	 * @param node
	 * @param attributeListRelevant
	 * @param examplesRelevant
	 * @return
	 */
	private Node buildDecisionTreeRecursion(Node node,
			List<String> attributeListRelevant,
			List<List<Integer>> examplesRelevant) {

		Integer countPos = 0, countNeg = 0;
		for (List<Integer> example : examplesRelevant) {
			if (example.get(attributeList.size()) == 0)
				countNeg++;
			else
				countPos++;
		}
		// If all the examples are negative examples
		if (countNeg == examplesRelevant.size()) {
			node.classValue = 0;
			return node;
		}
		// If all the examples are positive examples
		if (countPos == examplesRelevant.size()) {
			node.classValue = 1;
			return node;
		}

		// If all the attributes have already been used, then this is a leaf
		// node. Select class value and return
		if (attributeListRelevant.isEmpty()) {
			Integer count = 0;
			for (List<Integer> example : examplesRelevant) {
				if (example.get(attributeList.size()) == 0)
					count++;
				else
					count--;
			}
			if (count > 0)
				node.classValue = 0;
			else if (count < 0)
				node.classValue = 1;
			else
				node.classValue = mostFrequentClass;

			node.attribute = null;
			return node;
		}

		// Check if the same example is repeated in the entire examples list.
		boolean flag = false;
		Integer classValue = 0;
		for (int count = 0; count < examplesRelevant.size(); count++) {
			for (int attributeIndex = 0; attributeIndex < examplesRelevant.get(
					count).size(); attributeIndex++) {
				if (examplesRelevant.get(count).get(attributeIndex) != examplesRelevant
						.get(0).get(attributeIndex))
					flag = true;
			}
			classValue += examplesRelevant.get(count).get(attributeList.size());
		}

		if (flag == false) {
			node.classValue = mostFrequentClass;
			if (classValue > (examplesRelevant.size() - classValue))
				node.classValue = 1;
			else if (classValue < (examplesRelevant.size() - classValue))
				node.classValue = 0;
			else
				node.classValue = mostFrequentClass;

			return node;
		}
		// If its not a leaf node, get the best attribute and set it as this
		// node
		node.attribute = getBestAttribute(attributeListRelevant,
				examplesRelevant);
		attributeListRelevant.remove(node.attribute);

		// split the relevant examples into a number of lists. NoOfLists =
		// Number of possible values of the attribute.
		List<List<List<Integer>>> listOfExamples;
		listOfExamples = new ArrayList<List<List<Integer>>>();
		for (int i = 0; i < attributeValuesMap.get(node.attribute).size(); i++) {
			listOfExamples.add(new ArrayList<List<Integer>>());
		}

		Integer attributeIndex = attributeList.indexOf(node.attribute);
		List<Integer> allPossibleAttributeValues = attributeValuesMap
				.get(node.attribute);
		int childIndex = -1;
		for (List<Integer> example : examplesRelevant) {
			childIndex = allPossibleAttributeValues.indexOf(example
					.get(attributeIndex));
			listOfExamples.get(childIndex).add(example);
		}

		// create the children node and call buildTree recursively on them.
		node.children = new ArrayList<DecisionTree.Node>();
		for (int count = 0; count < attributeValuesMap.get(node.attribute)
				.size(); count++) {
			if (!listOfExamples.isEmpty()) {
				node.children.add(buildDecisionTreeRecursion(new Node(),
						new ArrayList<String>(attributeListRelevant),
						listOfExamples.get(count)));
			} else {
				Node tempNode = new Node();
				tempNode.classValue = mostFrequentClass;
				node.children.add(tempNode);
			}
		}

		return node;
	}

	/**
	 * This method returns the best attribute after calculating lowest entropy value from the list of attributes given
	 * and the list of examples given to the method.
	 * 
	 * @param attributeList
	 * @param examples
	 * @return
	 */
	private String getBestAttribute(List<String> attributeList,
			List<List<Integer>> examples) {
		// calculate Information Gain based on examples.
		// Smallest entropy from all the different attributes
		// Integer totalExamples = examples.size();

		double entropy, minEntropy = 1.1;
		int bestAttributeIndex = -1;

		for (int attrIndex = 0; attrIndex < attributeList.size(); attrIndex++) {

			entropy = calculateAttributeEntropy(examples,
					attributeList.get(attrIndex));
			if (minEntropy > entropy) {
				bestAttributeIndex = attrIndex;
				minEntropy = entropy;
			}
		}
		return attributeList.get(bestAttributeIndex);
	}

	/**
	 * This method calculates the entropy for each attribute and returns the calculated entropy as double value
	 * 
	 * @param examples
	 * @param attribute
	 * @return
	 */
	private double calculateAttributeEntropy(List<List<Integer>> examples,
			String attribute) {

		List<Integer> attributeValues = attributeValuesMap.get(attribute);
		List<Integer> countNeg = new ArrayList<Integer>();
		List<Integer> countPos = new ArrayList<Integer>();
		int tempIndex;
		double entropy;
		for (Integer attr : attributeValues) {
			countNeg.add(0);
			countPos.add(0);
		}
		for (List<Integer> example : examples) {
			tempIndex = attributeValues.indexOf(example.get(attributeList
					.indexOf(attribute)));
			if (example.get(attributeList.size()) == 0)
				countNeg.set(tempIndex, countNeg.get(tempIndex) + 1);
			else
				countPos.set(tempIndex, countPos.get(tempIndex) + 1);
		}
		entropy = 0;
		for (int index = 0; index < attributeValues.size(); index++) {
			entropy = entropy
					+ ((double) countPos.get(index) + (double) countNeg
							.get(index)) / (double) examples.size()
					* entropy(countPos.get(index), countNeg.get(index));
		}

		return entropy;
	}

	/**
	 * 
	 * Calculates the entropy and returns the double value. Used internally by the calculateAttributeEntropy method
	 * 
	 * @param countPos
	 * @param countNeg
	 * @return
	 */
	private double entropy(int countPos, int countNeg) {
		if (countPos == 0 && countNeg == 0)
			return 0.0;

		double probPos = (double) countPos / (double) (countPos + countNeg);
		double probNeg = (double) countNeg / (double) (countPos + countNeg);
		if (probPos == 0.0 && probNeg != 0.0)
			return 0.0;
		else if (probPos != 0.0 && probNeg == 0.0)
			return 0.0;
		else if (probPos == 0.0 && probNeg == 0.0)
			return 0.0;
		else
			return -probNeg * Math.log(probNeg) / Math.log(2) - probPos
					* Math.log(probPos) / Math.log(2);
	}

	/**
	 * 
	 * Populates the examples and attribute list from the trainining data file
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void populateAttributeListAndExamples(String fileName)
			throws FileNotFoundException {
		Scanner sc = new Scanner(new File(fileName));

		// Create the attribute list and count values
		String attributeLine = sc.nextLine();
		String[] tempAttrList = attributeLine.split("\\s");
		for (int i = 0; i < tempAttrList.length; i = i + 2) {
			attributeList.add(tempAttrList[i].trim());
			attributeValuesMap.put(tempAttrList[i].trim(),
					new ArrayList<Integer>());
		}

		// Populate the examples in examples
		List<Integer> oneExample;
		int count;
		while (sc.hasNextLine()) {
			oneExample = new ArrayList<Integer>();
			String tempLine = sc.nextLine();
			count = 0;
			for (String str : tempLine.split("\\s")) {
				oneExample.add(Integer.parseInt(str.trim()));
				if (count < attributeList.size()
						&& !attributeValuesMap.get(attributeList.get(count))
								.contains(Integer.parseInt(str.trim())))
					attributeValuesMap.get(attributeList.get(count)).add(
							Integer.parseInt(str.trim()));
				count++;
			}
			examples.add(oneExample);
		}

		// Sort all the attriibuteValueLists
		for (String attribute : attributeValuesMap.keySet()) {
			List<Integer> attributeValues = attributeValuesMap.get(attribute);
			Collections.sort(attributeValues);
		}

		// Populate the most frequent class value
		count = 0;
		for (List<Integer> example : examples) {
			if (example.get(attributeList.size()) == 0)
				count++;
		}

		if (count >= (examples.size() - count))
			mostFrequentClass = 0;
		else
			mostFrequentClass = 1;
		sc.close();
		sc = null;
	}

	/**
	 * Prints the decision tree
	 */
	public void printDecisionTree() {
		printTreeRecursively(this.root, 1);
	}
	
	/**
	 * Recursive function to print the decision tree.
	 * 
	 * @param node
	 * @param level
	 */
	private void printTreeRecursively(Node node, int level) {
		// Print for the attribute and its left subtree
		for (int count = 0; count < node.children.size(); count++) {
			for (int i = 2; i <= level; i++) {
				System.out.print("| ");
			}
			System.out
					.print(node.attribute + " = "
							+ attributeValuesMap.get(node.attribute).get(count)
							+ " : ");

			if (node.children.get(count).classValue != null)
				System.out.println(node.children.get(count).classValue);
			else {
				System.out.println();
				printTreeRecursively(node.children.get(count), level + 1);
			}
		}
	}

	/**
	 * Tests the decision tree on Training Data and prints the accuracy.
	 * 
	 * @param fileName
	 */
	public void testDecisionTreeOnTrainingData(String fileName) {
		int countSuccess = 0;
		for (List<Integer> example : examples) {
			if (example.get(attributeList.size()) == testDecisionTreeRecursive(
					example, this.root))
				countSuccess++;
		}
		System.out.printf("Accuracy on Training Set(" + examples.size()
				+ " instances): %.2f\n", (double) countSuccess
				/ (double) examples.size() * 100);
	}

	/**
	 * Evaluates the class of the example from the decision tree and returns the calculated class.
	 * 
	 * @param example
	 * @param node
	 * @return returns calculated class value for each example
	 */
	private Integer testDecisionTreeRecursive(List<Integer> example, Node node) {
		if (node.classValue != null) {
			return node.classValue;
		}
		List<Integer> attributeValues;
		attributeValues = this.attributeValuesMap.get(node.attribute);
		Integer index = attributeValues.indexOf(example.get(attributeList
				.indexOf(node.attribute)));
		return testDecisionTreeRecursive(example, node.children.get(index));

	}

	/**
	 * Read the test file and populate the test data into examples
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	private void readTestFile(String fileName) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(fileName));
		// Populate the examples in examples
		examples = new ArrayList<List<Integer>>();
		List<Integer> oneExample;
		int count;
		sc.nextLine();
		while (sc.hasNextLine()) {
			oneExample = new ArrayList<Integer>();
			String tempLine = sc.nextLine();
			count = 0;
			for (String str : tempLine.split("\\s")) {
				oneExample.add(Integer.parseInt(str.trim()));
				if (count < attributeList.size()
						&& !attributeValuesMap.get(attributeList.get(count))
								.contains(Integer.parseInt(str.trim())))
					attributeValuesMap.get(attributeList.get(count)).add(
							Integer.parseInt(str.trim()));
				count++;
			}
			examples.add(oneExample);
		}
	}

	/**
	 * This function reads from the test file and prints the accuracy of the
	 * decision tree on the test data.
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void testDecisionTreeOnTestingData(String fileName)
			throws FileNotFoundException {
		readTestFile(fileName);
		int countSuccess = 0;
		for (List<Integer> example : examples) {
			if (example.get(attributeList.size()) == testDecisionTreeRecursive(
					example, this.root))
				countSuccess++;
		}
		// System.out.printf("%.2f\n" ,
		// (double)countSuccess/(double)examples.size()*100);
		 System.out.printf("Accuracy on Testing Set("+ examples.size()
		 +" instances): %.2f\n" ,
		 (double)countSuccess/(double)examples.size()*100);

	}
	
	/**
	 * This function reads from the test file and prints the accuracy of the
	 * decision tree on the test data.
	 * 
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public List<Integer> testDecisionTreeBagging(String fileName)
			throws FileNotFoundException {
		readTestFile(fileName);
		List<Integer> resultSet = new ArrayList<>();
		for (List<Integer> example : examples) {
			resultSet.add(testDecisionTreeRecursive(example, this.root));
		}
		return resultSet;
	}
	
	
	
	

	public static void main(String[] args) throws FileNotFoundException {

		// for(int stepSize = 40; stepSize<122; stepSize=stepSize+5)
		// {
		// DecisionTree dt = new DecisionTree();
		// dt.populateAttributeListAndExamples(args[0]);
		// dt.buildDecisionTree(0,stepSize);
		// System.out.print(stepSize + "\n");
		// dt.testDecisionTreeOnTestingData(args[1]);
		// }
		//

		
		
		DecisionTree dt = new DecisionTree();
		dt.populateAttributeListAndExamples(args[0]);
		dt.buildDecisionTree();
		dt.printDecisionTree();
		dt.testDecisionTreeOnTrainingData(args[0]);
		dt.testDecisionTreeOnTestingData(args[1]);
	}

}
