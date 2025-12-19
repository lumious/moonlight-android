# GitHub Actions 工作流说明

本仓库包含两个 GitHub Actions 工作流，用于自动构建和发布 APK。

## 工作流文件

### 1. build-and-release.yml - 构建并发布到 Release

**触发条件：**
- 当推送带有 `v` 前缀的标签时（例如：`v1.0.0`, `v12.1-2501218`）
- 手动触发（workflow_dispatch）

**功能：**
- 自动构建 Release 和 Debug 版本的 APK
- 上传 APK 作为工作流产物
- 自动创建 GitHub Release 并附加所有 APK 文件
- Release 说明自动包含版本信息和下载说明

**生成的 APK 文件：**
- `app-nonRoot-release.apk` - 非 Root 正式版
- `app-root-release.apk` - Root 正式版（仅支持 Android O 以下）
- `app-nonRoot-debug.apk` - 非 Root 调试版
- `app-root-debug.apk` - Root 调试版

### 2. build-apk.yml - 持续集成构建

**触发条件：**
- 推送到 `master` 或 `main` 分支
- Pull Request 到 `master` 或 `main` 分支

**功能：**
- 自动构建 Release 和 Debug 版本的 APK
- 上传 APK 作为工作流产物（可在 Actions 页面下载）
- 不会创建 Release

## 使用方法

### 发布新版本

1. **更新版本号**
   - 编辑 `app/build.gradle` 文件
   - 更新 `versionName` 和 `versionCode`

2. **创建并推送标签**
   ```bash
   git tag v12.1-2501218
   git push origin v12.1-2501218
   ```

3. **等待构建完成**
   - 访问仓库的 Actions 页面查看构建进度
   - 构建完成后，会自动创建一个新的 Release

4. **编辑 Release 说明（可选）**
   - 访问 Releases 页面
   - 编辑自动创建的 Release，添加详细的更新说明

### 手动触发构建

1. 访问 Actions 页面
2. 选择 "Build and Release APK" 工作流
3. 点击 "Run workflow" 按钮
4. 选择要构建的分支
5. 点击 "Run workflow" 开始构建

### 查看构建产物

对于每次构建（无论是否创建 Release），你都可以：
1. 访问 Actions 页面
2. 点击相应的工作流运行
3. 在页面底部找到 "Artifacts" 部分
4. 下载 `release-apks` 或 `debug-apks`

## 工作流配置详情

### 构建环境
- **操作系统**: Ubuntu Latest
- **JDK**: Java 11 (Temurin)
- **Android NDK**: 27.0.12077973
- **Gradle**: 使用项目自带的 Gradle Wrapper

### 构建步骤
1. 检出代码（包括子模块）
2. 设置 Java 和 Android 开发环境
3. 安装 Android NDK
4. 构建 Release APK
5. 构建 Debug APK
6. 上传 APK 产物
7. （仅标签推送时）创建 GitHub Release

## 故障排查

### 构建失败
- 检查 Actions 页面的构建日志
- 确保 `app/build.gradle` 中的配置正确
- 确保子模块已正确初始化

### Release 未自动创建
- 确认推送的是标签（tag），而不是普通提交
- 标签名称必须以 `v` 开头（例如：`v1.0.0`）
- 检查仓库的 Settings > Actions > General 权限设置

### NDK 版本问题
- 如需更改 NDK 版本，需同时修改：
  - `app/build.gradle` 中的 `ndkVersion`
  - 工作流文件中的 NDK 安装命令

## 注意事项

1. **签名配置**: 当前工作流构建的是未签名的 APK。如需发布正式版本，需要配置签名密钥
2. **构建时间**: 完整构建大约需要 10-15 分钟
3. **存储空间**: 每个 APK 约 20-30MB，注意仓库和 Actions 的存储限制
4. **权限**: 确保 GitHub Actions 有权限创建 Release（在仓库设置中配置）

## 高级配置

### 添加签名配置

如需为 Release APK 添加签名，可以：

1. 在仓库的 Settings > Secrets 中添加以下密钥：
   - `KEYSTORE_FILE`: Base64 编码的 keystore 文件
   - `KEYSTORE_PASSWORD`: Keystore 密码
   - `KEY_ALIAS`: Key 别名
   - `KEY_PASSWORD`: Key 密码

2. 修改工作流文件，在构建步骤前添加：
   ```yaml
   - name: Decode Keystore
     run: |
       echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
   
   - name: Build Signed Release APK
     run: ./gradlew assembleRelease
     env:
       KEYSTORE_FILE: ../keystore.jks
       KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
       KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
       KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
   ```

3. 在 `app/build.gradle` 中配置签名：
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file(System.getenv("KEYSTORE_FILE") ?: "keystore.jks")
               storePassword System.getenv("KEYSTORE_PASSWORD")
               keyAlias System.getenv("KEY_ALIAS")
               keyPassword System.getenv("KEY_PASSWORD")
           }
       }
       
       buildTypes {
           release {
               signingConfig signingConfigs.release
               // ... 其他配置
           }
       }
   }
   ```
