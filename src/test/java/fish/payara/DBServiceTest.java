package fish.payara;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import fish.payara.jpa.PointsOfInterestResponse;
import jakarta.persistence.EntityManager;

import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fish.payara.jpa.RecipeSuggestion;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;


class DBServiceTest {
    @Mock
    private EntityManager entityManager;
    private DBService service;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new DBService(entityManager); // Initialization
    }

    @Test
    void saveRecipes() {
        EntityManager entityManager = mock(EntityManager.class);
        RecipeSuggestion mockRecipe = mock(RecipeSuggestion.class);

        DBService dbService = new DBService(entityManager);

        RecipeSuggestion result = dbService.saveRecipe(mockRecipe);

        ArgumentCaptor<RecipeSuggestion> argumentCaptor = ArgumentCaptor.forClass(RecipeSuggestion.class);
        verify(entityManager).persist(argumentCaptor.capture());

        verify(entityManager, times(1)).persist(mockRecipe);
        assertEquals(mockRecipe, result);

        assertEquals(mockRecipe, argumentCaptor.getValue());
    }





    @SuppressWarnings("unchecked")
    @Test
    void loadPoi() throws Exception {
        PointsOfInterestResponse poi = new PointsOfInterestResponse();
        TypedQuery<PointsOfInterestResponse> query = mock(TypedQuery.class);

        when(query.setParameter(anyString(), any())).thenReturn(query);

        when(query.getResultList()).thenReturn(Collections.singletonList(poi));
        when(entityManager.createNamedQuery(PointsOfInterestResponse.QUERY_NAME, PointsOfInterestResponse.class)).thenReturn(query);

        Integer key = 1;
        PointsOfInterestResponse result = service.loadPoi(key);
        assertEquals(poi, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void loadRecipe() throws Exception {
        RecipeSuggestion recipe = new RecipeSuggestion();
        TypedQuery<RecipeSuggestion> query = mock(TypedQuery.class);

        when(query.setParameter(anyString(), any())).thenReturn(query);

        when(query.getResultList()).thenReturn(Collections.singletonList(recipe));
        when(entityManager.createNamedQuery(RecipeSuggestion.QUERY_NAME, RecipeSuggestion.class)).thenReturn(query);

        Integer key = 1;
        RecipeSuggestion result = service.loadRecipe(key);
        assertEquals(recipe, result);
    }

    @Test
    void savePoi() {
        PointsOfInterestResponse poi = new PointsOfInterestResponse();

        PointsOfInterestResponse result = service.savePoi(poi);

        ArgumentCaptor<PointsOfInterestResponse> argumentCaptor = ArgumentCaptor.forClass(PointsOfInterestResponse.class);

        verify(entityManager, times(1)).persist(argumentCaptor.capture());
        assertEquals(poi, argumentCaptor.getValue());
        assertEquals(poi, result);
    }

    @Test
    void saveRecipe() {
        RecipeSuggestion recipe = new RecipeSuggestion();

        RecipeSuggestion result = service.saveRecipe(recipe);

        ArgumentCaptor<RecipeSuggestion> argumentCaptor = ArgumentCaptor.forClass(RecipeSuggestion.class);

        verify(entityManager, times(1)).persist(argumentCaptor.capture());
        assertEquals(recipe, argumentCaptor.getValue());
        assertEquals(recipe, result);
    }
}