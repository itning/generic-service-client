# Dubbo 泛化调用客户端

[![GitHub stars](https://img.shields.io/github/stars/itning/generic-service-client.svg?style=social&label=Stars)](https://github.com/itning/generic-service-client/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/itning/generic-service-client.svg?style=social&label=Fork)](https://github.com/itning/generic-service-client/network/members)
[![GitHub watchers](https://img.shields.io/github/watchers/itning/generic-service-client.svg?style=social&label=Watch)](https://github.com/itning/generic-service-client/watchers)
[![GitHub followers](https://img.shields.io/github/followers/itning.svg?style=social&label=Follow)](https://github.com/itning?tab=followers)

[![Github Action](https://github.com/itning/generic-service-client/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/itning/generic-service-client/actions/workflows/maven.yml)
[![GitHub issues](https://img.shields.io/github/issues/itning/generic-service-client.svg)](https://github.com/itning/generic-service-client/issues)
[![GitHub license](https://img.shields.io/github/license/itning/generic-service-client.svg)](https://github.com/itning/generic-service-client/blob/master/LICENSE)
[![GitHub last commit](https://img.shields.io/github/last-commit/itning/generic-service-client.svg)](https://github.com/itning/generic-service-client/commits)
[![GitHub release](https://img.shields.io/github/release/itning/generic-service-client.svg)](https://github.com/itning/generic-service-client/releases)
[![GitHub repo size in bytes](https://img.shields.io/github/repo-size/itning/generic-service-client.svg)](https://github.com/itning/generic-service-client)
![hitCount](https://hitcount.itning.top/?u=itning&r=generic-service-client)
[![language](https://img.shields.io/badge/language-JAVA-green.svg)](https://github.com/itning/generic-service-client)

## 项目架构

前端：Angular ；项目地址：[itning/generic-service-client-web](https://github.com/itning/generic-service-client-web)

后端：Spring Boot ；项目地址：[itning/generic-service-client](https://github.com/itning/generic-service-client)

## 介绍

- 主要功能：使用网页发起dubbo协议的请求。http协议转dubbo协议

- 内部原理：[dubbo泛化调用](https://dubbo.apache.org/zh/docs/v2.7/user/examples/generic-reference/)

**网页填写请求的信息->http协议->泛化调用->服务提供者**

## 快速开始

**[推荐]使用编译好的，直接打开即可：** https://github.com/itning/generic-service-client-electron/releases

直接 [下载JAR](https://github.com/itning/generic-service-client/releases/download/1.3.3-W1-RELEASE/generic-service-client-1.3.3-W1-RELEASE.jar) 包，输入命令`java -jar generic-service-client-1.3.3-W1-RELEASE.jar`

即可启动项目，浏览器输入：`http://localhost:8868` 即可！‘

如果需要更改配置文件，则输入该命令：`java -jar -Dspring.config.location=application.properties generic-service-client-1.3.3-W1-RELEASE.jar`

注意：该JAR包仅支持本地访问，如果需要支持其它访问，需要自行编译！

### 部署

#### 后端

目前接口**提示功能仅支持Zookeeper和Nacos注册中心**，如果注册中心不是用的Zookeeper或者Nacos就不需要配这个地址

[application.properties](https://github.com/itning/generic-service-client/blob/master/generic-service-run/src/main/resources/application.properties#L21) 文件配置ZK地址用于提示，如果不需要提示则不需要配置！

```properties
# 支持多个ZK注册中心，例如下面就写了三个注册中心（zk-A,zk-B,zk-C）
generic-service-support-zk.zk-list.zk-A=192.168.66.1:2181,192.168.66.2:2181:2181,192.168.66.3:2181
generic-service-support-zk.zk-list.zk-B=192.168.77.1:2181,192.168.77.2:2181:2181,192.168.77.3:2181
generic-service-support-zk.zk-list.zk-C=192.168.88.1:2181,192.168.88.2:2181:2181,192.168.88.3:2181
# 支持多个Nacos注册中心，例如下面就写了三个注册中心（nacos-A,nacos-B,nacos-C）
generic-service-support-nacos.nacos-list.nacos-A=127.0.0.1:8848
generic-service-support-nacos.nacos-list.nacos-B=127.0.0.1:8858
generic-service-support-nacos.nacos-list.nacos-C=127.0.0.1:8868
```

其它配置不需要改动，默认端口号：8868

配置后弱类型接口名输入框会有个下拉框，如图：

![xialakuang](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/xialakuang.jpg)

如果没有配置，则是这样的：

![meiyouxialakuang](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/meiyouxialakuang.jpg)

配置后点击输入框会有提示：

![zidong](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/zidong.jpg)

#### 前端

前端需要知道后端的地址，所以需要改动前端配置：

默认开发环境：[environment.ts](https://github.com/itning/generic-service-client-web/blob/master/src/environments/environment.ts#L7)

默认线上生产环境：[environment.prod.ts](https://github.com/itning/generic-service-client-web/blob/master/src/environments/environment.prod.ts#L3)

开发环境运行：`npm run start`

构建生产：`npm run build`

#### Docker

支持docker部署

[镜像：itning/generic-service-client Tags (docker.com)](https://hub.docker.com/r/itning/generic-service-client/tags?page=1&ordering=last_updated)

部署命令：

`docker run --name generic-service-client -d -p 8868:8868 -e generic-service-support-zk.zk-list.zk-A=192.168.66.1:2181,192.168.66.2:2181:2181,192.168.66.3:2181 -e generic-service-support-zk.zk-list.zk-B=192.168.77.1:2181,192.168.77.2:2181:2181,192.168.77.3:2181 itning/generic-service-client:latest`

环境说明：

| 键                                         | 值（例子）                                                 | 说明                                                         |
| ------------------------------------------ | ---------------------------------------------------------- | ------------------------------------------------------------ |
| generic-service-support-zk.zk-list.A       | 192.168.66.1:2181,192.168.66.2:2181:2181,192.168.66.3:2181 | 可选Zk地址，其中A可以换成其它名字，例如generic-service-support-zk.zk-list.BBB |
| generic-service-support-nacos.nacos-list.A | 127.0.0.1:8848                                             | 可选nacos注册中心地址，其中A可以换成其它名字，例如generic-service-support-nacos.nacos-list.BBB |
| generic-service-support-nexus.base-url     | http://localhost:8888/nexus                                | 可选nexus私服地址                                            |
| generic-service-support-nexus.file-dir     | /tmp                                                       | 可选从nexus私服下载的文件保存地址                            |
| generic-service-support-nexus.username     | root                                                       | 可选nexus私服用户名                                          |
| generic-service-support-nexus.password     | root                                                       | 可选nexus私服密码                                            |



### 使用说明

#### 当有多个服务提供者的时候，会提示选择：

![zidong](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/zidong.jpg)

#### 点击编辑参数，可以对参数进行修改：

![bianjicanshu](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/bianjicanshu.jpg)

参数数据结构：

```json
[
  {
    "top.itning.Request": {
      "age": 1
    }
  },
  {
    "top.itning.Request2": {
      "dto": {
        "a": "a",
        "b": [1,2, 3 ]
      }
    }
  }
]
```

最外层是数组数组中每个对象只有一个KEY，KEY名代表接口参数的类全路径名，值代表参数值，而后每一个KEY代表属性名，VALUE为属性值。

点击确定后，解析结果如图：

![jiexijieguo](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/jiexijieguo.jpg)

#### 支持dubbo URL解析：

![dubboURLjiexi](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/dubboURLjiexi.jpg)

#### 夜间模式

![dubboURLjiexi](https://raw.githubusercontent.com/itning/generic-service-client/master/pic/black.jpg)

#### API

提供对外API

URL：http://localhost:8868/dubbo/invoke

请求方法：POST

Content-Type：application/json

请求体：
```json
{
    "url": "dubbo://192.168.66.1:20880",
    "interfaceName": "top.itning.dubbotest.service.DemoService",
    "method": "sayHello",
    "version": "1.2",
    "group": "haha",
    "retries": 0,
    "timeout": 400,
    "params": [
        {
            "java.util.Date": "2021-01-09 19:22:42"
        },
        {
            "java.time.LocalDateTime": "2021-01-09T19:22:42"
        },
        {
            "java.lang.String": "aaaaa"
        }
    ]
}
```

该请求体对应的JAVA方法：

``public String sayHello(Date time, LocalDateTime localDateTime, String name)``
