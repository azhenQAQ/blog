# Repository Guidelines

## 项目概览

本仓库是个人博客后端，基于 Spring Boot 3.5、Java 21、Maven、MySQL 5.7、MyBatis-Plus 与 Sa-Token。默认服务地址为 `http://localhost:8123/api`，Knife4j 文档地址为 `/api/doc.html`。所有接口应优先使用统一响应 `BaseResponse<T>`，业务错误通过 `BusinessException`、`ErrorCode` 与 `GlobalExceptionHandler` 处理。

## 目录结构与模块组织

主要源码位于 `src/main/java/com/blog/backend`：

- `controller/`：REST 控制器，保持轻量，只处理参数、鉴权入口与响应组装。
- `service/`、`service/impl/`：业务接口与实现，核心业务逻辑放在这里。
- `mapper/`：MyBatis-Plus Mapper；XML 位于 `src/main/resources/mapper/`。
- `model/dto/`：请求对象；`model/entity/`：数据库实体；`model/vo/`：脱敏响应视图；`model/enums/`：枚举。
- `common/`、`exception/`、`config/`、`aop/`、`annotation/`、`constant/`：通用响应、异常、配置、权限切面、注解与常量。

标准调用链：`Controller -> Service -> ServiceImpl -> Mapper -> DB`。

## 构建、运行与测试命令

项目使用 Maven Wrapper：

- `./mvnw clean compile`：编译源码。
- `./mvnw test`：运行全部测试。
- `./mvnw clean package -DskipTests`：跳过测试打包。
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`：使用本地配置启动服务。

在 Windows PowerShell 中可使用 `./mvnw.cmd`。根据本仓库约定，代理不得主动执行 Maven 编译、打包、测试命令，也不得主动新增、修改或执行测试；仅在用户明确要求时执行。

## 编码风格与命名约定

Java 代码使用 4 空格缩进。包名小写，类名 `PascalCase`，方法和字段 `camelCase`，常量 `UPPER_SNAKE_CASE`。新增接口应复用现有响应、异常和权限模式，避免引入平行工具类。DTO、Entity、VO 分层清晰，禁止直接向外暴露敏感实体字段。

## 配置与安全

配置文件包括 `application.yml`、`application-local.yml`、`application-prod.yml`。本地开发依赖 MySQL 数据库 `blog`。不要提交真实生产凭据、Token、会话标识或敏感日志；修改配置时明确影响的 profile。

## 其他
- 临时文件，如 脚本、图片等存放在 `tmp` 目录下
