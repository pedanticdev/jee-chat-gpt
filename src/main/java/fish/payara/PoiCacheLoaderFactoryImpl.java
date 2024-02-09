package fish.payara;

import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class PoiCacheLoaderFactoryImpl implements CacheLoader<Integer, PointsOfInterestResponse>, CacheWriter<Integer, PointsOfInterestResponse> {

    @Inject
    EntityManager entityManager;


    @Override
    public PointsOfInterestResponse load(Integer key) throws CacheLoaderException {
        List<PointsOfInterestResponse> resultList = entityManager
                .createNamedQuery(PointsOfInterestResponse.QUERY_NAME, PointsOfInterestResponse.class)
                .setParameter(PointsOfInterestResponse.PARAM_NAME, key).getResultList();
        return !resultList.isEmpty() ? resultList.getFirst() : null;
    }

    @Override
    public Map<Integer, PointsOfInterestResponse> loadAll(Iterable<? extends Integer> keys) throws CacheLoaderException {
        return entityManager
                .createNamedQuery(PointsOfInterestResponse.QUERY_NAME_ALL, PointsOfInterestResponse.class)
                .setParameter(PointsOfInterestResponse.PARAM_NAME, keys).getResultList().stream()
                .collect(Collectors.toMap(PointsOfInterestResponse::getComputedHashCode, Function.identity()));

    }

    @Override
    @Transactional
    public void write(Cache.Entry<? extends Integer, ? extends PointsOfInterestResponse> entry) throws CacheWriterException {
        entityManager.persist(entry);

    }

    @Override
    @Transactional
    public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends PointsOfInterestResponse>> entries) throws CacheWriterException {
        entries.forEach(e -> {
            PointsOfInterestResponse value = e.getValue();
            Integer key = e.getKey();
            value.setComputedHashCode(key);
            entityManager.persist(value);

        });
    }

    @Override
    public void delete(Object key) throws CacheWriterException {

    }

    @Override
    public void deleteAll(Collection<?> keys) throws CacheWriterException {

    }
}
