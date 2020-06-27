## Main Feature
this project provide the ability to import the gateway component into your own application ,not only use by a center
gateway service

the base gateway component comes on zuul 

It support two kinds of deploy module below
### center gateway service
like the normal tradition ,it offers a type of remote service invoke 'dubbo' and the service discover using nacos
if you need use another kind of service discover ,you can develop it by yourself

### local gateway service
In this model ,you can intergate the gateway service into you own application

 
## Why we need it ?
Like the tradition ,we use a center gateway service to support our tech system,but it has some shortage 
- it's not stability
because all the tech depend on this service,such as high QPS but not import and low QPS but very import service
they will be impact each other
- It's not reliable
if some service has some bug that use more time and the service will be block by TIMEOUT,and the service will not suppot any other service


## How it works
the whole component works like this 
[architue]!(https://user-images.githubusercontent.com/66338301/85908961-18b7a280-b84a-11ea-898f-178dcecca0fe.png)

In this module,all the application gateway service work only in their own jvm,it will not impact othe jvm progress

## How to use it 
you can import the dependency 
```xml
   <dependency>
        <groupId>com.orion.gateway</groupId>
        <artifactId>gateway-client-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
```
then you and some configure like this ,and start your own application,it will works


## The config
```yaml
mgateway:
    gatewayPort:xxxx  ## the gateway listen pot
    printStaic: true ## print static info for service
    dubbo: 
      application: appName  ## applicationName
      addr: seveAddr  ## seviceName
      protocol: nacos  ## registe info
      timeout: xxx  ## timeout 
      retry: 1   ## retry info
    db:
      token: xx ## db connection info
      activeNum: 2 ## the active number of connection
    
```
