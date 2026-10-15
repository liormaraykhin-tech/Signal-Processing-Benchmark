import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void calculateMAE_returnsCorrectAverageError() {
        double[] actual = {2, 2, 5};
        double[] reference = {1, 2, 3};

        double result = Main.calculateMAE(actual, reference);

        assertEquals(1.0, result, 0.0001);
    }

    @Test
    void calculateMAE_returnsZeroWhenArraysAreEqual() {
        double[] actual = {2, 4, 6};
        double[] reference = {2, 4, 6};

        double result = Main.calculateMAE(actual, reference);

        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void calculateMAE_handlesActualValuesSmallerThanReference() {
        double[] actual = {1, 2, 3};
        double[] reference = {2, 4, 6};

        double result = Main.calculateMAE(actual, reference);

        assertEquals(2.0, result, 0.0001);
    }
    @Test
    void calculateMAE_throwsExceptionForEmptyArrays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Main.calculateMAE(new double[0], new double[0])
        );
    }

    @Test
    void applyThreePointFilter_calculatesCorrectAverages() {
        double[] input = {2, 4, 8, 10};

        double[] result = Main.applyThreePointFilter(input);

        assertEquals(3.0, result[0], 0.0001);
        assertEquals(14.0 / 3.0, result[1], 0.0001);
        assertEquals(22.0 / 3.0, result[2], 0.0001);
        assertEquals(9.0, result[3], 0.0001);
    }

    @Test
    void applyFivePointFilter_calculatesCorrectAverages() {
        double[] input = {2, 4, 6, 8, 10, 12};

        double[] result = Main.applyFivePointFilter(input);

        assertEquals(4.0, result[0], 0.0001);
        assertEquals(5.0, result[1], 0.0001);
        assertEquals(6.0, result[2], 0.0001);
        assertEquals(8.0, result[3], 0.0001);
        assertEquals(9.0, result[4], 0.0001);
        assertEquals(10.0, result[5], 0.0001);
    }

    @Test
    void addNoise_keepsNoiseWithinExpectedRange() {
        double[] input = {5.0, 5.0, 5.0};
        double noiseLevel = 2.0;
        Random random = new Random(42);

        double[] result = Main.addNoise(input, noiseLevel, random);

        for (int i = 0; i < result.length; i++) {
            assertEquals(true, result[i] >= 3.0 && result[i] < 7.0);
        }
    }
    @Test
    void addNoise_changesSignal() {
        double[] input = {5.0, 5.0, 5.0};
        Random random = new Random(42);

        double[] result = Main.addNoise(input, 2.0, random);

        assertTrue(result[0] != input[0]);
        assertTrue(result[1] != input[1]);
        assertTrue(result[2] != input[2]);
    }
    @Test
    void calculateMAE_throwsExceptionForDifferentLengths() {
        double[] actual = {1.0, 2.0, 3.0};
        double[] reference = {1.0, 2.0};

        assertThrows(
                IllegalArgumentException.class,
                () -> Main.calculateMAE(actual, reference)
        );
    }
}
