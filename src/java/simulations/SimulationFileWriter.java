package simulations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SimulationFileWriter {
    private static FileWriter writer;

    public static void writeResults(String duration, Map<String, String> sheepCapturedTimes) {
        String jcm = System.getProperty("simName");
        if (jcm.indexOf('.') == -1) {
            jcm += ".jcm";
        }
        String simName = jcm.substring(0, jcm.lastIndexOf('.'));

        String prefixDir = "simulations/results";
        String fullDir = String.format("%s/%s", prefixDir, simName);
        if (!new File(fullDir).exists()) {
            new File(fullDir).mkdir();
        }

        int count = 1;
        File dir = new File(fullDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".txt")) {
                    count++;
                }
            }
        }

        String filePath = String.format("%s/%s_%d.txt", fullDir, simName, count);
        try {
            writer = new FileWriter(filePath);
            writeLine("Simulation: " + jcm);
            writeLine("Duration: " + duration);
            writeLine("Sheeps: {");
            for (Map.Entry<String, String> entry : sheepCapturedTimes.entrySet()) {
                writeLine(String.format("%s: %s", entry.getKey(), entry.getValue()));
            }
            writeLine("}");
            System.out.println("Results written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeLine(String line) throws IOException {
        writer.write(line + "\n");
    }
}