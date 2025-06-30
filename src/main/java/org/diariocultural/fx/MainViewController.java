package org.diariocultural.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import org.diariocultural.*;
import org.diariocultural.fx.MainApp;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controlador da "moldura" principal da aplicação (MainView.fxml).
 * Sua principal responsabilidade é gerenciar a navegação e a troca de temas.
 */
public class MainViewController implements Initializable {

    @FXML private BorderPane contentArea;
    @FXML private Button libraryButton;
    @FXML private Button addBookButton;
    @FXML private Button addMovieButton;
    @FXML private Button addSeriesButton;
    @FXML private ToggleButton contrastButton;
    @FXML private Button helpButton;

    private MainApp mainApp;
    private LibraryService libraryService;
    private List<Button> navButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        navButtons = Arrays.asList(libraryButton, addBookButton, addMovieButton, addSeriesButton);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        showLibraryView();
    }

    // --- MÉTODOS DE NAVEGAÇÃO ---

    @FXML
    public void showLibraryView() {
        setActiveNavButton(libraryButton);
        loadPage("/view/LibraryView.fxml", null);
    }

    @FXML
    private void showBookFormView() {
        setActiveNavButton(addBookButton);
        loadPage("/view/BookFormView.fxml", null);
    }

    @FXML
    private void showMovieFormView() {
        setActiveNavButton(addMovieButton);
        loadPage("/view/MovieFormView.fxml", null);
    }

    @FXML
    private void showSeriesFormView() {
        setActiveNavButton(addSeriesButton);
        loadPage("/view/SeriesFormView.fxml", null);
    }

    // --- MÉTODO DE AJUDA ---

    @FXML
    private void onHelpButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HelpView.fxml"));
            Parent helpContent = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(contentArea.getScene().getWindow());
            dialog.setTitle("Guia de Uso - Diário Cultural");

            dialog.getDialogPane().setContent(helpContent);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setHeaderText(null);

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS DE EDIÇÃO ---

    public void showBookFormForEdit(Book book) {
        setActiveNavButton(addBookButton);
        loadPage("/view/BookFormView.fxml", book);
    }

    public void showMovieFormForEdit(Movie movie) {
        setActiveNavButton(addMovieButton);
        loadPage("/view/MovieFormView.fxml", movie);
    }

    public void showSeriesFormForEdit(Series series) {
        setActiveNavButton(addSeriesButton);
        loadPage("/view/SeriesFormView.fxml", series);
    }

    // --- LÓGICA DE ACESSIBILIDADE ---
    @FXML
    private void onToggleContrastMode() {
        if (mainApp == null) return;

        // 1. Pede à MainApp para adicionar ou remover o CSS da cena principal
        mainApp.toggleHighContrast(contrastButton.isSelected());

        // 2. Pega a página que está atualmente no centro do BorderPane
        Node currentPage = contentArea.getCenter();

        // 3. Re-aplica a lista de estilos (agora atualizada) a essa página
        if (currentPage != null && contentArea.getScene() != null) {
            currentPage.getStyleClass().clear();
            currentPage.getStyleClass().addAll(contentArea.getScene().getStylesheets());
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void setActiveNavButton(Button activeButton) {
        if (navButtons == null) return;
        for (Button button : navButtons) {
            if (button != null) {
                button.getStyleClass().remove("active-nav-button");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("active-nav-button");
        }
    }

    private void loadPage(String fxmlPath, Media mediaToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            if (contentArea.getScene() != null) {
                page.getStylesheets().addAll(contentArea.getScene().getStylesheets());
            }

            Object controller = loader.getController();

            if (controller instanceof LibraryViewController) {
                ((LibraryViewController) controller).setLibraryService(this.libraryService);
                ((LibraryViewController) controller).setMainViewController(this);
            } else if (controller instanceof BookFormController) {
                ((BookFormController) controller).setLibraryService(this.libraryService);
                ((BookFormController) controller).setMainViewController(this);
                if (mediaToEdit instanceof Book) ((BookFormController) controller).loadBookForEditing((Book) mediaToEdit);
            } else if (controller instanceof MovieFormController) {
                ((MovieFormController) controller).setLibraryService(this.libraryService);
                ((MovieFormController) controller).setMainViewController(this);
                if (mediaToEdit instanceof Movie) ((MovieFormController) controller).loadMovieForEditing((Movie) mediaToEdit);
            } else if (controller instanceof SeriesFormController) {
                ((SeriesFormController) controller).setLibraryService(this.libraryService);
                ((SeriesFormController) controller).setMainViewController(this);
                if (mediaToEdit instanceof Series) ((SeriesFormController) controller).loadSeriesForEditing((Series) mediaToEdit);
            }

            contentArea.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}