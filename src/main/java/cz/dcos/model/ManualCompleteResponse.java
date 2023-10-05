package cz.dcos.model;

import io.narayana.lra.LRAData;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ManualCompleteResponse {

    private List<LRAData> completedLras;
    private List<LRAData> remainingLras;
}
