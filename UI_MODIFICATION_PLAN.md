# Kế Hoạch Chỉnh Sửa UI cho MobileFinalGroup11
## (Dựa trên phân tích UI/UX của EnglishListeningPracticeWebsite)

---

## PHẦN 1: PHÂN TÍCH UI/UX CỦA WEB PROJECT

### 1.1 Design System (Hệ thống thiết kế)

| Thuộc tính | Giá trị |
|---|---|
| **Primary color** | `#E67300` (cam), hover: `#CC6600`, light: `#FFF3E0` |
| **Background** | body: `#FFFFFF`, section: `#F9FAFB`, card: `#FFFFFF`, footer: `#1E293B` |
| **Text** | primary: `#1A1A1A`, secondary: `#555555`, muted: `#888888` |
| **Border** | `#E5E7EB` |
| **Shadows** | 3 cấp: sm `0 1px 3px`, md `0 4px 12px`, lg `0 8px 30px` |
| **Border radius** | sm: `6px`, md: `10px`, lg: `16px` |
| **Font** | Inter, 16px base, line-height 1.6 |
| **Transition** | `0.25s ease` |
| **Container** | max-width `1140px`, padding `0 20px` |

### 1.2 Dark Mode (Web)

| Thuộc tính | Light | Dark |
|---|---|---|
| Background body | `#FFFFFF` | `#0F172A` |
| Background card | `#FFFFFF` | `#1E293B` |
| Background section | `#F9FAFB` | `#1E293B` |
| Text primary | `#1A1A1A` | `#F1F5F9` |
| Text secondary | `#555555` | `#94A3B8` |
| Text muted | `#888888` | `#64748B` |
| Border | `#E5E7EB` | `#334155` |
| Shadow | nhẹ (0.06-0.12) | đậm hơn (0.3-0.4) |

### 1.3 Các Component UI chính

1. **Navbar**: Sticky top, 60px, backdrop-filter blur, logo gradient cam, nav links, theme toggle, user dropdown, mobile hamburger
2. **Footer**: Nền tối `#1E293B`, grid 4 cột (Brand + Quick Links + Resources + Legal)
3. **Cards**: Border 1px solid, border-radius 8px, shadow nhẹ, hover shadow tăng
4. **Buttons**: Primary cam, hover translateY(-1px) + box-shadow, bo góc 6px
5. **Search bar**: Input text + Level dropdown + Search button nằm ngang
6. **Accordion**: Section header click mở/đóng, icon chevron xoay 180°, animation mượt
7. **Comments**: Avatar tròn (40px) + body (tên + time + nội dung) + actions (like/dislike/reply)
8. **Tabs**: Tab buttons nằm ngang, active tab có border-top cam + background trắng
9. **Breadcrumb**: "All topics / Category / Lesson" với link cam
10. **Progress bars**: Thanh ngang mỏng, fill gradient xanh lá
11. **Status badges**: Pill shape, "Completed" (xanh lá), "In Progress" (xám)
12. **Dividers**: Đường kẻ ngang mỏng `1px` ngăn cách sections

### 1.4 Các trang chính & UX Patterns

- **Home**: Hero 2 cột (text + slideshow), How-it-works 4 bước có icon, FAQ 2 cột
- **Auth**: Card căn giữa, Google OAuth, form fields, divider "Or"
- **Categories**: Grid cards (ảnh thumbnail 80x80 + thông tin), video badge vàng
- **Sections**: Breadcrumb → Search/Filter → Accordion sections → Lesson cards grid
- **Lesson Detail**: Breadcrumb → Tabs (Dictation/Transcript) → Audio player bar → Input + Check/






Skip → Feedback → Comments
- **Speaking**: Reference text box → Record button tròn lớn (80x80) → Best/Current result cards song song
- **Leaderboard**: 2 bảng cạnh nhau (7 ngày / 30 ngày), avatar tròn bo góc 4px, trophy icon vàng
- **Profile**: Avatar tròn lớn (100px) + thông tin, grid 2x2 detail cards, nút hành động

---

## PHẦN 2: SO SÁNH HIỆN TRẠNG ANDROID VS WEB

### 2.1 Những gì ĐÃ ĐÚNG ✅

- Bảng màu (colors.xml) đã khớp chính xác với web (`#E67300`, `#F9FAFB`, v.v.)
- Sử dụng Material3 components
- Cấu trúc card-based layout
- Tab-based navigation cho lesson
- Bottom navigation cho main sections
- Search + filter chips trong Explore
- Leaderboard có tabs Week/Month
- Comments section trong lesson
- Progress indicators (LinearProgressIndicator)
- Streak card + stats cards trên Home

### 2.2 Những gì CẦN CẢI THIỆN ❌

| # | Vấn đề | Web | Android hiện tại |
|---|---|---|---|
| 1 | **Dark mode colors** | Bộ màu dark riêng biệt (navy `#0F172A`) | Dùng lại y hệt màu light mode |
| 2 | **Avatar hình dạng** | Tròn (border-radius 50%) | Vuông, không bo góc |
| 3 | **Category card thiếu ảnh** | Có thumbnail 80x80 + status badge | Chỉ có text, không có ảnh |
| 4 | **Icons lỗi thời** | FontAwesome icons hiện đại | Dùng `@android:drawable` hệ thống cũ |
| 5 | **Bottom nav icons** | N/A (web dùng text links) | Dùng system drawables xấu |
| 6 | **Lesson feedback card** | Hint words phân màu (matched/new/hidden) | Card đơn giản, text cơ bản |
| 7 | **Speaking UI** | Record button tròn lớn + 2 result cards song song | FAB + TextViews đơn giản |
| 8 | **Leaderboard entry** | Avatar tròn có màu nền + trophy icon | Avatar vuông, không trophy |
| 9 | **Breadcrumb style** | Link cam có separator "/" | Text nối thô, ký tự ">" |
| 10 | **Accordion animation** | Chevron xoay mượt, toggle bằng CSS transition | Text "▾" tĩnh, không animation |
| 11 | **Profile avatar** | Tròn 100px, có upload/crop, camera icon | TextView 80dp vuông |
| 12 | **Onboarding** | N/A | Có nhưng icon placeholder (`ic_launcher_foreground`) |
| 13 | **Card elevation/shadow** | 3 cấp shadow (sm/md/lg) | Flat, chỉ dùng stroke border |
| 14 | **Comment avatar** | Tròn 40px, background xanh dương | TextView 40dp vuông |
| 15 | **Streak dots** | N/A | Container rỗng (logic ở Java) |

---

## PHẦN 3: KẾ HOẠCH CHỈNH SỬA CHI TIẾT

### PHASE 1: Design System Foundation (Nền tảng)

#### 1.1 Bổ sung Dark Mode colors
**File**: `res/values-night/colors.xml` (TẠO MỚI)
```xml
<!-- Tạo bộ màu dark riêng, không dùng lại light -->
<color name="tt_background">#0F172A</color>
<color name="tt_surface">#1E293B</color>
<color name="tt_surface_alt">#334155</color>
<color name="tt_text_primary">#F1F5F9</color>
<color name="tt_text_secondary">#94A3B8</color>
<color name="tt_text_muted">#64748B</color>
<color name="tt_border">#334155</color>
```

#### 1.2 Cập nhật Dark Mode theme
**File**: `res/values-night/themes.xml`
- Trỏ tới bộ màu dark mới thay vì dùng lại màu light

#### 1.3 Thêm drawable shapes chuẩn
**Tạo mới các file**:
- `res/drawable/shape_avatar_circle.xml` — Oval shape cho avatar tròn
- `res/drawable/shape_badge_pill.xml` — Pill shape cho status badges
- `res/drawable/shape_card_elevated.xml` — Card background có shadow

#### 1.4 Thêm dimensions
**File**: `res/values/dimens.xml` — Bổ sung:
```xml
<dimen name="avatar_large">80dp</dimen>
<dimen name="avatar_medium">40dp</dimen>
<dimen name="avatar_small">28dp</dimen>
<dimen name="shadow_elevation_sm">2dp</dimen>
<dimen name="shadow_elevation_md">4dp</dimen>
<dimen name="shadow_elevation_lg">8dp</dimen>
```

---

### PHASE 2: Custom Icons (Thay thế icon hệ thống)

#### 2.1 Tạo Vector Drawable icons
**Tạo mới trong** `res/drawable/`:
- `ic_nav_home.xml` — Home icon (Material Symbols)
- `ic_nav_explore.xml` — Compass/Search icon
- `ic_nav_leaderboard.xml` — Trophy/Chart icon
- `ic_nav_profile.xml` — Person icon
- `ic_play.xml` — Play button icon
- `ic_pause.xml` — Pause icon
- `ic_skip_next.xml` — Next icon
- `ic_skip_prev.xml` — Previous icon
- `ic_replay.xml` — Replay icon
- `ic_mic.xml` — Microphone icon
- `ic_arrow_back.xml` — Back arrow
- `ic_chevron_down.xml` — Chevron for accordion
- `ic_trophy.xml` — Trophy for leaderboard #1
- `ic_streak_fire.xml` — Fire icon for streak
- `ic_notification.xml` — Bell icon
- `ic_search.xml` — Search icon

#### 2.2 Cập nhật menu navigation
**File**: `res/menu/menu_main_navigation.xml`
- Thay tất cả `@android:drawable/...` bằng custom vector icons

#### 2.3 Cập nhật tất cả layout dùng system icons
**Files affected**: `activity_lesson.xml`, `fragment_home.xml`, `item_transcript_sentence.xml`, v.v.

---

### PHASE 3: Avatar & Badge System (Hệ thống avatar & badges)

#### 3.1 Avatar tròn
**Áp dụng cho tất cả avatar**:
- Dùng `ShapeableImageView` (Material) với `shapeAppearanceOverlay` circle
- Hoặc dùng `CardView` bọc `TextView` với `cardCornerRadius="50%"`
- Background màu nền (xanh dương `#3B82F6` cho comments, cam `#E67300` cho profile)

**Files cần sửa**:
- `fragment_profile.xml` — `textAvatar` → ShapeableImageView/CardView tròn
- `item_comment.xml` — `textAvatar` → Tròn
- `item_leaderboard_entry.xml` — `textAvatar` → Tròn
- `dialog_streak.xml`

#### 3.2 Status Badges (Pill shape)
**Tạo style mới**: Badges pill-shaped giống web
```xml
<!-- Completed: nền xanh nhạt, chữ xanh đậm -->
<color name="tt_badge_completed_bg">#D4EDDA</color>
<color name="tt_badge_completed_text">#155724</color>
<!-- In Progress: nền xám nhạt, chữ xám đậm -->
<color name="tt_badge_in_progress_bg">#E2E3E5</color>
<color name="tt_badge_in_progress_text">#383D41</color>
```

---

### PHASE 4: Card Elevation & Shadow (Bóng đổ cho cards)

#### 4.1 Thêm elevation cho MaterialCardView
**Tất cả MaterialCardView** hiện tại dùng flat (no elevation, chỉ stroke).
Cần thêm:
```xml
app:cardElevation="2dp"  <!-- sm -->
app:cardElevation="4dp"  <!-- md, cho cards quan trọng -->
app:cardElevation="8dp"  <!-- lg, cho modals/dialogs -->
```

**Files cần sửa** (tất cả layout files chứa MaterialCardView):
- `fragment_home.xml` — cardStreak, continue learning, stats cards
- `fragment_explore.xml` — category cards
- `fragment_leaderboard.xml` — rank card
- `fragment_profile.xml` — tất cả cards
- `activity_lesson.xml` — player card, feedback card, comments card
- `activity_lesson_list.xml` — category info card
- `item_explore_category.xml`
- `item_leaderboard_entry.xml`
- `item_lesson_summary.xml`
- `item_transcript_sentence.xml`

---

### PHASE 5: Explore & Category Cards (Cải tiến trang khám phá)

#### 5.1 Thêm thumbnail cho Category Card
**File**: `item_explore_category.xml`
- Thêm `ImageView` (hoặc `ShapeableImageView`) 80x80dp bên trái
- Layout chuyển thành horizontal: ảnh bên trái + info bên phải (giống web)
- Thêm Video badge (Chip vàng `#FFD43B`) nếu loại VIDEO

#### 5.2 Thêm thumbnail cho Recommendation Card
**File**: `item_recommendation.xml`
- Tương tự, thêm ảnh nhỏ phía trên card

---

### PHASE 6: Lesson Detail UI Polish (Cải tiến trang bài học)

#### 6.1 Cải tiến Audio Player
**File**: `activity_lesson.xml`
- Play button: Tròn hoàn hảo (sử dụng `ShapeableImageView` hoặc `MaterialButton` icon-only circular)
- Progress bar: `SliderWidget` hoặc styled `LinearProgressIndicator` mỏng hơn
- Time display: Font monospace

#### 6.2 Cải tiến Dictation Feedback
**File**: `activity_lesson.xml` — `cardFeedback`
- Phân biệt 3 loại feedback bằng màu nền:
  - **Correct**: nền `#ECFDF5`, viền `#A7F3D0`, text `#065F46`
  - **Incorrect/Hint**: nền trong suốt, hint words phân 3 màu (matched/new/hidden)
  - **Skipped**: nền `#FFFBEB`, viền `#FDE68A`, text `#92400E`

#### 6.3 Cải tiến Speaking UI
**File**: `activity_lesson.xml` — `speakingContainer`
- Reference text box: Card riêng với border, căn giữa, label "READ THIS" phía trên
- Record button: Tăng lên `96dp`, thêm shadow/glow effect
- Best/Current results: Chuyển thành 2 cards ngang song song giống web:
  - Best card: Border vàng `#F59E0B`, header gradient vàng
  - Current card: Border xanh `#3B82F6`, header gradient xanh

#### 6.4 Cải tiến Breadcrumb
**File**: `activity_lesson_list.xml`
- Thay ký tự ">" bằng "/" 
- "Explore" text thêm clickable với màu `tt_primary`
- Dùng icon `ic_arrow_back` thay `ic_media_previous`

---

### PHASE 7: Leaderboard Polish

#### 7.1 Cải tiến Leaderboard Entry
**File**: `item_leaderboard_entry.xml`
- Avatar tròn với background màu random (giống web)
- Thêm trophy icon (🏆) vàng cho rank #1
- Rank number styling: Bold, centered trong circle nhỏ cho top 3

#### 7.2 Thêm "Your active time" subtitle
**File**: `fragment_leaderboard.xml`
- Thêm subtitle text dưới mỗi tab header hiện thời gian hoạt động

---

### PHASE 8: Profile Page Enhancement

#### 8.1 Avatar lớn tròn
**File**: `fragment_profile.xml`
- `textAvatar` → `ShapeableImageView` hoặc custom CircleView, 80dp tròn
- Thêm camera icon overlay nhỏ (góc dưới phải) nếu có chức năng upload

#### 8.2 Weekly Activity Chart
- Cải tiến `activityChartContainer` với bar chart đẹp hơn (dùng custom View hoặc thư viện)

#### 8.3 Settings card styling
- Thêm divider giữa các setting items
- Icon bên trái mỗi setting (moon icon cho dark mode, bell cho notifications)

---

### PHASE 9: Onboarding Enhancement

#### 9.1 Cải tiến trang Onboarding
**File**: `activity_onboarding.xml`
- Thay placeholder `ic_launcher_foreground` bằng illustration/icon đẹp hơn
- Thêm emoji 🎧 trước tên app (giống web brand icon)
- Bottom glow: Gradient mượt hơn thay vì flat color

---

### PHASE 10: Comment Section Enhancement

#### 10.1 Cải tiến Comment Item
**File**: `item_comment.xml`
- Avatar tròn 40dp với background `#3B82F6`
- Thêm Like/Dislike buttons (icon + count)
- Reply button với indent cho nested replies
- Border-bottom divider giữa comments

---

## PHẦN 4: THỨ TỰ ƯU TIÊN THỰC HIỆN

| Ưu tiên | Phase | Lý do |
|---|---|---|
| 🔴 CAO | Phase 1 (Dark Mode) | Ảnh hưởng toàn bộ app, dễ làm |
| 🔴 CAO | Phase 2 (Icons) | Thay đổi visual lớn nhất, system icons rất xấu |
| 🔴 CAO | Phase 3 (Avatars) | Điểm nhận diện UI quan trọng |
| 🟡 TRUNG BÌNH | Phase 4 (Elevation) | Tăng chiều sâu, dễ áp dụng hàng loạt |
| 🟡 TRUNG BÌNH | Phase 5 (Category cards) | Cải thiện Explore page |
| 🟡 TRUNG BÌNH | Phase 6 (Lesson UI) | Trang core learning |
| 🟢 THẤP | Phase 7 (Leaderboard) | Trang phụ |
| 🟢 THẤP | Phase 8 (Profile) | Trang phụ |
| 🟢 THẤP | Phase 9 (Onboarding) | Chỉ thấy 1 lần |
| 🟢 THẤP | Phase 10 (Comments) | Chi tiết nhỏ |

---

## PHẦN 5: TÓM TẮT FILES CẦN SỬA

### Files tạo mới:
- `res/values-night/colors.xml`
- `res/drawable/ic_nav_home.xml`
- `res/drawable/ic_nav_explore.xml`
- `res/drawable/ic_nav_leaderboard.xml`
- `res/drawable/ic_nav_profile.xml`
- `res/drawable/ic_play.xml`, `ic_pause.xml`, `ic_skip_next.xml`, `ic_skip_prev.xml`
- `res/drawable/ic_replay.xml`, `ic_mic.xml`, `ic_arrow_back.xml`
- `res/drawable/ic_chevron_down.xml`, `ic_trophy.xml`, `ic_streak_fire.xml`
- `res/drawable/ic_notification.xml`, `ic_search.xml`
- `res/drawable/shape_avatar_circle.xml`
- `res/drawable/shape_badge_pill.xml`

### Files cần chỉnh sửa:
- `res/values/colors.xml` — Thêm badge colors
- `res/values/dimens.xml` — Thêm avatar/elevation dims
- `res/values-night/themes.xml` — Trỏ tới dark colors
- `res/menu/menu_main_navigation.xml` — Custom icons
- `res/layout/activity_main.xml` — Minor tweaks
- `res/layout/activity_onboarding.xml` — Illustration + emoji
- `res/layout/activity_login.xml` — Minor polish
- `res/layout/activity_register.xml` — Minor polish
- `res/layout/activity_lesson.xml` — Player, feedback, speaking UI
- `res/layout/activity_lesson_list.xml` — Breadcrumb, icons
- `res/layout/fragment_home.xml` — Icons, streak styling
- `res/layout/fragment_explore.xml` — Search icon
- `res/layout/fragment_leaderboard.xml` — Subtitle, styling
- `res/layout/fragment_profile.xml` — Avatar circle, icons
- `res/layout/item_explore_category.xml` — Thêm thumbnail
- `res/layout/item_recommendation.xml` — Minor polish
- `res/layout/item_leaderboard_entry.xml` — Avatar circle, trophy
- `res/layout/item_lesson_section.xml` — Chevron animation
- `res/layout/item_lesson_summary.xml` — Status badge
- `res/layout/item_comment.xml` — Avatar circle, actions
- `res/layout/item_transcript_sentence.xml` — Icon updates
- `res/layout/dialog_streak.xml` — Fire icon
