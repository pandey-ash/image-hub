# infrastructure
1. Verify if aws-cli is installed or not using command
``` sh
$ aws --version

```

2. If no output obtain then install aws-cli according to your operating system

3. Setup a cloud infrastructure. To set up cloud infrastructure run command
``` sh
$ aws cloudformation create-stack --stack-name nameOfstack --template-body file://csye6225-infra.yml --capabilities CAPABILITY_NAMED_IAM

```
4. To verify whether infrastructure is built or not, log in to aws console, navigate to cloud formation, under stack you can view your infrastructure.