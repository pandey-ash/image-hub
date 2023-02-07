export afterINstall=true
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ec2-user/cloudwatch-config.json -s
sudo chmod 777 amazon-cloudwatch-agent
sudo systemctl start amazon-cloudwatch-agent

