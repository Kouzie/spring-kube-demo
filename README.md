# spring-kube-demo

## 실행

build 및 docker image 생성 

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

```shell

```