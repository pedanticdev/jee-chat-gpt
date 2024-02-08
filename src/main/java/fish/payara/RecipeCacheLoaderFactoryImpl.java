package fish.payara;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;

@ApplicationScoped
public class RecipeCacheLoaderFactoryImpl
		implements CacheLoader<Integer, RecipeSuggestion>, CacheWriter<Integer, RecipeSuggestion> {

	@Inject
	EntityManager entityManager;

	@Override
	public RecipeSuggestion load(Integer key) throws CacheLoaderException {
		List<RecipeSuggestion> resultList = entityManager
				.createNamedQuery(RecipeSuggestion.QUERY_NAME, RecipeSuggestion.class)
				.setParameter(PointsOfInterestResponse.PARAM_NAME, key).getResultList();
		return !resultList.isEmpty() ? resultList.getFirst() : null;
	}

	@Override
	public Map<Integer, RecipeSuggestion> loadAll(Iterable<? extends Integer> keys) throws CacheLoaderException {
		return entityManager
				.createNamedQuery(RecipeSuggestion.QUERY_NAME_ALL, RecipeSuggestion.class)
				.setParameter(PointsOfInterestResponse.PARAM_NAME, keys).getResultList().stream()
				.collect(Collectors.toMap(RecipeSuggestion::getComputedHashCode, Function.identity()));

	}

	@Override
	public void write(Cache.Entry<? extends Integer, ? extends RecipeSuggestion> entry) throws CacheWriterException {
		entityManager.persist(entry);
	}

	@Override
	public void writeAll(Collection<Cache.Entry<? extends Integer, ? extends RecipeSuggestion>> entries)
			throws CacheWriterException {
		entries.forEach(e -> {
			RecipeSuggestion value = e.getValue();
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
