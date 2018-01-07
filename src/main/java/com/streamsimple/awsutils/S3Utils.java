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
package com.streamsimple.awsutils;

import java.io.File;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public final class S3Utils
{
  private S3Utils()
  {
  }

  public static final void putFile(AWSStaticCredentialsProvider credentialsProvider,
      String awsRegion,
      String awsBucket,
      String objectName,
      File localFile)
  {
    final AmazonS3 s3 = AmazonS3ClientBuilder
        .standard()
        .withCredentials(credentialsProvider)
        .withRegion(awsRegion)
        .build();

    s3.putObject(awsBucket, objectName, localFile);
  }

  public static final void putFile(String awsAccessKey,
      String awsSecretKey,
      String awsRegion,
      String awsBucket,
      String objectName,
      File localFile)
  {
    final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(awsAccessKey, awsSecretKey));

    putFile(credentialsProvider, awsRegion, awsBucket, objectName, localFile);
  }

  public static final void removeFile(AWSStaticCredentialsProvider credentialsProvider,
      String awsRegion,
      String awsBucket,
      String objectName)
  {
    final AmazonS3 s3 = AmazonS3ClientBuilder
        .standard()
        .withCredentials(credentialsProvider)
        .withRegion(awsRegion)
        .build();

    s3.deleteObject(awsBucket, objectName);
  }

  public static final void removeFile(String awsAccessKey,
      String awsSecretKey,
      String awsRegion,
      String awsBucket,
      String objectName)
  {
    final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(awsAccessKey, awsSecretKey));

    removeFile(credentialsProvider, awsRegion, awsBucket, objectName);
  }
}
