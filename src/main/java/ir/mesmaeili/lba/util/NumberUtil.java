package ir.mesmaeili.lba.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

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
}
