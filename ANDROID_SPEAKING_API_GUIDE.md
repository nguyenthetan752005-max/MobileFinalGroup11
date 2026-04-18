# Android Speaking API - Hướng dẫn tích hợp (Java)

## Tổng quan

Backend cung cấp 4 endpoint cho chức năng Speaking Practice trên Android:

| # | Endpoint | Mô tả |
|---|----------|-------|
| 1 | `POST /api/mobile/speaking/evaluate` | Gửi audio nói → nhận điểm + feedback |
| 2 | `GET /api/mobile/speaking/results` | Lấy kết quả CURRENT + BEST đã lưu |
| 3 | `GET /api/mobile/speaking/audio/current` | Tải audio lần nói hiện tại |
| 4 | `GET /api/mobile/speaking/audio/best` | Tải audio lần nói tốt nhất |

> **Quan trọng**: Tất cả endpoint đều yêu cầu JWT token. `userId` được tự động lấy từ token, Android **không cần** truyền `userId`.

---

## 1. Đánh giá Speaking

### Request
```
POST /api/mobile/speaking/evaluate
Content-Type: multipart/form-data
Authorization: Bearer {jwt_token}
```

| Field | Type | Required | Mô tả |
|-------|------|----------|-------|
| `audio` | File (multipart) | ✅ | File audio WAV do user ghi âm |
| `referenceText` | String | ✅ | Nội dung câu gốc (sentence.content) |
| `sentenceId` | Long | ✅ | ID của câu đang practice |

### Response (200 OK)
```json
{
    "referenceText": "Hello, how are you?",
    "transcribedText": "hello how are you",
    "score": 85,
    "feedback": "Phát âm tốt! Chú ý ngữ điệu câu hỏi.",
    "audioUrl": "https://ik.imagekit.io/.../audio.wav",
    "bestResult": {
        "score": 92,
        "transcribedText": "hello how are you",
        "feedback": "Xuất sắc!",
        "audioUrl": "https://ik.imagekit.io/.../audio.wav"
    }
}
```

### Logic lưu kết quả:
- **CURRENT**: Luôn ghi đè bằng kết quả mới nhất
- **BEST**: Chỉ cập nhật khi `score` mới > `score` cũ
- Nếu chưa có BEST → lấy CURRENT làm BEST
- Audio được upload lên ImageKit tại `EnglishListeningData/AudioSpeaking/`

### Error Response
```json
{ "error": "Chưa đăng nhập" }   // 401
{ "error": "Chi tiết lỗi..." }  // 500
```

---

## 2. Lấy kết quả đã lưu

### Request
```
GET /api/mobile/speaking/results?sentenceId={sentenceId}
Authorization: Bearer {jwt_token}
```

### Response (200 OK)
```json
{
    "referenceText": "Hello, how are you?",
    "transcribedText": "hello how are you",
    "score": 85,
    "feedback": "Phát âm tốt!",
    "audioUrl": "https://...",
    "bestResult": {
        "score": 92,
        "transcribedText": "hello how are you",
        "feedback": "Xuất sắc!",
        "audioUrl": "https://..."
    }
}
```

> Response trả `null` nếu user chưa practice câu này.
> Nếu chỉ có CURRENT mà chưa có BEST → bestResult = CURRENT (fallback).

---

## 3. Tải audio speaking (Proxy)

Backend đóng vai trò **proxy** — tải audio từ ImageKit rồi stream về Android. Android **không cần** truy cập trực tiếp ImageKit (tránh lỗi 401).

### 3.1. Audio lần nói hiện tại
```
GET /api/mobile/speaking/audio/current?sentenceId={sentenceId}
Authorization: Bearer {jwt_token}
Response: audio/wav (binary stream)
```

### 3.2. Audio lần nói tốt nhất
```
GET /api/mobile/speaking/audio/best?sentenceId={sentenceId}
Authorization: Bearer {jwt_token}
Response: audio/wav (binary stream)
```

> Trả 404 nếu chưa có audio. Android dùng `MediaPlayer` hoặc `ExoPlayer` để phát trực tiếp từ URL proxy này.

---

## 4. Flow hoàn chỉnh trên Android

```
1. User mở lesson → GET /api/mobile/lessons/{lessonId}
   → Nhận danh sách sentences (id, content, audioUrl, hintText, ...)

2. User chọn sentence → GET /api/mobile/speaking/results?sentenceId={id}
   → Load kết quả đã lưu (nếu có): điểm, feedback, referenceText

3. Muốn phát lại audio cũ:
   → GET /api/mobile/speaking/audio/current?sentenceId={id}  (stream WAV)
   → GET /api/mobile/speaking/audio/best?sentenceId={id}     (stream WAV)

4. User ghi âm mới → POST /api/mobile/speaking/evaluate
   → Gửi: audio file + referenceText (= sentence.content) + sentenceId
   → Nhận: điểm mới, feedback mới, bestResult

5. Hiển thị kết quả:
   - Score hiện tại vs Best score
   - Nút phát lại audio (gọi proxy endpoint)
   - Feedback AI
   - Nút ghi âm lại
```

---

## 5. Thay đổi so với phiên bản cũ (BREAKING CHANGES)

### 5.1. Bỏ param `userId` ở tất cả endpoint
userId giờ tự động lấy từ JWT token. Android chỉ cần gửi header `Authorization: Bearer {token}`.

### 5.2. Thêm proxy endpoint cho audio
Không cần truy cập trực tiếp ImageKit URL nữa. Dùng:
- `/api/mobile/speaking/audio/current?sentenceId={id}`
- `/api/mobile/speaking/audio/best?sentenceId={id}`

### 5.3. Response bổ sung `referenceText`
`GET /results` giờ trả về thêm field `referenceText` (= sentence.content).

---

## 6. Audio Storage

| Loại audio | Path trên ImageKit | Mô tả |
|------------|-------------------|-------|
| Audio bài học (mẫu) | `EnglishListeningData/AudioLessons/` | Admin upload |
| Audio nói của user | `EnglishListeningData/AudioSpeaking/` | User upload khi practice |

### Naming convention:
- **Current**: `user_{userId}_sentence_{sentenceId}_current`
- **Best**: `user_{userId}_sentence_{sentenceId}_best`

---

## 7. Cấu trúc dữ liệu (Java)

### SpeakingResultDTO
```java
public class SpeakingResultDTO {
    private String referenceText;      // Câu gốc (sentence.content)
    private String transcribedText;    // Text AI nhận dạng từ audio
    private int score;                 // Điểm 0-100
    private String feedback;           // Nhận xét AI
    private String audioUrl;           // URL audio CURRENT (dùng để tham chiếu, không truy cập trực tiếp)
    private BestResult bestResult;     // Kết quả tốt nhất

    public static class BestResult {
        private int score;             // Điểm cao nhất
        private String transcribedText;// Text nhận dạng lần tốt nhất
        private String feedback;       // Feedback lần tốt nhất
        private String audioUrl;       // URL audio BEST (dùng để tham chiếu)
    }
}
```

> **Lưu ý**: Các field `audioUrl` trong response JSON chỉ dùng tham chiếu nội bộ. Để phát audio, Android nên gọi proxy endpoint (mục 3) thay vì truy cập trực tiếp URL ImageKit.

### MobileSentenceResponse (từ lesson detail API)
```java
public class MobileSentenceResponse {
    private Long id;
    private Long lessonId;
    private String audioUrl;           // URL audio mẫu (qua proxy backend)
    private String content;            // ← Đây chính là referenceText cho speaking
    private String hintText;
    private Integer durationMillis;
    private Double startTime;
    private Double endTime;
    private Integer orderIndex;
}
```

---

## 8. Ví dụ code Android (Java + Retrofit)

### Gọi API evaluate
```java
// SpeakingApi.java
public interface SpeakingApi {
    @Multipart
    @POST("/api/mobile/speaking/evaluate")
    Call<SpeakingResultDTO> evaluate(
        @Part MultipartBody.Part audio,
        @Part("referenceText") RequestBody referenceText,
        @Part("sentenceId") RequestBody sentenceId
    );

    @GET("/api/mobile/speaking/results")
    Call<SpeakingResultDTO> getResults(@Query("sentenceId") long sentenceId);
}

// Gửi audio
File audioFile = new File(recordedFilePath);
RequestBody audioBody = RequestBody.create(audioFile, MediaType.parse("audio/wav"));
MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), audioBody);

RequestBody refText = RequestBody.create(sentence.getContent(), MediaType.parse("text/plain"));
RequestBody sentId = RequestBody.create(String.valueOf(sentence.getId()), MediaType.parse("text/plain"));

speakingApi.evaluate(audioPart, refText, sentId).enqueue(new Callback<SpeakingResultDTO>() {
    @Override
    public void onResponse(Call<SpeakingResultDTO> call, Response<SpeakingResultDTO> response) {
        if (response.isSuccessful()) {
            SpeakingResultDTO result = response.body();
            // Hiển thị score, feedback, bestResult...
        }
    }
    @Override
    public void onFailure(Call<SpeakingResultDTO> call, Throwable t) { }
});
```

### Phát audio speaking qua proxy
```java
// URL phát audio current
String currentAudioUrl = BASE_URL + "/api/mobile/speaking/audio/current?sentenceId=" + sentenceId;

// URL phát audio best
String bestAudioUrl = BASE_URL + "/api/mobile/speaking/audio/best?sentenceId=" + sentenceId;

// Dùng MediaPlayer (thêm header JWT)
// Hoặc tải về file tạm rồi phát
OkHttpClient client = new OkHttpClient();
Request request = new Request.Builder()
    .url(currentAudioUrl)
    .addHeader("Authorization", "Bearer " + jwtToken)
    .build();

client.newCall(request).enqueue(new okhttp3.Callback() {
    @Override
    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
        // Lưu response.body().bytes() vào file tạm → phát bằng MediaPlayer
        File tempFile = File.createTempFile("speaking_audio", ".wav", getCacheDir());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(response.body().bytes());
        }
        // Phát audio
        MediaPlayer player = new MediaPlayer();
        player.setDataSource(tempFile.getAbsolutePath());
        player.prepare();
        player.start();
    }
    @Override
    public void onFailure(okhttp3.Call call, IOException e) { }
});
```
