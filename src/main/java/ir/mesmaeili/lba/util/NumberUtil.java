package ir.mesmaeili.lba.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {

    public static double round(double number, int scale) {
        BigDecimal bd = new BigDecimal(number).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
