package in.vinaygupta.eventpipeline.api;

import in.vinaygupta.eventpipeline.domain.EventScenario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryEventRequest(
        @NotBlank String sku,
        @NotBlank String storeId,
        @Min(-500) @Max(500) int quantityDelta,
        EventScenario scenario
) {
    public InventoryEventRequest {
        if (scenario == null) {
            scenario = EventScenario.SUCCESS;
        }
    }
}
