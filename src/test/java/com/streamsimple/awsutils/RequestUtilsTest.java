package com.streamsimple.awsutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

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
}
