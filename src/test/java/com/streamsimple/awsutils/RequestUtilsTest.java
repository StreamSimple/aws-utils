package com.streamsimple.awsutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.Charsets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksRequest;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.ResourceStatus;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.streamsimple.awsutilstest.AWSUtils;
import com.streamsimple.categories.AWSTest;
import com.streamsimple.commons.io.FileUtils;
import com.streamsimple.javautil.poll.Poller;
import com.streamsimple.javautils.testutils.ResourceUtils;

public class RequestUtilsTest
{
  @Test
  public void testCreateTagFilterParamPairs()
  {
    final Set<Tag> tagSet = new HashSet<>();
    tagSet.add(new Tag("keyA", "valueA"));
    tagSet.add(new Tag("keyA", "valueB"));
    tagSet.add(new Tag("keyA", "valueC"));
    tagSet.add(new Tag("keyB", "valueAA"));
    tagSet.add(new Tag("keyC", "valueBB"));
    tagSet.add(new Tag("keyD", "valueA"));
    tagSet.add(new Tag("keyD", "valueB"));

    final List<RequestUtils.ParamPair> epp = new ArrayList<>();
    epp.add(new RequestUtils.ParamPair("Filter.1.Name", "tag:keyA"));
    epp.add(new RequestUtils.ParamPair("Filter.1.Value.1", "valueA"));
    epp.add(new RequestUtils.ParamPair("Filter.1.Value.2", "valueB"));
    epp.add(new RequestUtils.ParamPair("Filter.1.Value.3", "valueC"));
    epp.add(new RequestUtils.ParamPair("Filter.2.Name", "tag:keyB"));
    epp.add(new RequestUtils.ParamPair("Filter.2.Value.1", "valueAA"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Name", "tag:keyC"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.1", "valueBB"));
    epp.add(new RequestUtils.ParamPair("Filter.4.Name", "tag:keyD"));
    epp.add(new RequestUtils.ParamPair("Filter.4.Value.1", "valueA"));
    epp.add(new RequestUtils.ParamPair("Filter.4.Value.2", "valueB"));

    final RequestUtils.ParamPairs expected = new RequestUtils.ParamPairs(epp);
    final RequestUtils.ParamPairs actual = RequestUtils.createTagFilterParamPairs(tagSet);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testCreateTagAddParamPairs()
  {
    final Set<Tag> tagSet = new HashSet<>();
    tagSet.add(new Tag("keyA", "valueA"));
    tagSet.add(new Tag("keyB", "valueAA"));
    tagSet.add(new Tag("keyC", "valueBB"));
    tagSet.add(new Tag("keyD", "valueA"));

    final List<RequestUtils.ParamPair> epp = new ArrayList<>();
    epp.add(new RequestUtils.ParamPair("Tags.member.1.Key", "keyA"));
    epp.add(new RequestUtils.ParamPair("Tags.member.1.Value", "valueA"));
    epp.add(new RequestUtils.ParamPair("Tags.member.2.Key", "keyB"));
    epp.add(new RequestUtils.ParamPair("Tags.member.2.Value", "valueAA"));
    epp.add(new RequestUtils.ParamPair("Tags.member.3.Key", "keyC"));
    epp.add(new RequestUtils.ParamPair("Tags.member.3.Value", "valueBB"));
    epp.add(new RequestUtils.ParamPair("Tags.member.4.Key", "keyD"));
    epp.add(new RequestUtils.ParamPair("Tags.member.4.Value", "valueA"));

    final RequestUtils.ParamPairs expected = new RequestUtils.ParamPairs(epp);
    final RequestUtils.ParamPairs actual = RequestUtils.createTagAddParamPairs(tagSet);

    Assert.assertEquals(expected, actual);
  }

  /**
   * <b>Note:</b> If the test fails, delete any created stacks manually from the aws cli or console to avoid leaking
   * resources.
   */
  @Category(AWSTest.class)
  @Test
  public void testFilterTags() throws IOException, TimeoutException
  {
    final String region = AWSUtils.getAWSRegion();
    final AWSCredentialsProvider provider = AWSUtils.getCredentialsProvider();

    final AmazonCloudFormation client = AmazonCloudFormationClientBuilder.standard()
        .withRegion(region)
        .withCredentials(provider)
        .build();

    final File resourceFile = ResourceUtils.getResourceAsFile(Paths.get("tagFilterResource.json"));
    final String template = FileUtils.readFileToString(resourceFile, Charsets.UTF_8);

    final Tag tag1 = new Tag("ResourceType", "IntegrationTest1");
    final Tag tag2 = new Tag("ResourceType", "IntegrationTest2");

    final String id1 = createTagStack(client, tag1, template);
    final String id2 = createTagStack(client, tag2, template);

    final Set<String> expected1 = new HashSet<>();
    final Set<String> expected2 = new HashSet<>();

    expected1.add(id1);
    expected2.add(id2);

    final Set<String> actual1 = getStacksWithTag(tag1, client);
    final Set<String> actual2 = getStacksWithTag(tag2, client);

    Assert.assertEquals(expected1, actual1);
    Assert.assertEquals(expected2, actual2);

    try {
    } finally {
      deleteStack(id1, client);
      deleteStack(id2, client);
    }
  }

  private String createTagStack(final AmazonCloudFormation client,
                                final Tag tag,
                                final String template) throws IOException, TimeoutException
  {
    final com.amazonaws.services.cloudformation.model.Tag modelTag =
        new com.amazonaws.services.cloudformation.model.Tag()
        .withKey(tag.getKey())
        .withValue(tag.getValue());
    final String stackName = "TestDeploy_" + System.currentTimeMillis() + "_1";
    final CreateStackRequest request = new CreateStackRequest()
        .withStackName(stackName)
        .withTags(modelTag)
        .withTemplateBody(template);

    final CreateStackResult result1 = client.createStack(request);
    final String id = result1.getStackId();

    try {
      final boolean done = (Boolean)new Poller<Boolean>()
          .setTimeout(30_000L)
          .setInterval(200L)
          .poll(new Poller.Func<Boolean>() {
            @Override
            public Poller.Result<Boolean> run()
            {
              final DescribeStackEventsRequest request = new DescribeStackEventsRequest()
                  .withStackName(id);
              final DescribeStackEventsResult result = client.describeStackEvents(request);
              final List<StackEvent> stackEvents = result.getStackEvents();
              final String resourceStatusString = stackEvents
                  .iterator()
                  .next()
                  .getResourceStatus();
              final ResourceStatus resourceStatus = ResourceStatus.valueOf(resourceStatusString);

              switch (resourceStatus) {
                case CREATE_IN_PROGRESS:
                  return Poller.Result.notDone();
                case CREATE_FAILED:
                  return Poller.Result.done(false);
                case CREATE_COMPLETE:
                  return Poller.Result.done(true);
                default: {
                  final String message = String.format("Unknown ResourceStatus %s", resourceStatus);
                  throw new UnsupportedOperationException(message);
                }
              }
            }
          });

      if (!done) {
        deleteStack(id, client);
      }
    } catch (Exception e) {
      deleteStack(id, client);
      throw e;
    }

    return id;
  }

  private Set<String> getStacksWithTag(final Tag tag, final AmazonCloudFormation client)
  {
    final ListStacksRequest request = new ListStacksRequest();
    final Set<Tag> tagSet = new HashSet<>();
    tagSet.add(tag);

    RequestUtils.addTagFilters(tagSet, request);

    final ListStacksResult result = client.listStacks(request);
    final Set<String> stackIds = new HashSet<>();

    for (final StackSummary stackSummary: result.getStackSummaries()) {
      stackIds.add(stackSummary.getStackId());
    }

    return stackIds;
  }

  private void deleteStack(final String id, final AmazonCloudFormation client)
  {
    final DeleteStackRequest request = new DeleteStackRequest()
        .withStackName(id);
    client.deleteStack(request);
  }
}
