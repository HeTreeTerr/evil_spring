# evil_spring
手写spring

## 访问路径
http://localhost:8082/hello/spring?id=4396

## 问题总结
在生成controller层bean时，controller层需要注入service层。但service还没有
经过工厂生成出来，抛出异常。

解决：简化spring三级缓存，在beanMap变量的基础上引入beanMapCache。将紧急
需要生成的bean暂存在map中，供程序使用。当正式注入式，先从cache中获取，
没有则重新生成。保证功能又保证性能。