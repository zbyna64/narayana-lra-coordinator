package cz.dcos;

import cz.dcos.model.ManualCancelResponse;
import cz.dcos.model.ManualCompleteResponse;
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/manual/compensate")
@Tag(name = "Manual Compensation", description = "Operations to manually compensate desired LRA's")
public class ManualCompensationController {
    private static final Logger LOGGER = Logger.getLogger("Manual-compensation-controller");
    private final LRAService service = LRARecoveryModule.getService();

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Compensate all LRAs known", description = "Call the compensation action for all LRA's known to coordinator")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCancelResponse compensate() {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        List<LRAData> canceledLras = new ArrayList<>();
        for (LRAData lra : lras) {
            canceledLras.add(service.endLRA(lra.getLraId(), true, false));
            LOGGER.info("Canceled LRA : " + lra);
        }
        lras.removeAll(canceledLras);
        LOGGER.info("Remaining LRA's to cancel : " + lras.size());

        return new ManualCancelResponse(canceledLras, lras);
    }

    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Compensate all active LRAs", description = "Call the compensation action for all LRA's with active LRA Status")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCancelResponse compensateActive() {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        Set<LRAData> active = lras.stream()
                .filter(lra -> lra.getStatus().equals(LRAStatus.Active))
                .collect(Collectors.toSet());

        return getManualCancelResponse(lras, active);
    }

    @GET
    @Path("/canceling")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Compensate all cancelling LRAs", description = "Call the compensation action for all LRA's with cancelling LRA Status")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCancelResponse compensateCanceling() {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        Set<LRAData> canceling = lras.stream()
                .filter(lra ->
                        lra.getStatus().equals(LRAStatus.Cancelling))
                .collect(Collectors.toSet());

        return getManualCancelResponse(lras, canceling);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Complete all LRAs by status parameter", description = "Call the completion action for all LRA's with chosen status")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCompleteResponse completeByStatus(@Parameter(name = "status", description = "Filter the lra's based on chosen LRA status", required = true, schema = @Schema(implementation = LRAStatus.class))
                                                   @QueryParam("status")
                                                   String status
    ) {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        LRAStatus paramStatus = LRAStatus.valueOf(status);

        Set<LRAData> chosen = lras.stream()
                .filter(lra -> lra.getStatus().equals(paramStatus))
                .collect(Collectors.toSet());

        List<LRAData> completedLras = new ArrayList<>();
        for (LRAData lra : chosen) {
            completedLras.add(service.endLRA(lra.getLraId(), true, false));
            LOGGER.info("Compensated LRA : " + lra);
        }
        lras.removeAll(completedLras);
        LOGGER.info("Remaining LRA's: " + lras.size());

        return new ManualCompleteResponse(completedLras, lras);
    }

    private ManualCancelResponse getManualCancelResponse(List<LRAData> lras, Set<LRAData> canceling) {
        List<LRAData> canceledLras = new ArrayList<>();
        for (LRAData lra : canceling) {
            canceledLras.add(service.endLRA(lra.getLraId(), true, false));
            LOGGER.info("Canceled LRA : " + lra);
        }
        lras.removeAll(canceledLras);
        LOGGER.info("Remaining LRA's: " + lras.size());

        return new ManualCancelResponse(canceledLras, lras);
    }
}
