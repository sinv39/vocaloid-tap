# 连音成歌 - 音符点击游戏

一个基于 SpringBoot 3.3.3 + MySQL 8.0 的前后端分离游戏项目。

## 功能特性

- **游戏页面**: 点击7个音符按钮(1-7)，每次点击立即发送当前旋律字符串进行匹配，2秒内无操作自动清空输入
- **管理页面**: 可以添加和删除歌曲，支持上传MP3音频文件

## 技术栈

- **后端**: SpringBoot 3.3.3, Java 17, Spring Data JPA, MySQL 8.0
- **前端**: 原生 HTML/CSS/JavaScript
- **构建工具**: Maven

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

## 数据库配置

1. 创建数据库:
```sql
CREATE DATABASE lianyinchengge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application.yml` 中的数据库连接信息:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lianyinchengge?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

## 运行项目

1. 克隆项目后，确保 MySQL 数据库已创建并配置正确

2. 使用 Maven 运行:
```bash
mvn spring-boot:run
```

3. 或者先打包再运行:
```bash
mvn clean package
java -jar target/lian-yin-cheng-ge-1.0.0.jar
```

4. 访问应用:
   - 游戏页面: http://localhost:10001/index.html
   - 管理页面: http://localhost:10001/admin.html

## 项目结构

```
连音成歌/
├── pom.xml                              # Maven 配置文件
├── src/main/
│   ├── java/com/lianyinchengge/
│   │   ├── LianYinChengGeApplication.java
│   │   ├── controller/                  # 控制器层
│   │   ├── entity/                      # 实体类
│   │   ├── repository/                  # 数据访问层
│   │   ├── service/                     # 业务逻辑层
│   │   └── config/                      # 配置类
│   └── resources/
│       ├── application.yml              # 配置文件
│       └── static/                      # 前端静态资源
│           ├── index.html              # 游戏页面
│           ├── admin.html              # 管理页面
│           ├── css/                    # 样式文件
│           └── js/                     # JavaScript 文件
└── README.md
```

## API 接口

### 游戏接口

- `GET /api/game/search?melody=xxx` - 根据旋律查询歌曲

### 管理接口

- `GET /api/admin/songs` - 获取歌曲列表
- `POST /api/admin/songs` - 添加歌曲（需要参数: title, melody, audioFile）
- `DELETE /api/admin/songs/{id}` - 删除歌曲

## 使用说明

1. **添加歌曲**: 在管理页面填写歌名、旋律（1-7的数字组合）和上传MP3音频文件
2. **游戏**: 在游戏页面点击音符按钮，每次点击立即匹配，2秒内无操作自动清空输入

## 注意事项

- 旋律字段只接受 1-7 的数字
- 音频文件建议使用 MP3 格式
- 确保 MySQL 数据库已正确配置字符集为 utf8mb4
