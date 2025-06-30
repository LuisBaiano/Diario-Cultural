package org.diariocultural.fx; // Pacote corrigido para consistência

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.diariocultural.*;

import java.net.URL;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para a página de formulário de Livro (BookFormView.fxml).
 * Responsável por criar novos livros e editar existentes.
 */
public class BookFormController implements Initializable {

    private LibraryService libraryService;
    private MainViewController mainViewController;
    private Book bookToEdit = null;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField originalTitleField;
    @FXML private TextField publisherField;
    @FXML private TextField isbnField;
    @FXML private TextField genreField;
    @FXML private CheckBox hasCopyCheckBox;
    @FXML private CheckBox readStatusCheckBox;
    @FXML private DatePicker readDatePicker;
    @FXML private TextField ratingField;
    @FXML private TextArea reviewCommentArea;
    @FXML private Button saveButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Começa com os campos de avaliação desabilitados por padrão
        setReviewFieldsDisabled(true);

        // Adiciona um "ouvinte" de eventos à propriedade 'selected' do CheckBox
        readStatusCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // 'newValue' é true se a caixa está marcada, false se desmarcada.
            // Nós desabilitamos os campos se a caixa NÃO estiver marcada (!newValue).
            setReviewFieldsDisabled(!newValue);
        });
    }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    public void loadBookForEditing(Book book) {
        this.bookToEdit = book;
        saveButton.setText("Salvar Alterações");

        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        yearField.setText(String.valueOf(book.getReleaseYear()));
        originalTitleField.setText(book.getOriginalTitle());
        publisherField.setText(book.getPublisher());
        isbnField.setText(book.getISBN());
        if (book.getGenre() != null) {
            genreField.setText(String.join(", ", book.getGenre()));
        }
        hasCopyCheckBox.setSelected(book.hasCopy());
        readStatusCheckBox.setSelected(book.isReadStatus());

        if (book.getReadDate() != null) {
            readDatePicker.setValue(book.getReadDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            readDatePicker.setValue(null);
        }

        if (book.getReviewInfo() != null && !book.getReviewInfo().getReviews().isEmpty()) {
            Review lastReview = book.getReviewInfo().getReviews().get(book.getReviewInfo().getReviews().size() - 1);
            ratingField.setText(String.valueOf(lastReview.rating()));
            reviewCommentArea.setText(lastReview.comment());
        }
        setReviewFieldsDisabled(!book.isReadStatus());
    }

    @FXML
    private void onSaveButtonClick() {
        try {
            if (titleField.getText().isBlank() || authorField.getText().isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Título e Autor são obrigatórios.");
                return;
            }

            if (bookToEdit == null) {
                Book newBook = createBookFromFormData();
                libraryService.getBookController().addBookViaObject(newBook);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Livro Adicionado!");
            } else {
                updateBookFromFormData();
                libraryService.getBookController().updateBook(bookToEdit);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Livro Atualizado!");
            }

            if (mainViewController != null) {
                mainViewController.showLibraryView();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Os campos 'Ano' e 'Nota' devem ser números.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBookFromFormData() throws NumberFormatException {
        bookToEdit.setTitle(titleField.getText());
        bookToEdit.setAuthor(authorField.getText());
        bookToEdit.setReleaseYear(yearField.getText().isBlank() ? 0 : Integer.parseInt(yearField.getText()));
        bookToEdit.setOriginalTitle(originalTitleField.getText().isBlank() ? bookToEdit.getTitle() : originalTitleField.getText());
        bookToEdit.setPublisher(publisherField.getText());
        bookToEdit.setISBN(isbnField.getText());
        bookToEdit.setGenre(parseList(genreField.getText()));
        bookToEdit.setHasCopy(hasCopyCheckBox.isSelected());
        bookToEdit.setReadStatus(readStatusCheckBox.isSelected());

        Date readDate = null;
        if (readStatusCheckBox.isSelected() && readDatePicker.getValue() != null) {
            readDate = Date.from(readDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        bookToEdit.setReadDate(readDate);

        if (readStatusCheckBox.isSelected() && (!ratingField.getText().isBlank() || !reviewCommentArea.getText().isBlank())) {
            int rating = ratingField.getText().isBlank() ? 0 : Integer.parseInt(ratingField.getText());
            String comment = reviewCommentArea.getText();
            bookToEdit.addReview(rating, comment);
        }

    }

    private Book createBookFromFormData() throws NumberFormatException {
        String title = titleField.getText();
        String author = authorField.getText();
        String originalTitle = originalTitleField.getText().isBlank() ? title : originalTitleField.getText();
        String publisher = publisherField.getText();
        String isbn = isbnField.getText();
        int year = yearField.getText().isBlank() ? 0 : Integer.parseInt(yearField.getText());
        List<String> genres = parseList(genreField.getText());
        boolean hasCopy = hasCopyCheckBox.isSelected();
        boolean readStatus = readStatusCheckBox.isSelected();
        Date readDate = null;
        if (readStatus && readDatePicker.getValue() != null) {
            readDate = Date.from(readDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        ReviewInfo reviewInfo = new ReviewInfo();
        if (readStatus && (!ratingField.getText().isBlank() || !reviewCommentArea.getText().isBlank())) {
            int rating = ratingField.getText().isBlank() ? 0 : Integer.parseInt(ratingField.getText());
            String comment = reviewCommentArea.getText();
            reviewInfo.evaluate(rating, comment);
        }

        Book newBook = new Book(title, originalTitle, genres, year, author, publisher, isbn, hasCopy, readStatus, readDate, reviewInfo);
        return newBook;
    }

    private List<String> parseList(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private void setReviewFieldsDisabled(boolean disabled) {
        readDatePicker.setDisable(disabled);
        ratingField.setDisable(disabled);
        reviewCommentArea.setDisable(disabled);

        if (disabled) {
            readDatePicker.setValue(null);
            ratingField.clear();
            reviewCommentArea.clear();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}