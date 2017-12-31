package com.streamsimple.awsutils;

public class Tag
{
  private final String key;
  private final String value;

  public Tag(final String key, final String value)
  {
    if (key == null) {
      throw new NullPointerException();
    }

    if (value == null) {
      throw new NullPointerException();
    }

    this.key = key;
    this.value = value;
  }

  public String getKey()
  {
    return key;
  }

  public String getValue()
  {
    return value;
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

    Tag tag = (Tag) o;

    if (!key.equals(tag.key)) {
      return false;
    }
    return value.equals(tag.value);
  }

  @Override
  public int hashCode()
  {
    int result = key.hashCode();
    result = 31 * result + value.hashCode();
    return result;
  }

  @Override
  public String toString()
  {
    return "Tag{" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        '}';
  }

  public static class Comparator implements java.util.Comparator<Tag>
  {
    public static final Comparator INSTANCE = new Comparator();

    private Comparator()
    {
    }

    @Override
    public int compare(Tag thisTag, Tag thatTag)
    {
      int cmp = thisTag.getKey().compareTo(thatTag.getKey());

      if (cmp != 0) {
        return cmp;
      }

      return thisTag.getValue().compareTo(thatTag.getValue());
    }
  }
}
