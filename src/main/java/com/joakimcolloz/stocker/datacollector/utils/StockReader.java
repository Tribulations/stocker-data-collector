package com.joakimcolloz.stocker.datacollector.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Utility class for reading stock names from a resource file.
 *
 * @author Joakim Colloz
 */
public class StockReader {

    /**
     * Reads stock names from a resource file and returns them as an ArrayList
     * @param resourcePath the path to the resource file (e.g., "stocks.txt" or "data/stocks.txt")
     * @return ArrayList<String> containing all stock names
     * @throws IOException if resource cannot be read
     */
    public static ArrayList<String> readStockNamesFromResource(String resourcePath) throws IOException {
        ArrayList<String> stockNames = new ArrayList<>();

        // Get resource as InputStream from classpath
        InputStream inputStream = StockReader.class.getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath); // TODO temp logging improve!
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Trim whitespace and skip empty lines
                line = line.trim();
                if (!line.isEmpty()) {
                    stockNames.add(line);
                }
            }
        }

        return stockNames;
    }
}
