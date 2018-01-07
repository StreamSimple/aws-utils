/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
