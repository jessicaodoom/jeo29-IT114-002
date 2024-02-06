import java.util.Arrays;

public class Problem3 {
    public static void main(String[] args) {
        // Don't edit anything here
        Integer[] a1 = new Integer[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        Integer[] a2 = new Integer[]{-1, 1, -2, 2, 3, -3, -4, 5};
        Double[] a3 = new Double[]{-0.01, -0.0001, -.15};
        String[] a4 = new String[]{"-1", "2", "-3", "4", "-5", "5", "-6", "6", "-7", "7"};

        bePositive(a1);
        bePositive(a2);
        bePositive(a3);
        bePositive(a4);
    }

    static <T> void bePositive(T[] arr) {
        System.out.println("Processing Array:" + Arrays.toString(arr));

        // Create an output array of the same length and type as the input array
        T[] output = Arrays.copyOf(arr, arr.length);

        // Convert each value to its positive version
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Integer) {
                output[i] = (T) Integer.valueOf(Math.abs((Integer) arr[i]));
            } else if (arr[i] instanceof Double) {
                output[i] = (T) Double.valueOf(Math.abs((Double) arr[i]));
            } else if (arr[i] instanceof String) {
                // Handle conversion from String to Integer for strings that represent integers
                try {
                    Integer intValue = Integer.parseInt((String) arr[i]);
                    output[i] = (T) Integer.valueOf(Math.abs(intValue));
                } catch (NumberFormatException e) {
                    // If parsing fails, leave the original String value in the output
                    output[i] = arr[i];
                }
            }
        }

        // Convert the result datatype to the same type as the original datatype
        // (in case there were String conversions)
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof String) {
                if (output[i] instanceof Integer) {
                    output[i] = (T) String.valueOf(output[i]);
                }
            }
        }

        /// jeo29 February 1, 2024
        System.out.println("\njeo29 February 5, 2024");

        // Print the result with data type information
        StringBuilder sb = new StringBuilder();
        for (Object value : output) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(String.format("%s (%s)", value, value.getClass().getSimpleName().substring(0, 1)));
        }
        System.out.println("Result: " + sb.toString());
    }
}