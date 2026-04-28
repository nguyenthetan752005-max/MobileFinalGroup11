# Kế hoạch refactor tổng hợp (Đợt 1 -> tiếp theo)

Tài liệu này ghi lại:
- Những gì **đã làm** ở Đợt 1-4 và Đợt 5.
- Những gì **còn lại / dự kiến làm tiếp** ở các đợt sau.

---

## Tổng quan trạng thái

- Đợt 1: ✅ Hoàn tất
- Đợt 2: ✅ Hoàn tất
- Đợt 3: ✅ Hoàn tất
- Đợt 4: ✅ Hoàn tất
- Đợt 5: ✅ Hoàn tất (42 test LessonVM, 13 test UserSessionStore)
- Đợt 6: ✅ Hoàn tất (Test sâu Use Cases: 32 tests mới)
- Đợt 7: ✅ Hoàn tất (Fix 14 violations DTO, 5 domain models mới, CI local scripts)
- Đợt 8: ✅ Hoàn tất (Cleanup code, chuẩn hóa naming)
- Đợt 9: ✅ Hoàn tất (Backend: 35 test cases mới cho Auth/JWT/Progress)
- Đợt 10: ✅ Hoàn tất (Backend Security: Logout blacklisting, Global Handler)

---

## Đợt 1 - Hardening DB + session + dọn nền

### Mục tiêu
- Tăng an toàn truy cập DB và session.
- Loại bỏ cấu hình Room rủi ro.
- Dọn artefact dư thừa.

### Đã thực hiện
- `DatabaseModule`:
  - Bỏ `allowMainThreadQueries()`.
  - Đổi `fallbackToDestructiveMigration()` sang `fallbackToDestructiveMigrationOnDowngrade()`.
- `UserSessionStore`:
  - Chuyển lưu token sang `EncryptedSharedPreferences` (AES256).
  - Có migration tự động từ plaintext preferences cũ.
  - Có fallback an toàn khi encrypted prefs lỗi runtime.
- Build/deps:
  - Thêm `androidx.security:security-crypto`.
- Dọn file:
  - Xóa file tạm `LessonViewModel.java~`.

### Kết quả
- Session token bảo mật hơn.
- Không còn Room main-thread query do cấu hình module gây ra.

---

## Đợt 2 - Tách `mobileApiService` khỏi `ui/*` theo MVVM + UseCase

### Mục tiêu
- UI không gọi Retrofit trực tiếp.
- Chuẩn hóa flow qua ViewModel + UseCase.

### Đã thực hiện
- Tạo nhóm UseCase mới cho các mảng:
  - Auth, Profile, Notification, Lesson network ops, Comment ops.
- Refactor các màn:
  - `LoginActivity`, `RegisterActivity`, `ForgotPasswordActivity`.
  - `OnboardingActivity`, `EditProfileActivity`, `NotificationCenterActivity`.
  - `LessonActivity` (đưa các callback network vào ViewModel/UseCase).
- Refactor ViewModel tương ứng + factory.
- Di chuyển helper parse auth response khỏi package UI.
- Wire đầy đủ UseCase vào `AppContainer`.

### Kết quả
- `ui/*` không còn reference trực tiếp tới `MobileApiService`.
- Tách lớp rõ hơn giữa presentation và data access.

---

## Đợt 3 - Giảm coupling trong Lesson module

### Mục tiêu
- Làm `LessonViewModel` sạch hơn về phụ thuộc tầng view/config.

### Đã thực hiện
- Tách model hàng transcript:
  - Tạo `LessonTranscriptRow` top-level.
  - `LessonViewModel` không dùng type lồng của adapter nữa.
- Tách logic build audio URL:
  - Tạo `SpeakingAudioUrls`.
  - `LessonViewModel` không build URL bằng `BuildConfig` inline.

### Kết quả
- Giảm coupling ViewModel <-> Adapter.
- Giảm logic hạ tầng/URL trong ViewModel.

---

## Đợt 4 - Gọn `AppContainer` (không tách quá đà)

### Mục tiêu
- Giảm độ phình của `AppContainer`, nhưng giữ thay đổi vừa phải.

### Đã thực hiện
- Tách mapping profile sync ra class riêng:
  - `ProfileSyncMapper`.
- `AppContainer` giữ vai trò orchestration chính, mapping chi tiết chuyển sang mapper.
- Dọn import/util thừa trong `AppContainer`.

### Kết quả
- `AppContainer` ngắn hơn và tập trung hơn vào wiring/orchestration.

---

## Đợt 5 - Test và guard kiến trúc (đang triển khai)

### Mục tiêu
- Dựng hàng rào chống regression kiến trúc + tăng độ tin cậy logic.

### Đã thực hiện
- Thêm test dependencies:
  - `mockito-core`, `archunit-junit4`, `androidx.arch.core:core-testing`.
- Sửa version catalog để resolve đúng các dependency test mới.
- Tạo `ArchitectureTest` (ArchUnit):
  - Cấm lớp trong `ui..` phụ thuộc trực tiếp `..data.remote.api..`.
  - Cấm lớp trong `ui..` phụ thuộc trực tiếp Room/DAO package.
- Thêm unit test nền tảng:
  - `SpeakingAudioUrlsTest`.
  - `AuthResultTest`.
  - `ChangePasswordUseCaseTest`.
  - `UpdateUsernameUseCaseTest`.
- Chạy `testDebugUnitTest`: build/test pass (lưu ý PowerShell có thể trả exit code khác do stderr note deprecation).

### Còn thiếu trong Đợt 5
- Test sâu `LessonViewModel`:
  - state transitions cho dictation/speaking/comment/navigation.
- Test migration chi tiết `UserSessionStore`:
  - plaintext -> encrypted, fallback behavior.
- Siết thêm rule ArchUnit theo đúng phạm vi package UI thực tế.

---

## Kế hoạch các đợt tiếp theo

## Đợt 6 - Hoàn thiện test cốt lõi

### Làm tiếp
- Bổ sung test cho `LessonViewModel` theo nhóm ca:
  - Khởi tạo session + render state ban đầu.
  - Chuyển câu / cập nhật progress.
  - Dictation check đúng/sai + skip.
  - Comment add/vote/delete và event state.
  - Speaking evaluate/get results/track time ở mức state update.
- Bổ sung test migration `UserSessionStore`:
  - Dữ liệu cũ migrate đúng.
  - Không mất token/userId khi migrate.
  - Fallback khi encrypted prefs không khởi tạo được.

### Tiêu chí xong
- Các test mới chạy pass trong `testDebugUnitTest`.
- Không tạo flaky test.

---

## Đợt 7 - Củng cố guard kiến trúc + quality gate

### Làm tiếp
- Mở rộng ArchUnit rules:
  - `ui..` không phụ thuộc trực tiếp `data.remote.*`, `data.local.dao.*`, `androidx.room.*`.
  - (tuỳ chọn) `domain..` không phụ thuộc `android.*`.
- Tạo checklist CI local:
  - `compileDebugJavaWithJavac`.
  - `testDebugUnitTest`.

### Tiêu chí xong
- Rule chạy pass ổn định.
- Bất kỳ vi phạm layering mới đều fail test sớm.

---

## Đợt 8 - Tối ưu hóa độ dài code có kiểm soát (không phá kiến trúc)

### Làm tiếp
- Chỉ gộp những phần có lợi rõ ràng, ví dụ:
  - Utility test/common fixture để giảm lặp trong test.
  - Chuẩn hóa naming/state object giữa các auth/profile màn.
- Không gộp mù quáng gây "class béo" hoặc mất SRP.

### Tiêu chí xong
- Giảm trùng lặp nhưng vẫn giữ testability và readability.

---

## Quy ước khi chạy/giám sát build-test

- Không theo dõi tiến trình bằng tên chung (`java`, `node`, `python`).
- Chỉ theo dõi bằng command/process id cụ thể hoặc stream log bằng `Get-Content -Wait`.

---

## Changelog nhanh theo mốc

- mốc A: Hoàn tất tách UI khỏi Retrofit trực tiếp.
- mốc B: Hoàn tất tách phụ thuộc view khỏi `LessonViewModel`.
- mốc C: Hoàn tất trích mapper khỏi `AppContainer`.
- mốc D: Thiết lập nền tảng test + ArchUnit guard.
- mốc E (kế tiếp): Hoàn thiện test sâu `LessonViewModel` + `UserSessionStore` migration.
