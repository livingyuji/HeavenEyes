# BUILD & INSTALLATION GUIDE — HEAVEN EYES

**Author:** LivingYuji  
**Target Platform:** Paper 1.21.x  
**Java Requirement:** Java 21 LTS  

---

## 1. Prerequisites

Before compiling or deploying Heaven Eyes, ensure your environment meets the following requirements:
- **Java Toolchain:** Java 21 (e.g. Eclipse Adoptium Temurin 21 or OpenJDK 21).
- **Server Platform:** Paper 1.21.x (or compatible downstream forks such as Purpur).
- **Optional Dependencies:** LuckPerms 5.4+ (for advanced permission management commands without groups).

---

## 2. Compilation Instructions

The project uses Gradle with the included Gradle Wrapper.

### On Windows:
```powershell
java -version
.\gradlew.bat clean test
.\gradlew.bat build
```

### On Linux / macOS:
```bash
java -version
chmod +x ./gradlew
./gradlew clean test
./gradlew build
```

### Build Artifact Verification
Upon successful compilation, Gradle produces the production-ready plugin JAR at:
```text
build/libs/HeavenEyes-1.0.0.jar
```

---

## 3. Server Installation

1. Stop your Minecraft Paper server safely (`stop`).
2. Copy `build/libs/HeavenEyes-1.0.0.jar` into the server's `plugins/` directory.
3. (Optional) Ensure `LuckPerms.jar` is installed in `plugins/` if you want LuckPerms user management.
4. Start the server.
5. Heaven Eyes will automatically generate default configuration files under `plugins/HeavenEyes/`:
   - `config.yml`
   - `checks.yml`
   - `punishments.yml`
   - `messages.yml`
   - `staff.yml`
6. Verify successful startup in the console:
   ```text
   [INFO] [HeavenEyes] =================================================
   [INFO] [HeavenEyes]   Heaven Eyes Anti-Cheat v1.0.0 Enabled
   [INFO] [HeavenEyes]   Author: LivingYuji
   [INFO] [HeavenEyes]   Enabled Checks: 14/14
   [INFO] [HeavenEyes] =================================================
   ```

---

## 4. Troubleshooting & Common Questions

- **LuckPerms commands say unavailable:** LuckPerms is a soft dependency. If LuckPerms is not installed, Heaven Eyes starts normally, but `/he perm` commands will inform you that LuckPerms is not present.
- **Reloading configuration:** To reload configuration changes without restarting your server, execute `/he reload` from the console or with the `heaveneyes.reload` permission.
