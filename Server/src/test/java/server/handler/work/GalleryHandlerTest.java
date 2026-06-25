package server.handler.work;

import common.dto.work.GalleryItem;
import common.dto.work.GetGalleryRequest;
import common.dto.work.GetWorkRequest;
import common.dto.work.GetWorkResponse;
import common.model.Packet;
import common.protocol.CommandType;
import common.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.database.model.SavedWork;
import server.handler.BaseHandlerTest;

import static org.junit.jupiter.api.Assertions.*;

class GalleryHandlerTest extends BaseHandlerTest {

    private GetGalleryHandler galleryHandler;
    private GetWorkHandler workHandler;

    @BeforeEach
    void setUp() {
        galleryHandler = new GetGalleryHandler(savedWorkDao, dispatcher);
        workHandler = new GetWorkHandler(savedWorkDao, dispatcher);
    }

    // ===== GetGalleryHandler =====

    @Test
    void getGallery_noFilters_returnsAllPublicWorks() {
        int u1 = createUser("alice", "pass", "USER");
        int u2 = createUser("bob", "pass", "USER");
        saveWork(u1, "Public Work 1", true);
        saveWork(u2, "Public Work 2", true);
        saveWork(u1, "Private Work", false);

        Packet request = buildPacket(1L, 1L, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(null, null)));

        galleryHandler.handle(request);

        Packet response = lastSent();
        assertCmdType(response, CommandType.GALLERY);
        assertPktIdEchoed(request, response);

        GalleryItem[] items = JsonUtil.fromBytes(response.bMsg().payload(), GalleryItem[].class);
        assertEquals(2, items.length);
    }

    @Test
    void getGallery_filterByTitle_returnsMatchingWorks() {
        int userId = createUser("alice", "pass", "USER");
        saveWork(userId, "Pixel Art", true);
        saveWork(userId, "Abstract", true);
        saveWork(userId, "Pixel Dream", true);

        Packet request = buildPacket(1L, 2L, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest("Pixel", null)));

        galleryHandler.handle(request);

        GalleryItem[] items = JsonUtil.fromBytes(lastSent().bMsg().payload(), GalleryItem[].class);
        assertEquals(2, items.length);
        for (GalleryItem item : items) {
            assertTrue(item.title().toLowerCase().contains("pixel"));
        }
    }

    @Test
    void getGallery_filterByAuthor_returnsMatchingWorks() {
        int aliceId = createUser("alice", "pass", "USER");
        int bobId = createUser("bob", "pass", "USER");
        saveWork(aliceId, "Alice Work", true);
        saveWork(bobId, "Bob Work", true);

        Packet request = buildPacket(1L, 3L, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(null, "alice")));

        galleryHandler.handle(request);

        GalleryItem[] items = JsonUtil.fromBytes(lastSent().bMsg().payload(), GalleryItem[].class);
        assertEquals(1, items.length);
        assertEquals("Alice Work", items[0].title());
    }

    @Test
    void getGallery_emptyGallery_returnsEmptyList() {
        Packet request = buildPacket(1L, 4L, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(null, null)));

        galleryHandler.handle(request);

        GalleryItem[] items = JsonUtil.fromBytes(lastSent().bMsg().payload(), GalleryItem[].class);
        assertEquals(0, items.length);
    }

    @Test
    void getGallery_privateWorksNotIncluded() {
        int userId = createUser("user", "pass", "USER");
        saveWork(userId, "Private Only", false);

        galleryHandler.handle(buildPacket(1L, 5L, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(null, null))));

        GalleryItem[] items = JsonUtil.fromBytes(lastSent().bMsg().payload(), GalleryItem[].class);
        assertEquals(0, items.length);
    }

    @Test
    void getGallery_pktIdEchoed() {
        long pktId = 88L;
        galleryHandler.handle(buildPacket(1L, pktId, CommandType.GET_GALLERY, 0,
                JsonUtil.toBytes(new GetGalleryRequest(null, null))));

        assertEquals(pktId, lastSent().bPktId());
    }

    // ===== GetWorkHandler =====

    @Test
    void getWork_publicWork_returnsWorkData() {
        int userId = createUser("artist", "pass", "USER");
        int workId = saveWork(userId, "Public Piece", true);

        Packet request = buildPacket(1L, 1L, CommandType.GET_WORK, 0,
                JsonUtil.toBytes(new GetWorkRequest(workId)));

        workHandler.handle(request);

        Packet response = lastSent();
        assertCmdType(response, CommandType.WORK);
        assertPktIdEchoed(request, response);

        GetWorkResponse work = JsonUtil.fromBytes(response.bMsg().payload(), GetWorkResponse.class);
        assertEquals(workId, work.id());
        assertEquals("Public Piece", work.title());
        assertNotNull(work.pixels());
    }

    @Test
    void getWork_privateWork_returnsForbidden() {
        int userId = createUser("artist", "pass", "USER");
        int workId = saveWork(userId, "Private Piece", false);

        workHandler.handle(buildPacket(1L, 2L, CommandType.GET_WORK, 0,
                JsonUtil.toBytes(new GetWorkRequest(workId))));

        assertErrorMessage(lastSent(), "Forbidden");
    }

    @Test
    void getWork_nonExistentWork_returnsError() {
        workHandler.handle(buildPacket(1L, 3L, CommandType.GET_WORK, 0,
                JsonUtil.toBytes(new GetWorkRequest(9999))));

        assertErrorMessage(lastSent(), "Work not found");
    }

    @Test
    void getWork_pktIdEchoed() {
        int userId = createUser("artist", "pass", "USER");
        int workId = saveWork(userId, "Test", true);
        long pktId = 66L;

        workHandler.handle(buildPacket(1L, pktId, CommandType.GET_WORK, 0,
                JsonUtil.toBytes(new GetWorkRequest(workId))));

        assertEquals(pktId, lastSent().bPktId());
    }

    // ---- Хелпер ----

    private int saveWork(int ownerId, String title, boolean isPublic) {
        return (int) savedWorkDao.save(SavedWork.builder()
                .ownerId(ownerId)
                .title(title)
                .isPublic(isPublic)
                .imageData(new byte[16 * 16 * 4])
                .canvasW(16)
                .canvasH(16)
                .build());
    }
}