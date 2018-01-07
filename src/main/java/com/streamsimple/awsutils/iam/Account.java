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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.ServiceFailureException;
import com.amazonaws.services.identitymanagement.model.User;

public class Account
{
  public static final String ACCOUNT_NUMBER_REGEX_PATTERN_STRING = "[0-9]{12}";
  public static final Pattern ACCOUNT_NUMBER_REGEX_PATTERN = Pattern.compile(ACCOUNT_NUMBER_REGEX_PATTERN_STRING);
  public static final String ARN_ACCOUNT_NUMBER_PATTERN_STRING = "arn:aws:iam::(" + ACCOUNT_NUMBER_REGEX_PATTERN_STRING + "):user/.*";
  public static final Pattern ARN_ACCOUNT_NUMBER_PATTERN = Pattern.compile(ARN_ACCOUNT_NUMBER_PATTERN_STRING);

  private final String id;
  private final String number;
  private final String userName;

  public Account(final String id, final String number, final String userName)
  {
    if (id == null) {
      throw new NullPointerException();
    }

    if (number == null) {
      throw new NullPointerException();
    }

    if (userName == null) {
      throw new NullPointerException();
    }

    if (!ACCOUNT_NUMBER_REGEX_PATTERN.matcher(number).matches()) {
      final String message = String.format("The given number %s does not match the regex \"%s\"",
          number, ACCOUNT_NUMBER_REGEX_PATTERN_STRING);
      throw new IllegalArgumentException(message);
    }

    this.id = id;
    this.number = number;
    this.userName = userName;
  }

  public String getId()
  {
    return id;
  }

  public String getNumber()
  {
    return number;
  }

  public String getUserName()
  {
    return userName;
  }

  /**
   * @param client
   * @return
   * @throws NoSuchEntityException
   *         The request was rejected because it referenced an entity that does not exist. The error message describes
   *         the entity.
   * @throws ServiceFailureException
   */
  public static Account getCurrentAccount(final AmazonIdentityManagement client)
  {
    final GetUserResult userResult = client.getUser();
    final User user = userResult.getUser();
    final String id = user.getUserId();
    final String number = extractFromARN(user.getArn());
    final String userName = user.getUserName();

    return new Account(id, number, userName);
  }

  public static String extractFromARN(final String arn)
  {
    final Matcher matcher = ARN_ACCOUNT_NUMBER_PATTERN.matcher(arn);

    if (!matcher.matches()) {
      final String message = String.format("The given arn %s does not match the regex \"%s\"",
          arn, ARN_ACCOUNT_NUMBER_PATTERN_STRING);
      throw new IllegalArgumentException(message);
    }

    return matcher.group(1);
  }
}
