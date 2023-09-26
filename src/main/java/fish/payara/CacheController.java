package fish.payara;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CacheController {
	private Cache<Integer, PointsOfInterestResponse> geoCache;
	@PostConstruct
	void init() {
		CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

		MutableConfiguration<Integer, PointsOfInterestResponse> mutableConfig = new MutableConfiguration<>();
		mutableConfig.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
		geoCache = cacheManager.createCache("myCache", mutableConfig);
	}

	public PointsOfInterestResponse getResponse(final Integer key) {
		return geoCache.get(key);
	}


	@PreDestroy
	void cleanUp() {
		geoCache.close();
	}

}
