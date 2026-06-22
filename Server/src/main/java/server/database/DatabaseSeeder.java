package server.database;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import server.core.ServerModule;
import server.database.dao.SavedWorkDao;
import server.database.dao.UserDao;
import server.database.model.SavedWork;
import server.database.model.User;

import java.nio.ByteBuffer;

public class DatabaseSeeder {

    private final JdbcTemplate jdbc;
    private final UserDao userDao;
    private final SavedWorkDao savedWorkDao;

    @Inject
    public DatabaseSeeder(JdbcTemplate jdbc, UserDao userDao, SavedWorkDao savedWorkDao) {
        this.jdbc = jdbc;
        this.userDao = userDao;
        this.savedWorkDao = savedWorkDao;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ServerModule());
        injector.getInstance(DatabaseInitializer.class).initialize();
        DatabaseSeeder seeder = injector.getInstance(DatabaseSeeder.class);
        seeder.run();
        System.out.println("Database successfully cleared and populated with seed data.");
    }

    public void run() {
        clearDatabase();

        int adminId = createMockUser("admin", "adminpass", "ADMIN");
        int artistId = createMockUser("valeriia", "password123", "USER");
        int designerId = createMockUser("designer", "designpass", "USER");

        createMockWork(adminId, "Blank Canvas", 16, 16, generateWhitePixels(16, 16));
        createMockWork(artistId, "Color Gradient", 32, 32, generateGradientPixels(32, 32));
        createMockWork(designerId, "Pink Aesthetic Grid", 24, 24, generateGridPixels(24, 24));
    }

    private void clearDatabase() {
        jdbc.update("DELETE FROM auth_tokens");
        jdbc.update("DELETE FROM saved_works");
        jdbc.update("DELETE FROM rooms");
        jdbc.update("DELETE FROM users");
    }

    private int createMockUser(String username, String password, String role) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        return (int) userDao.save(User.builder()
                .username(username)
                .password(hashedPassword)
                .role(role)
                .build());
    }

    private void createMockWork(int ownerId, String title, int width, int height, int[] pixels) {
        ByteBuffer buf = ByteBuffer.allocate(pixels.length * 4);
        for (int pixel : pixels) {
            buf.putInt(pixel);
        }
        savedWorkDao.save(SavedWork.builder()
                .ownerId(ownerId)
                .title(title)
                .isPublic(true)
                .imageData(buf.array())
                .canvasW(width)
                .canvasH(height)
                .build());
    }

    private int[] generateWhitePixels(int w, int h) {
        int[] pixels = new int[w * h];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0xFFFFFFFF;
        }
        return pixels;
    }

    private int[] generateGradientPixels(int w, int h) {
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = 0xFF000000 | ((x * 8) << 16) | ((y * 8) << 8);
            }
        }
        return pixels;
    }

    private int[] generateGridPixels(int w, int h) {
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = (x % 4 == 0 || y % 4 == 0) ? 0xFFF06292 : 0xFFFFFFFF;
            }
        }
        return pixels;
    }
}