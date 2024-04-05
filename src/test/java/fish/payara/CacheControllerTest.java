package fish.payara;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fish.payara.jpa.PointOfInterest;
import fish.payara.jpa.PointsOfInterestResponse;
import fish.payara.jpa.RecipeSuggestion;

class CacheControllerTest {

    @Mock
    private DBService dbService;

    @Mock(strictness = Mock.Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
    private Cache<Integer, PointsOfInterestResponse> pointsOfInterestCache;
    @Mock(strictness = Mock.Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)

    Map<Integer, PointsOfInterestResponse> pointsOfInterestCacheMap;
    @Mock(strictness = Mock.Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)

    Map<Integer, RecipeSuggestion> recipeSuggestionMap;
    @Mock(strictness = Mock.Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)

    Cache<Integer, RecipeSuggestion> recipeCache;



    @Mock
    private CacheManager cacheManager;

    private CacheController cacheController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheController = new CacheController();
        cacheController.dbService = dbService;
        cacheController.pointsOfInterestCache = pointsOfInterestCache;
        cacheController.pointsOfInterestCacheMap = pointsOfInterestCacheMap;
        cacheController.recipeCache = recipeCache;
        cacheController.recipeSuggestionMap = recipeSuggestionMap;
        cacheController.cacheManager = cacheManager;
        cacheController.cacheEnabled = true;
    }

    @Test
    void testGetResponse_CacheHit() {
        Integer key = 123;
        PointsOfInterestResponse expectedResponse = createTestPoiResponse(); // Helper method

        when(pointsOfInterestCache.get(key)).thenReturn(expectedResponse);

        PointsOfInterestResponse actualResponse = cacheController.getResponse(key);

        assertEquals(expectedResponse, actualResponse);
        verify(dbService, never()).loadPoi(key);
    }

    @Test
    void testGetResponse_CacheMiss_DBSuccess() {
        Integer key = 456;
        PointsOfInterestResponse expectedResponse = createTestPoiResponse();
        cacheController.cacheEnabled = false;
        when(pointsOfInterestCache.get(key)).thenReturn(null);
        when(dbService.loadPoi(key)).thenReturn(expectedResponse);

        PointsOfInterestResponse actualResponse = cacheController.getResponse(key);

        assertNotNull(actualResponse);

        assertEquals(expectedResponse, actualResponse);
        verify(dbService).loadPoi(key);
        verify(pointsOfInterestCacheMap).put(key, expectedResponse);
    }

    @Test
    void testGetResponse_CacheMiss_DBFailure() {
        Integer key = 789;
        cacheController.cacheEnabled = false;
        when(pointsOfInterestCache.get(key)).thenReturn(null);
        when(dbService.loadPoi(key)).thenReturn(null); // Simulate DB failure

        PointsOfInterestResponse actualResponse = cacheController.getResponse(key);

        assertNull(actualResponse);
        verify(dbService).loadPoi(key);
    }

    @Test
    void testCachePoi_CacheEnabled_ValidResponse() {
        Integer key = 101;
        cacheController.cacheEnabled = true;
        PointsOfInterestResponse response = createTestPoiResponse();
        response.setComputedHashCode(key);

        doNothing().when(pointsOfInterestCache).put(key, response);

        cacheController.cachePoi(key, response);

        verify(pointsOfInterestCache).put(key, response);
        verify(dbService, never()).savePoi(response);
    }


    private PointsOfInterestResponse createTestPoiResponse() {
        PointsOfInterestResponse pointsOfInterestResponse = PointsOfInterestResponse.of();
        pointsOfInterestResponse.getPointsOfInterest().add(PointOfInterest.of());
        return pointsOfInterestResponse;
    }
}
