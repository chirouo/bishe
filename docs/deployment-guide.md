# 跨平台部署教程

本文档说明如何在 Windows、Linux、macOS 上部署本项目。

## 1. 仓库地址

远程仓库地址：

```bash
git@github.com:chirouo/bishe.git
```

如果当前设备没有配置 GitHub SSH Key，也可以在 GitHub 页面中改用 HTTPS 克隆。

## 2. 通用依赖

所有平台都需要：

- Git
- Java 11
- Maven 3.9+
- Node.js 18+
- MySQL 8

## 3. 克隆项目

### SSH 方式

```bash
git clone git@github.com:chirouo/bishe.git
cd bishe
```

### HTTPS 方式

```bash
git clone https://github.com/chirouo/bishe.git
cd bishe
```

## 4. 初始化数据库

确保本地 MySQL 中存在 `root / 123456`，或者按实际环境修改 [backend/src/main/resources/application.yml](../backend/src/main/resources/application.yml)。

执行：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql
```

## 5. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：

- `http://localhost:8080`
- 健康检查：`http://localhost:8080/api/health`

## 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：

- `http://localhost:5173`

## 7. 演示账号

- 教师：`teacher01 / 123456`
- 学生：`student01 / 123456`

## 8. Windows 部署

### 8.1 安装建议

- Git for Windows
- JDK 11
- Maven
- Node.js 18+
- MySQL 8

### 8.2 PowerShell 启动步骤

```powershell
git clone git@github.com:chirouo/bishe.git
cd bishe
mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql
cd backend
mvn spring-boot:run
```

新开一个 PowerShell：

```powershell
cd bishe\frontend
npm install
npm run dev
```

### 8.3 常见问题

- `mvn` 找不到：检查 Maven 是否加入 `PATH`
- `java` 版本不对：执行 `java -version`，确认是 11
- MySQL 无法连接：检查账号密码和端口，必要时修改 `application.yml`

## 9. Linux 部署

### 9.1 Ubuntu/Debian 依赖安装示例

```bash
sudo apt update
sudo apt install -y git openjdk-11-jdk maven mysql-client
```

Node.js 可通过 NodeSource、nvm 或系统包安装，建议使用 Node 18 或更高版本。

### 9.2 启动步骤

```bash
git clone git@github.com:chirouo/bishe.git
cd bishe
mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql
cd backend && mvn spring-boot:run
```

新终端：

```bash
cd bishe/frontend
npm install
npm run dev
```

## 10. macOS 部署

### 10.1 安装建议

- 使用 Homebrew 安装 Git、Maven、Node
- 使用 Oracle JDK / Temurin JDK 11
- 安装 MySQL 8

### 10.2 Homebrew 示例

```bash
brew install git maven node
```

JDK 建议安装 `Temurin 11`，并执行：

```bash
java -version
mvn -version
node -v
```

### 10.3 启动步骤

```bash
git clone git@github.com:chirouo/bishe.git
cd bishe
mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql
cd backend && mvn spring-boot:run
```

新终端：

```bash
cd bishe/frontend
npm install
npm run dev
```

## 11. 生产或异机部署建议

- 如果另一台电脑的 MySQL 账号密码不同，修改 `backend/src/main/resources/application.yml`
- 如果不想把 AI Key 固定在代码里，后续建议改回环境变量读取
- 若端口被占用，可修改前端 `vite.config.js` 和后端 `application.yml`
- 首次部署后先访问健康检查接口，确认后端正常，再打开前端页面

## 12. 浏览器级验证命令

部署完成后，建议执行以下命令验证整链路：

```bash
conda run -n qdx_bishe python tests/browser/full_browser_smoke.py
```

如果新机器没有 `qdx_bishe` 环境，可先创建：

```bash
conda create -n qdx_bishe python=3.11 -y
conda run -n qdx_bishe pip install requests pytest playwright
conda run -n qdx_bishe playwright install chromium
```
