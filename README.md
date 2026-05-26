
## kind k8s 에서 실행

```shell
brew install kind
kind create cluster
# kind-kind 클러스터 설치 확인
kubectl config get-contexts

docker ps
#CONTAINER ID   IMAGE                  COMMAND                   CREATED         STATUS         PORTS                       NAMES
#4d3f3859d6b4   kindest/node:v1.35.0   "/usr/local/bin/entr…"   2 minutes ago   Up 2 minutes   127.0.0.1:52247->6443/tcp   kind-control-plane
```

### image registry 설치

kind 클러스터는 외부 레지스트리에 접근할 수 없으므로 로컬 레지스트리를 별도로 구성한다.  
`registry:2` 는 Docker 공식 컨테이너 레지스트리 이미지이며, Docker Hub 자체도 이 소프트웨어 기반이다.

```shell
# htpasswd 파일 생성 (bcrypt 암호화) - registry:2 인증에 사용
# macOS 기본 설치된 htpasswd 사용 (-B: bcrypt, -c: 파일 새로 생성, -b: 비밀번호 인자로 전달)
htpasswd -Bbc /tmp/htpasswd test test

# registry:2 컨테이너 실행
# -p 5001:5000 : 호스트 5001 → 컨테이너 5000 (jib push 는 localhost:5001 사용)
docker run -d --restart=always -p 5001:5000 --name kind-registry \
  -v /tmp/htpasswd:/auth/htpasswd \
  -e REGISTRY_AUTH=htpasswd \
  -e REGISTRY_AUTH_HTPASSWD_REALM="Registry Realm" \
  -e REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd \
  registry:2

# kind-registry 컨테이너를 kind Docker 네트워크에 연결
# k8s 파드가 kind-registry:5000 으로 이미지를 pull 할 수 있게 됨
docker network connect kind kind-registry
```

#### containerd insecure registry 설정

kind 노드(containerd)가 `kind-registry:5000` 을 HTTP로 접근하도록 설정.  
이 설정 없이는 `http: server gave HTTP response to HTTPS client` 에러 발생.

```shell
docker exec kind-control-plane bash -c '
mkdir -p /etc/containerd/certs.d/kind-registry:5000
cat > /etc/containerd/certs.d/kind-registry:5000/hosts.toml << EOF
server = "http://kind-registry:5000"

[host."http://kind-registry:5000"]
  capabilities = ["pull", "resolve"]
  skip_verify = true
EOF
systemctl restart containerd
'
```

```shell
# spring 네임스페이스 및 regcred Secret 생성 (namespace, dockerconfigjson 포함)
kubectl apply -f k8s/default.yaml
```

### docker image build & push

```shell
# kind 로컬 레지스트리 환경변수 설정
export REGISTRY_URL=localhost:5001
export REGISTRY_USERNAME=test
export REGISTRY_PASSWORD=test

# 레지스트리 로그인
docker login $REGISTRY_URL -u $REGISTRY_USERNAME -p $REGISTRY_PASSWORD
```

jib 으로 생성

```sh
./gradlew api:calculating:jib \
    -Ptags=latest \
    -PregistryUrl=$REGISTRY_URL \
    -PregistryUsername=$REGISTRY_USERNAME \
    -PregistryPassword=$REGISTRY_PASSWORD

# greeting deployment 는 0.0.1 태그를 참조하므로 동일하게 맞춤
./gradlew api:greeting:jib \
    -Ptags=0.0.1 \
    -PregistryUrl=$REGISTRY_URL \
    -PregistryUsername=$REGISTRY_USERNAME \
    -PregistryPassword=$REGISTRY_PASSWORD
```

### k8s resource 생성

```shell
# RBAC, ConfigMap, DB Secret 적용
kubectl apply -f k8s/config/spring-role.yaml
kubectl apply -f k8s/config/config.yaml
kubectl apply -f k8s/config/db-secret.yaml

# greeting-secret 은 envsubst 로 값 주입 후 적용
GREETING_MESSAGE=Hello_k8s \
envsubst < k8s/config/greeting-secret.yaml | \
kubectl apply -f -
```

#### 서비스 배포

```shell
# calculating, greeting deployment 생성
# REGISTRY_URL 은 k8s 내부에서 pull 하는 주소 — kind-registry:5000 사용
REGISTRY_URL=kind-registry:5000 envsubst < k8s/deploy/calc-deployment.yaml | kubectl apply -f -
REGISTRY_URL=kind-registry:5000 envsubst < k8s/deploy/greet-deployment.yaml | kubectl apply -f -
```

### Monitoring

DaemonSet 방식으로 otel 컬렉터 설치 (노드당 1개, /var/log/pods 직접 수집)

**설치 순서가 중요한 이유**
- **cert-manager** → K8s는 opentelemetry-operator 같은 Operator의 Webhook 서버와 통신할 때 보안을 위해 HTTPS만 허용함.
  cert-manager가 이 HTTPS 인증서를 자동으로 발급/갱신해줌
- **opentelemetry-operator** → `OpenTelemetryCollector` 라는 커스텀 리소스를 K8s가 이해할 수 있게 해주는 컨트롤러.
  이게 없으면 `kubectl apply -f otel-daemonset.yaml` 시 `no matches for kind` 에러 발생
- cert-manager가 완전히 뜬 다음 operator를 설치해야 인증서 발급이 정상 처리됨

> <https://github.com/open-telemetry/opentelemetry-operator>  
> <https://opentelemetry.io/docs/kubernetes/operator/>  
> <https://cert-manager.io/docs/installation/>

```shell
# 1. cert-manager 설치
# opentelemetry-operator 가 Webhook TLS 인증서를 자동 발급/갱신하는 데 cert-manager 를 사용함
# cert-manager 없이 operator 를 설치하면 Webhook 인증서 생성 실패로 operator 가 기동되지 않음
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.20.2/cert-manager.yaml

# cert-manager 파드가 완전히 뜬 뒤 다음 단계로 진행 (중간에 설치하면 인증서 발급 실패)
kubectl wait --for=condition=Available deployment --all -n cert-manager --timeout=120s

# 2. opentelemetry-operator 설치
# OpenTelemetryCollector 같은 커스텀 리소스(CRD)를 K8s 가 인식하게 해주는 컨트롤러
# 이 operator 없이 k8s/otel-daemonset.yaml 을 apply 하면
# "no matches for kind OpenTelemetryCollector" 에러 발생
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/download/v0.152.0/opentelemetry-operator.yaml

# operator 컨트롤러가 준비된 뒤 CRD 를 apply 해야 정상 처리됨
kubectl wait --for=condition=Available deployment --all -n opentelemetry-operator-system --timeout=120s

# 3. OTel DaemonSet 배포
# operator 가 OpenTelemetryCollector 리소스를 감지하고 실제 DaemonSet 으로 변환해서 띄워줌
kubectl apply -f k8s/otel-daemonset.yaml

# 상태 확인
kubectl get all -n cert-manager
kubectl get all -n opentelemetry-operator-system
kubectl get opentelemetrycollector -n spring
```

### test

```sh
kubectl port-forward svc/calc-service 8080:8080 -n spring
kubectl port-forward svc/greet-service 8081:8080 -n spring

curl http://localhost:8080/calculating
curl http://localhost:8080/calculating/1/2

curl http://localhost:8081/greeting
curl http://localhost:8081/greeting/1/2
```
