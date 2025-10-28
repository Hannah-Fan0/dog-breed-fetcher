package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {

    private static final String BASE_URL = "https://dog.ceo/api/breed/";
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub-breeds for the given breed from the dog.ceo API.
     *
     * @param breed the breed to fetch sub-breeds for
     * @return list of sub-breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException("Invalid breed name: " + breed);
        }

        String url = BASE_URL + breed.toLowerCase().trim() + "/list";

        try {
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {

                if (response.body() == null) {
                    throw new BreedNotFoundException("Empty response from API");
                }

                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                String status = json.optString("status", "");

                // Expected responses:
                // success → {"status":"success","message":[...]}
                // error   → {"status":"error","message":"Breed not found (main breed does not exist)","code":404}
                if ("error".equals(status)) {
                    throw new BreedNotFoundException(json.optString("message", "Breed not found"));
                }

                if (!"success".equals(status)) {
                    throw new BreedNotFoundException("Unexpected API status: " + status);
                }

                JSONArray arr = json.getJSONArray("message");
                List<String> subBreeds = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    subBreeds.add(arr.getString(i));
                }

                return Collections.unmodifiableList(subBreeds);
            }
        } catch (IOException e) {
            // Convert any network/parse error into the checked exception
            throw new BreedNotFoundException("Network or API error: " + e.getMessage());
        }
    }
}
