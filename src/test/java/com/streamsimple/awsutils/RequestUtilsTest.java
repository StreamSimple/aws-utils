package com.streamsimple.awsutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import org.apache.commons.codec.Charsets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackInstancesRequest;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ResourceStatus;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
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
    epp.add(new RequestUtils.ParamPair("Filter.2.Name", "tag:keyA"));
    epp.add(new RequestUtils.ParamPair("Filter.2.Value.1", "valueA"));
    epp.add(new RequestUtils.ParamPair("Filter.2.Value.2", "valueB"));
    epp.add(new RequestUtils.ParamPair("Filter.2.Value.3", "valueC"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Name", "tag:keyB"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.1", "valueAA"));
    epp.add(new RequestUtils.ParamPair("Filter.4.Name", "tag:keyC"));
    epp.add(new RequestUtils.ParamPair("Filter.4.Value.1", "valueBB"));
    epp.add(new RequestUtils.ParamPair("Filter.5.Name", "tag:keyD"));
    epp.add(new RequestUtils.ParamPair("Filter.5.Value.1", "valueA"));
    epp.add(new RequestUtils.ParamPair("Filter.5.Value.2", "valueB"));

    final RequestUtils.ParamPairs expected = new RequestUtils.ParamPairs(epp);
    final RequestUtils.ParamPairs actual = RequestUtils.createTagFilterParamPairs(2, tagSet);

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

  @Test
  public void testCreatePropertyFilterParamPairs()
  {
    final List<RequestUtils.ParamPair> epp = new ArrayList<>();
    epp.add(new RequestUtils.ParamPair("Filter.3.Name", "myProp"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.1", "val1"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.2", "val2"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.3", "val3"));
    epp.add(new RequestUtils.ParamPair("Filter.3.Value.4", "val4"));

    final RequestUtils.ParamPairs expected = new RequestUtils.ParamPairs(epp);
    final RequestUtils.ParamPairs actual = RequestUtils.createPropertyFilterParamPairs(3,
        "myProp", "val1", "val2", "val3", "val4");

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testFilterIndexPattern1()
  {
    final Matcher matcher = RequestUtils.FILTER_INDEX_PATTERN.matcher("Filter.3.A_:");

    Assert.assertTrue(matcher.matches());
    Assert.assertEquals(3, Integer.parseInt(matcher.group(1)));
  }

  @Test
  public void testFilterIndexPattern2()
  {
    final Matcher matcher = RequestUtils.FILTER_INDEX_PATTERN.matcher("a.Filter.3.A_:");

    Assert.assertFalse(matcher.matches());
  }

  @Test
  public void testFilterIndexPattern3()
  {
    final Matcher matcher = RequestUtils.FILTER_INDEX_PATTERN.matcher("Filter.5.a.b.c");

    Assert.assertTrue(matcher.matches());
    Assert.assertEquals(5, Integer.parseInt(matcher.group(1)));
  }

  @Test
  public void testGetMaxFilterIndexNoMaxIndex()
  {
    final Map<String, List<String>> map = new HashMap<>();

    map.put("a", new ArrayList<String>());

    final int expected = -1;
    final int actual = RequestUtils.getMaxFilterIndex(map);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testGetMaxFilterIndex()
  {
    final Map<String, List<String>> map = new HashMap<>();

    map.put("Filter.1.a.b.c", new ArrayList<String>());
    map.put("Filter.1.va.b.c", new ArrayList<String>());
    map.put("Filter.2.5.b.c", new ArrayList<String>());
    map.put("Filter.3.1.b.c", new ArrayList<String>());
    map.put("Filter.4.2.b.c", new ArrayList<String>());

    final int expected = 4;
    final int actual = RequestUtils.getMaxFilterIndex(map);

    Assert.assertEquals(expected, actual);
  }
}
