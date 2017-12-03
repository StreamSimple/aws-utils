package com.streamsimple.awsutils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.File;

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
