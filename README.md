
## docker desktop k8s 에서 실행

`[greeting, calculating]` 서비스만 `docker desktop k8s` 에서 실행

### build 및 docker image 생성 

```shell
# api/greeting 위치에서 실행
gradle build && docker build -t greeting .
```

```shell
# api/calculating 위치에서 실행
gradle build && docker build -t calculating .
```

### k8s 실행

```shell
# k8s 위치에서 실행
kubectl apply -f rbac.yaml
kubectl apply -f config.yaml 
kubectl apply -f calc-deployment.yaml 
kubectl apply -f greet-deployment.yaml 
```

### jib & skaffold

```shell
gradle :api:greeting:jibDockerBuild
gradle :api:calculating:jibDockerBuild

cd k8s
skaffold dev
```

## AWS EKS 에서 실행

```shell
export ACCOUNT_ID=55....
```

### eks namespace 변경

```shell
kubectl apply -f k8s/eks/namespace.yaml

kubectl config set-context eks-spring --cluster arn:aws:eks:ap-northeast-2:$ACCOUNT_ID\:cluster/eks-work-cluster \
--user arn:aws:eks:ap-northeast-2:$ACCOUNT_ID\:cluster/eks-work-cluster \
--namespace spring

kubectl config use-context eks-spring
```

### docker 이미지 빌드

아래 프로세스를 순서대로 진행  

- docker login  
- docker image build  
- docker image tagging  
- docker image push  

```shell
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com
docker build -t region-repo:1.0.0 .
docker tag region-repo:1.0.0 $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:1.0.0
docker push $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:1.0.0
```

만약 ARM 계열의 맥북을 사용중이라면 amd64 형식으로 docker build 하도록 명시  
그렇지 않으면 k8s 에서 ARM docker container 를 실행하게되고 요류가 발생한다.  

```shell
docker build --platform=linux/amd64 -t region-repo:1.0.0 . 
```

### application 배포  

아래와 같이 `envsubst` 명령어를 사용하면 문서에 매개변수 형식으로 값을 전달 가능    

secret 생성

```shell
export RDS_HOST_NAME=eckdemo.....ap-northeast-2.rds.amazonaws.com

DB_URL=jdbc:postgresql://$RDS_HOST_NAME/myworkdb \
DB_PASSWORD=myworkpassword \
envsubst < k8s/eks/db-secret.yaml | \
kubectl apply -f -
```

deployment 생성, service 생성 (Loadbalancer 생성확인)

```shell
ECR_REGION_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:1.0.0 \
envsubst < k8s/eks/region-deployment.yaml | \
kubectl apply -f -
```


```shell
kubectl get all
# EXTERNAL-IP 확인

curl -s http://<EXTERNAL-IP>:8080/health
```

### calculating, greeting 배포

```shell
# api/calculating 디렉토리
gradle build
docker build --platform=linux/amd64 -t calculating-repo:1.0.0 .
docker tag calculating-repo:1.0.0 $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/calculating-repo:1.0.0
docker push $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/calculating-repo:1.0.0

# api/greeting 디렉토리
gradle build
docker build --platform=linux/amd64 -t greeting-repo:1.0.0 .
docker tag greeting-repo:1.0.0 $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/greeting-repo:1.0.0
docker push $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/greeting-repo:1.0.0
```

```shell
kubectl apply -f k8s/eks/rbac.yaml
kubectl apply -f k8s/eks/config.yaml
```

```shell
ECR_REGION_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/greeting-repo:1.0.0 \
envsubst < k8s/eks/greet-deployment.yaml | \
kubectl apply -f -

ECR_REGION_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/calculating-repo:1.0.0 \
envsubst < k8s/eks/calc-deployment.yaml | \
kubectl apply -f -
```