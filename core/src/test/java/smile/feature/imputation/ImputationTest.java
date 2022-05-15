/*
 * Copyright (c) 2010-2021 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */

package smile.feature.imputation;

import java.util.function.Function;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import smile.test.data.SyntheticControl;
import smile.math.MathEx;
import static org.junit.Assert.*;

/**
 *
 * @author Haifeng Li
 */
public class ImputationTest {

    public ImputationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private void impute(Function<double[][], double[][]> imputer, double[][] data, double rate, double expected) throws Exception {
        MathEx.setSeed(19650218); // to get repeatable results.

        int n = 0;
        double[][] missing = new double[data.length][data[0].length];
        for (int i = 0; i < missing.length; i++) {
            for (int j = 0; j < missing[i].length; j++) {
                if (MathEx.random() < rate) {
                    n++;
                    missing[i][j] = Double.NaN;
                } else {
                    missing[i][j] = data[i][j];
                }
            }
        }

        double[][] imputed = imputer.apply(missing);

        double error = 0.0;
        for (int i = 0; i < imputed.length; i++) {
            for (int j = 0; j < imputed[i].length; j++) {
                error += Math.abs(data[i][j] - imputed[i][j]) / data[i][j];
            }
        }

        error = 100 * error / n;
        System.out.format("The error of %d%% missing values = %.2f%n", (int) (100 * rate),  error);
        assertEquals(expected, error, 1E-2);
    }

    @Test(expected = Test.None.class)
    public void testAverage() throws Exception {
        System.out.println("Column Average Imputation");
        double[][] data = SyntheticControl.x;

        impute(SimpleImputer::impute, data, 0.01, 39.11);
        impute(SimpleImputer::impute, data, 0.05, 48.86);
        impute(SimpleImputer::impute, data, 0.10, 45.24);
        impute(SimpleImputer::impute, data, 0.15, 44.59);
        impute(SimpleImputer::impute, data, 0.20, 41.93);
        impute(SimpleImputer::impute, data, 0.25, 44.77);
    }

    @Test(expected = Test.None.class)
    public void testSVD() throws Exception {
        System.out.println("SVD Imputation");
        double[][] data = SyntheticControl.x;
        int k = data[0].length / 5;

        Function<double[][], double[][]> imputer = x -> SVDImputer.impute(x, k, 10);
        impute(imputer, data, 0.01, 13.50);
        impute(imputer, data, 0.05, 15.84);
        impute(imputer, data, 0.10, 14.94);
        // Matrix will be rank deficient with higher missing rate.
    }

    @Test(expected = Test.None.class)
    public void testLLS() throws Exception {
        System.out.println("LLS Imputation");
        double[][] data = SyntheticControl.x;

        Function<double[][], double[][]> imputer = x -> LLSImputer.impute(x, 10);
        impute(imputer, data, 0.01, 14.66);
        impute(imputer, data, 0.05, 16.35);
        impute(imputer, data, 0.10, 15.50);
        impute(imputer, data, 0.15, 15.55);
        impute(imputer, data, 0.20, 15.26);
        impute(imputer, data, 0.25, 15.86);
    }
}