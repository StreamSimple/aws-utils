package com.streamsimple.awsutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.AmazonWebServiceRequest;

public class RequestUtils
{
  public static final Pattern FILTER_INDEX_PATTERN = Pattern.compile("^Filter\\.(\\d+)\\..*");

  /**
   * There can only be one value for each key.
   * @param tagSet
   * @return
   */
  public static ParamPairs createTagAddParamPairs(final Set<Tag> tagSet)
  {
    final List<ParamPair> paramPairs = new ArrayList<>();
    // Validate that there is only one value for each key.
    final Set<String> keyToTag = new HashSet<>();

    for (Tag tag: tagSet) {
      if (!keyToTag.add(tag.getKey())) {
        final String message = String.format("Duplicate key %s", tag.getKey());
        throw new IllegalArgumentException(message);
      }
    }

    final List<Tag> tagList = new ArrayList<>();
    tagList.addAll(tagSet);
    Collections.sort(tagList, Tag.Comparator.INSTANCE);

    // AWS Tag indices start at 1
    for (int tagIndex = 1; tagIndex <= tagList.size(); tagIndex++) {
      final Tag tag = tagList.get(tagIndex - 1);
      final String tagKeyParam = createTagKeyParam(tagIndex);
      final String tagValueParam = createTagValueParam(tagIndex);

      final ParamPair keyParamPair = new ParamPair(tagKeyParam, tag.getKey());
      final ParamPair valueParamPair = new ParamPair(tagValueParam, tag.getValue());

      paramPairs.add(keyParamPair);
      paramPairs.add(valueParamPair);
    }

    return new ParamPairs(paramPairs);
  }

  public static void addTags(Set<Tag> tagSet, AmazonWebServiceRequest request)
  {
    final ParamPairs paramPairs = createTagAddParamPairs(tagSet);
    addParamPairs(paramPairs, request);
  }

  public static String createTagKeyParam(int tagIndex)
  {
    return String.format("Tags.member.%d.Key", tagIndex);
  }

  public static String createTagValueParam(int tagIndex)
  {
    return String.format("Tags.member.%d.Value", tagIndex);
  }

  public static ParamPairs createTagFilterParamPairs(final int startIndex, final Set<Tag> tagSet)
  {
    if (startIndex < 1) {
      final String message = String.format("Start index was %d, but must be greater than or equal to 1", startIndex);
      throw new IllegalArgumentException(message);
    }

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

    int keyFilterIndex = startIndex; // Amazon filter indexes start at 1.

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

  public static void addTagFilters(final Set<Tag> tagSet, final AmazonWebServiceRequest request)
  {
    final int startIndex = getNextFilterIndex(request);
    final ParamPairs paramPairs = createTagFilterParamPairs(startIndex, tagSet);
    addParamPairs(paramPairs, request);
  }

  public static void addPropertyFilter(final AmazonWebServiceRequest request,
                                       final String name, final String value, final String... values)
  {
    final int startIndex = getNextFilterIndex(request);
    final ParamPairs paramPairs = createPropertyFilterParamPairs(startIndex, name, value, values);
    addParamPairs(paramPairs, request);
  }

  public static ParamPairs createPropertyFilterParamPairs(final int startIndex,
                                                          final String name,
                                                          final String value,
                                                          final String... values)
  {
    final List<ParamPair> paramPairs = new ArrayList<>();
    final String[] valueArr = new String[1 + values.length];
    valueArr[0] = value;

    for (int index = 0; index < values.length; index++) {
      valueArr[index + 1] = values[index];
    }

    final String filterNameParam = createFilterNameParam(startIndex);
    paramPairs.add(new ParamPair(filterNameParam, name));

    for (int valueIndex = 0; valueIndex < valueArr.length; valueIndex++) {
      final String valueAtIndex = valueArr[valueIndex];
      final String filterValueParam = createFilterValueParam(startIndex, valueIndex + 1);
      paramPairs.add(new ParamPair(filterValueParam, valueAtIndex));
    }

    return new ParamPairs(paramPairs);
  }

  public static int getMaxFilterIndex(final AmazonWebServiceRequest request)
  {
    return getMaxFilterIndex(request.getCustomQueryParameters());
  }

  public static int getNextFilterIndex(final AmazonWebServiceRequest request)
  {
    int maxIndex = getMaxFilterIndex(request);
    return maxIndex < 0? 1: maxIndex + 1;
  }

  public static int getMaxFilterIndex(final Map<String, List<String>> params)
  {
    int maxIndex = -1;

    if (params == null) {
      return maxIndex;
    }

    for (String key: params.keySet()) {
      final Matcher matcher = FILTER_INDEX_PATTERN.matcher(key);

      if (!matcher.matches()) {
        continue;
      }

      int index = Integer.parseInt(matcher.group(1));

      if (maxIndex < index) {
        maxIndex = index;
      }
    }

    return maxIndex;
  }

  private static void addParamPairs(final ParamPairs paramPairs, final AmazonWebServiceRequest request)
  {
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

    @Override
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ParamPairs that = (ParamPairs) o;

      return paramPairs.equals(that.paramPairs);
    }

    @Override
    public int hashCode()
    {
      return paramPairs.hashCode();
    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder("[");
      String sep = "";

      for (ParamPair paramPair: paramPairs) {
        sb.append(paramPair.toString());
        sb.append(sep);
        sep = ", ";
      }

      sb.append("]");

      return "ParamPairs{" +
          "paramPairs=" + sb.toString() +
          '}';
    }
  }
}
