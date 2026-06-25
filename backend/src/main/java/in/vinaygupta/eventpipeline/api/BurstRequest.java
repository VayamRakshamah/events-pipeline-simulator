package in.vinaygupta.eventpipeline.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BurstRequest(@Min(1) @Max(25) int count) {
}
