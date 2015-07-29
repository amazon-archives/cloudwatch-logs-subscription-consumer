## CloudWatch Logs Subscription Consumer

The **CloudWatch Logs Subscription Consumer** is a specialized Amazon Kinesis stream reader (based on the [Amazon Kinesis Connector Library][amazon-kinesis-connectors]) that can help you deliver data from [Amazon CloudWatch Logs][aws-cloudwatch-logs] to any other system in near real-time using a [CloudWatch Logs Subscription Filter][cwl-subscriptions].

The current version of the CloudWatch Logs Subscription Consumer comes with built-in connectors for [Elasticsearch][elasticsearch] and [Amazon S3][aws-s3], but it can easily be extended to support other destinations using the Amazon Kinesis Connector Library framework.

## One-Click Setup: CloudWatch Logs + Elasticsearch + Kibana

This project includes a sample [CloudFormation][aws-cloudformation] template that can quickly bring up an Elasticsearch cluster on [Amazon EC2][amazon-ec2] fed with real-time data from any CloudWatch Logs log group. The CloudFormation template will also install [Kibana 3][kibana3] and [Kibana 4.1][kibana4], and it comes bundled with a few sample Kibana 3 dashboards for the following sources of AWS log data: 

+ [Amazon VPC Flow Logs][sending-vpc-flow-logs]
+ [AWS Lambda][aws-lambda]
+ [AWS CloudTrail][sending-cloudtrail-logs]
 
You can also connect your Elasticsearch cluster to any other custom CloudWatch Logs log group and then use Kibana to interactively analyze your log data with ad-hoc visualizations and custom dashboards.

If you already have an active CloudWatch Logs log group, you can launch a **CloudWatch Logs + Elasticsearch + Kibana** stack right now with this launch button: 

[![Launch your Elasticsearch stack fed by CloudWatch Logs data](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)][launch-stack]

You can find the CloudFormation template in: [configuration/cloudformation/cwl-elasticsearch.template][cfn-template]

**NOTE**: This template creates one or more Amazon EC2 instances, an Amazon Kinesis stream and an Elastic Load Balancer. You will be billed for the AWS resources used if you create a stack from this template.

#### Sample Kibana 3 Dashboards (Click to Expand)

The following are snapshots of the sample Kibana 3 dashboards that come built-in with the provided CloudFormation stack. Click on any of the screenshots below to expand to a full view.

##### Amazon VPC Flow Logs

[![VPC Flow Logs Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-VPCFlowLogs-Dashboard.png)][dashboard-vpc]

##### AWS Lambda

[![Lambda Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-Lambda-Dashboard.png)][dashboard-lambda]

##### AWS CloudTrail

[![CloudTrail Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-CloudTrail-Dashboard.png)][dashboard-cloudtrail]

#### Setting up Kibana 4 for CloudWatch Logs

The CloudFormation template sets up Kibana 3 with the correct Elasticsearch index patterns for this application, but Kibana 4 needs to be configured manually. When you visit the Kibana 4 URL for the first time you will be prompted to configure an index pattern. You should: 

+ Turn on "Index contains time-based events"
+ Turn on "Use event times to create index names"
+ Pick "Daily" for the "index pattern interval" field
+ Enter `[cwl-]YYYY.MM.DD` for the "index name or pattern" field
+ Choose `@timestamp` for the "Time-field name"

Then you can go ahead and create the index pattern and start using Kibana 4 with data from CloudWatch Logs.

#### Elasticsearch Administration

The CloudFormation template also installs the [kopf plugin][kopf] which allows you to monitor and manage your Elasticsearch cluster from a web interface.

![Kopf Web Interface](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-Kopf-UI.png)

#### Getting CloudWatch Logs data indexed in Elasticsearch

##### JSON Data

The CloudWatch Logs Subscription Consumer will automatically put log event messages that are valid JSON as [Object fields][object-types] in Elasticsearch. Elasticsearch is able to understand these Object types and their inner hierarchies, providing query support for all the inner fields. You do not have to specify anything beyond the source log group in the CloudFormation input parameters to have JSON data indexed in Elasticsearch.

##### Fixed-Column Data

Other log events that have a fixed-column format (such as traditional web server access logs) can get indexed easily in Elasticsearch by defining the field names in the CloudWatch Logs subscription filter pattern using the [Filter Pattern Syntax][pattern-syntax]. For example, if you had log data in this format:

```
127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif" 200 2326
```

... then you can use the following subscription filter pattern: 

```
[ip, user_identifier, user, timestamp, request, status_code, response_size]
```

... and its fields would get automatically indexed in Elasticsearch using the specified column names.

Filter patterns can also be used to restrict what flows from CloudWatch Logs to Elasticsearch. You can set conditions on any of the fields. Here are a few examples that would match the sample log event from above:

1. Equals condition on one field:

  `[ip, user_identifier, user, timestamp, request, status_code = 200, response_size]`

2. Prefix match on one field:

  `[ip, user_identifier, user, timestamp, request, status_code = 2*, response_size]`

3. OR condition on one field:

  `[ip, user_identifier, user, timestamp, request, status_code = 200 || status_code = 400, response_size]`

4. BETWEEN condition on one field:

  `[ip, user_identifier, user, timestamp, request, status_code >= 200 && status_code <= 204, response_size]`

5. Conditions on multiple fields:

  `[ip != 10.0.0.1, user_identifier, user, timestamp, request, status_code = 200, response_size]`
  
6. Compound conditions:

  `[(ip != 10.* && ip != 192.*) || ip = 127.*, user_identifier, user, timestamp, request, status_code, response_size]`

##### Other Less-Structured Data

The last field in a subscription filter pattern is always greedy, and in case the log event message has more fields than what is expressed in the filter, all additional data would get assigned to the last field. For example, the following filter pattern: 

```
[timestamp, request_id, event]
```

... would match this log event message:

```
2015-07-08T01:42:25.679Z 8bd492bcaede Decoded payload: Hello World
```

... and the indexed fields in Elasticsearch would be:

```json
{
  "timestamp": "2015-07-08T01:42:25.679Z",
  "request_id": "8bd492bcaede",
  "event": "Decoded payload: Hello World"
}
```

If one of the fields were to contain a valid JSON string, it would get put as an [Object field][object-types] in Elasticsearch rather than as an escaped JSON string. For example, using the `[timestamp, request_id, event]` filter pattern against the following log event: 

```
2015-07-08T01:42:25.679Z 8bd492bcaede { "payloadSize": 100, "responseCode": "HTTP 200 OK" }
```

... would result in the following Elasticsearch document:

```json
{
  "timestamp": "2015-07-08T01:42:25.679Z",
  "request_id": "8bd492bcaede",
  "event": {
      "payloadSize": 100, 
      "responseCode": "HTTP 200 OK" 
    }
  }
}
```

##### Indexing Amazon VPC Flow Logs

The sample [Kibana dashboard for Amazon VPC Flow Logs][dashboard-vpc] that comes built-in with the provided CloudFormation stack expects a CloudWatch Log subscription with the following filter pattern.

```
[version, account_id, interface_id, srcaddr, dstaddr, srcport, dstport, protocol, packets, bytes, start, end, action, log_status]
```

If you choose "Amazon VPC Flow Logs" in the *LogFormat* parameter of the CloudFormation template, the subscription filter will get created with the above filter pattern automatically.

If you prefer to only have a subset of the VPC Flow Logs going to Elasticsearch, you can choose "Custom" for the *LogFormat* parameter, and then specify the above filter pattern with conditions on some of the fields. For example, if you are only interested in analyzing rejected traffic, you can add the condition `action = REJECT` on the `action` field.

##### Indexing AWS Lambda Logs

The sample [Kibana dashboard for AWS Lambda][dashboard-lambda] that comes built-in with the provided CloudFormation stack expects a CloudWatch Log subscription with the following filter pattern.

```
[timestamp=*Z, request_id="*-*", event]
```

If you choose "AWS Lambda" in the *LogFormat* parameter of the CloudFormation template, the subscription filter will get created with the above filter pattern automatically.

In your JavaScript Lambda functions you can use the `JSON.stringify` method for logging structured data that would get automatically indexed in Elasticsearch. For example, the following is a slightly modified example from the Kinesis Process Record Lambda template that logs some JSON structured data using `JSON.stringify`. The first one simply logs the entire Kinesis record, and the second one logs some statistics on the function's activity and performance:

```javascript
exports.handler = function(event, context) {
    var start = new Date().getTime();
    var bytesRead = 0;

    event.Records.forEach(function(record) {
        // Kinesis data is base64 encoded so decode here
        payload = new Buffer(record.kinesis.data, 'base64').toString('ascii');
        bytesRead += payload.length;

        // log each record
        console.log(JSON.stringify(record, null, 2));
    });

    // collect statistics on the function's activity and performance
    console.log(JSON.stringify({ 
        "recordsProcessed": event.Records.length,
        "processTime": new Date().getTime() - start,
        "bytesRead": bytesRead,
    }, null, 2));
    
    context.succeed("Successfully processed " + event.Records.length + " records.");
};
```

The following snapshot from Kibana shows how the `recordsProcessed` and `bytesRead` got indexed in Elasticsearch and rendered as a graph in Kibana. Click on the image to expand to a full view of the sample dashboard for AWS Lambda that comes built-in with the provided CloudFormation stack:

[![Lambda Sample Dashboard Detail](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Full-Lambda-Dashboard-Detail.png)][dashboard-lambda]

##### Indexing AWS CloudTrail Logs

The sample [Kibana dashboard for AWS CloudTrail][dashboard-cloudtrail] that comes built-in with the provided CloudFormation stack does not require a subscription filter pattern because CloudTrail data is always valid JSON. If you prefer to only have a subset of the CloudTrail Logs going to Elasticsearch, you can choose "Custom" for the *LogFormat* parameter of the CloudFormation template, and then specify a JSON filter using the [Filter Pattern Syntax][pattern-syntax]. Here's an example of a valid JSON filter pattern that would filter the subscription feed to events that were made by Root accounts against the Autoscaling service:

```
{$.userIdentity.type = Root && $.eventSource = autoscaling*}
```

#### Elasticsearch Access Control

The current version of the CloudFormation template allows you to configure two basic methods for controlling access to the Elasticsearch API and Kibana UI:
+ IP Address restrictions configured with [EC2 security groups][ec2-security-groups].
+ [HTTP Basic Auth][http-basic-auth] configured through an [nginx][nginx] proxy that sits in front of the Elasticsearch endpoint.

This is generally considered an insufficient level of access control for clusters holding confidential data. It is highly recommended that you put additional security measures and access control mechanisms before you use this stack with production data. 

The nginx setup can be easily modified to enable other security and access control features, such as:
+ Adding HTTPS support to authenticate the endpoint and protect the Basic Auth credentials.
+ Adding HTTPS Client Authentication to restrict access to authorized users only.

You can find the nginx configuration used by the CloudFormation template in: [configuration/nginx/nginx.conf][nginx-conf]

## Building from source

Once you check out the code from GitHub, you can build it using Maven. To disable the GPG-signing in the build, use: 
```
mvn clean install -Dgpg.skip=true
```

## Running locally

After building from source you can run the applicaiton locally using any of these three Maven profiles: `Stdout`, `Elasticsearch` or `S3`. For example: 
```
mvn exec:java -P Elasticsearch
```

The Maven profile defines which connector destination you would use.

You can configure your application by updating the relevant [properties file][properties-files] for the Maven profile you choose. You can also override any setting in the properties file using JVM system properties as in the following example:

```
mvn exec:java -P Stdout -DkinesisInputStream=application-log-stream -DregionName=us-west-2
```

## Related Resources

+ [Amazon CloudWatch Logs][aws-cloudwatch-logs]
+ [CloudWatch Logs Subscriptions][cwl-subscriptions]
+ [Elasticsearch][elasticsearch]
+ [Kibana][kibana]
+ [Setting up Amazon VPC Flow Logs][sending-vpc-flow-logs]
+ [Amazon VPC Flow Logs Documentation][vpc-flow-documentation]
+ [Sending CloudTrail Events to CloudWatch Logs][sending-cloudtrail-logs]
+ [Amazon S3][aws-s3]
+ [AWS Lambda][aws-lambda]
+ [AWS CloudTrail][aws-cloudtrail]
+ [AWS CloudFormation][aws-cloudformation]
+ [Amazon EC2][amazon-ec2]

[amazon-ec2]: http://aws.amazon.com/ec2/
[aws-s3]: http://aws.amazon.com/s3/
[aws-cloudwatch-logs]: http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/WhatIsCloudWatchLogs.html
[cwl-subscriptions]: http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/Subscriptions.html
[elasticsearch]: https://www.elastic.co/
[kibana]: https://www.elastic.co/products/kibana
[kibana3]: https://www.elastic.co/guide/en/kibana/3.0/index.html
[kibana4]: https://www.elastic.co/guide/en/kibana/4.1/index.html
[amazon-kinesis-connectors]: https://github.com/awslabs/amazon-kinesis-connectors
[aws-cloudformation]: http://aws.amazon.com/cloudformation/
[aws-lambda]: http://aws.amazon.com/lambda/
[aws-cloudtrail]: http://aws.amazon.com/cloudtrail/
[vpc-flow-documentation]: http://docs.aws.amazon.com/AmazonVPC/latest/UserGuide/flow-logs.html
[launch-stack]: https://console.aws.amazon.com/cloudformation/home?#/stacks/new?stackName=CWL-Elasticsearch&templateURL=https:%2F%2Fs3.amazonaws.com%2Faws-cloudwatch%2Fdownloads%2Fcloudwatch-logs-subscription-consumer%2Fcwl-elasticsearch.template
[dashboard-vpc]: https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Full-VPCFlowLogs-Dashboard.png
[dashboard-lambda]: https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Full-Lambda-Dashboard.png
[dashboard-cloudtrail]: https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Full-CloudTrail-Dashboard.png
[sending-vpc-flow-logs]: https://aws.amazon.com/blogs/aws/vpc-flow-logs-log-and-view-network-traffic-flows/
[sending-cloudtrail-logs]: http://docs.aws.amazon.com/awscloudtrail/latest/userguide/send-cloudtrail-events-to-cloudwatch-logs.html
[object-types]: https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-object-type.html
[pattern-syntax]: http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/FilterAndPatternSyntax.html
[cfn-template]: https://github.com/awslabs/cloudwatch-logs-subscription-consumer/blob/master/configuration/cloudformation/cwl-elasticsearch.template
[properties-files]: https://github.com/awslabs/cloudwatch-logs-subscription-consumer/tree/master/src/main/resources
[ec2-security-groups]: http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-network-security.html
[http-basic-auth]: https://en.wikipedia.org/wiki/Basic_access_authentication
[nginx]: http://nginx.org/
[nginx-conf]: https://github.com/awslabs/cloudwatch-logs-subscription-consumer/blob/master/configuration/nginx/nginx.conf
[kopf]: https://github.com/lmenezes/elasticsearch-kopf
