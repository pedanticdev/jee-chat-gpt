package fish.payara;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;

@ApplicationScoped
public class CacheController {

	@Inject
	@ConfigProperty(name = "cache.enabled", defaultValue = "false")
	Boolean cacheEnabled;

	@Inject
	DBService dbService;
	CacheManager cacheManager;

	Cache<Integer, PointsOfInterestResponse> pointsOfInterestCache;
	Cache<Integer, RecipeSuggestion> recipeCache;
	Map<Integer, PointsOfInterestResponse> pointsOfInterestCacheMap;
	Map<Integer, RecipeSuggestion> recipeSuggestionMap;

	@PostConstruct
	void init() {

		// Payara Cloud has data-grid disabled. If on that environment default to concurrent map
		if (isCacheEnabled()) {

			cacheManager = Caching.getCachingProvider().getCacheManager();

			MutableConfiguration<Integer, PointsOfInterestResponse> mutableConfig = new MutableConfiguration<>();
			mutableConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
			mutableConfig.setReadThrough(true);
			mutableConfig.setCacheLoaderFactory(PoiCacheLoaderFactoryImpl::new);
			mutableConfig.setWriteThrough(true);
			mutableConfig.setCacheWriterFactory(PoiCacheLoaderFactoryImpl::new);

			pointsOfInterestCache = cacheManager.createCache("poiCache", mutableConfig);

			MutableConfiguration<Integer, RecipeSuggestion> recipeCacheConfig = new MutableConfiguration<>();
			recipeCacheConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
			recipeCacheConfig.setReadThrough(true);
			recipeCacheConfig.setCacheLoaderFactory(RecipeCacheLoaderFactoryImpl::new);
			recipeCacheConfig.setWriteThrough(true);
			recipeCacheConfig.setCacheWriterFactory(RecipeCacheLoaderFactoryImpl::new);

			recipeCache = cacheManager.createCache("recipeCache", recipeCacheConfig);

		} else {
			pointsOfInterestCacheMap = new ConcurrentHashMap<>();
			recipeSuggestionMap = new ConcurrentHashMap<>();
		}
	}

	public PointsOfInterestResponse getResponse(final Integer key) {
		if (isCacheEnabled()) {
			return pointsOfInterestCache.get(key);
		}
		if (pointsOfInterestCacheMap.containsKey(key)) {
			return pointsOfInterestCacheMap.get(key);
		}
		PointsOfInterestResponse pointsOfInterestResponse = dbService.loadPoi(key);
		if (pointsOfInterestResponse != null) {
			pointsOfInterestCacheMap.put(key, pointsOfInterestResponse);
		}
		return pointsOfInterestResponse;
	}

	public void cachePoi(final Integer key, PointsOfInterestResponse response) {
		response.setComputedHashCode(key);

		if (isCacheEnabled() && !response.getPointsOfInterest().isEmpty()) {
			pointsOfInterestCache.put(key, response);
		} else {
			pointsOfInterestCacheMap.put(key, dbService.savePoi(response));
		}
	}

	public boolean isPointOfInterestCached(final Integer key) {
		if (isCacheEnabled()) {
			return pointsOfInterestCache.containsKey(key);
		}
		return dbService.loadPoi(key) != null;
	}

	public RecipeSuggestion getCachedRecipeSuggestion(final Integer key) {
		if (isCacheEnabled()) {
			return recipeCache.get(key);
		}
		RecipeSuggestion recipeSuggestion = dbService.loadRecipe(key);
		if (recipeSuggestion != null) {
			recipeSuggestionMap.put(key, recipeSuggestion);
		}
		return recipeSuggestion;
	}

	public void cacheRecipeSuggestion(final Integer key, final RecipeSuggestion recipeSuggestion) {
		recipeSuggestion.setComputedHashCode(key);
		if (isCacheEnabled() && !recipeSuggestion.getRecipes().isEmpty()) {
			recipeCache.put(key, recipeSuggestion);
		} else {
			recipeSuggestionMap.put(key, dbService.saveRecipe(recipeSuggestion));
		}
	}

	public boolean isRecipeCached(final Integer key) {
		if (isCacheEnabled()) {
			return recipeCache.containsKey(key);
		} else {
			return dbService.loadRecipe(key) != null;
		}

	}

	@PreDestroy
	void cleanUp() {
		if (!Caching.getCachingProvider().getCacheManager().isClosed()) {
			Caching.getCachingProvider().getCacheManager().close();
		}
	}

	private boolean isCacheEnabled() {
		return Boolean.TRUE.equals(cacheEnabled);
	}
}
