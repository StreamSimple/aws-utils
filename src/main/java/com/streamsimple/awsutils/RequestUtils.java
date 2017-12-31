package com.streamsimple.awsutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.amazonaws.AmazonWebServiceRequest;

public class RequestUtils
{
  public static ParamPairs createTagFilterParamPairs(Set<Tag> tagSet)
  {
    final List<ParamPair> paramPairs = new ArrayList<>();
    final List<Tag> tagList = new ArrayList<>();
    tagList.addAll(tagSet);
    Collections.sort(tagList, Tag.Comparator.INSTANCE);

    final TreeMap<String, List<String>> tagMap = new TreeMap<>();

    for (Tag tag: tagList) {
      List<String> values = tagMap.get(tag.getKey());

      if (values == null) {
        values = new ArrayList<>();
        tagMap.put(tag.getKey(), values);
      }

      values.add(tag.getValue());
    }

    int keyFilterIndex = 1; // Amazon filter indexes start at 1.

    for (Map.Entry<String, List<String>> entry: tagMap.entrySet()) {
      final String nameParamKey = createFilterNameParam(keyFilterIndex);
      final String nameParamValue = createNamParamValue(entry.getKey());
      paramPairs.add(new ParamPair(nameParamKey, nameParamValue));

      final List<String> tagValues = entry.getValue();
      int valueFilterIndex = 1; // Amazon filter indexes start at 1.

      for (final String valueParamValue: tagValues) {
        final String valueParamKey = createFilterValueParam(keyFilterIndex, valueFilterIndex);
        paramPairs.add(new ParamPair(valueParamKey, valueParamValue));
        valueFilterIndex++;
      }

      keyFilterIndex++;
    }

    return new ParamPairs(paramPairs);
  }

  public static void addTagFilters(Set<Tag> tagSet, AmazonWebServiceRequest request)
  {
    final ParamPairs paramPairs = createTagFilterParamPairs(tagSet);

    for (ParamPair paramPair: paramPairs.getParamPairs()) {
      request.putCustomQueryParameter(paramPair.getKey(), paramPair.getValue());
    }
  }

  private static final String createNamParamValue(final String tagKey)
  {
    return "tag:" + tagKey;
  }

  private static final String createFilterNameParam(int keyIndex)
  {
    return String.format("Filter.%d.Name", keyIndex);
  }

  private static final String createFilterValueParam(int keyIndex, int valueIndex)
  {
    return String.format("Filter.%d.Value.%d", keyIndex, valueIndex);
  }

  public static class ParamPair extends Tag
  {
    protected ParamPair(final String key, final String value)
    {
      super(key, value);
    }
  }

  public static class ParamPairs
  {
    private final List<ParamPair> paramPairs;

    public ParamPairs(final List<ParamPair> paramPairs)
    {
      if (paramPairs == null) {
        throw new NullPointerException();
      }

      final List<ParamPair> newParamPairs = new ArrayList<>();
      newParamPairs.addAll(paramPairs);
      this.paramPairs = Collections.unmodifiableList(newParamPairs);
    }

    public List<ParamPair> getParamPairs()
    {
      return paramPairs;
    }
  }
}
