package fish.payara;

import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import javax.cache.integration.CacheLoaderException;

// @DataSourceDefinition(name = "java:global/cloud-postgres",
//		className = "org.postgresql.ds.PGSimpleDataSource",
//		url = "${MPCONFIG=DB_URL}",
//		databaseName = "${MPCONFIG=DB_NAME}",
//		serverName = "${MPCONFIG=DB_SERVER}",
//		user = "${MPCONFIG=DB_USER}",
//		password = "${MPCONFIG=DB_PASSWORD}",
//		properties = {
//		"sslmode=require" })
@Stateless
public class DBService {

    @Inject EntityManager entityManager;

    public PointsOfInterestResponse savePoi(PointsOfInterestResponse pointsOfInterestResponse) {
        entityManager.persist(pointsOfInterestResponse);
        return pointsOfInterestResponse;
    }

    public RecipeSuggestion saveRecipe(RecipeSuggestion recipeSuggestion) {
        entityManager.persist(recipeSuggestion);
        return recipeSuggestion;
    }

    public PointsOfInterestResponse loadPoi(Integer key) throws CacheLoaderException {
        List<PointsOfInterestResponse> resultList =
                entityManager
                        .createNamedQuery(
                                PointsOfInterestResponse.QUERY_NAME, PointsOfInterestResponse.class)
                        .setParameter(PointsOfInterestResponse.PARAM_NAME, key)
                        .getResultList();
        return !resultList.isEmpty() ? resultList.getFirst() : null;
    }

    public RecipeSuggestion loadRecipe(Integer key) throws CacheLoaderException {
        List<RecipeSuggestion> resultList =
                entityManager
                        .createNamedQuery(RecipeSuggestion.QUERY_NAME, RecipeSuggestion.class)
                        .setParameter(PointsOfInterestResponse.PARAM_NAME, key)
                        .getResultList();
        return !resultList.isEmpty() ? resultList.getFirst() : null;
    }
}
