package com.streamsimple.awsutils.iam;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.streamsimple.awsutilstest.AWSUtils;
import com.streamsimple.categories.AWSTest;

public class AccountTest
{
  @Test
  @Category(AWSTest.class)
  public void testGetCurrentAccount()
  {
    final AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder
        .standard()
        .withCredentials(AWSUtils.getCredentialsProvider())
        .build();

    Account.getCurrentAccount(client);
    // A successful test run is\\\
  }

  @Test
  public void testExtractFromARN()
  {
    final String expected = "199331666061";
    final String testArn = "arn:aws:iam::" + expected + ":user/timTest";
    final String actual = Account.extractFromARN(testArn);

    Assert.assertEquals(expected, actual);
  }
}
