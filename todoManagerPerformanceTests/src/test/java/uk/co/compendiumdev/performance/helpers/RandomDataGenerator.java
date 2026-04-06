package uk.co.compendiumdev.performance.helpers;

import java.util.Random;
import java.util.UUID;

public class RandomDataGenerator {

    private static final Random RANDOM = new Random();

    private static final String[] TITLE_ADJECTIVES = {
        "urgent", "important", "optional", "critical", "pending",
        "scheduled", "overdue", "new", "recurring", "delegated"
    };

    private static final String[] TITLE_NOUNS = {
        "meeting", "report", "review", "email", "call",
        "update", "task", "followup", "presentation", "deadline"
    };

    public static Payloads.TodoPayload randomTodo() {
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title = randomTitle();
        todo.description = "Generated todo: " + UUID.randomUUID().toString().substring(0, 8);
        todo.doneStatus = RANDOM.nextBoolean();
        return todo;
    }

    public static Payloads.TodoPayload randomUpdatedTodo(String existingTitle) {
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title = existingTitle + " (updated)";
        todo.description = "Updated desc: " + UUID.randomUUID().toString().substring(0, 8);
        todo.doneStatus = RANDOM.nextBoolean();
        return todo;
    }

    private static String randomTitle() {
        String adj = TITLE_ADJECTIVES[RANDOM.nextInt(TITLE_ADJECTIVES.length)];
        String noun = TITLE_NOUNS[RANDOM.nextInt(TITLE_NOUNS.length)];
        return adj + " " + noun + " " + UUID.randomUUID().toString().substring(0, 4);
    }
}
