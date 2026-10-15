import java.util.Random;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JFrame;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {

        // 1. Signal settings
        double amplitude = 5.0;
        double frequency = 2.0;
        //noise
        double noiseLevel = 2.0;

        // 2. Sampling settings
        int[] sampleRates = {20, 40, 60, 80, 100};
        int durationSeconds = 2;
        int numberOfRuns = 100;
        //MAE_Findings
        double[] maeBeforeResults = new double[sampleRates.length];
        double[] maeThreeResults = new double[sampleRates.length];
        double[] maeFiveResults = new double[sampleRates.length];
        double[] cleanThreeResults = new double[sampleRates.length];
        double[] cleanFiveResults = new double[sampleRates.length];

        // 3. Create random number generator
        Random random = new Random();
        int rateIndex = 0;
        for (int sampleRate : sampleRates) {
            double totalMaeAcrossRuns = 0.0;
            double totalFilteredMaeAcrossRuns = 0.0;
            double totalFiveFilteredMaeAcrossRuns = 0.0;
            // 5. Generate the original signal
            double[] voltages = generateSignal(sampleRate, durationSeconds, amplitude, frequency);
            // Test filtering without noise
            double[] cleanFilteredThree = applyThreePointFilter(voltages);
            double[] cleanFilteredFive = applyFivePointFilter(voltages);

            double cleanThreeMae = calculateMAE(cleanFilteredThree, voltages);
            double cleanFiveMae = calculateMAE(cleanFilteredFive, voltages);

            cleanThreeResults[rateIndex] = cleanThreeMae;
            cleanFiveResults[rateIndex] = cleanFiveMae;

            System.out.println("Clean signal test at " + sampleRate + " Hz");
            System.out.println("Clean signal MAE with 3-point filter: " + cleanThreeMae + " V");
            System.out.println("Clean signal MAE with 5-point filter: " + cleanFiveMae + " V");
            for (int run = 0; run < numberOfRuns; run++) {
                double[] noisyVoltages = addNoise(voltages, noiseLevel, random);
                double[] filteredVoltages = applyThreePointFilter(noisyVoltages);
                double[] filteredFiveVoltages = applyFivePointFilter(noisyVoltages);
                double mae = calculateMAE(noisyVoltages, voltages);
                double filteredMae = calculateMAE(filteredVoltages, voltages);
                totalMaeAcrossRuns += mae;
                totalFilteredMaeAcrossRuns += filteredMae;
                double fiveFilteredMae = calculateMAE(filteredFiveVoltages, voltages);
                totalFiveFilteredMaeAcrossRuns += fiveFilteredMae;
            }


            double averageMae = totalMaeAcrossRuns / numberOfRuns;
            double averageFilteredMae = totalFilteredMaeAcrossRuns / numberOfRuns;
            double averageFiveFilteredMae = totalFiveFilteredMaeAcrossRuns / numberOfRuns;
            double improvementFive = (averageMae - averageFiveFilteredMae) / averageMae * 100.0;
            maeBeforeResults[rateIndex] = averageMae;
            maeThreeResults[rateIndex] = averageFilteredMae;
            maeFiveResults[rateIndex] = averageFiveFilteredMae;
            rateIndex++;
            double comparisonFiveVsThree = (averageFilteredMae - averageFiveFilteredMae) / averageFilteredMae * 100.0;

            double improvement = (averageMae - averageFilteredMae) / averageMae * 100.0;
            printResults(sampleRate, averageMae, averageFilteredMae, averageFiveFilteredMae, improvement, improvementFive, comparisonFiveVsThree);
        }

        System.out.println("\nMAE SUMMARY");
        System.out.printf("%-10s %-12s %-12s %-12s%n", "Rate", "Before", "3-point", "5-point");

        for (int i = 0; i < sampleRates.length; i++) {
            System.out.printf("%-10d %-12.3f %-12.3f %-12.3f%n", sampleRates[i], maeBeforeResults[i], maeThreeResults[i], maeFiveResults[i]);
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < sampleRates.length; i++) {
            dataset.addValue(maeBeforeResults[i], "Before filtering", String.valueOf(sampleRates[i]));
            dataset.addValue(maeThreeResults[i], "3-point filter", String.valueOf(sampleRates[i]));
            dataset.addValue(maeFiveResults[i], "5-point filter", String.valueOf(sampleRates[i]));
        }

        JFreeChart chart = ChartFactory.createLineChart("MAE vs. Sampling Rate", "Sampling Rate (Hz)", "MAE (V)", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer =
                (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);  // Before filtering
        renderer.setSeriesPaint(1, Color.BLUE); // 3-point filter
        renderer.setSeriesPaint(2, Color.GREEN); // 5-point filter
        JFrame frame = new JFrame("Signal Lab Results");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        DefaultCategoryDataset cleanDataset = new DefaultCategoryDataset();

        for (int i = 0; i < sampleRates.length; i++) {
            cleanDataset.addValue(cleanThreeResults[i], "3-point filter", String.valueOf(sampleRates[i]));
            cleanDataset.addValue(cleanFiveResults[i], "5-point filter", String.valueOf(sampleRates[i]));
        }

        JFreeChart cleanChart = ChartFactory.createLineChart("Filter Distortion on Clean Signal", "Sampling Rate (Hz)", "MAE (V)", cleanDataset);
        CategoryPlot cleanPlot = cleanChart.getCategoryPlot();
        LineAndShapeRenderer cleanRenderer = (LineAndShapeRenderer) cleanPlot.getRenderer();
        cleanRenderer.setSeriesPaint(0, Color.BLUE);  // 3-point filter
        cleanRenderer.setSeriesPaint(1, Color.GREEN); // 5-point filter
        JFrame cleanFrame = new JFrame("Clean Signal Results");
        cleanFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cleanFrame.add(new ChartPanel(cleanChart));
        cleanFrame.pack();
        cleanFrame.setLocationRelativeTo(null);
        cleanFrame.setVisible(true);

    }

    private static double[] generateSignal(int sampleRate, int durationSeconds, double amplitude, double frequency) {

        int numberOfSamples = sampleRate * durationSeconds;
        double[] voltages = new double[numberOfSamples];

        for (int i = 0; i < numberOfSamples; i++) {
            double time = (double) i / sampleRate;
            double angle = 2 * Math.PI * frequency * time;
            voltages[i] = amplitude * Math.sin(angle);
        }

        return voltages;
    }

    static double[] addNoise(double[] voltages, double noiseLevel, Random random) {
        double[] noisyVoltages = new double[voltages.length];
        for (int i = 0; i < voltages.length; i++) {
            double noise = (random.nextDouble() - 0.5) * 2.0 * noiseLevel;
            noisyVoltages[i] = voltages[i] + noise;
        }

        return noisyVoltages;
    }


    static double calculateMAE(double[] actual, double[] reference) {
        if (actual.length != reference.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        if (actual.length == 0) {
            throw new IllegalArgumentException("Arrays must not be empty");
        }

        double totalError = 0.0;

        for (int i = 0; i < actual.length; i++) {
            totalError += Math.abs(actual[i] - reference[i]);
        }

        return totalError / actual.length;
    }

    static double[] applyThreePointFilter(double[] voltages) {
        int numberOfSamples = voltages.length;
        if (numberOfSamples < 2) {
            throw new IllegalArgumentException(
                    "At least 2 samples are required"
            );
        }
        double[] filteredVoltages = new double[numberOfSamples];

        for (int i = 1; i < numberOfSamples - 1; i++) {
            filteredVoltages[i] = (voltages[i - 1] + voltages[i] + voltages[i + 1]) / 3.0;
        }
        filteredVoltages[0] = (voltages[0] + voltages[1]) / 2.0;
        filteredVoltages[numberOfSamples - 1] = (voltages[numberOfSamples - 2] + voltages[numberOfSamples - 1]) / 2.0;

        return filteredVoltages;
    }


    static double[] applyFivePointFilter(double[] voltages) {
        int numberOfSamples = voltages.length;

        if (numberOfSamples < 5) {
            throw new IllegalArgumentException(
                    "At least 5 samples are required"
            );
        }
        double[] filteredVoltages = new double[numberOfSamples];
        for (int i = 2; i < numberOfSamples - 2; i++) {
            filteredVoltages[i] = (voltages[i - 2] + voltages[i - 1] + voltages[i] + voltages[i + 1] + voltages[i + 2]) / 5.0;
        }
        filteredVoltages[0] = (voltages[0] + voltages[1] + voltages[2]) / 3.0;
        filteredVoltages[1] = (voltages[0] + voltages[1] + voltages[2] + voltages[3]) / 4.0;
        filteredVoltages[numberOfSamples - 2] = (voltages[numberOfSamples - 4] + voltages[numberOfSamples - 3] + voltages[numberOfSamples - 2] + voltages[numberOfSamples - 1]) / 4.0;
        filteredVoltages[numberOfSamples - 1] = (voltages[numberOfSamples - 3] + voltages[numberOfSamples - 2] + voltages[numberOfSamples - 1]) / 3.0;
        return filteredVoltages;
    }


    private static void printResults(int sampleRate, double averageMae, double averageFilteredMae, double averageFiveFilteredMae, double improvement, double improvementFive, double comparisonFiveVsThree) {

        System.out.println("Sampling rate: " + sampleRate + " samples/second");
        System.out.println("--------------------------------");
        System.out.println("Average MAE before filtering: " + averageMae + " V");
        System.out.println("Average MAE after filtering: " + averageFilteredMae + " V");
        System.out.println("Average MAE after 5-point filtering: " + averageFiveFilteredMae + " V");
        System.out.println("Average improvement: " + improvement + "%");
        System.out.println("Improvement with 5-point filter: " + improvementFive + "%");
        System.out.println("5-point vs 3-point improvement: " + comparisonFiveVsThree + "%");
    }
}

