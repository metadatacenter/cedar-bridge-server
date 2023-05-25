package org.metadatacenter.cedar.bridge.resources;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.metadatacenter.util.json.JsonMapper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataCiteResourceTest extends AbstractBridgeServerResourceTest
{

  @BeforeClass public static void oneTimeSetUp()
  {
  }

  @Before public void setUp()
  {
  }

  @After public void tearDown()
  {
  }

  @Test public void getDoiMetadataTest()
  {
    Map<String, Object> content = new HashMap<>();

    Entity postContent = Entity.entity(content, MediaType.APPLICATION_JSON);
    Response response = client.target(baseUrlGetDoiMetadata).request().header("Authorization", authHeader1).post(postContent);

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Assert.assertEquals(MediaType.APPLICATION_JSON, response.getHeaderString(HttpHeaders.CONTENT_TYPE));

    Map<String, Object> summary = response.readEntity(new GenericType<>(){});
    System.out.println(JsonMapper.MAPPER.valueToTree(summary));
  }
}