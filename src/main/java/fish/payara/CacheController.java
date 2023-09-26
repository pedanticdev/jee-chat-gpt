package fish.payara;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fish.payara.jpa.RecipeSuggestion;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CacheController {

	@Inject
	@ConfigProperty(name = "cache.enabled", defaultValue = "false")
	private Boolean cacheEnabled;

	private Cache<Integer, PointsOfInterestResponse> pointsOfInterestCache;
	private Cache<Integer, RecipeSuggestion> recipeCache;
	private Map<Integer, PointsOfInterestResponse> pointsOfInterestCacheMap;
	private Map<Integer, RecipeSuggestion> recipeSuggestionMap;

	@PostConstruct
	void init() {

		// Payara Cloud has data-grid disabled. If on that environment default to concurrent map
		if (isCacheEnabled()) {

			CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

			MutableConfiguration<Integer, PointsOfInterestResponse> mutableConfig = new MutableConfiguration<>();
			mutableConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
			pointsOfInterestCache = cacheManager.createCache("poiCache", mutableConfig);

			MutableConfiguration<Integer, RecipeSuggestion> recipeCacheConfig = new MutableConfiguration<>();
			recipeCacheConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
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
		return pointsOfInterestCacheMap.get(key);
	}

	public void cachePoi(final Integer key, final PointsOfInterestResponse response) {
		if (isCacheEnabled()) {
			pointsOfInterestCache.put(key, response);
		} else {
			pointsOfInterestCacheMap.put(key, response);
		}
	}

	public boolean isPointOfInterestCached(final Integer key) {
		if (isCacheEnabled()) {
			return pointsOfInterestCache.containsKey(key);
		}
		return pointsOfInterestCacheMap.containsKey(key);
	}

	public RecipeSuggestion getCachedRecipeSuggestion(final Integer key) {
		if (isCacheEnabled()) {
			return recipeCache.get(key);
		}
		return recipeSuggestionMap.get(key);
	}

	public void cacheRecipeSuggestion(final Integer key, final RecipeSuggestion recipeSuggestion) {
		if (isCacheEnabled()) {
			recipeCache.put(key, recipeSuggestion);
		} else {
			recipeSuggestionMap.put(key, recipeSuggestion);
		}
	}

	public boolean isRecipeCached(final Integer key) {
		if (isCacheEnabled()) {
			return recipeCache.containsKey(key);
		} else {
			return recipeSuggestionMap.containsKey(key);
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
