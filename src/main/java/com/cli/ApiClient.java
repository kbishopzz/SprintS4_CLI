package com.cli;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Airport;
import com.model.City;
import com.model.Passenger;
import com.model.Plane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApiClient {

  private final String baseUrl;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ApiClient() {
    this(System.getProperty("backend.base-url", "http://localhost:8080"));
  }

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public List<Passenger> getAllPassengers() throws IOException {
    return getList("/api/passengers", Passenger.class);
  }

  public Passenger getPassengerById(long id) throws IOException {
    return get("/api/passengers/" + id, Passenger.class);
  }

  public Passenger createPassenger(Passenger passenger) throws IOException {
    return post("/api/passengers", passenger, Passenger.class);
  }

  public Passenger updatePassenger(long id, Passenger passenger) throws IOException {
    return put("/api/passengers/" + id, passenger, Passenger.class);
  }

  public void deletePassenger(long id) throws IOException {
    delete("/api/passengers/" + id);
  }

  public List<Airport> getAllAirports() throws IOException {
    return getList("/airports", Airport.class);
  }

  public Airport getAirport(long id) throws IOException {
    return get("/airports/" + id, Airport.class);
  }

  public Airport createAirport(Airport airport) throws IOException {
    return post("/airports", airport, Airport.class);
  }

  public Airport updateAirport(long id, Airport airport) throws IOException {
    return put("/airports/" + id, airport, Airport.class);
  }

  public void deleteAirport(long id) throws IOException {
    delete("/airports/" + id);
  }

  public List<City> getCities(int page, int size) throws IOException {
    return getCities(page, size, null);
  }

  public List<City> getCities(int page, int size, String sort) throws IOException {
    StringBuilder path = new StringBuilder("/cities?page=").append(page).append("&size=").append(size);
    if (sort != null && !sort.isBlank()) {
      path.append("&sort=").append(URLEncoder.encode(sort, StandardCharsets.UTF_8));
    }
    return getList(path.toString(), City.class);
  }

  public City getCity(long id) throws IOException {
    return get("/cities/" + id, City.class);
  }

  public List<Airport> getAirportsInCity(long id) throws IOException {
    return getList("/cities/" + id + "/airports", Airport.class);
  }

  public List<Plane> getPlanesForPassenger(long passengerId) throws IOException {
    return getList("/api/passengers/" + passengerId + "/planes", Plane.class);
  }

  public List<Airport> getAirportsForPassenger(long passengerId) throws IOException {
    return getList("/api/passengers/" + passengerId + "/airports", Airport.class);
  }

  public List<Airport> getAirportsForPlane(long planeId) throws IOException {
    return getList("/planes/" + planeId + "/airports", Airport.class);
  }

  public City createCity(City city) throws IOException {
    return post("/cities", city, City.class);
  }

  public City updateCity(long id, City city) throws IOException {
    return put("/cities/" + id, city, City.class);
  }

  public void deleteCity(long id) throws IOException {
    delete("/cities/" + id);
  }

  public List<Plane> getAllPlanes() throws IOException {
    return getList("/planes", Plane.class);
  }

  public Plane getPlaneById(long id) throws IOException {
    return get("/planes/" + id, Plane.class);
  }

  public Plane createPlane(Plane plane) throws IOException {
    return post("/planes", plane, Plane.class);
  }

  public Plane updatePlane(long id, Plane plane) throws IOException {
    return put("/planes/" + id, plane, Plane.class);
  }

  public void deletePlane(long id) throws IOException {
    delete("/planes/" + id);
  }

  private <T> T get(String path, Class<T> responseType) throws IOException {
    return sendRequest(path, "GET", null, responseType);
  }

  private <T> List<T> getList(String path, Class<T> elementType) throws IOException {
    HttpURLConnection connection = null;
    try {
      connection = openConnection(path, "GET");
      int code = connection.getResponseCode();
      String responseBody = readBody(connection);
      if (code >= 400) {
        throw new IOException("Request failed with HTTP " + code + ": " + responseBody);
      }
      if (responseBody == null || responseBody.isBlank()) {
        return List.of();
      }

      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode items = root;
      if (root.isObject() && root.has("content")) {
        items = root.get("content");
      }

      JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
      return objectMapper.readValue(items.traverse(), listType);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private <T> T post(String path, Object body, Class<T> responseType) throws IOException {
    return sendRequest(path, "POST", body, responseType);
  }

  private <T> T put(String path, Object body, Class<T> responseType) throws IOException {
    return sendRequest(path, "PUT", body, responseType);
  }

  private void delete(String path) throws IOException {
    HttpURLConnection connection = null;
    try {
      connection = openConnection(path, "DELETE");
      int code = connection.getResponseCode();
      if (code >= 400) {
        throw new IOException("Request failed with HTTP " + code + ": " + readBody(connection));
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private <T> T sendRequest(String path, String method, Object body, Class<T> responseType) throws IOException {
    HttpURLConnection connection = null;
    try {
      connection = openConnection(path, method);
      if (body != null) {
        byte[] payload = objectMapper.writeValueAsBytes(body);
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
          outputStream.write(payload);
        }
      }

      int code = connection.getResponseCode();
      String responseBody = readBody(connection);
      if (code >= 400) {
        throw new IOException("Request failed with HTTP " + code + ": " + responseBody);
      }
      if (responseBody == null || responseBody.isBlank()) {
        return null;
      }
      return objectMapper.readValue(responseBody, responseType);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private HttpURLConnection openConnection(String path, String method) throws IOException {
    URL url = new URL(baseUrl + path);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("Accept", "application/json");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    return connection;
  }

  private String readBody(HttpURLConnection connection) throws IOException {
    InputStream inputStream = connection.getErrorStream();
    if (inputStream == null) {
      inputStream = connection.getInputStream();
    }
    if (inputStream == null) {
      return "";
    }
    byte[] bytes = inputStream.readAllBytes();
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
