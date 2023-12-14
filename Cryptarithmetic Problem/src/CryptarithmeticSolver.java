import java.io.*;
import java.util.*;

public class CryptarithmeticSolver {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Input file name
        System.out.print("Enter Absolute Path of Input File: ");
        String fileName = scanner.nextLine();

        if (new java.io.File(fileName).exists()) {
            List<String> puzzleLines = new ArrayList<>();
            Map<String, List<String>> constraints = new HashMap<>();
            Map<String, String> variableAssignment = new HashMap<>();

            try {
                // Initialize the puzzle, constraints, and variable assignments
                initializePuzzle(fileName, puzzleLines, constraints, variableAssignment);

                // Solve the cryptarithmetic puzzle
                Map<String, String> solution = solveCryptarithmetic(constraints, variableAssignment, String.join("", puzzleLines));

                // Generate and print the output
                generateOutput("output_" + fileName.substring(fileName.lastIndexOf('/') + 1), solution, String.join("", puzzleLines));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File does not exist.");
        }

        scanner.close();
    }

    /**
     * Check if the assignment of variables is complete and consistent with the problem.
     *
     * @param variableAssignment Current assignment of variables
     * @param problem            Cryptarithmetic problem
     * @return True if assignment is complete and consistent, false otherwise
     */
    private static boolean isAssignmentComplete(Map<String, String> variableAssignment, String problem) {
        // Check if all variables have been assigned a value
        for (Map.Entry<String, String> entry : variableAssignment.entrySet()) {
            if (entry.getValue() == null) {
                return false;
            }
        }

        // Build numbers using the assigned values
        StringBuilder firstNumber = new StringBuilder();
        StringBuilder secondNumber = new StringBuilder();
        StringBuilder thirdNumber = new StringBuilder();
        for (int index = 0; index < problem.length(); index++) {
            char currentChar = problem.charAt(index);
            String variableValue = variableAssignment.get(String.valueOf(currentChar));
            if (index < 4) {
                firstNumber.append(variableValue);
            } else if (index < 8) {
                secondNumber.append(variableValue);
            } else {
                thirdNumber.append(variableValue);
            }
        }

        // Check if the equation holds true
        return Integer.parseInt(firstNumber.toString()) + Integer.parseInt(secondNumber.toString()) == Integer.parseInt(thirdNumber.toString());
    }

    /**
     * Select an unassigned variable based on minimum remaining values and degree heuristics.
     *
     * @param constraints         Map of constraints for each variable
     * @param variableAssignment  Current assignment of variables
     * @return Selected unassigned variable
     */
    private static String selectUnassignedVariable(Map<String, List<String>> constraints, Map<String, String> variableAssignment) {
        List<String> unassignedVariables = new ArrayList<>();

        // Find unassigned variables
        for (String variable : variableAssignment.keySet()) {
            if (variableAssignment.get(variable) == null) {
                unassignedVariables.add(variable);
            }
        }

        // If all variables are assigned, return null
        if (unassignedVariables.isEmpty()) {
            return null;
        }

        // Select variable with minimum remaining values and degree heuristics
        String selectedVariable = null;
        int minConstraints = Integer.MAX_VALUE;

        for (String currentVariable : unassignedVariables) {
            int variableConstraints = countVariableConstraints(currentVariable, constraints, variableAssignment);
            if (variableConstraints < minConstraints) {
                selectedVariable = currentVariable;
                minConstraints = variableConstraints;
            }
        }

        return selectedVariable;
    }

    /**
     * Count the number of constraints for a variable.
     *
     * @param variable            Variable for which constraints are counted
     * @param constraints         Map of constraints for each variable
     * @param variableAssignment  Current assignment of variables
     * @return Number of constraints for the variable
     */
    private static int countVariableConstraints(String variable, Map<String, List<String>> constraints, Map<String, String> variableAssignment) {
        int count = 0;
        for (String value : constraints.get(variable)) {
            if (isConsistent(variable, value, new HashMap<>(variableAssignment))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Order domain values for a variable based on constraints and heuristics.
     *
     * @param constraints         Map of constraints for each variable
     * @param variable            Variable for which domain values are ordered
     * @param variableAssignment  Current assignment of variables
     * @return Ordered list of domain values
     */
    private static List<String> orderDomainValues(Map<String, List<String>> constraints, String variable, Map<String, String> variableAssignment) {
        // Get the domain for the variable
        List<String> domain = new ArrayList<>(Objects.requireNonNull(constraints.get(variable)));

        // Sort the domain in reverse order
        domain.sort(Collections.reverseOrder());

        return domain;
    }

    /**
     * Check if assigning a value to a variable is consistent with the current assignment.
     *
     * @param variable            Variable to be assigned
     * @param value               Value to be assigned to the variable
     * @param variableAssignment  Current assignment of variables
     * @return True if assignment is consistent, false otherwise
     */
    private static boolean isConsistent(String variable, String value, Map<String, String> variableAssignment) {
        // If the variable is a constraint, return true
        if (Arrays.asList("c1", "c2", "c3", "c4").contains(variable)) {
            return true;
        }

        // Put the value in the assignment map
        variableAssignment.put(variable, value);

        // Remove the carry variables
        variableAssignment.remove("c1");
        variableAssignment.remove("c2");
        variableAssignment.remove("c3");
        variableAssignment.remove("c4");

        // Remove null values
        List<String> assignedValues = new ArrayList<>(variableAssignment.values());
        assignedValues.removeIf(Objects::isNull);

        // Check for consistency by comparing unique values
        return new HashSet<>(assignedValues).size() == assignedValues.size();
    }

    /**
     * Solve the cryptarithmetic puzzle using backtracking.
     *
     * @param constraints         Map of constraints for each variable
     * @param variableAssignment  Current assignment of variables
     * @param problem             Cryptarithmetic problem
     * @return Solution to the puzzle
     */
    private static Map<String, String> solveCryptarithmetic(Map<String, List<String>> constraints, Map<String, String> variableAssignment, String problem) {
        // Check if the assignment is complete
        if (isAssignmentComplete(variableAssignment, problem)) {
            return variableAssignment;
        }

        // Select an unassigned variable
        String variable = selectUnassignedVariable(constraints, variableAssignment);

        // If all variables are assigned, return null
        if (variable == null) {
            return null;
        }

        // Iterate through the ordered domain values for the variable
        for (String value : orderDomainValues(constraints, variable, variableAssignment)) {
            // Check if assigning the value is consistent
            if (isConsistent(variable, value, new HashMap<>(variableAssignment))) {
                // Assign the value
                variableAssignment.put(variable, value);

                // Recursively solve the puzzle
                Map<String, String> result = solveCryptarithmetic(constraints, variableAssignment, problem);

                // If a solution is found, return it
                if (result != null) {
                    return result;
                }

                // Backtrack by unassigning the variable
                variableAssignment.put(variable, null);
            }
        }

        // If no solution is found, return null
        return null;
    }

    /**
     * Initialize the puzzle, constraints, and variable assignments from the input file.
     *
     * @param fileName            Name of the input file
     * @param puzzleLines         List to store lines of the puzzle
     * @param constraints         Map of constraints for each variable
     * @param variableAssignment  Current assignment of variables
     * @throws IOException If there is an issue reading from the file
     */
    private static void initializePuzzle(String fileName, List<String> puzzleLines, Map<String, List<String>> constraints, Map<String, String> variableAssignment) throws IOException {
        // Initialize constraints map with a list for each variable
        constraints.put("constraints", new ArrayList<>());

        // Define auxiliary variables used in the puzzle
        List<String> auxiliaryVariables = Arrays.asList("c1", "c2", "c3", "c4");

        // Define possible constraints for the puzzle
        List<List<Object>> possibleConstraints = Arrays.asList(
                Arrays.asList(3, 7, 12, "c1"),
                Arrays.asList(2, 6, 11, "c1", "c2"),
                Arrays.asList(1, 5, 10, "c2", "c3"),
                Arrays.asList(0, 4, 9, "c3", "c4"),
                Arrays.asList(8, "c4")
        );

        String problemString;

        // Read the puzzle lines from the input file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                puzzleLines.add(line.trim());
            }
        }

        // Concatenate the puzzle lines to form the complete problem string
        problemString = String.join("", puzzleLines);

        // Populate the constraints map based on possible constraints
        for (List<Object> constraint : possibleConstraints) {
            Set<String> variablesInConstraint = new HashSet<>();
            for (Object variable : constraint) {
                String currentVar;
                // If the variable is an integer, get it from the problem string
                if (variable instanceof Integer) {
                    currentVar = String.valueOf(problemString.charAt((Integer) variable));
                } else {
                    currentVar = (String) variable;
                }
                variablesInConstraint.add(currentVar);
            }
            // Add variables in the constraint to the "constraints" list
            constraints.get("constraints").addAll(variablesInConstraint);
        }

        // Add variables not present in auxiliaryVariables to the "constraints" list
        List<String> constraintsCopy = new ArrayList<>(constraints.get("constraints"));
        constraintsCopy.removeIf(auxiliaryVariables::contains);
        constraints.get("constraints").addAll(constraintsCopy);

        // Initialize domain values for variables based on puzzle structure
        for (int index : Arrays.asList(1, 2, 3, 5, 6, 7, 9, 10, 11, 12)) {
            constraints.put(String.valueOf(problemString.charAt(index)), new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        }

        for (int index : Arrays.asList(0, 4)) {
            constraints.put(String.valueOf(problemString.charAt(index)), new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9")));
        }

        // Initialize domain values for auxiliary variables
        for (String constant : auxiliaryVariables) {
            constraints.put(constant, new ArrayList<>(Arrays.asList("0", "1")));
        }

        // Set specific values for certain variables to reduce the search space
        constraints.put("c4", new ArrayList<>(Collections.singletonList("1")));
        constraints.put(String.valueOf(problemString.charAt(8)), new ArrayList<>(Collections.singletonList("1")));

        // Create a set of all variables (including auxiliary variables)
        Set<String> variables = new HashSet<>(Arrays.asList(problemString.split("")));
        variables.addAll(auxiliaryVariables);

        // Initialize variable assignment map with null values for all variables
        for (String variable : variables) {
            variableAssignment.put(variable, null);
        }
    }

    /**
     * Generate and write the solution to the output file.
     *
     * @param fileName       Name of the output file
     * @param solution       Solution to the puzzle
     * @param initialProblem Cryptarithmetic problem
     */
    private static void generateOutput(String fileName, Map<String, String> solution, String initialProblem) {
        try {
            System.out.println(fileName);
            // Create the output file in the current directory

            try (FileWriter fileWriter = new FileWriter(fileName)) {
                // Write the first line of the solution
                for (int i = 0; i < 4; i++) {
                    fileWriter.write(solution.get(String.valueOf(initialProblem.charAt(i))));
                }
                fileWriter.write("\n");

                // Write the second line of the solution
                for (int i = 4; i < 8; i++) {
                    fileWriter.write(solution.get(String.valueOf(initialProblem.charAt(i))));
                }
                fileWriter.write("\n");

                // Write the third line of the solution
                for (int i = 8; i < 13; i++) {
                    fileWriter.write(solution.get(String.valueOf(initialProblem.charAt(i))));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
