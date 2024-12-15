package ir.mesmaeili.lba.algorithm;

import lombok.SneakyThrows;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PsiPredictionClient {

    private static final String SERVICE_URL = "http://127.0.0.1:8000/predict-psi";
    private final HttpClient client;

    public PsiPredictionClient() {
        this.client = HttpClient.newHttpClient(); // Create HttpClient instance
    }

    /**
     * Sends a request to the FastAPI service with avg_cpu and lambda_value
     * and returns the predicted Psi value.
     *
     * @param avgCpu      Average CPU utilization
     * @param lambdaValue Lambda value
     * @return Predicted Psi value
     * @throws Exception If the request fails
     */
    @SneakyThrows
    public float getPredictedPsi(float avgCpu, float lambdaValue) {
        // Create JSON payload
        JSONObject json = new JSONObject();
        json.put("avg_cpu", avgCpu);
        json.put("lambda_value", lambdaValue);

        // Build the HTTP POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVICE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the response is successful
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get response: " + response.statusCode());
        }

        // Parse the JSON response and extract the predicted Psi value
        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getFloat("predicted_psi");
    }

    public static void main(String[] args) {
        PsiPredictionClient client = new PsiPredictionClient();

        // Input values for testing
        float avgCpu = 0.6f;
        float lambdaValue = 0.8f;

        try {
            // Call the service and print the predicted Psi
            double predictedPsi = client.getPredictedPsi(avgCpu, lambdaValue);
            System.out.println("Predicted Psi: " + predictedPsi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}