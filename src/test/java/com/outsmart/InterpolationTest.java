package com.outsmart;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


/**
 * @author Vadim Bobrov
 */
public class InterpolationTest {

    @Test
    public void testInterpolate() {

        long start = System.currentTimeMillis();
        double x[] = { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0,5.5, 6.0, 7.0,8.0, 9.0, 10.0,11.0, 12.0, 13.0, 14.0, 15.0, 16.0 };
        double y[] = { 1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0,1.0, -1.0, 2.0 };


        UnivariateInterpolator interpolator = new SplineInterpolator();
        UnivariateFunction function = interpolator.interpolate(x, y);

        double interpolationX = 0.5;
        double interpolatedY = function.value(interpolationX);

        for(double valuex = 0; valuex < 2; valuex += 0.001)
            function.value(valuex);

        System.out.println((System.currentTimeMillis() - start) + "ms");
        assertEquals(interpolatedY, 0);


        //System.out println("f(" + interpolationX + ") = " + interpolatedY);

    }
}
