package cz.dcos;

import cz.dcos.model.ManualCancelResponse;
import cz.dcos.model.ManualCompleteResponse;
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
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

import static io.narayana.lra.LRAConstants.STATUS_PARAM_NAME;

@ApplicationScoped
@Path("/manual/complete")
@Tag(name = "Manual Completion", description = "Operations to manually complete desired LRA's")
public class ManualCompletionController {

    private static final Logger LOGGER = Logger.getLogger("Manual-completion-controller");
    private final LRAService service = LRARecoveryModule.getService();

    @GET
    @Path("/closing")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Complete all closing LRAs", description = "Call the completion action for all LRA's with closing LRA Status")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCompleteResponse completeClosing() {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        Set<LRAData> closing = lras.stream()
                .filter(lra -> lra.getStatus().equals(LRAStatus.Closing))
                .collect(Collectors.toSet());

        List<LRAData> completedLras = new ArrayList<>();
        for (LRAData lra : closing) {
            completedLras.add(service.endLRA(lra.getLraId(), false, false));
            LOGGER.info("Completed LRA : " + lra);
        }
        lras.removeAll(completedLras);
        LOGGER.info("Remaining LRA's: " + lras.size());

        return new ManualCompleteResponse(completedLras, lras);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Complete all LRAs known", description = "Call the completion action for all LRA's known to coordinator")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "The LRAData json array which is known to coordinator",
                    content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ManualCancelResponse.class))),
    })
    public ManualCompleteResponse complete() {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        List<LRAData> completedLras = new ArrayList<>();
        for (LRAData lra : lras) {
            completedLras.add(service.endLRA(lra.getLraId(), false, false));
            LOGGER.info("Completed LRA : " + lra);
        }
        lras.removeAll(completedLras);
        LOGGER.info("Remaining LRA's: " + lras.size());

        return new ManualCompleteResponse(completedLras, lras);
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
                                                   String status) {
        List<LRAData> lras = service.getAll();
        LOGGER.info("LRA service getting all LRA's. Number of transactions = " + lras.size());

        LRAStatus paramStatus = LRAStatus.valueOf(status);

        Set<LRAData> chosen = lras.stream()
                .filter(lra -> lra.getStatus().equals(paramStatus))
                .collect(Collectors.toSet());

        List<LRAData> completedLras = new ArrayList<>();
        for (LRAData lra : chosen) {
            completedLras.add(service.endLRA(lra.getLraId(), false, false));
            LOGGER.info("Completed LRA : " + lra);
        }
        lras.removeAll(completedLras);
        LOGGER.info("Remaining LRA's: " + lras.size());

        return new ManualCompleteResponse(completedLras, lras);
    }
}
