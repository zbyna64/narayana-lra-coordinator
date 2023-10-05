package cz.dcos.model;

import io.narayana.lra.LRAData;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ManualCancelResponse {

    private List<LRAData> canceledLras;
    private List<LRAData> remainingLras;
}
