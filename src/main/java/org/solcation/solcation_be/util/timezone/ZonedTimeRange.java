package org.solcation.solcation_be.util.timezone;

import java.time.Instant;

/**
 * [start, end)
 * @param start
 * @param end
 */
public record ZonedTimeRange(Instant start, Instant end) {
}
