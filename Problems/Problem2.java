import java.util.Arrays;
import java.text.DecimalFormat;

public class Problem2 {
    public static void main(String[] args) {
        // Don't edit anything here
        double[] a1 = new double[]{10.001, 11.591, 0.011, 5.991, 16.121, 0.131, 100.981, 1.001};
        double[] a2 = new double[]{1.99, 1.99, 0.99, 1.99, 0.99, 1.99, 0.99, 0.99};
        double[] a3 = new double[]{0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};
        double[] a4 = new double[]{10.01, -12.22, 0.23, 19.20, -5.13, 3.12};

        getTotal(a1);
        getTotal(a2);
        getTotal(a3);
        getTotal(a4);
    }

    static void getTotal(double[] arr) {
        System.out.println("Processing Array:" + Arrays.toString(arr));
        double total = 0;

        // Only arr is used (no direct usage of a1-a4 variables)
        // Passed in array's values get summed
        for (double value : arr) {
            total += value;
        }

        // Round the total to two decimal places like currency
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String totalOutput = decimalFormat.format(total);

        // jeo29 February 1, 2024
        System.out.println("\njeo29 February 5, 2024");
        // Terminal output is fully visible
        System.out.println("Total is " + totalOutput);
        System.out.println("End process");
    }
}

