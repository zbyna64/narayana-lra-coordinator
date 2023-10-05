package cz.dcos;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import io.narayana.lra.LRAData;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import cz.dcos.model.Action;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Startup
@ApplicationScoped
public class AppLifecycleBean {
    private static final Logger LOGGER = Logger.getLogger("ListenerBean");
    private final LRAService service = LRARecoveryModule.getService();
    private final int scanFrequency = 60000;

    JDBCStore store;
    @Inject
    EntityManager em;

    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        LOGGER.info("The application is starting...");

        LOGGER.info("Getting LRA's from DB...");
        List<Action> results = retrieveLrasFromDb();

        LOGGER.info("# LRA's in DB = " + results.size());
        LOGGER.info("Following LRA's will be compensated up to 60 sec");
        results.forEach(result -> {
            LOGGER.info("LRA ID: " + result.getUidstring());
        });

//        if (!results.isEmpty()) {
//            List<LRAData> lras = new ArrayList<>();
//            int count = 0;
//            while (lras.isEmpty()) {
//                count++;
//                Thread.sleep(scanFrequency);
//
//                lras = service.getAll(null);
//                LOGGER.info("Scan: " + count + ", lra's size = " + lras.size() + ". Total time: " + (count * scanFrequency) / 1000 + " s");
//                if (count > 2) {
//                    return;
//                }
//            }
//            LOGGER.info(lras.toString());
//            LOGGER.info("LRA's to compensate = " + lras.size());
//
//            cancelActiveLras(lras);
//        }
//        LOGGER.info("Application's ready!");
    }

    public List<Action> retrieveLrasFromDb() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> query = cb.createQuery(Action.class);
        query.from(Action.class);
        return em.createQuery(query).getResultList();
    }

    public void cancelActiveLras(List<LRAData> lras) {
        for (LRAData lra : lras) {
            if (lra.getStatus().equals(LRAStatus.Active)) {
                URI id = lra.getLraId();
                LOGGER.info("URI to compensate: " + id);
                LOGGER.info(service.endLRA(id, true, false).toString());
            }
        }
    }
}