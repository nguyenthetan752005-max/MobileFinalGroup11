package hcmute.edu.vn.nguyenthetan.data.local.seed;

import java.util.Arrays;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.core.AppDefaults;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.RecommendationEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;

final class CatalogSeedFactory {

    private CatalogSeedFactory() {
    }

    static List<CategoryEntity> createCategories() {
        return Arrays.asList(
                new CategoryEntity(101L, AppDefaults.DEFAULT_CATEGORY_SLUG, "Daily Conversations", "", "Beginner - Intermediate", "AUDIO", "Listening", 24, "Short, practical conversations for everyday listening and speaking practice.", 1),
                new CategoryEntity(102L, "business", "Business English", "", "Intermediate", "AUDIO", "Listening", 16, "Meetings, workplace chats, and office scenarios.", 2),
                new CategoryEntity(103L, "ted", "TED Talks", "", "Intermediate - Advanced", "VIDEO", "Listening", 10, "Real-world talks for longer listening practice.", 3),
                new CategoryEntity(104L, "travel", "Travel Phrases", "", "Beginner", "AUDIO", "Speaking", 18, "Useful spoken English for common travel situations.", 4),
                new CategoryEntity(105L, "ielts", "IELTS Speaking", "", "Advanced", "AUDIO", "Speaking", 14, "Targeted sentence practice for IELTS responses.", 5),
                new CategoryEntity(106L, "movies", "Movie Clips", "", "Intermediate", "VIDEO", "Listening", 11, "Short scenes to practice natural speed listening.", 6)
        );
    }

    static List<SectionEntity> createSections() {
        return Arrays.asList(
                new SectionEntity(201L, 101L, "Section 1: Greetings", "Warm-up conversations and common greetings.", 1),
                new SectionEntity(202L, 101L, "Section 2: At the Restaurant", "Practical restaurant dialogs and quick replies.", 2),
                new SectionEntity(203L, 102L, "Section 1: Meetings", "Short workplace listening drills.", 1),
                new SectionEntity(204L, 103L, "Section 1: Talks", "Curated TED-style excerpts.", 1),
                new SectionEntity(205L, 104L, "Section 1: Airports", "Useful travel speaking prompts.", 1),
                new SectionEntity(206L, 105L, "Section 1: Fluency", "Advanced speaking sentence drills.", 1),
                new SectionEntity(207L, 106L, "Section 1: Everyday Scenes", "Movie clip listening prompts.", 1)
        );
    }

    static List<LessonEntity> createLessons() {
        return Arrays.asList(
                new LessonEntity(1001L, 201L, "Meeting New Friends", "Beginner", "Audio", "AUDIO", 6, 70, null, 1),
                new LessonEntity(1002L, 201L, "Small Talk at School", "Beginner", "Audio", "AUDIO", 6, 70, null, 2),
                new LessonEntity(1003L, 201L, "Morning Routine Chat", "Beginner", "Video", "VIDEO", 5, 70, "video-morning-routine", 3),
                new LessonEntity(1004L, 202L, "Ordering Coffee", "Beginner", "Audio", "AUDIO", 6, 70, null, 1),
                new LessonEntity(AppDefaults.DEFAULT_LESSON_ID, 202L, "Daily English Conversations - Lesson 5", "Beginner", "Audio", "AUDIO", 6, 70, null, 2),
                new LessonEntity(1006L, 202L, "Paying the Bill", "Intermediate", "Video", "VIDEO", 7, 70, "video-paying-bill", 3),
                new LessonEntity(1101L, 203L, "Office Catch-up", "Intermediate", "Audio", "AUDIO", 5, 70, null, 1),
                new LessonEntity(1201L, 204L, "Confidence in Public Speaking", "B2", "Video", "VIDEO", 5, 70, "ted-confidence", 1),
                new LessonEntity(1301L, 205L, "Checking in at the Airport", "Beginner", "Audio", "AUDIO", 5, 70, null, 1),
                new LessonEntity(1401L, 206L, "Handling Follow-up Questions", "Advanced", "Audio", "AUDIO", 5, 75, null, 1),
                new LessonEntity(1501L, 207L, "Cafe Scene Breakdown", "Intermediate", "Video", "VIDEO", 4, 70, "movie-cafe-scene", 1)
        );
    }

    static List<SentenceEntity> createSentences() {
        return Arrays.asList(
                new SentenceEntity(5001L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/1.mp3", "Good morning, how are you today", "", 5000L, null, null, 1),
                new SentenceEntity(5002L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/2.mp3", "I like listening to Michael Jackson songs", "Michael Jackson", 6000L, null, null, 2),
                new SentenceEntity(5003L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/3.mp3", "Could you tell me where the station is", "", 5500L, null, null, 3),
                new SentenceEntity(5004L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/4.mp3", "This restaurant opens at half past seven", "", 6500L, null, null, 4),
                new SentenceEntity(5005L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/5.mp3", "We should review the transcript one more time", "", 6200L, null, null, 5),
                new SentenceEntity(5006L, AppDefaults.DEFAULT_LESSON_ID, "https://res.cloudinary.com/demo/video/upload/tungtung/daily/6.mp3", "Practice every day and your pronunciation will improve", "", 7000L, null, null, 6),
                new SentenceEntity(5101L, 1001L, "", "Nice to meet you, my name is Anna", "Anna", 5200L, null, null, 1),
                new SentenceEntity(5102L, 1001L, "", "I am from Ho Chi Minh City", "Ho Chi Minh City", 5300L, null, null, 2),
                new SentenceEntity(5103L, 1001L, "", "Do you study English every day", "", 4800L, null, null, 3),
                new SentenceEntity(5104L, 1001L, "", "I usually listen to short stories", "", 5400L, null, null, 4),
                new SentenceEntity(5105L, 1001L, "", "Speaking slowly helps me hear each sound", "", 6000L, null, null, 5),
                new SentenceEntity(5106L, 1001L, "", "See you again tomorrow morning", "", 4700L, null, null, 6),
                new SentenceEntity(5201L, 1002L, "", "Our classroom is on the third floor", "", 4800L, null, null, 1),
                new SentenceEntity(5202L, 1002L, "", "The history test starts after lunch", "", 5000L, null, null, 2),
                new SentenceEntity(5203L, 1002L, "", "Can you help me with this homework", "", 5100L, null, null, 3),
                new SentenceEntity(5204L, 1002L, "", "We have football practice this afternoon", "", 5300L, null, null, 4),
                new SentenceEntity(5205L, 1002L, "", "My friend forgot her notebook again", "", 4900L, null, null, 5),
                new SentenceEntity(5206L, 1002L, "", "Let us review the lesson before class ends", "", 5600L, null, null, 6),
                new SentenceEntity(5301L, 1003L, "", "I wake up at six every morning", "", 4000L, 0.0, 4.0, 1),
                new SentenceEntity(5302L, 1003L, "", "Then I make breakfast for my family", "", 4200L, 4.2, 8.4, 2),
                new SentenceEntity(5303L, 1003L, "", "After that I walk to the bus stop", "", 4300L, 8.5, 12.8, 3),
                new SentenceEntity(5304L, 1003L, "", "The ride to school takes twenty minutes", "", 4400L, 12.9, 17.3, 4),
                new SentenceEntity(5305L, 1003L, "", "I always listen to music on the way", "", 4600L, 17.4, 22.0, 5),
                new SentenceEntity(5401L, 1004L, "", "Can I get a hot latte please", "", 4300L, null, null, 1),
                new SentenceEntity(5402L, 1004L, "", "Would you like sugar with that", "", 4100L, null, null, 2),
                new SentenceEntity(5403L, 1004L, "", "Please make it less sweet for me", "", 4700L, null, null, 3),
                new SentenceEntity(5404L, 1004L, "", "Your order will be ready in five minutes", "", 5200L, null, null, 4),
                new SentenceEntity(5405L, 1004L, "", "You can pay by cash or card", "", 4300L, null, null, 5),
                new SentenceEntity(5406L, 1004L, "", "Thank you, have a nice day", "", 3600L, null, null, 6),
                new SentenceEntity(5501L, 1006L, "", "Could we have the check please", "", 3800L, 0.0, 3.8, 1),
                new SentenceEntity(5502L, 1006L, "", "Did you enjoy your meal tonight", "", 4200L, 3.9, 8.1, 2),
                new SentenceEntity(5503L, 1006L, "", "Everything was delicious and fresh", "", 4100L, 8.2, 12.3, 3),
                new SentenceEntity(5504L, 1006L, "", "Would you like to leave a tip", "", 3900L, 12.4, 16.3, 4),
                new SentenceEntity(5505L, 1006L, "", "Please come back again next week", "", 4400L, 16.4, 20.8, 5),
                new SentenceEntity(5506L, 1006L, "", "Our restaurant closes at ten oclock", "", 4300L, 20.9, 25.2, 6),
                new SentenceEntity(5507L, 1006L, "", "I will call a taxi for you", "", 4200L, 25.3, 29.5, 7),
                new SentenceEntity(5601L, 1101L, "", "The client meeting starts at nine sharp", "", 4200L, null, null, 1),
                new SentenceEntity(5602L, 1101L, "", "Please send me the latest sales report", "", 4500L, null, null, 2),
                new SentenceEntity(5603L, 1101L, "", "We should confirm the agenda before lunch", "", 4700L, null, null, 3),
                new SentenceEntity(5604L, 1101L, "", "I will follow up with the design team", "", 4900L, null, null, 4),
                new SentenceEntity(5605L, 1101L, "", "Let us close the meeting with next steps", "", 5200L, null, null, 5),
                new SentenceEntity(5701L, 1201L, "", "Confidence grows when practice becomes a habit", "", 4300L, 0.0, 4.3, 1),
                new SentenceEntity(5702L, 1201L, "", "A small audience can still teach you a lot", "", 4400L, 4.4, 8.8, 2),
                new SentenceEntity(5703L, 1201L, "", "Your voice changes when your ideas feel clear", "", 4500L, 8.9, 13.4, 3),
                new SentenceEntity(5704L, 1201L, "", "People remember stories more than numbers", "", 4200L, 13.5, 17.7, 4),
                new SentenceEntity(5705L, 1201L, "", "The best speakers pause without losing energy", "", 4600L, 17.8, 22.4, 5),
                new SentenceEntity(5801L, 1301L, "", "I would like to check in for my flight", "", 4300L, null, null, 1),
                new SentenceEntity(5802L, 1301L, "", "Do you have any bags to drop off today", "", 4500L, null, null, 2),
                new SentenceEntity(5803L, 1301L, "", "Your gate number has just changed", "", 4200L, null, null, 3),
                new SentenceEntity(5804L, 1301L, "", "Please keep your passport ready", "", 3900L, null, null, 4),
                new SentenceEntity(5805L, 1301L, "", "Boarding will begin in twenty minutes", "", 4100L, null, null, 5),
                new SentenceEntity(5901L, 1401L, "", "Could you explain your main point again", "", 4500L, null, null, 1),
                new SentenceEntity(5902L, 1401L, "", "I agree, but I would add one detail", "", 4600L, null, null, 2),
                new SentenceEntity(5903L, 1401L, "", "That example clearly supports my argument", "", 4400L, null, null, 3),
                new SentenceEntity(5904L, 1401L, "", "The first reason is related to daily habits", "", 4700L, null, null, 4),
                new SentenceEntity(5905L, 1401L, "", "In conclusion, this choice is more practical", "", 5000L, null, null, 5),
                new SentenceEntity(6001L, 1501L, "", "The waiter forgot to bring the menu", "", 3800L, 0.0, 3.8, 1),
                new SentenceEntity(6002L, 1501L, "", "She ordered tea but received coffee instead", "", 4200L, 3.9, 8.1, 2),
                new SentenceEntity(6003L, 1501L, "", "The background music makes the dialog harder", "", 4500L, 8.2, 12.7, 3),
                new SentenceEntity(6004L, 1501L, "", "Listen for the final consonants at the end", "", 4300L, 12.8, 17.1, 4)
        );
    }

    static List<RecommendationEntity> createRecommendations() {
        return Arrays.asList(
                new RecommendationEntity(1L, "Business English", "B1", 24, "Listening", 1),
                new RecommendationEntity(2L, "Travel Phrases", "A2", 18, "Speaking", 2),
                new RecommendationEntity(3L, "TED Talks", "B2", 12, "Listening", 3)
        );
    }
}
