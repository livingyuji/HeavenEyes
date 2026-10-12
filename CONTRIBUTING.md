# CONTRIBUTING TO HEAVEN EYES

Thank you for your interest in contributing to **Heaven Eyes**, an advanced Minecraft Paper anti-cheat created and maintained by **LivingYuji**.

---

### 1. Code Quality & Architecture Standards

1. **Originality:** All contributions must consist of original code. Submissions containing decompiled, reverse-engineered, or copyrighted code from commercial anti-cheats (such as Vulcan, Grim, Karhu, or Intave) are strictly prohibited and will be rejected immediately.
2. **Platform & Toolchain:** Code must target **Paper 1.21.x** using **Java 21** language features.
3. **Thread Safety & Performance:**
   - Never block the primary Minecraft server tick thread with database queries, synchronous disk writes, or CPU-intensive statistical algorithms.
   - Use asynchronous executors (`runTaskAsynchronously`) for file persistence.
   - Clean up per-player session state on disconnect (`PlayerQuitEvent`) and plugin shutdown.
4. **Error Handling:** Do not suppress exceptions silently. Catch expected I/O failures, log informative messages with context, and provide fallback states.

---

### 2. Testing Expectations

Before submitting any Pull Request:
1. Verify that all automated JUnit 5 tests execute and pass:
   ```bash
   ./gradlew test
   ```
2. Include dedicated automated tests for any new duration parsers, heuristic algorithms, permission validators, or configuration handlers.
3. Verify compilation and packaging:
   ```bash
   ./gradlew build
   ```

---

### 3. Licensing & Intellectual Property

By submitting a Pull Request, you acknowledge that your contribution will be incorporated into the proprietary Heaven Eyes codebase under the project's copyright held by **LivingYuji**, without separate royalty obligations.
