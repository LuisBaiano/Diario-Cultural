package org.diariocultural.fx; // <-- PACOTE CORRIGIDO

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image; // Faltava este import
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.diariocultural.*;

import java.net.URL;
import java.time.LocalDate; // Faltava este import
import java.time.ZoneId;    // Faltava este import
import java.util.ResourceBundle;

/**
 * Controlador para a janela de diálogo que exibe os detalhes de uma mídia (MediaDetailView.fxml).
 */
public class MediaDetailViewController implements Initializable {

    // --- DEPENDÊNCIAS ---
    private LibraryService libraryService;
    private MainViewController mainViewController;
    private Media currentMedia;
    private Runnable refreshCallback;

    // --- COMPONENTES DA UI (@FXML) ---
    @FXML private Label titleLabel, originalTitleLabel, typeLabel, yearLabel, genresLabel, castLabel;
    @FXML private Label creatorTitleLabel, endYearTitleLabel, durationTitleLabel, isbnTitleLabel, castTitleLabel, seasonsTitleLabel, genresTitleLabel;
    @FXML private Label creatorNameLabel, endYearLabel, durationLabel, isbnLabel;
    @FXML private Label seriesRatingTitleLabel;
    @FXML private Label seriesRatingLabel;
    @FXML private VBox lastReviewBox;
    @FXML private Label lastReviewRatingLabel;
    @FXML private Label lastReviewCommentLabel;
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
            return new SimpleStringProperty((avgRating > 0) ? String.format("%.1f ★", avgRating) : "-");
        });
        seasonCastColumn.setCellValueFactory(cellData -> {
            Season season = cellData.getValue();
            if (season.getCast() != null && !season.getCast().isEmpty()) {
                return new SimpleStringProperty(String.join(", ", season.getCast()));
            }
            return new SimpleStringProperty("-");
        });
        seasonCommentColumn.setCellValueFactory(cellData -> {
            Season season = cellData.getValue();
            if (season.getReviewInfo() != null && !season.getReviewInfo().getReviews().isEmpty()) {
                Review lastReview = season.getReviewInfo().getReviews().get(season.getReviewInfo().getReviews().size() - 1);
                return new SimpleStringProperty(lastReview.comment());
            }
            return new SimpleStringProperty("-");
        });
    }

    public void setup(Media media, LibraryService libraryService, MainViewController mainViewController, Runnable refreshCallback) {
        this.currentMedia = media;
        this.libraryService = libraryService;
        this.mainViewController = mainViewController;
        this.refreshCallback = refreshCallback;
        displayMediaDetails();
    }

    private void displayMediaDetails() {
        hideAllOptionalNodes();

        titleLabel.setText(currentMedia.getTitle());
        yearLabel.setText(String.valueOf(currentMedia.getReleaseYear()));

        if (currentMedia.getGenre() != null && !currentMedia.getGenre().isEmpty()) {
            showNodes(genresTitleLabel, genresLabel);
            genresLabel.setText(String.join(", ", currentMedia.getGenre()));
        }

        ReviewInfo reviewInfo = null;
        if (currentMedia instanceof Book) reviewInfo = ((Book) currentMedia).getReviewInfo();
        else if (currentMedia instanceof Movie) reviewInfo = ((Movie) currentMedia).getReviewInfo();

        if (reviewInfo != null && reviewInfo.getReviewCount() > 0) {
            showNodes(lastReviewBox);
            Review lastReview = reviewInfo.getReviews().get(reviewInfo.getReviews().size() - 1);
            lastReviewRatingLabel.setText(String.format("Nota: %.1f ★", (double) lastReview.rating()));
            lastReviewCommentLabel.setText("\"" + lastReview.comment() + "\"");
        }

        if (currentMedia instanceof Book book) {
            typeLabel.setText("Livro");
            originalTitleLabel.setText(book.getOriginalTitle());
            showNodes(creatorTitleLabel, creatorNameLabel, isbnTitleLabel, isbnLabel);
            creatorTitleLabel.setText("Autor:");
            creatorNameLabel.setText(book.getAuthor());
            isbnLabel.setText(book.getISBN());
        } else if (currentMedia instanceof Movie movie) {
            typeLabel.setText("Filme");
            originalTitleLabel.setText(movie.getOriginalTitle());
            showNodes(creatorTitleLabel, creatorNameLabel, durationTitleLabel, durationLabel, castTitleLabel, castLabel);
            creatorTitleLabel.setText("Diretor:");
            creatorNameLabel.setText(movie.getDirector());
            durationLabel.setText(movie.getDuration() + " minutos");
            if (movie.getCast() != null) castLabel.setText(String.join(", ", movie.getCast()));
        } else if (currentMedia instanceof Series series) {
            typeLabel.setText("Série");
            originalTitleLabel.setText(series.getOriginalTitle());

            // --- AQUI ESTÁ A CORREÇÃO ---
            showNodes(creatorTitleLabel, creatorNameLabel, endYearTitleLabel, endYearLabel, castTitleLabel, castLabel, seasonsTitleLabel, seasonsTableView, seriesRatingTitleLabel, seriesRatingLabel);

            creatorTitleLabel.setText("Criador(es):");
            creatorNameLabel.setText(series.getCreator());
            endYearLabel.setText(series.getEndYear() == 0 ? "Em andamento" : String.valueOf(series.getEndYear()));

            double avgRating = series.getAverageRating();
            if (avgRating > 0) {
                seriesRatingLabel.setText(String.format("%.1f ★", avgRating));
            } else {
                seriesRatingLabel.setText("-");
            }
            if (series.getCast() != null) castLabel.setText(String.join(", ", series.getCast()));
            if (series.getSeasons() != null) seasonsTableView.setItems(FXCollections.observableArrayList(series.getSeasons()));
        }
    }

    @FXML
    private void handleEditAction() {
        if (currentMedia == null) return;
        closeDialog();
        if (currentMedia instanceof Book) mainViewController.showBookFormForEdit((Book) currentMedia);
        else if (currentMedia instanceof Movie) mainViewController.showMovieFormForEdit((Movie) currentMedia);
        else if (currentMedia instanceof Series) mainViewController.showSeriesFormForEdit((Series) currentMedia);
    }

    @FXML
    private void handleDeleteAction() {
        if (currentMedia == null) return;
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir: " + currentMedia.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.setHeaderText("Confirmar Exclusão");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (currentMedia instanceof Book) libraryService.getBookController().removeBook((Book) currentMedia);
                else if (currentMedia instanceof Movie) libraryService.getMovieController().removeMovie((Movie) currentMedia);
                else if (currentMedia instanceof Series) libraryService.getSeriesController().removeSeries((Series) currentMedia);
                if (refreshCallback != null) refreshCallback.run();
                closeDialog();
            }
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    private void hideAllOptionalNodes() {
        setNodesVisible(false, creatorTitleLabel, creatorNameLabel, endYearTitleLabel, endYearLabel,
                durationTitleLabel, durationLabel, isbnTitleLabel, isbnLabel, castTitleLabel, castLabel,
                seasonsTitleLabel, seasonsTableView, lastReviewBox, genresTitleLabel, genresLabel,
                seriesRatingTitleLabel, seriesRatingLabel); // A lista aqui já estava correta
    }

    private void showNodes(Node... nodes) { setNodesVisible(true, nodes); }

    private void setNodesVisible(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            if(node != null) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }
}