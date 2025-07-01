package org.diariocultural.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.diariocultural.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para a página do Acervo (LibraryView.fxml).
 * Gerencia a exibição, busca, filtro e ordenação da lista unificada de mídias.
 */
public class LibraryViewController implements Initializable {

    // --- DEPENDÊNCIAS ---
    private LibraryService libraryService;
    private MainViewController mainViewController;

    // --- COMPONENTES DA UI (@FXML) ---
    @FXML private TableView<Media> mediaTableView;
    @FXML private TableColumn<Media, String> typeColumn;
    @FXML private TableColumn<Media, String> titleColumn;
    @FXML private TableColumn<Media, String> creatorColumn;
    @FXML private TableColumn<Media, Integer> yearColumn;
    @FXML private TableColumn<Media, String> ratingColumn;

    // --- Componentes de Filtro/Ordenação ---
    @FXML private TextField searchField;
    @FXML private TextField genreFilterField;
    @FXML private TextField yearFilterField;
    @FXML private ComboBox<String> sortComboBox;

    /**
     * Injeta a dependência do serviço principal. Chamado pelo MainViewController.
     */
    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
        refreshMediaTable(this.libraryService.getAllMedia());
    }

    /**
     * Injeta a dependência do controlador principal para permitir a navegação.
     */
    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    /**
     * Inicializa o controlador após o FXML ser carregado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Configura as colunas da tabela
        setupTableColumns();

        // 2. Configura o evento de duplo-clique na tabela
        mediaTableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Media selectedMedia = mediaTableView.getSelectionModel().getSelectedItem();
                if (selectedMedia != null) {
                    showDetailsDialog(selectedMedia);
                }
            }
        });

        // 3. Preenche as opções do ComboBox de ordenação
        sortComboBox.getItems().addAll("Padrão", "Melhor Avaliados", "Pior Avaliados");
        sortComboBox.setValue("Padrão");
    }



    /**
     * Chamado pelo botão "Aplicar Filtros".
     * Reúne todos os critérios e atualiza a tabela.
     */
    @FXML
    private void onApplyFiltersButtonClick() {
        String textSearch = searchField.getText(); // Pega o texto de busca
        String genre = genreFilterField.getText();
        int year = 0;
        try {
            if (!yearFilterField.getText().isBlank()) {
                year = Integer.parseInt(yearFilterField.getText());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ano Inválido", "Por favor, insira um número válido para o ano.");
            return;
        }

        String sortOrder = sortComboBox.getValue();

        // Chama o novo método do serviço com todos os parâmetros
        List<Media> results = libraryService.getFilteredAndSortedMedia(textSearch, genre, year, sortOrder);
        refreshMediaTable(results);
    }


    /**
     * Chamado pelo botão "Limpar Filtros". Reseta todos os campos e a tabela.
     */
    @FXML
    private void onResetButtonClick() {
        searchField.clear();
        genreFilterField.clear();
        yearFilterField.clear();
        sortComboBox.setValue("Padrão");
        refreshMediaTable(libraryService.getAllMedia());
    }


    /**
     * Abre a janela de detalhes para a mídia selecionada.
     */
    private void showDetailsDialog(Media media) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MediaDetailView.fxml"));
            Parent page = loader.load();

            MediaDetailViewController controller = loader.getController();

            Runnable refreshAction = this::refreshMediaTable;
            controller.setup(media, this.libraryService, this.mainViewController, refreshAction);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mediaTableView.getScene().getWindow());
            dialog.setTitle("Detalhes da Mídia");
            dialog.getDialogPane().setContent(page);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setHeaderText(null);

            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a janela de detalhes.");
        }
    }

    /**
     * Atualiza a TableView com uma nova lista de mídias.
     */
    public void refreshMediaTable() {
        refreshMediaTable(libraryService.getAllMedia());
    }

    private void refreshMediaTable(List<Media> mediaList) {
        if (mediaTableView != null) {
            mediaTableView.setItems(FXCollections.observableArrayList(mediaList));
        }
    }

    /**
     * Configura as fábricas de células para cada coluna da tabela.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));

        typeColumn.setCellValueFactory(cellData -> {
            Media media = cellData.getValue();
            String type = "";
            if (media instanceof Book) type = "Livro";
            else if (media instanceof Movie) type = "Filme";
            else if (media instanceof Series) type = "Série";
            return new SimpleStringProperty(type);
        });

        creatorColumn.setCellValueFactory(cellData -> {
            Media media = cellData.getValue();
            String creator = "N/A";
            if (media instanceof Book) creator = ((Book) media).getAuthor();
            else if (media instanceof Movie) creator = ((Movie) media).getDirector();
            else if (media instanceof Series) creator = ((Series) media).getCreator();
            return new SimpleStringProperty(creator);
        });
        ratingColumn.setCellValueFactory(cellData -> {
            Media media = cellData.getValue();
            double avgRating = media.getAverageRating();
            if (avgRating > 0) {
                // Formata o número para exibir com uma casa decimal e o símbolo de estrela
                return new SimpleStringProperty(String.format("%.1f ★", avgRating));
            }
            return new SimpleStringProperty("-"); // Mostra um traço se não houver nota
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}