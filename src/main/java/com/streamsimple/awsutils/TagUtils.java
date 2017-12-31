package com.streamsimple.awsutils;

public class TagUtils
{
  public static final String TAG_KEY_RESOURCE_TYPE = "ResourceType";
  public static final String TAG_VALUE_RESOURCE_TYPE_TEST = "Test";
  public static final String TAG_VALUE_RESOURCE_TYPE_PROD = "Prod";

  public static final Tag TAG_RESOURCE_TYPE_TEST = new Tag(TAG_KEY_RESOURCE_TYPE, TAG_VALUE_RESOURCE_TYPE_TEST);
  public static final Tag TAG_RESOURCE_TYPE_PROD = new Tag(TAG_KEY_RESOURCE_TYPE, TAG_VALUE_RESOURCE_TYPE_PROD);
}
