package M2;

public class Problem2 extends BaseClass {
    private static double[] array1 = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6 };
    private static double[] array2 = { 1.0000001, 1.0000002, 1.0000003, 1.0000004, 1.0000005 }; 
    private static double[] array3 = { 1.0 / 3.0, 2.0 / 3.0, 4.0 / 3.0, 8.0 / 3.0,8.0 / 3.0 }; 
    private static double[] array4 = { 1e16, 1.0, -1e16, 2.0, -2.0, 1e-16 };
    private static double[] array5 = { Math.PI, Math.E, Math.sqrt(2), Math.sqrt(3), Math.sqrt(5), Math.log(2),
            Math.log10(3) };

    private static void sumValues(double[] arr, int arrayNumber) {
        // Only make edits between the designated "Start" and "End" comments
        printArrayInfo(arr, arrayNumber);

        // Challenge 1: Sum all the values of the passed in array and assign to `total`
        // Challenge 2: Have the sum be represented as a number with exactly 2 decimal places, assign to `modifiedTotal`
        // Example: 0.1 would be shown as 0.10, 1 would be shown as 1.00, etc
        // Step 1: sketch out plan using comments (include ucid and date)
        // Step 2: Add/commit your outline of comments (required for full credit)
        // Step 3: Add code to solve the problem (add/commit as needed)
        double total = 0;
        // Start Solution Edits
        // Solve Challenge 1 here
        
      
        // Solve Challenge 2 here
        Object modifiedTotal = "?";

        // End Solution Edits
        System.out.println("Total Raw Value: " +total);
        System.out.println("Total Modified Value: " + modifiedTotal);
        System.out.println("");
        System.out.println("______________________________________");
    }

    public static void main(String[] args) {
        final String ucid = "mt85"; // <-- change to your UCID
        // no edits below this line
        printHeader(ucid, 2);
        sumValues(array1, 1);
        sumValues(array2, 2);
        sumValues(array3, 3);
        sumValues(array4, 4);
        sumValues(array5, 5);
        printFooter(ucid, 2);

    }
}