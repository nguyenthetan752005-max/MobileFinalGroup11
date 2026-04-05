# TUNG TUNG - BÁO CÁO TỔNG HỢP CHI TIẾT

> File này được tạo để tổng hợp toàn bộ kiến thức, hướng thiết kế, tiến độ và phân tích chi tiết về dự án TungTung.

---

## MỤC LỤC

1. [Chi Tiết Project Hiện Tại](#1-chi-tiết-project-hiện-tại)
2. [Hướng Thiết Kế Từ Prompt Ban Đầu](#2-hướng-thiết-kế-từ-prompt-ban-đầu)
3. [Tiến Độ Hiện Tại - Được Và Chưa Được](#3-tiến-độ-hiện-tại---được-và-chưa-được)
4. [Nhận Xét và Hướng Giải Quyết](#4-nhận-xét-và-hướng-giải-quyết)

---

## 1. CHI TIẾT PROJECT HIỆN TẠI

### 1.1 TỔNG QUAN KIẾN TRÚC

Dự án TungTung là một ứng dụng Android học tiếng Anh (English Learning App) sử dụng kiến trúc **MVVM (Model-View-ViewModel)** kết hợp với **Clean Architecture** được triển khai qua các layer: Domain, Data (Local + Remote), và UI.

**Công nghệ chính:**
- Ngôn ngữ: Java
- Platform: Android (minSdk: 24, targetSdk: 36, compileSdk: 36)
- Database Local: Room (SQLite abstraction)
- Network: Retrofit + OkHttp + Gson
- Architecture Components: ViewModel, LiveData
- Dependency Injection: Manual (AppContainer pattern)
- Build System: Gradle Kotlin DSL

### 1.2 CẤU TRÚC PROJECT ANDROID

```
hcmute.edu.vn.nguyenthetan/
├── MainActivity.java                    # Entry point, bottom navigation
├── TungTungApplication.java             # Application class, DI container
├── core/
│   ├── AppDefaults.java                 # Constants mặc định
│   └── di/
│       └── AppContainer.java            # DI container thủ công
├── data/
│   ├── local/                           # Layer Local Database
│   │   ├── dao/                         # Data Access Objects
│   │   │   ├── catalog/
│   │   │   │   ├── CategoryDao.java
│   │   │   │   ├── RecommendationDao.java
│   │   │   │   └── SectionDao.java
│   │   │   ├── community/
│   │   │   │   ├── CommentDao.java
│   │   │   │   └── LeaderboardDao.java
│   │   │   ├── lesson/
│   │   │   │   ├── GuestProgressDao.java
│   │   │   │   ├── LessonDao.java
│   │   │   │   └── SentenceDao.java
│   │   │   ├── system/
│   │   │   │   └── SyncStateDao.java
│   │   │   └── user/
│   │   │       ├── AppSettingsDao.java
│   │   │       ├── DailyActivityDao.java
│   │   │       ├── ProfileDao.java
│   │   │       └── StreakDayDao.java
│   │   ├── db/
│   │   │   ├── RoomConverters.java      # Type converters cho Room
│   │   │   └── TungTungDatabase.java    # Database class chính
│   │   ├── entity/                      # Entities (bảng Room)
│   │   │   ├── catalog/
│   │   │   │   ├── CategoryEntity.java
│   │   │   │   ├── RecommendationEntity.java
│   │   │   │   └── SectionEntity.java
│   │   │   ├── community/
│   │   │   │   ├── CommentEntity.java
│   │   │   │   ├── LeaderboardEntryEntity.java
│   │   │   │   └── LeaderboardMetaEntity.java
│   │   │   ├── lesson/
│   │   │   │   ├── GuestSentenceProgressEntity.java
│   │   │   │   ├── LessonEntity.java
│   │   │   │   └── SentenceEntity.java
│   │   │   ├── system/
│   │   │   │   └── SyncStateEntity.java
│   │   │   └── user/
│   │   │       ├── AppSettingsEntity.java
│   │   │       ├── DailyActivityEntity.java
│   │   │       ├── ProfileEntity.java
│   │   │       └── StreakDayEntity.java
│   │   └── seed/                        # Seed data cho development
│   │       ├── CatalogSeedFactory.java
│   │       ├── CommunitySeedFactory.java
│   │       ├── DatabaseSeeder.java
│   │       └── UserSeedFactory.java
│   ├── remote/                          # Layer Remote API
│   │   ├── api/
│   │   │   └── MobileApiService.java    # Retrofit interface
│   │   ├── dto/
│   │   │   ├── MobileBootstrapCommentDto.java
│   │   │   ├── MobileBootstrapDto.java
│   │   │   ├── MobileCategoryDto.java
│   │   │   ├── MobileLeaderboardDto.java
│   │   │   ├── MobileLeaderboardEntryDto.java
│   │   │   ├── MobileLessonDto.java
│   │   │   ├── MobileSectionDto.java
│   │   │   └── MobileSentenceDto.java
│   │   ├── mapper/
│   │   │   └── RemoteEntityMapper.java  # Map DTO -> Entity
│   │   └── sync/
│   │       └── RemoteCatalogSyncManager.java
│   └── repository/                      # Repository implementations
│       ├── RoomCatalogRepository.java
│       ├── RoomCommentRepository.java
│       ├── RoomHomeRepository.java
│       ├── RoomLeaderboardRepository.java
│       ├── RoomLessonRepository.java
│       ├── RoomProfileRepository.java
│       └── support/
│           └── RepositoryFormatters.java
├── domain/                              # Domain Layer (Pure Java)
│   ├── model/
│   │   ├── LessonProgress.java
│   │   ├── comment/
│   │   │   └── Comment.java
│   │   ├── explore/
│   │   │   ├── ExploreCategory.java
│   │   │   ├── LessonCollection.java
│   │   │   ├── LessonSection.java
│   │   │   └── LessonSummary.java
│   │   ├── home/
│   │   │   ├── CurrentLesson.java
│   │   │   ├── HomeDashboard.java
│   │   │   ├── Recommendation.java
│   │   │   ├── StreakSummary.java
│   │   │   └── StudyStats.java
│   │   ├── leaderboard/
│   │   │   ├── LeaderboardEntry.java
│   │   │   └── LeaderboardPeriod.java
│   │   ├── lesson/
│   │   │   ├── CurrentLesson.java
│   │   │   ├── DictationFeedback.java
│   │   │   ├── LessonSession.java
│   │   │   ├── Sentence.java
│   │   │   ├── SentenceStatus.java
│   │   │   └── SpeakingAttempt.java
│   │   └── profile/
│   │       ├── Profile.java
│   │       └── UserStats.java
│   ├── repository/                      # Repository interfaces
│   │   ├── CatalogRepository.java
│   │   ├── CommentRepository.java
│   │   ├── HomeRepository.java
│   │   ├── LeaderboardRepository.java
│   │   ├── LessonRepository.java
│   │   └── ProfileRepository.java
│   └── usecase/                         # Use cases
│       ├── comment/
│       │   └── GetCommentsUseCase.java
│       ├── explore/
│       │   └── GetExploreCatalogUseCase.java
│       ├── home/
│       │   └── GetHomeDashboardUseCase.java
│       ├── leaderboard/
│       │   └── GetLeaderboardUseCase.java
│       ├── lesson/
│       │   ├── CheckDictationAnswerUseCase.java
│       │   ├── GetLessonCollectionUseCase.java
│       │   ├── GetLessonProgressUseCase.java
│       │   ├── GetLessonSessionUseCase.java
│       │   ├── SaveSentenceStatusUseCase.java
│       │   └── SaveSpeakingAttemptUseCase.java
│       └── profile/
│           └── GetProfileUseCase.java
└── ui/                                  # UI Layer
    ├── common/
    ├── explore/
    ├── home/
    ├── leaderboard/
    ├── lesson/
    ├── lessonlist/
    ├── onboarding/
    └── profile/
```

### 1.3 CHI TIẾT DATABASE LOCAL (ROOM)

#### 1.3.1 Các Entities và Mối Quan Hệ

**Tổng cộng 16 entities trong TungTungDatabase (version 1):**

1. **AppSettingsEntity** - Cài đặt ứng dụng
   - id, languageCode, onboardingCompleted
   - lastOpenedLessonId, lastOpenedSentenceId, lastViewedCategorySlug

2. **CategoryEntity** - Danh mục bài học (catalog/categories)
   - id (PK), slug (unique index), name
   - imageUrl, levelRange, contentType, practiceType
   - totalLessons, description, orderIndex

3. **CommentEntity** - Bình luận cộng đồng
   - id, sentenceId, parentCommentId
   - author, avatarLabel, timeAgo, content
   - likeCount, dislikeCount, orderIndex

4. **DailyActivityEntity** - Hoạt động học tập hàng ngày
   - date, activeTimeSeconds

5. **GuestSentenceProgressEntity** - Tiến trình học của guest user
   - sentenceId (PK), lessonId
   - status (NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED)
   - bestSpeakingScore, bestSpeakingTranscript, bestSpeakingFeedback
   - currentSpeakingScore, currentSpeakingTranscript, currentSpeakingFeedback
   - lastAccessedAt
   - Foreign key: sentenceId -> SentenceEntity.id

6. **LeaderboardEntryEntity** - Bảng xếp hạng
   - id, period (WEEKLY/MONTHLY)
   - rank, name, avatarLabel, activeTime, currentUser

7. **LeaderboardMetaEntity** - Metadata leaderboard
   - id, currentUserRank, currentUserTime

8. **LessonEntity** - Bài học
   - id (PK), sectionId (FK -> SectionEntity)
   - title, level, practiceType, contentType
   - totalSentences, passThreshold
   - youtubeVideoId, orderIndex

9. **ProfileEntity** - Hồ sơ người dùng
   - id, name, level, joinDate
   - todayStudyMinutes, thisWeekStudyMinutes
   - longestStreak, completedLessons
   - nativeLanguage, targetLanguage

10. **RecommendationEntity** - Đề xuất bài học
    - id, title, level, lessonCount, practiceType, orderIndex

11. **SectionEntity** - Phần/section trong category
    - id (PK), categoryId (FK -> CategoryEntity)
    - name, description, orderIndex

12. **SentenceEntity** - Câu nói trong bài học
    - id (PK), lessonId (FK -> LessonEntity)
    - audioUrl, content, hintText
    - durationMillis, startTime, endTime, orderIndex

13. **StreakDayEntity** - Ngày streak liên tiếp
    - id, dayLabel, completed

14. **SyncStateEntity** - Trạng thái đồng bộ
    - key (PK), version, lastSyncAt

#### 1.3.2 Các DAO (Data Access Objects)

Mỗi entity có DAO tương ứng với các phương thức CRUD và các query đặc biệt:

- **CategoryDao**: getAllOrdered(), getBySlug(), getFirstCategory(), count(), deleteAll(), insertAll()
- **SectionDao**: getByCategoryId(), insertAll()
- **LessonDao**: getById(), getBySectionId(), getAllOrdered(), insertAll()
- **SentenceDao**: getByLessonId(), insertAll()
- **GuestProgressDao**: getByLessonId(), getBySentenceId(), getCompletedCountForLesson(), getTouchedCountForLesson(), upsert(), insertAll()
- **CommentDao**: getBySentenceId(), insertAll()
- **LeaderboardDao**: getAllOrdered(), getMeta(), deleteEntries(), deleteMeta(), insertEntries(), upsertMeta()
- **ProfileDao**: getProfile(), upsert()
- **AppSettingsDao**: getSettings(), upsert()
- **SyncStateDao**: getByKey(), upsert(), insertAll()
- Và các DAO khác...

### 1.4 CHI TIẾT REMOTE LAYER

#### 1.4.1 Retrofit API Service

```java
public interface MobileApiService {
    @GET("api/mobile/bootstrap")
    Call<MobileBootstrapDto> getBootstrap();
}
```

Base URL: `http://10.0.2.2:8080/` (Android emulator localhost)

#### 1.4.2 Data Transfer Objects (DTOs)

**MobileBootstrapDto** - Payload bootstrap đầy đủ:
- version, generatedAt
- categories (List<MobileCategoryDto>)
- sections (List<MobileSectionDto>)
- lessons (List<MobileLessonDto>)
- sentences (List<MobileSentenceDto>)
- comments (List<MobileBootstrapCommentDto>)
- leaderboard (MobileLeaderboardDto)

**MobileCategoryDto:**
- id, slug, name, imageUrl, levelRange
- contentType, practiceType, totalLessons, description, orderIndex

**MobileLessonDto:**
- id, sectionId, title, level, displayType
- contentType, totalSentences, passThreshold, youtubeVideoId, orderIndex

**MobileSentenceDto:**
- id, lessonId, audioUrl, content, hintText
- durationMillis, startTime, endTime, orderIndex

#### 1.4.3 Remote Sync Manager

**RemoteCatalogSyncManager** thực hiện:
1. Gọi API bootstrap
2. Kiểm tra version so với sync state local
3. Nếu version khác hoặc chưa có -> thực hiện sync
4. Chạy trong transaction:
   - Xóa categories cũ, insert mới
   - Insert sections, lessons, sentences
   - Insert comments
   - Update leaderboard
   - Update sync state
5. Xử lý lỗi: IOException -> log warning, giữ local data

### 1.5 CHI TIẾT REPOSITORY LAYER

#### 1.5.1 RoomCatalogRepository

Implement `CatalogRepository` interface:
- `getExploreCategories()` - Lấy danh sách category cho màn Explore
  - Đếm completedLessons dựa trên guest progress
  - Trả về progressLabel: "Completed", "In Progress", "New"
  
- `getLessonCollection(String categorySlug)` - Lấy collection bài học theo category
  - Resolve category theo slug (fallback về default)
  - Trả về LessonCollection với sections và lesson summaries
  - Mỗi lesson có status dựa trên guest progress

#### 1.5.2 RoomLessonRepository

Implement `LessonRepository` interface:
- `getLessonSession(long lessonId)` - Lấy session đầy đủ cho một lesson
  - Lấy lesson entity, sentences
  - Tìm best speaking attempt từ guest progress
  - Trả về LessonSession với category title

- `getLessonProgress(long lessonId)` - Lấy progress hiện tại
  - Map sentence statuses
  - Resolve current sentence index
  - Trả về current speaking attempt

- `saveSentenceStatus(...)` - Lưu trạng thái câu
  - Upsert vào guest_sentence_progress_local
  - Cập nhật lastOpened

- `saveSpeakingAttempt(...)` - Lưu kết quả speaking
  - So sánh với best attempt
  - Update status COMPLETED nếu pass threshold
  - Lưu current và best attempt

#### 1.5.3 RoomHomeRepository

Implement `HomeRepository` interface:
- `getHomeDashboard()` - Lấy dữ liệu cho màn Home
  - Greeting với first name từ profile
  - Resolve current lesson (từ settings hoặc default)
  - Study stats từ profile
  - Recommendations
  - Streak summary

#### 1.5.4 Các Repository Khác

- **RoomProfileRepository**: Profile, daily activities, streak days
- **RoomLeaderboardRepository**: Leaderboard entries và meta
- **RoomCommentRepository**: Comments theo sentence

### 1.6 CHI TIẾT DOMAIN LAYER

#### 1.6.1 Domain Models

Các domain model là POJO (Plain Old Java Objects) immutable với final fields:

**LessonSession:**
- lessonId, title, categoryTitle, level, passThreshold
- List<Sentence> sentences
- SpeakingAttempt bestAttempt

**LessonProgress:**
- Map<Long, SentenceStatus> sentenceStatuses
- int currentSentenceIndex
- SpeakingAttempt currentAttempt

**SentenceStatus enum:**
- NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED

**SpeakingAttempt:**
- score, transcript, feedback

**HomeDashboard:**
- greeting, subtitle, StreakSummary
- CurrentLesson, StudyStats, List<Recommendation>

**ExploreCategory:**
- id, title, levelRange, lessonCount, practiceType, progressLabel

**LessonCollection:**
- categoryId, title, description, totalLessons, List<LessonSection> sections

#### 1.6.2 Repository Interfaces

Định nghĩa contract cho data layer:
- CatalogRepository: getExploreCategories(), getLessonCollection()
- LessonRepository: getLessonSession(), getLessonProgress(), saveSentenceStatus(), saveSpeakingAttempt()
- HomeRepository: getHomeDashboard()
- ProfileRepository: getProfile()
- LeaderboardRepository: getLeaderboardEntries(), getLeaderboardMeta()
- CommentRepository: getCommentsBySentenceId()

#### 1.6.3 Use Cases

Mỗi use case là một class đơn lẻ, implement single responsibility:

- **GetLessonSessionUseCase**: Gọi repository.getLessonSession()
- **GetLessonProgressUseCase**: Gọi repository.getLessonProgress()
- **SaveSpeakingAttemptUseCase**: Gọi repository.saveSpeakingAttempt()
- **CheckDictationAnswerUseCase**: Xử lý logic kiểm tra dictation
- **GetHomeDashboardUseCase**: Gọi homeRepository.getHomeDashboard()
- **GetExploreCatalogUseCase**: Gọi catalogRepository.getExploreCategories()
- Và các use case khác...

### 1.7 CHI TIẾT UI LAYER

#### 1.7.1 Activities và Fragments

**MainActivity:**
- BottomNavigationView với 4 tab: Home, Explore, Leaderboard, Profile
- Quản lý fragment transaction
- Lắng nghe sync state từ AppContainer để hiển thị progress bar
- Implement listeners từ các fragment để chuyển màn

**HomeFragment:**
- Hiển thị dashboard với greeting, streak summary, current lesson, stats, recommendations
- Kế thừa vào HomeViewModel
- Listener: onContinueLearning(), onOpenStreakDialog()

**ExploreFragment:**
- Grid/ danh sách các category
- Listener: onOpenCategory()

**LessonActivity:**
- Host cho lesson flow
- Tab layout: Dictation, Transcript, Speaking, Community
- Sử dụng LessonViewModel

**LessonListActivity:**
- Hiển thị sections và lessons theo category

**LeaderboardFragment:**
- Bảng xếp hạng weekly/monthly

**ProfileFragment:**
- Hồ sơ user, streak calendar, settings

#### 1.7.2 ViewModels

**HomeViewModel:**
- MutableLiveData<HomeDashboard> dashboardState
- Phương thức load(), forceLoad()
- Sử dụng GetHomeDashboardUseCase

**LessonViewModel:**
- MutableLiveData<UiState> uiState - chứa tất cả UI state
- Quản lý lesson session, sentence statuses
- Xử lý user interactions: selectTab(), onInputChanged(), togglePlayback(), checkAnswer(), skip(), nextSentence(), toggleRecording(), etc.
- CountDownTimer cho playback progress
- Logic phức tạp để cập nhật UI state

**Các ViewModel khác:** tương tự pattern

#### 1.7.3 View Binding

Sử dụng ViewBinding (generated) cho type-safe view access:
- ActivityMainBinding
- Các binding cho fragments

### 1.8 CHI TIẾT DEPENDENCY INJECTION

**AppContainer** - Manual DI container:
- Khởi tạo TungTungDatabase với Room
- Seed database nếu cần
- Tạo OkHttpClient với logging interceptor
- Tạo Retrofit instance
- Tạo MobileApiService
- Khởi tạo RemoteCatalogSyncManager và chạy sync trên background thread
- Tạo tất cả repositories, inject DAOs
- Tạo tất cả use cases, inject repositories
- Expose use cases cho UI

**Lưu ý quan trọng:**
- Đang sử dụng `allowMainThreadQueries()` - chạy Room trên main thread (chỉ cho development)
- Sync chạy trên `Executors.newSingleThreadExecutor()`

### 1.9 CHI TIẾT BACKEND SPRING BOOT

#### 1.9.1 Project Structure

```
D:\EnglishListeningPracticeWebsite/
├── src/main/java/com/english/learning/
│   ├── config/                    # Configuration classes
│   │   ├── ActiveStatusInitializer.java
│   │   ├── AdminDataInitializer.java
│   │   ├── CloudinaryConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SessionListenerConfig.java
│   ├── controller/
│   │   ├── admin/                 # Admin MVC controllers
│   │   ├── api/admin/             # Admin REST APIs
│   │   ├── api/learning/          # Dictation & Speaking APIs
│   │   │   ├── DictationApiController.java
│   │   │   └── SpeakingApiController.java
│   │   ├── api/mobile/            # Mobile APIs
│   │   │   ├── MobileContentApiController.java
│   │   │   └── MobileContentApiExceptionHandler.java
│   │   ├── auth/                  # Authentication
│   │   ├── community/             # Comments
│   │   ├── learning/              # Learning MVC
│   │   └── site/                  # Public pages
│   ├── dto/
│   │   ├── mobile/                # Mobile DTOs
│   │   │   ├── MobileBootstrapCommentDto.java
│   │   │   ├── MobileBootstrapLiteResponse.java
│   │   │   ├── MobileBootstrapResponse.java
│   │   │   ├── MobileCategoryDto.java
│   │   │   ├── MobileCommentDto.java
│   │   │   ├── MobileContentVersionDto.java
│   │   │   ├── MobileLeaderboardDto.java
│   │   │   ├── MobileLeaderboardEntryDto.java
│   │   │   ├── MobileLessonDetailResponse.java
│   │   │   ├── MobileLessonDto.java
│   │   │   ├── MobileSectionDto.java
│   │   │   └── MobileSentenceDto.java
│   │   └── ... các DTO khác
│   ├── entity/                    # JPA Entities
│   │   ├── Category.java
│   │   ├── Comment.java
│   │   ├── CommentVote.java
│   │   ├── DailyStudyStatistic.java
│   │   ├── Lesson.java
│   │   ├── PasswordResetToken.java
│   │   ├── Section.java
│   │   ├── Sentence.java
│   │   ├── Slideshow.java
│   │   ├── SpeakingResult.java
│   │   ├── User.java
│   │   └── UserProgress.java
│   ├── enums/                     # Enums
│   ├── repository/                # Spring Data JPA Repositories
│   ├── security/                  # Security components
│   ├── service/                   # Business logic
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── comment/
│   │   ├── content/
│   │   ├── impl/                  # Implementations
│   │   ├── integration/
│   │   ├── leaderboard/
│   │   ├── learning/
│   │   ├── mobile/                # Mobile services
│   │   │   ├── MobileCatalogService.java (interface)
│   │   │   ├── MobileCommentService.java (interface)
│   │   │   └── MobileLeaderboardService.java (interface)
│   │   ├── progress/
│   │   ├── settings/
│   │   ├── tracking/
│   │   └── user/
│   └── util/
└── src/main/resources/
```

#### 1.9.2 Database Entities (JPA)

**Category:**
- id, name, imageUrl, cloudImageId, levelRange
- type (LessonType enum: AUDIO, VIDEO)
- practiceType (PracticeType enum: LISTENING, SPEAKING, BOTH)
- totalLessons, description, orderIndex
- status (ContentStatus), isDeleted

**Section:**
- id, name, description, orderIndex
- ManyToOne -> Category
- status, isDeleted

**Lesson:**
- id, title, level, youtubeVideoId
- ManyToOne -> Section
- totalSentences
- status, isDeleted

**Sentence:**
- id, audioUrl, cloudAudioId, content
- startTime, endTime (Double - timestamps trong video)
- orderIndex
- @Transient properNouns (không lưu DB)
- ManyToOne -> Lesson

**User:**
- id, username, email, password
- name, avatar, bio, level
- active, createdAt, updatedAt
- roles

**UserProgress:**
- id
- ManyToOne -> User
- ManyToOne -> Sentence
- status, lastAccessedAt

**SpeakingResult:**
- id
- ManyToOne -> User
- ManyToOne -> Sentence
- score, transcript, feedback

**Comment:**
- id, content, createdAt
- ManyToOne -> User (author)
- ManyToOne -> Sentence
- parentCommentId (self-referencing)
- status

**DailyStudyStatistic:**
- id, userId, studyDate
- activeTimeMinutes, sentencesCompleted
- lessonsCompleted

#### 1.9.3 Mobile API Endpoints

```java
@RestController
@RequestMapping("/api/mobile")
public class MobileContentApiController {
    
    @GetMapping("/content/version")
    public MobileContentVersionDto getContentVersion();
    
    @GetMapping("/catalog/bootstrap-lite")
    public MobileBootstrapLiteResponse getBootstrapLite();
    // Trả về: categories + sections + lessons (không có sentences)
    
    @GetMapping("/bootstrap")
    public MobileBootstrapResponse getBootstrap();
    // Trả về: categories + sections + lessons + sentences + comments + leaderboard
    
    @GetMapping("/lessons/{lessonId}")
    public MobileLessonDetailResponse getLessonDetail(Long lessonId);
    // Trả về: lesson + sentences
    
    @GetMapping("/leaderboard")
    public MobileLeaderboardDto getLeaderboard();
    
    @GetMapping("/sentences/{sentenceId}/comments")
    public List<MobileCommentDto> getComments(Long sentenceId);
}
```

#### 1.9.4 Dictation & Speaking APIs

```java
@RestController
@RequestMapping("/api/dictation")
public class DictationApiController {
    @PostMapping("/check")
    public DictationResultDTO checkAnswer(@RequestBody CheckDictationRequest request);
    
    @PostMapping("/skip")
    public DictationResultDTO skipSentence(@RequestBody SkipSentenceRequest request);
}

@RestController
@RequestMapping("/api/speaking")
public class SpeakingApiController {
    @PostMapping("/evaluate")
    public SpeakingResultDTO evaluate(@RequestBody MultipartFile audio, ...);
}
```

### 1.10 CHI TIẾT DATA FLOW

#### 1.10.1 Luồng Dữ Liệu Chính

```
┌─────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
│  │ Category │  │ Section  │  │  Lesson  │  │   Sentence   │  │
│  │  Entity  │  │  Entity  │  │  Entity  │  │   Entity     │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬───────┘  │
│       │             │             │               │          │
│       └─────────────┴─────────────┴───────────────┘          │
│                         │                                    │
│                ┌────────▼────────┐                           │
│                │  Repositories   │                           │
│                └────────┬────────┘                           │
│                         │                                    │
│       ┌─────────────────┼─────────────────┐                 │
│       │                 │                 │                  │
│  ┌────▼────┐      ┌────▼────┐      ┌──────▼──────┐           │
│  │   Web   │      │  Admin  │      │  Mobile API │           │
│  │   MVC   │      │   API   │      │  Controller │           │
│  │Controller│      │Controller│      │             │           │
│  └────┬────┘      └────┬────┘      └──────┬──────┘           │
│       │                 │                 │                  │
│       └─────────────────┴─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/REST
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    ANDROID APP (TungTung)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Remote Layer                       │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │MobileApiService│ │RemoteEntityMapper│ │RemoteCatalog │  │  │
│  │  │  (Retrofit)   │  │  (DTO->Entity)   │ │SyncManager  │  │  │
│  │  └─────────────┘  └──────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                            │                                │
│                            ▼                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Data Layer                         │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │  Room DAOs  │  │   Entities   │  │ Repositories │  │  │
│  │  │             │  │  (16 tables) │  │  (6 repos)   │  │  │
│  │  └─────────────┘  └──────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                            │                                │
│                            ▼                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                   Domain Layer                        │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │   Models    │  │ Repositories │  │  Use Cases   │  │  │
│  │  │  (Domain)   │  │ (Interfaces) │  │  (14 use)    │  │  │
│  │  └─────────────┘  └──────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                            │                                │
│                            ▼                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                     UI Layer                          │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐  │  │
│  │  │  Activities │  │  ViewModels    │  │   Fragments  │  │  │
│  │  │  (5 acts)   │  │   (5 VMs)     │  │   (5 frags)  │  │  │
│  │  └─────────────┘  └──────────────┘  └─────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### 1.10.2 Luồng Sync Bootstrap

1. App khởi động -> TungTungApplication onCreate()
2. AppContainer constructor:
   - Tạo Room database
   - Seed data nếu cần
   - Tạo Retrofit client
   - Tạo RemoteCatalogSyncManager
   - Chạy sync trên background thread
3. RemoteCatalogSyncManager.sync():
   - Gọi mobileApiService.getBootstrap().execute()
   - Kiểm tra response.success và body != null
   - Kiểm tra version so với sync state local
   - Nếu version khác: chạy transaction
     - Xóa và insert categories, sections, lessons, sentences
     - Insert comments
     - Update leaderboard
     - Update sync state
   - IOException -> log warning, skip sync

#### 1.10.3 Luồng User Học Bài

1. User chọn category từ Explore
2. Mở LessonListActivity -> hiển thị sections và lessons
3. User chọn lesson -> mở LessonActivity
4. LessonViewModel.load():
   - Gọi GetLessonSessionUseCase -> RoomLessonRepository
   - Gọi GetLessonProgressUseCase -> lấy progress đã lưu
   - Map sentence statuses
   - Gọi GetCommentsUseCase cho current sentence
   - Publish UI state
5. User học dictation/speaking:
   - Nhập text / record audio
   - Check dictation / evaluate speaking
   - Gọi SaveSentenceStatusUseCase / SaveSpeakingAttemptUseCase
   - Repository lưu vào guest_sentence_progress_local
   - Publish UI state mới

### 1.11 CHI TIẾT SEED DATA

**CatalogSeedFactory** tạo dữ liệu mẫu:

**Categories (6):**
1. Daily Conversations (101) - 24 lessons, Beginner-Intermediate, Listening
2. Business English (102) - 16 lessons, Intermediate, Listening
3. TED Talks (103) - 10 lessons, Intermediate-Advanced, Listening
4. Travel Phrases (104) - 18 lessons, Beginner, Speaking
5. IELTS Speaking (105) - 14 lessons, Advanced, Speaking
6. Movie Clips (106) - 11 lessons, Intermediate, Listening

**Sections (7):**
- Section 1: Greetings, At the Restaurant (thuộc Daily Conversations)
- Section 1: Meetings (Business English)
- Section 1: Talks (TED Talks)
- Section 1: Airports (Travel Phrases)
- Section 1: Fluency (IELTS Speaking)
- Section 1: Everyday Scenes (Movie Clips)

**Lessons (11):**
- 1001: Meeting New Friends
- 1002: Small Talk at School
- 1003: Morning Routine Chat (Video)
- 1004: Ordering Coffee
- 1005 (DEFAULT_LESSON_ID): Daily English Conversations - Lesson 5
- 1006: Paying the Bill (Video)
- 1101: Office Catch-up
- 1201: Confidence in Public Speaking (Video)
- 1301: Checking in at the Airport
- 1401: Handling Follow-up Questions
- 1501: Cafe Scene Breakdown (Video)

**Sentences (59):** Các câu nói với audioUrl, content, hintText, duration, timestamps

### 1.12 CHI TIẾT BUILD CONFIGURATION

**app/build.gradle.kts:**
- compileSdk: 36
- minSdk: 24 (Android 7.0)
- targetSdk: 36
- Java 11
- ViewBinding: enabled
- BuildConfig: enabled

**Dependencies:**
- AndroidX (AppCompat, Material, Activity, ConstraintLayout, Fragment)
- Lifecycle (LiveData, ViewModel)
- RecyclerView
- Room (runtime + compiler)
- Retrofit + Gson converter
- OkHttp Logging Interceptor
- JUnit + Espresso (testing)

**libs.versions.toml** (được tham chiếu qua alias):
- androidx.appcompat: 1.7.0
- material: 1.12.0
- room: 2.6.1
- retrofit: 2.11.0
- okhttp: 4.12.0
- lifecycle: 2.8.7

---

## 2. HƯỚNG THIẾT KẾ TỪ PROMPT BAN ĐẦU

### 2.1 TÓM TẮT YÊU CẦU GỐC

Từ lịch sử chat, người dùng đã yêu cầu:

**1. Kiến trúc Database:**
- Room chỉ lưu metadata + sync state + local cache path
- Không lưu binary media (audio/image/video) trong Room
- Audio/image/video nằm ở Cloudinary hoặc cache file riêng
- Tách 2 lớp: Remote (manifest JSON/API) và Local (Room cache)

**2. Data Models chuẩn:**
- Category, Section, Lesson, Sentence
- UserProgress, SpeakingResult
- SyncMetadata
- MediaCache (nếu cần offline media)

**3. Backend Architecture:**
- MySQL là nguồn dữ liệu thật của hệ thống
- Spring Boot expose API cho Android
- Android gọi API, parse JSON, lưu vào Room
- Room là cache/local mirror
- Cloudinary chỉ chứa file media

**4. Sync Strategy:**
- App launch -> SyncRepository -> Tải manifest JSON
- Parse DTO -> Map sang Entity Room
- Upsert theo transaction
- Ghi lastSyncVersion, lastSyncAt
- Version-based sync (chỉ sync khi version thay đổi)

**5. Media Handling:**
- audio_url, image_url, youtube_video_id, thumbnail_url: chỉ lưu string trong Room
- Audio: stream trực tiếp từ Cloudinary
- Image: load bằng Glide/Coil
- Video: YouTube player hoặc WebView
- Nếu cần offline: tải file vào filesDir/cachesDir, Room chỉ lưu localPath

**6. Guest Mode:**
- Guest trước, login thật sau
- Guest progress lưu local trong Room
- Chưa cần backend cho progress
- Sau này login thì merge progress

**7. Offline Media (Phase sau):**
- MediaCache entity: localPath, downloadStatus, etag/hash
- Tải file vào filesDir hoặc cacheDir

**8. API Backend cho Mobile:**
- GET /api/mobile/catalog - categories + sections + lessons
- GET /api/mobile/categories/{id}/sections
- GET /api/mobile/lessons/{id} - lesson detail + sentences
- GET /api/mobile/lessons/{id}/comments
- POST /api/dictation/check
- POST /api/speaking/evaluate

**9. Thứ tự triển khai đề xuất:**
1. Thiết kế Room schema
2. Viết script Python xuất manifest.json
3. Tạo API/file tĩnh để Android tải manifest
4. Viết SyncRepository và import metadata vào Room
5. Sửa UI để đọc từ Room thay vì fake repository
6. Thêm offline audio cache nếu cần

### 2.2 PHÂN TÍCH KẾ HOẠCH SO VỚI THỰC TẾ

| Kế hoạch | Thực tế đã làm | Đánh giá |
|----------|---------------|----------|
| Room chỉ lưu metadata | Đúng - Room lưu entities với URLs | ✅ |
| Không lưu binary media | Đúng - audioUrl là string | ✅ |
| Cloudinary chứa media | Đúng - audioUrl trỏ đến Cloudinary | ✅ |
| 2 lớp Remote/Local | Đúng - DTOs và Entities riêng | ✅ |
| Sync theo version | Đúng - RemoteCatalogSyncManager kiểm tra version | ✅ |
| Guest mode | Đúng - GuestSentenceProgressEntity lưu local | ✅ |
| API backend Spring Boot | Đúng - MobileContentApiController đã có | ✅ |
| Script Python xuất manifest | Chưa làm - dùng API trực tiếp | ⚠️ |
| Offline audio cache | Chưa làm - đúng theo phase | ⏳ |

---

## 3. TIẾN ĐỘ HIỆN TẠI - ĐƯỢC VÀ CHƯA ĐƯỢC

### 3.1 NHỮNG GÌ ĐÃ ĐƯỢC (COMPLETED)

#### ✅ 1. Room Database Schema
- TungTungDatabase với 16 entities
- Foreign keys và indexes đúng chuẩn
- Type converters cho enums
- DAOs đầy đủ CRUD operations

#### ✅ 2. Repository Pattern
- 6 repository implementations
- Tách interface và implementation
- Không còn God Repository
- Mỗi repo có trách nhiệm riêng

#### ✅ 3. MVVM Architecture
- Domain models immutable
- Use cases đơn lẻ, single responsibility
- ViewModels sử dụng LiveData
- UI layer tách biệt

#### ✅ 4. Guest Progress Local
- GuestSentenceProgressEntity lưu tất cả progress
- Status: NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED
- Best và current speaking attempts
- Last accessed tracking

#### ✅ 5. Remote Sync Infrastructure
- Retrofit + OkHttp setup
- MobileApiService interface
- RemoteEntityMapper DTO -> Entity
- RemoteCatalogSyncManager với version check
- Background thread execution

#### ✅ 6. Backend Mobile API
- MobileContentApiController với 6 endpoints
- DTOs cho tất cả responses
- Service interfaces cho mobile

#### ✅ 7. Seed Data
- DatabaseSeeder với seed factories
- 6 categories, 7 sections, 11 lessons, 59 sentences
- User profile, settings, activities, streak
- Comments và leaderboard samples

#### ✅ 8. UI Layer
- MainActivity với bottom navigation
- 4 main fragments: Home, Explore, Leaderboard, Profile
- LessonActivity với 4 tabs
- ViewModels đầy đủ

#### ✅ 9. Lesson Flow
- Dictation checking
- Speaking recording (mock)
- Transcript viewing
- Community comments
- Progress tracking

#### ✅ 10. Dependency Injection
- AppContainer manual DI
- Tất cả dependencies wired đúng
- Lifecycle awareness

### 3.2 NHỮNG GÌ CHƯA ĐƯỢC (INCOMPLETE)

#### ⏳ 1. API Performance Optimization
**Vấn đề:** `/api/mobile/bootstrap` trả về 144,914 sentences = 41.6MB payload
**Thời gian:** ~20.2 giây
**Impact:** App khởi động chậm, timeout risk, tốn bandwidth

**Giải pháp cần làm:**
- Chuyển sang `/api/mobile/catalog/bootstrap-lite` (không có sentences)
- Lazy load sentences khi vào lesson
- Phân trang hoặc incremental sync

#### ⏳ 2. Lesson Detail API Integration
**Vấn đề:** Android chưa dùng `GET /api/mobile/lessons/{lessonId}`
**Hiện tại:** Tất cả sentences được sync trong bootstrap
**Nên làm:** Tách riêng, chỉ tải sentences khi cần

#### ⏳ 3. Background Thread Optimization
**Vấn đề:** Đang dùng `allowMainThreadQueries()` trong Room
**Vấn đề:** Sync chạy trên single thread executor đơn giản
**Cần làm:**
- Bỏ allowMainThreadQueries()
- Dùng Coroutines/RxJava cho async operations
- Repository methods trả về LiveData/Flow

#### ⏳ 4. Error Handling
**Vấn đề:** RemoteCatalogSyncManager chỉ log warning khi lỗi
**Vấn đề:** Chưa có retry mechanism
**Vấn đề:** Chưa có offline mode rõ ràng
**Cần làm:**
- Exponential backoff retry
- Offline-first architecture
- Queue sync operations

#### ⏳ 5. Audio Playback
**Vấn đề:** Chưa có MediaPlayer implementation thật
**Hiện tại:** LessonViewModel chỉ có CountDownTimer mock playback
**Cần làm:**
- Tích hợp ExoPlayer hoặc MediaPlayer
- Streaming từ Cloudinary URLs
- Cache management

#### ⏳ 6. Speaking Evaluation
**Vấn đề:** Đang dùng mock logic (score random)
**Vấn đề:** Chưa tích hợp Speech-to-Text
**Cần làm:**
- Google Speech Recognition
- Backend evaluation API
- Local scoring algorithm (optional)

#### ⏳ 7. User Authentication
**Vấn đề:** Chỉ có Guest mode
**Vấn đề:** Chưa có login/register
**Vấn đề:** Progress chưa sync với backend
**Cần làm:**
- Login/Register screens
- JWT token management
- Progress sync API
- Guest progress merge

#### ⏳ 8. Leaderboard Thật
**Vấn đề:** Leaderboard đang từ seed data
**Cần làm:**
- Integrate với `/api/mobile/leaderboard`
- Cập nhật real-time hoặc periodic sync
- Submit user scores

#### ⏳ 9. Comments Integration
**Vấn đề:** Comments đang từ seed/local
**Cần làm:**
- Integrate với `/api/mobile/sentences/{id}/comments`
- Post comment API
- Real-time updates (WebSocket hoặc polling)

#### ⏳ 10. Image Loading
**Vấn đề:** Category images chưa được load
**Cần làm:**
- Tích hợp Glide hoặc Coil
- Image caching
- Placeholder và error handling

### 3.3 LỖI ĐÃ BIẾT (KNOWN ISSUES)

#### ⚠️ 1. Bootstrap Payload Quá Lớn
```
Endpoint: GET /api/mobile/bootstrap
Status: 200 OK
Size: ~41.6 MB
Time: ~20.2 seconds
Data: 144,914 sentences
```
**Ảnh hưởng:**
- App khởi động cực chậm
- Timeout risk trên mạng yếu
- OOM risk trên devices cũ

#### ⚠️ 2. N+1 Query Problem
Trong `RoomLessonRepository.findSection()`:
```java
for (CategoryEntity category : categoryDao.getAllOrdered()) {
    for (SectionEntity section : sectionDao.getByCategoryId(category.id)) {
        // Check each section
    }
}
```
Có thể gây chậm khi data lớn.

#### ⚠️ 3. Main Thread Queries
```java
// AppContainer.java
.allowMainThreadQueries() // ❌ Không nên dùng production
```

#### ⚠️ 4. Mock Speaking Evaluation
```java
// LessonViewModel.java
int score = (currentIndex % 2 == 0) ? 78 : 65; // Mock!
```
Chưa có integration thật.

#### ⚠️ 5. Audio URL Sai
Một số sentences có audioUrl = "" (empty string) trong seed data.

#### ⚠️ 6. Database Migration
```java
.fallbackToDestructiveMigration(false) // Không có migration
```
Nếu schema thay đổi -> crash.

#### ⚠️ 7. Singleton Executor
```java
ioExecutor = Executors.newSingleThreadExecutor();
```
Tất cả operations chạy trên 1 thread - có thể bottleneck.

#### ⚠️ 8. No Retry Logic
```java
catch (IOException exception) {
    Log.w(TAG, "Bootstrap sync failed: " + exception.getMessage());
    // Chỉ log, không retry
}
```

---

## 4. NHẬN XÉT VÀ HƯỚNG GIẢI QUYẾT

### 4.1 NHẬN XÉT TỔNG QUAN

#### 👍 Điểm Tốt

1. **Kiến trúc sạch sẽ:** MVVM + Clean Architecture được implement đúng chuẩn. Các layer tách biệt rõ ràng.

2. **Repository Pattern:** Tách repository theo feature là quyết định đúng đắn, tránh God Object.

3. **Domain Layer:** Models immutable, use cases đơn lẻ, không phụ thuộc framework.

4. **Guest Progress:** Thiết kế guest_sentence_progress_local rất tốt, lưu đủ thông tin best và current attempts.

5. **Seed Data:** DatabaseSeeder giúp development dễ dàng, có data để test ngay.

6. **Backend API Design:** Có sự phân biệt bootstrap-lite vs bootstrap đầy đủ.

7. **Version-based Sync:** Kiểm tra version trước khi sync giúp giảm tải.

#### 👎 Điểm Cần Cải Thiện

1. **Sync Strategy:** Đang sync toàn bộ 145k sentences một lúc. Đây là vấn đề nghiêm trọng nhất.

2. **Threading:** allowMainThreadQueries() và single thread executor không phù hợp production.

3. **Offline Strategy:** Chưa có offline-first architecture rõ ràng. Nếu mất mạng, app sẽ dùng stale data mà không biết.

4. **Error Handling:** Thiếu retry, thiếu graceful degradation.

5. **Media Handling:** Chưa có audio playback thật, chưa có image loading.

6. **Authentication:** Chưa có user auth, progress chỉ local.

7. **Testing:** Chưa thấy unit tests hoặc integration tests.

### 4.2 HƯỚNG GIẢI QUYẾT CHI TIẾT

#### 🎯 Đợt 1: Tối Ưu Sync (Ưu tiên CAO)

**Mục tiêu:** Giải quyết vấn đề payload 41.6MB

**Thay đổi Backend:**
```java
// MobileContentApiController
@GetMapping("/catalog/bootstrap-lite")
public MobileBootstrapLiteResponse getBootstrapLite() {
    // Chỉ trả: categories + sections + lessons
    // Không trả: sentences, comments, leaderboard
}

@GetMapping("/lessons/{lessonId}/detail")
public MobileLessonDetailResponse getLessonDetail(@PathVariable Long lessonId) {
    // Trả: lesson + sentences cho lesson cụ thể
}
```

**Thay đổi Android:**
```java
// RemoteCatalogSyncManager
public void sync() {
    // 1. Gọi bootstrap-lite thay vì bootstrap
    // 2. Chỉ lưu categories, sections, lessons
    // 3. Không lưu sentences
}

// Khi vào lesson:
// 1. Kiểm tra sentences trong Room
// 2. Nếu chưa có hoặc stale -> gọi API lesson detail
// 3. Lưu sentences vào Room
// 4. Hiển thị
```

**Benefits:**
- Bootstrap giảm từ 41.6MB xuống ~100KB (chỉ catalog)
- Thời gian sync giảm từ 20s xuống <1s
- Lazy load lesson detail khi cần

#### 🎯 Đợt 2: Threading và Performance

**Mục tiêu:** Bỏ main thread queries, optimize threading

**Thay đổi:**
```java
// Bỏ allowMainThreadQueries()
TungTungDatabase database = Room.databaseBuilder(...)
    .fallbackToDestructiveMigration(false)
    // .allowMainThreadQueries() // REMOVE
    .build();

// Dùng ExecutorService riêng cho từng loại operation
ExecutorService diskIO = Executors.newFixedThreadPool(4);
ExecutorService networkIO = Executors.newCachedThreadPool();

// Repository methods trả về LiveData
public LiveData<List<CategoryEntity>> getAllCategories() {
    return categoryDao.getAllOrderedLive(); // Room return LiveData
}
```

#### 🎯 Đợt 3: Audio và Media

**Mục tiêu:** Tích hợp audio playback thật

**Thay đổi:**
```java
// Tích hợp ExoPlayer
public class AudioPlayer {
    private final ExoPlayer exoPlayer;
    
    public void play(String audioUrl) {
        // Load từ Cloudinary URL
        // Cache với CacheDataSource
    }
}

// LessonViewModel
private final AudioPlayer audioPlayer;

public void togglePlayback() {
    String audioUrl = getCurrentSentence().getAudioUrl();
    audioPlayer.play(audioUrl); // Thật, không còn mock
}
```

**Image Loading:**
```java
// Tích hợp Glide
Glide.with(context)
    .load(category.getImageUrl())
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .into(imageView);
```

#### 🎯 Đợt 4: Authentication và Cloud Sync

**Mục tiêu:** Thêm login và sync progress với backend

**Thay đổi:**
```java
// Auth API
public interface AuthApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}

// UserProgress API
public interface UserProgressApiService {
    @POST("api/user/progress/sync")
    Call<SyncResponse> syncProgress(@Body List<GuestSentenceProgressEntity> progress);
    
    @GET("api/user/progress")
    Call<List<UserProgressDto>> getUserProgress();
}

// Merge logic khi login
public void mergeGuestProgressToUser(String userId) {
    // 1. Lấy guest progress từ local
    // 2. Gửi lên server
    // 3. Nhận merged progress từ server
    // 4. Xóa guest progress, lưu user progress
}
```

#### 🎯 Đợt 5: Offline-First Architecture

**Mục tiêu:** App hoạt động tốt khi offline

**Thay đổi:**
```java
// WorkManager cho background sync
public class SyncWorker extends Worker {
    @Override
    public Result doWork() {
        // Sync khi có mạng
        // Queue operations khi offline
    }
}

// Network Observer
public class NetworkObserver {
    public void onNetworkAvailable() {
        // Trigger sync
    }
}

// Offline indicator trong UI
if (!isNetworkAvailable()) {
    showOfflineBanner();
}
```

#### 🎯 Đợt 6: Testing

**Unit Tests:**
```java
// Test repositories với in-memory Room
@RunWith(AndroidJUnit4.class)
public class RoomLessonRepositoryTest {
    @Test
    public void saveSpeakingAttempt_updatesBestScore() {
        // Test logic
    }
}
```

**Integration Tests:**
- Test API endpoints
- Test sync flow
- Test offline scenarios

### 4.3 KẾ HOẠCH TRIỂN KHAI THEO THỨ TỰ

| Đợt | Nội dung | Thời gian ước tính | Priority |
|-----|----------|------------------|----------|
| 1 | Tối ưu sync (bootstrap-lite + lazy load) | 2-3 ngày | 🔴 Cao |
| 2 | Threading optimization | 1-2 ngày | 🔴 Cao |
| 3 | Audio playback (ExoPlayer) | 2-3 ngày | 🟡 TB |
| 4 | Image loading (Glide) | 1 ngày | 🟡 TB |
| 5 | Speaking evaluation | 3-5 ngày | 🟡 TB |
| 6 | Authentication | 2-3 ngày | 🟢 Thấp |
| 7 | Cloud sync progress | 2-3 ngày | 🟢 Thấp |
| 8 | Offline-first | 3-5 ngày | 🟢 Thấp |
| 9 | Testing | Song song | 🔴 Cao |

### 4.4 CÔNG NGHỆ ĐỀ XUẤT BỔ SUNG

**Ngay lập tức:**
- **Paging 3**: Cho lazy loading lessons và sentences
- **WorkManager**: Cho background sync
- **DataStore**: Thay thế SharedPreferences cho settings

**Trung hạn:**
- **Kotlin Coroutines**: Thay thế ExecutorService
- **Flow**: Reactive streams cho data
- **Hilt**: Dependency injection tự động
- **MockWebServer**: Testing APIs

**Dài hạn:**
- **Jetpack Compose**: Thay thế XML layouts
- **Ktor**: Thay thế Retrofit (nếu chuyển sang Kotlin)
- **Room with FTS**: Full-text search cho content

### 4.5 RỦI RO VÀ GIẢM THIỂU

| Rủi ro | Xác suất | Impact | Giảm thiểu |
|--------|----------|--------|------------|
| Bootstrap API quá chậm | Cao | Cao | Đợt 1 optimization |
| Main thread ANR | Cao | Cao | Đợt 2 threading |
| Audio không play | TB | Cao | Đợt 3 media player |
| Data loss khi login | TB | Cao | Merge logic cẩn thận |
| Schema migration crash | Thấp | Cao | Versioning + tests |
| Backend thay đổi API | TB | TB | API versioning |

---

## PHỤ LỤC

### A. Danh sách đầy đủ các files quan trọng

**Android:**
- `c:\Users\ADMIN\AndroidStudioProjects\TungTung\app\src\main\java\hcmute\edu\vn\nguyenthetan\core\di\AppContainer.java`
- `c:\Users\ADMIN\AndroidStudioProjects\TungTung\app\src\main\java\hcmute\edu\vn\nguyenthetan\data\local\db\TungTungDatabase.java`
- `c:\Users\ADMIN\AndroidStudioProjects\TungTung\app\src\main\java\hcmute\edu\vn\nguyenthetan\data\remote\sync\RemoteCatalogSyncManager.java`
- `c:\Users\ADMIN\AndroidStudioProjects\TungTung\app\src\main\java\hcmute\edu\vn\nguyenthetan\data\repository\RoomLessonRepository.java`
- `c:\Users\ADMIN\AndroidStudioProjects\TungTung\app\src\main\java\hcmute\edu\vn\nguyenthetan\ui\lesson\LessonViewModel.java`

**Backend:**
- `D:\EnglishListeningPracticeWebsite\src\main\java\com\english\learning\controller\api\mobile\MobileContentApiController.java`
- `D:\EnglishListeningPracticeWebsite\src\main\java\com\english\learning\entity\Category.java`
- `D:\EnglishListeningPracticeWebsite\src\main\java\com\english\learning\entity\Sentence.java`

### B. Schema SQL tóm tắt

```sql
-- Backend MySQL
CREATE TABLE categories (id, name, image_url, cloud_image_id, ...);
CREATE TABLE sections (id, category_id, name, ...);
CREATE TABLE lessons (id, section_id, title, ...);
CREATE TABLE sentences (id, lesson_id, audio_url, content, ...);
CREATE TABLE users (...);
CREATE TABLE user_progress (...);

-- Android Room
CREATE TABLE category_local (...);
CREATE TABLE section_local (...);
CREATE TABLE lesson_local (...);
CREATE TABLE sentence_local (...);
CREATE TABLE guest_sentence_progress_local (...);
```

### C. API Endpoints Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| /api/mobile/content/version | GET | Kiểm tra version catalog |
| /api/mobile/catalog/bootstrap-lite | GET | Lấy catalog nhẹ (không sentences) |
| /api/mobile/bootstrap | GET | Lấy full bootstrap (có sentences) |
| /api/mobile/lessons/{id} | GET | Lấy lesson detail + sentences |
| /api/mobile/leaderboard | GET | Lấy bảng xếp hạng |
| /api/mobile/sentences/{id}/comments | GET | Lấy comments |
| /api/dictation/check | POST | Kiểm tra dictation |
| /api/speaking/evaluate | POST | Đánh giá speaking |

---

> **Tóm tắt cuối cùng:**
> 
> Project TungTung đã hoàn thành nền tảng kiến trúc tốt với MVVM, Room, và Repository pattern. Các layer tách biệt rõ ràng, code sạch sẽ. Tuy nhiên, vấn đề nghiêm trọng nhất là API bootstrap đang trả về 41.6MB payload gây chậm khởi động. Cần ưu tiên đợt 1 để tối ưu sync strategy. Sau đó giải quyết threading, media playback, và authentication theo thứ tự.

---

*Document Version: 1.0*
*Generated: April 2026*
