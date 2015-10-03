package uk.co.malbec.hound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static <T> Map<String, ?> map(String name, T o) {
        Map<String, T> map = new HashMap<>();
        map.put(name, o);
        return map;
    }

    public static double interpolateList(int point, List<Long> times) {

        double run = (double) 100 / times.size();

        int lowIndex = 0;
        int currentIndex = 0;
        while (currentIndex * run < point) {
            lowIndex = currentIndex;
            currentIndex++;
        }
        int highIndex = currentIndex;
        long low = times.get(lowIndex - 1);
        long high = times.get(highIndex - 1);

        return linearInterpolate(lowIndex, low, highIndex, high, point) + low;

    }

    public static double linearInterpolate(double leftX, double leftY, double rightX, double rightY, double point){
        double rise =  rightY - leftY;
        double run = rightX  - leftX;
        double coefficient = rise/run;
        double x =  (rightX - leftX) * (point/100);
        double y = x * coefficient;
        return y;
    }

}
