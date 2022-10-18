import boto3
import requests
from requests_aws4auth import AWS4Auth

region = ''  # e.g. us-west-1
service = 'es'
credentials = boto3.Session().get_credentials()
awsauth = AWS4Auth(credentials.access_key, credentials.secret_key, region, service, session_token=credentials.token)

host = 'https://search-recording-5p4pfow5utwpwhxth3u7oywdzm.us-east-1.es.amazonaws.com'
index = 'recording-index'
type = '_doc'
url = host + '/' + index + '/' + type

headers = {"Content-Type": "application/json"}

s3 = boto3.client('s3')


def handler(event, context):
    print(event)
    for record in event['Records']:
        # Get the bucket name and key for the new file
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']

        # Get, read, and split the file into lines
        obj = s3.get_object(Bucket=bucket, Key=key)
        document = {"callId": '222', "direction": '2222', "endTime": '2'}
        r = requests.post(url, auth=awsauth, json=document, headers=headers)
        
