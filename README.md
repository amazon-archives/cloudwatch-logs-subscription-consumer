## CloudWatch Logs Subscription Consumer

The **CloudWatch Logs Subscription Consumer** is a Java application that can help you deliver data from [Amazon CloudWatch Logs][aws-cloudwatch-logs] to any other system in near real-time. The current version comes with built-in connectors for [Elasticsearch][elasticsearch] and [Amazon S3][aws-s3], but it can easily be extended to support other destinations using the [Amazon Kinesis Connector Library][amazon-kinesis-connectors].

## One-Click Stack: CloudWatch Logs + Elasticsearch + Kibana

This project also comes with a sample [CloudFormation][aws-cloudformation] template that can quickly bring you up an Elasticsearch cluster on [Amazon EC2][amazon-ec2], fed with real-time data from any CloudWatch Logs log group. The template also includes sample [Kibana][kibana] dashboards for some of the common sources of AWS log data: [Amazon VPC Flow Logs][sending-vpc-flow-logs], [AWS Lambda][aws-lambda], and [AWS CloudTrail][sending-cloudtrail-logs]. You can however connect your Elasticsearch cluster to any other log group, and then customize the Kibana dashboard based on your own log structures.

If you already have an active CloudWatch Logs log group, you can launch a CloudWatch Logs + Elasticsearch + Kibana stack right now with this launch button: 

[![Launch your Elasticsearch stack fed by CloudWatch Logs data](https://s3.amazonaws.com/cloudformation-examples/cloudformation-launch-stack.png)][launch-stack]

#### Sample Dashboards (Click to Expand)

##### Amazon VPC Flow Logs

[![VPC Flow Logs Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-VPCFlowLogs-Dashboard.png)][dashboard-vpc]

#### AWS Lambda

[![Lambda Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-Lambda-Dashboard.png)][dashboard-lambda]

#### AWS CloudTrail

[![CloudTrail Sample Dashboard](https://s3.amazonaws.com/aws-cloudwatch/downloads/cloudwatch-logs-subscription-consumer/Small-CloudTrail-Dashboard.png)][dashboard-cloudtrail]


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
