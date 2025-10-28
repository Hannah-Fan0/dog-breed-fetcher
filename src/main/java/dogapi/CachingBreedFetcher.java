package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {

    /** Underlying fetcher (e.g., DogApiBreedFetcher or BreedFetcherForLocalTesting). */
    private final BreedFetcher fetcher;

    /** Cache mapping breed names → sub-breed lists. */
    private final Map<String, List<String>> cache = new HashMap<>();

    /** Number of calls actually made to the underlying fetcher. */
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        // Normalize key (case-insensitive)
        String key = breed.toLowerCase(Locale.ROOT);

        // Check cache first
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            // Call underlying fetcher and record one real API call
            callsMade++;
            List<String> result = fetcher.getSubBreeds(breed);

            // Cache successful results (copy to prevent mutation)
            cache.put(key, List.copyOf(result));
            return result;
        } catch (BreedNotFoundException e) {
            // Do NOT cache exceptions
            throw e;
        }
    }

    public int getCallsMade() {
        return callsMade;
    }
}
