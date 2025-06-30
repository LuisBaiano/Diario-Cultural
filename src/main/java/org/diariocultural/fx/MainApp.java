package org.diariocultural.fx;

import javafx.collections.ObservableList;
import org.diariocultural.BookController;
import org.diariocultural.MovieController;
import org.diariocultural.SeriesController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private final String defaultCss = Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm();
    private final String highContrastCss = Objects.requireNonNull(getClass().getResource("/styles/high-contrast.css")).toExternalForm();
    private static final LibraryService libraryService = new LibraryService(new BookController(), new MovieController(), new SeriesController());
    private Stage primaryStage;
    private Scene mainScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Diário Cultural");
        showWelcomeScreen();
    }

    /**
     * Carrega e exibe a tela inicial de boas-vindas.
     */
    public void showWelcomeScreen() {
        try {
            // Caminho para o FXML
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/WelcomeScreen.fxml")));
            Parent page = loader.load();
            WelcomeScreenController controller = loader.getController();

            // A ação do botão "Acessar" chama o método para mostrar a tela principal
            controller.setOnAccessAction(this::showMainView);

            Scene scene = new Scene(page);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/styles.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega e exibe a interface principal da aplicação.
     */
    public void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/view/MainView.fxml")));
            Parent page = loader.load();

            MainViewController controller = loader.getController();
            controller.setLibraryService(libraryService);

            // Cria a cena e APLICA O CSS PADRÃO
            mainScene = new Scene(page, 1280, 768);
            mainScene.getStylesheets().add(defaultCss);

            // Pega o controller e passa as dependências
            controller.setLibraryService(libraryService);
            controller.setMainApp(this);
            primaryStage.setScene(mainScene);
            primaryStage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleHighContrast(boolean enable) {
        if (mainScene == null) return;

        ObservableList<String> stylesheets = mainScene.getStylesheets();
        if (enable) {
            // Adiciona o CSS de alto contraste. Ele sobrescreve o padrão.
            if (!stylesheets.contains(highContrastCss)) {
                stylesheets.add(highContrastCss);
            }
        } else {
            // Remove o CSS de alto contraste, voltando ao padrão.
            stylesheets.remove(highContrastCss);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}