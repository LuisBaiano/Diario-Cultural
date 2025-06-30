package org.diariocultural.fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.diariocultural.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SeriesFormController implements Initializable {

    private LibraryService libraryService;
    private MainViewController mainViewController;
    private Series seriesToEdit = null;
    private final ObservableList<Season> seasonsData = FXCollections.observableArrayList();

    // --- CAMPOS DA SÉRIE ---
    @FXML private TextField titleField;
    @FXML private TextField originalTitleField;
    @FXML private TextField creatorField;
    @FXML private TextField releaseYearField;
    @FXML private TextField endYearField;
    @FXML private TextField genreField;
    @FXML private TextArea castArea;
    @FXML private TextField whereToWatchField;
    @FXML private CheckBox watchedStatusCheckBox;
    @FXML private Button saveButton;

    // --- COMPONENTES DAS TEMPORADAS ---
    @FXML private TableView<Season> seasonsTableView;
    @FXML private TableColumn<Season, Integer> seasonNumberColumn;
    @FXML private TableColumn<Season, Integer> seasonYearColumn;
    @FXML private TableColumn<Season, Integer> episodesColumn;
    @FXML private TableColumn<Season, String> ratingColumn;
    @FXML private TableColumn<Season, String> seasonCastColumn;
    @FXML private TableColumn<Season, String> seasonCommentColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        seasonNumberColumn.setCellValueFactory(new PropertyValueFactory<>("seasonNumber"));
        seasonYearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        episodesColumn.setCellValueFactory(new PropertyValueFactory<>("episodes"));
        ratingColumn.setCellValueFactory(cellData -> {
            Season season = cellData.getValue();
            double avgRating = (season.getReviewInfo() != null) ? season.getReviewInfo().getAverageRating() : 0.0;
            return new SimpleStringProperty(avgRating > 0 ? String.format("%.1f ★", avgRating) : "-");

        });

        seasonCastColumn.setCellValueFactory(cellData -> {
            Season season = cellData.getValue();
            String castText = "-";
            if (season.getCast() != null && !season.getCast().isEmpty()) {
                castText = String.join(", ", season.getCast());
            }
            return new SimpleStringProperty(castText);
        });

        seasonCommentColumn.setCellValueFactory(cellData -> {
            Season season = cellData.getValue();
            String commentText = "-";
            if (season.getReviewInfo() != null && !season.getReviewInfo().getReviews().isEmpty()) {
                commentText = season.getReviewInfo().getReviews().get(season.getReviewInfo().getReviews().size() - 1).comment();
            }
            return new SimpleStringProperty(commentText);
        });
        seasonsTableView.setItems(seasonsData);
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    public void loadSeriesForEditing(Series series) {
        this.seriesToEdit = series;
        saveButton.setText("Salvar Alterações");

        titleField.setText(series.getTitle());
        originalTitleField.setText(series.getOriginalTitle());
        creatorField.setText(series.getCreator());
        releaseYearField.setText(String.valueOf(series.getReleaseYear()));
        endYearField.setText(String.valueOf(series.getEndYear()));
        watchedStatusCheckBox.setSelected(series.isWatchedStatus());
        if (series.getGenre() != null) genreField.setText(String.join(", ", series.getGenre()));
        if (series.getCast() != null) castArea.setText(String.join(", ", series.getCast()));
        if (series.getWhereToWatch() != null) whereToWatchField.setText(String.join(", ", series.getWhereToWatch()));

        seasonsData.clear();
        if (series.getSeasons() != null) seasonsData.addAll(series.getSeasons());
    }

    @FXML
    private void onSaveButtonClick() {
        try {
            String creator = creatorField.getText();
            String title = titleField.getText();
            int releaseYear = Integer.parseInt(releaseYearField.getText());
            int endYear = endYearField.getText().isBlank() ? 0 : Integer.parseInt(endYearField.getText());
            List<String> genres = parseList(genreField.getText());
            List<String> cast = parseList(castArea.getText());
            List<String> whereToWatch = parseList(whereToWatchField.getText());
            boolean watchedStatus = watchedStatusCheckBox.isSelected();
            List<Season> currentSeasons = new ArrayList<>(seasonsData);

            if (seriesToEdit == null) {
                // MODO CRIAÇÃO
                Series newSeries = new Series(title, originalTitleField.getText(), creator, genres, releaseYear, endYear, whereToWatch, cast, watchedStatus);
                newSeries.setSeasons(currentSeasons);
                libraryService.getSeriesController().addSeriesViaObject(newSeries);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Série adicionada!");
            } else {
                // MODO EDIÇÃO
                seriesToEdit.setTitle(title);
                seriesToEdit.setOriginalTitle(originalTitleField.getText());
                seriesToEdit.setCreator(creator);
                seriesToEdit.setReleaseYear(releaseYear);
                seriesToEdit.setEndYear(endYear);
                seriesToEdit.setGenre(genres);
                seriesToEdit.setCast(cast);
                seriesToEdit.setWhereToWatch(whereToWatch);
                seriesToEdit.setWatchedStatus(watchedStatus);
                seriesToEdit.setSeasons(currentSeasons);
                libraryService.getSeriesController().updateSeries(seriesToEdit);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Série atualizada!");
            }
            clearFormAndState();
            if (mainViewController != null) {
                mainViewController.showLibraryView();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Os campos de ano devem ser números.");
        }
    }

    @FXML
    private void onAddSeason() {
        Dialog<Season> dialog = createSeasonDialog(null); // Cria um diálogo para uma nova temporada
        Optional<Season> result = dialog.showAndWait();
        result.ifPresent(seasonsData::add);
    }

    // Você pode conectar este método a um duplo clique na tabela ou a um botão "Editar Temporada"
    @FXML
    private void onEditSeason() {
        Season selectedSeason = seasonsTableView.getSelectionModel().getSelectedItem();
        if (selectedSeason == null) {
            showAlert(Alert.AlertType.WARNING, "Seleção Vazia", "Selecione uma temporada para editar.");
            return;
        }
        Dialog<Season> dialog = createSeasonDialog(selectedSeason); // Cria um diálogo preenchido com os dados
        Optional<Season> result = dialog.showAndWait();
        result.ifPresent(editedSeason -> {
            // A atualização do objeto na lista é um pouco mais complexa,
            // mas para o caso de um record, precisamos substituir o antigo pelo novo
            int index = seasonsData.indexOf(selectedSeason);
            if (index != -1) {
                seasonsData.set(index, editedSeason);
            }
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Serie Atualizado!");
        });
    }

    @FXML
    private void onRemoveSeason() {
        Season selectedSeason = seasonsTableView.getSelectionModel().getSelectedItem();
        if (selectedSeason != null) {
            seasonsData.remove(selectedSeason);
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Série Atualizado!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhuma Temporada", "Selecione uma temporada para remover.");
        }
    }

    private Dialog<Season> createSeasonDialog(Season seasonToEdit) {
        Dialog<Season> dialog = new Dialog<>();
        dialog.setTitle(seasonToEdit == null ? "Adicionar Nova Temporada" : "Editar Temporada");
        dialog.setHeaderText("Preencha os dados da temporada.");

        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        if (saveButton.getScene() != null) {
            dialog.getDialogPane().getStylesheets().addAll(saveButton.getScene().getStylesheets());
        }

        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField seasonNumberField = new TextField();
        seasonNumberField.setPromptText("Número");
        TextField yearField = new TextField();
        yearField.setPromptText("Ano de Lançamento");
        TextField episodesField = new TextField();
        episodesField.setPromptText("Qtd. de Episódios");

        // --- LÓGICA DE HABILITAR/DESABILITAR DENTRO DO DIÁLOGO ---
        CheckBox seasonWatchedCheckBox = new CheckBox("Já assisti esta temporada");
        TextField ratingField = new TextField();
        ratingField.setPromptText("Nota (0-5)");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Comentário");

        // Começam desabilitados
        ratingField.setDisable(true);
        commentArea.setDisable(true);

        // Listener para o CheckBox do diálogo
        seasonWatchedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ratingField.setDisable(!newVal);
            commentArea.setDisable(!newVal);
            if (!newVal) {
                ratingField.clear();
                commentArea.clear();
            }
        });

        grid.add(new Label("Temporada Nº:"), 0, 0); grid.add(seasonNumberField, 1, 0);
        grid.add(new Label("Ano:"), 0, 1); grid.add(yearField, 1, 1);
        grid.add(new Label("Episódios:"), 0, 2); grid.add(episodesField, 1, 2);
        grid.add(seasonWatchedCheckBox, 1, 3); // CheckBox adicionado
        grid.add(new Label("Nota (0-5):"), 0, 4); grid.add(ratingField, 1, 4);
        grid.add(new Label("Comentário:"), 0, 5); grid.add(commentArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Preenche os dados se estiver no modo de edição
        if (seasonToEdit != null) {
            seasonNumberField.setText(String.valueOf(seasonToEdit.getSeasonNumber()));
            yearField.setText(String.valueOf(seasonToEdit.getReleaseYear()));
            episodesField.setText(String.valueOf(seasonToEdit.getEpisodes()));
            if(seasonToEdit.getReviewInfo() != null && seasonToEdit.getReviewInfo().getReviewCount() > 0) {
                seasonWatchedCheckBox.setSelected(true); // Se tem review, foi assistida
                Review review = seasonToEdit.getReviewInfo().getReviews().get(0);
                ratingField.setText(String.valueOf(review.rating()));
                commentArea.setText(review.comment());
            }
        } else {
            seasonNumberField.setText(String.valueOf(seasonsData.size() + 1));
        }

        // Converte o resultado para um objeto Season
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // ... (cria o objeto Season como antes, mas agora usando ratingField e commentArea)
                    ReviewInfo reviewInfo = new ReviewInfo();
                    if (seasonWatchedCheckBox.isSelected()) {
                        int rating = ratingField.getText().isBlank() ? 0 : Integer.parseInt(ratingField.getText());
                        reviewInfo.evaluate(rating, commentArea.getText());
                    }
                    return new Season(
                            Integer.parseInt(seasonNumberField.getText()),
                            Integer.parseInt(episodesField.getText()),
                            Integer.parseInt(yearField.getText()),
                            new ArrayList<>(), // Elenco adicional pode ser adicionado aqui
                            reviewInfo
                    );
                } catch (NumberFormatException e) { return null; }
            }
            return null;
        });

        return dialog;
    }

    private void clearFormAndState() {
        titleField.clear(); originalTitleField.clear(); releaseYearField.clear();
        endYearField.clear(); genreField.clear(); castArea.clear(); whereToWatchField.clear();
        watchedStatusCheckBox.setSelected(false);
        creatorField.clear();
        seasonsData.clear();
        seriesToEdit = null;
        saveButton.setText("Adicionar Série");
    }

    private List<String> parseList(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}