package org.metadatacenter.cedar.bridge.resources.doi;

import org.metadatacenter.cedar.bridge.resource.datacite.DataciteConstants;
import org.metadatacenter.constant.HttpConstants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * The requests the DOI workflow makes of DataCite's REST API. Each carries the repository credential,
 * and each answer is returned as DataCite gave it, for the workflow to interpret.
 */
final class DataCiteClient {

  /** DataCite's answer to one request: its status and its body as it arrived. */
  record Answer(int status, String body) {

    boolean succeeded() {
      return status >= 200 && status < 300;
    }
  }

  private final DataCiteSettings settings;
  private final DataCiteHttpClient http;

  DataCiteClient(DataCiteSettings settings, DataCiteHttpClient http) {
    this.settings = settings;
    this.http = http;
  }

  /**
   * What DataCite holds for one DOI, with affiliation detail. DataCite answers a read by identifier
   * from its database rather than from its search index, so a DOI registered a moment ago is already
   * found, and a draft is found because the request carries its repository's credential.
   */
  Answer read(String doi) throws IOException, InterruptedException {
    return readPath("/" + doi);
  }

  /** {@link #read(String)} for a DOI given as the path of its resolver URL, such as {@code /10.5072/abcd-1234}. */
  Answer readPath(String doiPath) throws IOException, InterruptedException {
    return send(HttpRequest.newBuilder(URI.create(settings.endpointUrl() + doiPath + DataciteConstants.QUERY_AFFILIATION))
        .GET());
  }

  /**
   * The drafts DataCite lists in this repository for a landing page. Only a draft registered before
   * CEDAR recorded its DOIs is found this way. DataCite lists a draft only to a request that names the
   * repository holding it and asks for drafts, and it lists from a search index that lags each write.
   */
  Answer listDrafts(String landingPage) throws IOException, InterruptedException {
    String query = settings.endpointUrl() + DataciteConstants.QUERY_AFFILIATION + DataciteConstants.QUERY_DETAIL
        + DataciteConstants.QUERY_CLIENT_ID
        + URLEncoder.encode(settings.repositoryId().toLowerCase(Locale.ROOT), StandardCharsets.UTF_8)
        + DataciteConstants.QUERY_STATE_DRAFT
        + DataciteConstants.QUERY_PUBLISHER + "%20AND%20url:%22"
        + URLEncoder.encode(landingPage, StandardCharsets.UTF_8) + "%22";
    return send(HttpRequest.newBuilder(URI.create(query)).GET());
  }

  /** Registers the DOI the body names. */
  Answer create(String body) throws IOException, InterruptedException {
    return send(HttpRequest.newBuilder(URI.create(settings.endpointUrl()))
        .header(DataciteConstants.CONTENT_TYPE, DataciteConstants.APPLICATION_VND_API_JSON)
        .POST(HttpRequest.BodyPublishers.ofString(body)));
  }

  /** Replaces the metadata of a DOI DataCite holds, and publishes it when the body's event asks for that. */
  Answer update(String doi, String body) throws IOException, InterruptedException {
    return send(HttpRequest.newBuilder(URI.create(settings.endpointUrl() + "/" + doi))
        .header(DataciteConstants.CONTENT_TYPE, DataciteConstants.APPLICATION_VND_API_JSON)
        .PUT(HttpRequest.BodyPublishers.ofString(body)));
  }

  private Answer send(HttpRequest.Builder request) throws IOException, InterruptedException {
    HttpResponse<String> response =
        http.send(request.header(HttpConstants.HTTP_HEADER_AUTHORIZATION, settings.authorization()));
    return new Answer(response.statusCode(), response.body());
  }
}
