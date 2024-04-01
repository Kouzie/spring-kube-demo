
## baremetal k8s 에서 실행

`[greeting, calculating]` 는 단순 Spring Application 임으로 `docker desktop k8s` 에서 실행가능  

사전에 harbor 와 같은 private registry 설치 후 진행  

### docker image build & push 

Dockerimage 로 생성

```shell
# api/greeting 위치에서 실행
./gradlew build && docker build -t greeting .
docker tag greeting core.harbor.domain/demo/greeting:latest
docker push core.harbor.domain/demo/greeting:latest

# api/calculating 위치에서 실행
./gradlew build && docker build -t calculating .
docker tag calculating core.harbor.domain/demo/calculating:latest
docker push core.harbor.domain/demo/calculating:latest
```

jib 으로 생성

```sh
./gradlew clean api:calculating:jib \
    -Ptags=latest \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345


./gradlew clean api:greeting:jib \
    -Ptags=latest \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345
```

### k8s resource 생성

```shell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/rbac.yaml
kubectl apply -f k8s/config/config.yaml

GREETING_MESSAGE=Hello_k8s \
envsubst < k8s/config/greeting-secret.yaml | \
kubectl apply -f -
```

#### 서비스 배포

```sh
# private registry 인증을 위한 secret
HARBOR_DOCKER_AUTH=$(echo -n 'admin:Harbor12345' | base64) \
HARBOR_DOCKER_CONFIG_JSON=$(echo -n '{"auths": {"core.harbor.domain": {"auth": "'$HARBOR_DOCKER_AUTH'"}}}' | base64) \
envsubst < k8s/config/registry-secret.yaml | \
kubectl apply -f -
```

```shell
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
./gradlew build && docker build -t region .
docker tag region-repo:latest $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:latest
docker push $ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/region-repo:latest
```

jib 으로 생성

```sh
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
&& ECR_PASSWORD=$(aws ecr get-login-password --region ap-northeast-2) \
&& ./gradlew clean api:greeting:jib \
    -Ptags=0.0.1 \
    -PregistryUrl=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
    -PregistryUsername=AWS \
    -PregistryPassword=$ECR_PASSWORD

ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
&& ECR_PASSWORD=$(aws ecr get-login-password --region ap-northeast-2) \
&& ./gradlew clean api:calculating:jib \
    -Ptags=latest \
    -PregistryUrl=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
    -PregistryUsername=AWS \
    -PregistryPassword=$ECR_PASSWORD
```

```sh
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
&& ECR_PASSWORD=$(aws ecr get-login-password --region ap-northeast-2) \
&& ./gradlew clean api:region:jib \
    -Ptags=latest \
    -PregistryUrl=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
    -PregistryUsername=AWS \
    -PregistryPassword=$ECR_PASSWORD
```

### k8s resource 생성

```sh
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
REGISTRY_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
envsubst < k8s/deploy/calc-deployment.yaml | \
kubectl apply -f -

ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
REGISTRY_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
envsubst < k8s/deploy/greet-deployment.yaml | \
kubectl apply -f -
```

```shell
# secret 생성
RDS_HOST_NAME=eckdemo.....ap-northeast-2.rds.amazonaws.com \
DB_URL=jdbc:postgresql://$RDS_HOST_NAME/myworkdb \
DB_PASSWORD=myworkpassword \
envsubst < k8s/eks/db-secret.yaml | \
kubectl apply -f -

# region deployment 생성
ACCOUNT_ID=$(aws sts get-caller-identity --query "Account" --output text) \
REGISTRY_URL=$ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com \
envsubst < k8s/deploy/region-deployment.yaml | \
kubectl apply -f -
```

### Monitoring

sidecar 방식으로 otel 컬렉터 설치

> <https://github.com/open-telemetry/opentelemetry-operator>  
> <https://opentelemetry.io/docs/kubernetes/operator/>  
> <https://cert-manager.io/docs/installation/helm/>

```shell
# cert-manager 설치
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.2/cert-manager.yaml
kubectl get all -n cert-manager

# opentelemetry-operator 설치
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/latest/download/opentelemetry-operator.yaml
kubectl get all -n opentelemetry-operator-system
```
