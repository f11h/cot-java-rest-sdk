package com.telekom.m2m.cot.restsdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.GsonUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * Created by Patrick Steinert on 30.01.16.
 */
public class CloudOfThingsRestClient {

    private Gson gson = GsonUtils.createGson();
    private final String encodedAuthString;
    private final String host;

    protected OkHttpClient client;

    public CloudOfThingsRestClient(OkHttpClient okHttpClient, String host, String user, String password) {
        this.host = host;
        try {
            encodedAuthString = Base64.getEncoder().encodeToString((user + ":" + password).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new CotSdkException("Error generating auth string.", e);
        }
        client = okHttpClient;
    }

    /**
     * Proceedes a HTTP POST request and parses the response Header.
     * Response header 'Location' will be split to get the ID of the object (mostly created).
     *
     * @param json        Request body, needs to be a json object correlating to the contentType.
     * @param api         the REST API string.
     * @param contentType the Content-Type of the JSON Object.
     * @return the id of the Object.
     */
    public String doRequestWithIdResponse(String json, String api, String contentType) {

        Response response = null;
        try {
            RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Basic " + encodedAuthString)
                    .addHeader("Content-Type", contentType)
                    .addHeader("Accept", contentType)
                    //.url(tenant + ".test-ram.m2m.telekom.com/" + api)
                    .url(host + "/" + api)
                    .post(body)
                    .build();
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                JsonObject o = gson.fromJson(response.body().string(), JsonObject.class);
                String err = "Request failed.";
                if (o.has("error"))
                    err += " Platform provided details: '" + o.get("error") + "'";
                throw new CotSdkException(response.code(), err);
            }
            String location = response.header("Location");
            String result = null;
            if (location != null) {
                String[] pathParts = location.split("\\/");
                result = pathParts[pathParts.length - 1];
            }
            return result;
        } catch (CotSdkException e) {
            throw e;
        } catch (Exception e) {
            throw new CotSdkException("Problem", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }
    }

    /**
     * Proceedes a HTTP POST request and returns the response body as String.
     *
     * @param json        Request body, needs to be a json object correlating to the contentType.
     * @param api         the REST API string.
     * @param contentType the Content-Type of the JSON Object.
     * @return the received JSON response body.
     */
    /**
     * Proceedes a HTTP POST request and returns the response body as String.
     *
     * @param json        Request body, needs to be a json object correlating to the contentType.
     * @param api         the REST API string.
     * @param contentType the Content-Type of the JSON Object.
     * @return the received JSON response body.
     */
    public String doPostRequest(String json, String api, String contentType) {

        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", contentType)
                .url(host + "/" + api)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();

            return response.body().string();
        } catch (IOException e) {
            throw new CotSdkException("Unexpected error during POST request.", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }
    }

    /**
     * Proceedes a HTTP POST request and returns the response body as String.
     * Method will throw an exception if the response code is indicating
     * a non successfull request.
     *
     * @param json Request body, needs to be a json object.
     * @param api  the REST API string.
     * @return the received JSON response body.
     */
    public String doPostRequest(String json, String api) {

        RequestBody body = RequestBody.create(null, json);
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(host + "/" + api)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new CotSdkException(response.code(), "Unexpected response code for POST request.");
            }
            return response.body().string();
        } catch (IOException e) {
            throw new CotSdkException("Unexpected error during POST request.", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }
    }


    public String getResponse(String id, String api, String contentType) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(host + "/" + api + "/" + id)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String result = null;
            if (response.isSuccessful()) {
                result = response.body().string();
            } else {
                if (response.code() != 404) {
                    throw new CotSdkException(response.code(), "Error in request.");
                }
            }
            return result;
        } catch (Exception e) {
            throw new CotSdkException("Error in requets", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }

    }

    public String getResponse(String api, String contentType) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(host + "/" + api)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String result;
            if (response.isSuccessful()) {
                result = response.body().string();
            } else {
                final JsonObject o = gson.fromJson(response.body().string(), JsonObject.class);
                String err = "Request failed.";
                if (o.has("error"))
                    err += " Platform provided details: '" + o.get("error") + "'";
                throw new CotSdkException(response.code(), err);
            }
            return result;
        } catch (Exception e) {
            throw new CotSdkException("Error in request", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }

    }

    public void doPutRequest(String json, String api, String contentType) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", contentType)
                .url(host + "/" + api)
                .put(body)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new CotSdkException(response.code(),
                        "Requested returned error code");
            }
        } catch (Exception e) {
            throw new CotSdkException("Error in request", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }
    }

    public void doPutRequest(String json, String id, String api, String contentType) {
        RequestBody body = RequestBody.create(MediaType.parse(contentType), json);
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .addHeader("Content-Type", contentType)
                .addHeader("Accept", contentType)
                .url(host + "/" + api + "/" + id)
                .put(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                int i = 1;
            } else {
                int i = 2;
            }
        } catch (Exception e) {
            throw new CotSdkException("Error in request", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }

    }

    public void delete(String id, String api) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(host + "/" + api + "/" + id)
                .delete()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new CotSdkException(response.code(), "Error in delete with ID '" + id + "' (see https://http.cat/" + response.code() + ")");
            }
        } catch (Exception e) {
            throw new CotSdkException("Error in request", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }
    }

    public void delete(String url) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(url)
                .delete()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new CotSdkException(response.code(), "Error in delete with URL '" + url + "' (see https://http.cat/" + response.code() + ")");
            }
        } catch (Exception e) {
            throw new CotSdkException("Error in request", e);
        } finally {
            closeResponseBodyIfResponseAndBodyNotNull(response);
        }

    }

    private void closeResponseBodyIfResponseAndBodyNotNull(final Response response) {
        if (response != null && response.body() != null) {
            response.body().close();
        }
    }

}
