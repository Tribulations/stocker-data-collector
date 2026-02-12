package com.joakimcolloz.stocker.datacollector.data.validation;

import com.joakimcolloz.stocker.datacollector.model.Granularity;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates input parameters for Coinbase cryptocurrency data operations.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class CryptoInputValidator {

    // Matches product IDs like "BTC-EUR", "SHIB-EUR", "AVAX-USD"
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}-[A-Z]{2,5}$");

    /**
     * Validates a Coinbase product ID.
     *
     * @param productId the product ID to validate (e.g., "BTC-EUR")
     * @throws IllegalArgumentException if the product ID is null, empty, or has invalid format
     */
    public void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (!PRODUCT_ID_PATTERN.matcher(productId).matches()) {
            throw new IllegalArgumentException("Invalid product ID format: " + productId +
                    ". Expected format: 2-10 uppercase alphanumeric characters, hyphen, 2-5 uppercase letters (e.g., BTC-EUR)");
        }
    }

    /**
     * Validates a list of product IDs.
     *
     * @param productIds the list of product IDs to validate
     * @throws IllegalArgumentException if the list is null, empty, or contains invalid product IDs
     */
    public void validateProductIdList(List<String> productIds) {
        if (productIds == null) {
            throw new IllegalArgumentException("Product ID list cannot be null");
        }
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("Product ID list cannot be empty");
        }
        for (int i = 0; i < productIds.size(); i++) {
            try {
                validateProductId(productIds.get(i));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid product ID at index " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Validates a time range for candle fetching.
     *
     * @param startEpoch the start timestamp in seconds since epoch
     * @param endEpoch   the end timestamp in seconds since epoch
     * @throws IllegalArgumentException if the time range is invalid
     */
    public void validateTimeRange(long startEpoch, long endEpoch) {
        if (startEpoch <= 0) {
            throw new IllegalArgumentException("Start epoch must be positive, got: " + startEpoch);
        }
        if (endEpoch <= 0) {
            throw new IllegalArgumentException("End epoch must be positive, got: " + endEpoch);
        }
        if (startEpoch >= endEpoch) {
            throw new IllegalArgumentException("Start epoch (" + startEpoch +
                    ") must be before end epoch (" + endEpoch + ")");
        }
    }

    /**
     * Validates a granularity value.
     *
     * @param granularity the granularity to validate
     * @throws IllegalArgumentException if the granularity is null
     */
    public void validateGranularity(Granularity granularity) {
        if (granularity == null) {
            throw new IllegalArgumentException("Granularity cannot be null");
        }
    }
}
