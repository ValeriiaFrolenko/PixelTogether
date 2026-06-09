package server.core;

import java.util.Random;

public class NicknameGenerator {

    private static final String[] ANIMALS = {
            "Panda", "Fox", "Turtle", "Zebra", "Llama",
            "Capybara", "Raccoon", "Giraffe", "Penguin", "Octopus"
    };

    private static final Random random = new Random();

    public static String generate() {
        String animal = ANIMALS[random.nextInt(ANIMALS.length)];
        return "Unknown " + animal;
    }
}