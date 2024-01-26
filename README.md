
## baremetal k8s 에서 실행

`[greeting, calculating]` 는 단순 Spring Application 임으로 `docker desktop k8s` 에서 실행가능  

### docker image build & push 

Dockerimage 로 생성

```shell
# api/greeting 위치에서 실행
gradle build && docker build -t greeting .
docker tag greeting core.harbor.domain/demo/greeting:latest
docker push core.harbor.domain/demo/greeting:latest

# api/calculating 위치에서 실행
gradle build && docker build -t calculating .
docker tag calculating core.harbor.domain/demo/calculating:latest
docker push core.harbor.domain/demo/calculating:latest
```

jib 으로 생성

```sh
gradle clean api:calculating:jib \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345

gradle clean api:greeting:jib \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345
```

### k8s resource 생성

```shell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/config/config.yaml

GREETING_MESSAGE=Hello_k8s \
envsubst < k8s/config/greeting-secret.yaml | \
kubectl apply -f -

# calculating deployment 생성
REGISTRY_URL=core.harbor.domain/demo \
envsubst < k8s/deploy/calc-deployment.yaml | \
kubectl apply -f -

# greeting deployment 생성
REGISTRY_URL=core.harbor.domain/demo \
envsubst < k8s/deploy/greet-deployment.yaml | \
kubectl apply -f -
```


### test

```sh
kubectl port-forward deployment.apps/calc-deployment 8080:8080 -n spring
kubectl port-forward deployment.apps/greet-deployment 8081:8080 -n spring

curl http://localhost:8080/calculating
curl http://localhost:8080/calculating/1/2

curl http://localhost:8081/greeting
curl http://localhost:8081/greeting/1/2
```

## AWS EKS 에서 실행

region 서비스 생성 명령어만 예시로 작성
사전에 각 서비스별로 aws ECR private repo 생성이 필요함.  

### docker image build & push 

Dockerimage 로 생성

```shell
# docker login  
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com

# api/region 위치에서 실행
gradle build && docker build -t region .
docker tag region-repo:latest $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:latest
docker push $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:latest
```

jib 으로 생성

```sh
ACCOUNT_ID=55... \
ECR_PASSWORD=$(aws ecr get-login-password --region ap-northeast-2) \
gradle clean api:region:jib \
    -PregistryUrl=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
    -PregistryUsername=AWS \
    -PregistryPassword=$ECR_PASSWORD
```

### k8s resource 생성

```shell
# secret 생성
RDS_HOST_NAME=eckdemo.....ap-northeast-2.rds.amazonaws.com \
DB_URL=jdbc:postgresql://$RDS_HOST_NAME/myworkdb \
DB_PASSWORD=myworkpassword \
envsubst < k8s/eks/db-secret.yaml | \
kubectl apply -f -

# region deployment 생성
ACCOUNT_ID=55... \
REGISTRY_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
envsubst < k8s/deploy/region-deployment.yaml | \
kubectl apply -f -
```
