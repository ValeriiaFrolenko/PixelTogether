package frolenko.client.controller.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.dto.work.GalleryItem;
import frolenko.client.config.AppView;
import frolenko.client.core.AppState;
import frolenko.client.core.RoomState;
import frolenko.client.core.ViewManager;
import frolenko.client.service.GalleryService;
import frolenko.client.util.AlertHelper;
import frolenko.client.util.Debouncer;
import frolenko.client.util.UiUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

@Singleton
public class GalleryTabController {

    private final GalleryService galleryService;
    private final AppState appState;
    private final ViewManager viewManager;
    private final Debouncer debouncer = new Debouncer(400);

    @FXML private ListView<GalleryItem> galleryListView;
    @FXML private TextField titleFilter;
    @FXML private TextField authorFilter;
    @FXML private Button viewButton;
    @FXML private Button refreshButton;

    @Inject
    public GalleryTabController(GalleryService galleryService, AppState appState, ViewManager viewManager) {
        this.galleryService = galleryService;
        this.appState = appState;
        this.viewManager = viewManager;
    }

    @FXML
    public void initialize() {
        galleryListView.setItems(appState.getGallery());
        galleryListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GalleryItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.title() + " by " + item.ownerUsername()
                        + " (" + item.canvasW() + "x" + item.canvasH() + ")");
            }
        });
        onFilterChanged();
    }

    @FXML
    public void onFilterChanged() {
        debouncer.debounce(() -> {
            String title = titleFilter.getText().trim();
            String author = authorFilter.getText().trim();
            galleryService.getGallery(
                    title.isEmpty() ? null : title,
                    author.isEmpty() ? null : author,
                    items -> Platform.runLater(() -> appState.getGallery().setAll(items)),
                    error -> Platform.runLater(() -> AlertHelper.showError(error))
            );
        });
    }

    @FXML
    public void onRefresh() {
        Runnable unblock = UiUtil.withLoading(refreshButton);
        String title = titleFilter.getText().trim();
        String author = authorFilter.getText().trim();
        galleryService.getGallery(
                title.isEmpty() ? null : title,
                author.isEmpty() ? null : author,
                items -> Platform.runLater(() -> {
                    unblock.run();
                    appState.getGallery().setAll(items);
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }

    @FXML
    public void onViewWork() {
        GalleryItem selected = galleryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a work first.");
            return;
        }
        Runnable unblock = UiUtil.withLoading(viewButton);
        galleryService.getWork(selected.id(),
                work -> Platform.runLater(() -> {
                    unblock.run();
                    viewManager.navigateTo(AppView.WORK_VIEW);
                    appState.setCurrentRoom(new RoomState(-1, work.canvasW(), work.canvasH(), work.pixels(), false));
                }),
                error -> Platform.runLater(() -> {
                    unblock.run();
                    AlertHelper.showError(error);
                })
        );
    }
}