package ir.mesmaeili.lba.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class NumberUtil {
    private static Random rand = new Random();

    public static double round(double number, int scale) {
        BigDecimal bd = new BigDecimal(number).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int argMax(double[] array) {
        if (array == null || array.length == 0) {
            return -1;
        }
        int maxIndex = 0;
        double maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static double generateNormal(double mean, double stdDev) {
        // Generate two uniformly distributed random numbers
        double u1 = rand.nextDouble();
        double u2 = rand.nextDouble();

        // Apply the Box-Muller transform
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

        // Scale and shift to the desired mean and standard deviation
        return mean + stdDev * z;
    }

    public static double generateRandomDouble(double X) {
        Random random = new Random();
        return -X / 2 + X * random.nextDouble();
    }

    public static float generateRandomFloat(float X) {
        Random random = new Random();
        return -X / 2 + X * random.nextFloat();
    }

}
