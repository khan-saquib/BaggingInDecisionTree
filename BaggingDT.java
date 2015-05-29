import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BaggingDT {

	static List<String> fileNames = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		String trainFile = args[0], testFile = args[1];
		int k = Integer.parseInt(args[2]);
		DecisionTree dt = new DecisionTree();
		dt.populateAttributeListAndExamples(trainFile);
		dt.buildDecisionTree();
		// dt.printDecisionTree();
		dt.testDecisionTreeOnTrainingData(trainFile);
		dt.testDecisionTreeOnTestingData(testFile);

		// Bagging Part of the code
		splitTrainFileIntoKFiles(trainFile, k);

		List<List<Integer>> resultSet = new ArrayList<>();
		// Generate predicted class for all the bagging trees
		for (String fileName : fileNames) {
			DecisionTree dtBagging = new DecisionTree();
			dtBagging.populateAttributeListAndExamples(fileName);
			dtBagging.buildDecisionTree();
			resultSet.add(dtBagging.testDecisionTreeBagging(testFile));
		}

		for (String fileName : fileNames) {
			File file = new File(fileName);
			file.delete();
		}

		// Get the ACCURACY
		int countSuccess = 0;
		List<Integer> tempList;
		for (int example = 0; example < resultSet.get(0).size(); example++) {
			tempList = new ArrayList<>();
			for (int fileNo = 0; fileNo < k; fileNo++) {
				tempList.add(resultSet.get(fileNo).get(example));
			}
			if (maxRepeating(tempList.toArray(new Integer[0]), tempList.size(),
					10) == dt.examples.get(example)
					.get(dt.attributeList.size())) {
				countSuccess++;
			}

		}
		System.out.printf("Accuracy on Bag Size " + k + " = %.2f\n",
				(double) countSuccess / (double) resultSet.get(0).size() * 100);

	}

	static int maxRepeating(Integer[] arr, int n, int k) {
		// Iterate though input array, for every element
		// arr[i], increment arr[arr[i]%k] by k
		for (int i = 0; i < n; i++)
			arr[arr[i] % k] += k;

		// Find index of the maximum repeating element
		int max = arr[0], result = 0;
		for (int i = 1; i < n; i++) {
			if (arr[i] > max) {
				max = arr[i];
				result = i;
			}
		}

		/*
		 * Uncomment this code to get the original array back for (int i = 0; i<
		 * n; i++) arr[i] = arr[i]%k;
		 */

		// Return index of the maximum element
		return result;
	}

	/**
	 * 
	 * Read the train file and split it into k files
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public static void splitTrainFileIntoKFiles(String fileName, int k)
			throws IOException {
		Scanner sc = new Scanner(new File(fileName));
		List<String> trainFile = new ArrayList<>();
		String tempLine;

		while (sc.hasNextLine()) {
			tempLine = sc.nextLine();
			trainFile.add(tempLine);
		}
		sc.close();
		int index;
		List<List<String>> kFiles = new ArrayList<>();
		for (int counter = 0; counter < k; counter++) {
			List<String> tempFile = new ArrayList<>();
			tempFile.add(trainFile.get(0));
			for (int i = 0; i < trainFile.size(); i++) {
				index = (int) (Math.random() * trainFile.size() - 1) + 1;
				tempFile.add(trainFile.get(index));
			}
			kFiles.add(tempFile);
		}

		for (int counter = 0; counter < k; counter++) {
			String newFile = fileName.substring(0, fileName.lastIndexOf("."))
					+ counter + ".dat";
			fileNames.add(newFile);
			OutputStream out = new FileOutputStream(newFile);
			try {
				// Write data to 'out'
				for (String line : kFiles.get(counter)) {
					out.write(line.getBytes());
					out.write('\n');
				}
			} finally {
				// Make sure to close the file when done
				out.close();
			}
		}
	}

}
