1. Compile the DecisionTree.java file

2. Compile the BaggingDT.java file

3. Run the BaggingDT.java using the following argument: 
	java BaggingDT <TRAIN_FILE> <TEST_FILE> <BAG_SIZE>
	java BaggingDT "src/train-1.dat" "src/test-1.dat" 24

4. BaggingDT.java calls methods of DecisionTree.java internally.

5. BaggingDT.java also creates temporary .dat files and deletes them after execution. Make sure the program is not blocked from creating files in the directory in which TRAIN_FILE AND TEST_FILE is kept. 
