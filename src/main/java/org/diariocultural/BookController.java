package org.diariocultural;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*; // Para Comparator, Collections, Date, Optional, etc.
import java.util.stream.Collectors;

public class BookController {

    private List<Book> books;
    private final BookView bookView;
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "books.json";
    private static final String FILE_PATH = DATA_DIRECTORY + File.separator + FILE_NAME;

    public BookController() {
        this.bookView = new BookView();
        loadData();
        if (this.books == null) {
            this.books = new ArrayList<>();
        }
        Book.updateBookCounterBasedOnLoadedData(this.books);
    }

    public void addBook() {
        Book book = bookView.getBookDetails();
        if (book != null) {
            books.add(book);
            System.out.println("📖 Livro '" + book.getTitle() + "' adicionado com sucesso!");
            saveData();
        } else {
            System.out.println("❌ Cadastro de livro cancelado ou falhou.");
        }
    }

    public void updateBook(String title) {
        Optional<Book> bookOptional = findBookByTitleInternal(title);

        if (bookOptional.isPresent()) {
            Book bookToUpdate = bookOptional.get();
            bookView.displayMessage("\n--- Atualizando Livro: " + bookToUpdate.getTitle() + " [ID: " + bookToUpdate.getBookId() + "] ---");
            bookView.displayMessage("Deixe o campo em branco para manter o valor atual.");

            // --- Atualização de Campos do Livro ---
            String newTitle = bookView.getInput("Novo Título (Atual: '" + bookToUpdate.getTitle() + "'):");
            if (!newTitle.isBlank()) {
                bookToUpdate.setTitle(newTitle); // Supondo setter em Media ou Book
            }
            String newOriginalTitle = bookView.getInput("Novo Título Original (Atual: '" + bookToUpdate.getOriginalTitle() + "'):");
            if (!newOriginalTitle.isBlank()) {
                bookToUpdate.setOriginalTitle(newOriginalTitle);
            }
            String newAuthor = bookView.getInput("Novo Autor (Atual: '" + bookToUpdate.getAuthor() + "'):");
            if (!newAuthor.isBlank()) {
                bookToUpdate.setAuthor(newAuthor);
            }
            String newPublisher = bookView.getInput("Nova Editora (Atual: '" + bookToUpdate.getPublisher() + "'):");
            if (!newPublisher.isBlank()) {
                bookToUpdate.setPublisher(newPublisher);
            }
            String newISBN = bookView.getInput("Novo ISBN (Atual: '" + bookToUpdate.getISBN() + "'):");
            if (!newISBN.isBlank()) {
                bookToUpdate.setISBN(newISBN);
            }

            List<String> currentGenres = bookToUpdate.getGenre();
            String currentGenresStr = (currentGenres != null && !currentGenres.isEmpty()) ? String.join(", ", currentGenres) : "N/A";
            // getListInput retorna null se entrada for vazia (mantém) ou nova lista se houver entrada
            List<String> newGenres = bookView.getListInput("Novos Gêneros (Atual: [" + currentGenresStr + "], deixe em branco para manter):");
            if (newGenres != null) { // Se não for null, significa que o usuário digitou algo ou quer limpar
                bookToUpdate.setGenre(newGenres); // Book.setGenre deve lidar com lista vazia para "limpar"
            }

            int currentYear = bookToUpdate.getReleaseYear();
            int newYear = bookView.getOptionalValidatedInt("Novo Ano de Publicação (Atual: " + currentYear + "):", 0, java.time.Year.now().getValue() + 5);
            if (newYear != -1) { // -1 significa "manter"
                bookToUpdate.setReleaseYear(newYear);
            }

            Boolean newHasCopyOpt = bookView.getOptionalYesNoInput("Tem cópia física? (s/n) (Atual: " + (bookToUpdate.hasCopy() ? "Sim" : "Não") + "):");
            if (newHasCopyOpt != null) { // Se não deixou em branco
                bookToUpdate.setHasCopy(newHasCopyOpt);
            }

            // --- Lógica de Atualização de Status de Leitura e Data de Leitura ---
            boolean currentReadStatus = bookToUpdate.isReadStatus();
            Boolean newReadStatusOpt = bookView.getOptionalYesNoInput("Já foi lido? (s/n) (Atual: " + (currentReadStatus ? "Sim" : "Não") + "):");

            if (newReadStatusOpt != null) {
                bookToUpdate.setReadStatus(newReadStatusOpt);
                if (newReadStatusOpt) { // Se o NOVO status é LIDO
                    bookView.displayMessage("Status alterado para Lido.");
                    Date currentReadDate = bookToUpdate.getReadDate();
                    String currentReadDateStr = (currentReadDate != null) ? bookView.formatDate(currentReadDate) : "N/A";
                    // Pergunta para definir/atualizar a data de leitura, mesmo que já estivesse como "Lido"
                    Date updatedReadDate = bookView.getOptionalWatchDateInput(
                            "Definir/Atualizar data de leitura? (dd/MM/yyyy) (Atual: " + currentReadDateStr + ", deixe em branco para não alterar):"
                    );
                    if (updatedReadDate != null) { // Se uma nova data (ou a mesma) foi fornecida e não em branco
                        bookToUpdate.setReadDate(updatedReadDate);
                    }
                } else { // Se o NOVO status é NÃO LIDO
                    bookToUpdate.setReadDate(null); // Limpa a data de leitura
                    bookView.displayMessage("Status alterado para Não Lido. Data de leitura removida.");
                }
            }

            // --- ADIÇÃO DE NOVA AVALIAÇÃO ---
            if (bookToUpdate.isReadStatus()) {
                bookView.displayMessage("\n--- Avaliações Anteriores do Livro ---");
                displayReviewHistory(bookToUpdate);

                if (bookView.getYesNoInput("Deseja adicionar uma NOVA avaliação para este livro? (s/n)")) {
                    int rating = bookView.getValidatedInt("Nova nota (0-5):", 0, 5);
                    String comment = bookView.getInput("Novo comentário (opcional):");
                    bookToUpdate.addReview(rating, comment);
                    bookView.displayMessage("Nova avaliação adicionada!");
                }
            } else {
                bookView.displayMessage("\nℹ️ Para adicionar uma avaliação, primeiro marque o livro como 'Lido'.");
            }

            System.out.println("✅ Livro '" + bookToUpdate.getTitle() + "' atualizado com sucesso!");
            saveData();
        } else {
            System.out.println("❌ Livro com título '" + title + "' não encontrado para atualização!");
        }
    }

    private void displayReviewHistory(Book book) {
        // (Seu método displayReviewHistory, como já definido antes)
        if (book == null || book.getReviewInfo() == null) return;
        ReviewInfo ri = book.getReviewInfo();
        List<Review> reviews = ri.getReviews();
        if (reviews.isEmpty()) {
            bookView.displayMessage("  Nenhuma avaliação registrada anteriormente para este livro.");
        } else {
            bookView.displayMessage("  Total de avaliações: " + ri.getReviewCount());
            bookView.displayMessage("  Histórico (mais recentes primeiro):");
            List<Review> reversedReviews = new ArrayList<>(reviews);
            Collections.reverse(reversedReviews);
            int limit = Math.min(reversedReviews.size(), 5);
            for (int i = 0; i < limit; i++) {
                bookView.displayMessage("    - " + reversedReviews.get(i).toString());
            }
            if (reversedReviews.size() > limit) {
                bookView.displayMessage("    ... (e mais " + (reversedReviews.size() - limit) + " outras avaliações)");
            }
        }
    }

    public void removeBook(String title) {
        // (Seu método removeBook, como já definido antes)
        Optional<Book> bookOptional = findBookByTitleInternal(title);
        if (bookOptional.isPresent()) {
            Book bookToRemove = bookOptional.get();
            if (bookView.getYesNoInput("Tem certeza que deseja remover o livro '" + bookToRemove.getTitle() + "'? (s/n)")) {
                books.remove(bookToRemove);
                System.out.println("✅ Livro '" + bookToRemove.getTitle() + "' removido com sucesso!");
                saveData();
            } else {
                System.out.println("Remoção cancelada.");
            }
        } else {
            System.out.println("❌ Livro com título '" + title + "' não encontrado para remoção!");
        }
    }

    public Optional<Book> findBookByTitle(String title) {
        return findBookByTitleInternal(title);
    }

    private Optional<Book> findBookByTitleInternal(String title) {
        // (Seu método findBookByTitleInternal, como já definido antes)
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }
        String lowerTitle = title.toLowerCase().trim();
        Optional<Book> found = books.stream()
                .filter(b -> b.getTitle().equalsIgnoreCase(lowerTitle))
                .findFirst();
        if (found.isEmpty()) {
            found = books.stream()
                    .filter(b -> b.getOriginalTitle() != null && b.getOriginalTitle().equalsIgnoreCase(lowerTitle))
                    .findFirst();
        }
        return found;
    }

    public void listAllBooks() {
        // (Seu método listAllBooks, com applyBookFilters e applyBookSorting, como já definido antes)
        if (books.isEmpty()) {
            bookView.displayMessage("📚 Nenhum livro cadastrado no momento.");
            return;
        }
        List<Book> displayList = new ArrayList<>(this.books);
        boolean wasFilteredOrSorted = false;

        List<Book> filteredList = applyBookFilters(displayList);
        if (filteredList.size() != displayList.size()) {
            wasFilteredOrSorted = true;
        }
        displayList = filteredList;

        if (!displayList.isEmpty()) {
            List<Book> sortedList = applyBookSorting(displayList);
            if (sortedList != displayList || (wasFilteredOrSorted && !displayList.isEmpty())) { // Correção na lógica da flag
                wasFilteredOrSorted = true;
            }
            displayList = sortedList;
        }

        if (displayList.isEmpty()) {
            if (wasFilteredOrSorted) {
                bookView.displayMessage("ℹ️ Nenhum livro encontrado após aplicar os filtros/ordenação selecionados.");
            }
            // Não precisa do else, pois o primeiro if de books.isEmpty() já trataria.
        } else {
            String listHeader = "\n=== LISTA DE LIVROS (" + displayList.size() + ")";
            if (wasFilteredOrSorted) {
                listHeader += " (Resultados filtrados/ordenados)";
            }
            listHeader += " ===";
            bookView.displayMessage(listHeader);
            // Loop para exibir cada livro (já que BookView não tem displayBooks(List<Book>) mas sim displayBook(Book))
            for (Book book : displayList) {
                bookView.displayBook(book);
                bookView.displayMessage("--------------------");
            }
        }
    }

    private List<Book> applyBookFilters(List<Book> currentList) {
        // (Seu método applyBookFilters, como já definido antes)
        List<Book> filteredList = new ArrayList<>(currentList);
        if (bookView.askToFilterByGenre()) {
            String genreFilter = bookView.getGenreFilterInput();
            if (genreFilter != null && !genreFilter.isBlank()) {
                String lowerGenreFilter = genreFilter.toLowerCase().trim();
                filteredList = filteredList.stream()
                        .filter(book -> book.getGenre() != null && book.getGenre().stream()
                                .anyMatch(g -> g.toLowerCase().contains(lowerGenreFilter)))
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) {
                    bookView.displayMessage("Nenhum livro encontrado para o gênero: '" + genreFilter + "'.");
                }
            }
        }
        if (!filteredList.isEmpty() && bookView.askToFilterByYear()) {
            int yearFilter = bookView.getYearFilterInput();
            if (yearFilter > 0) {
                filteredList = filteredList.stream()
                        .filter(book -> book.getReleaseYear() == yearFilter)
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) {
                    bookView.displayMessage("Nenhum livro encontrado para o ano: " + yearFilter + ".");
                }
            }
        }
        return filteredList;
    }

    private List<Book> applyBookSorting(List<Book> currentList) {
        // (Seu método applyBookSorting, ajustado para as opções da BookView, como já definido antes)
        if (currentList.isEmpty()) {
            return currentList;
        }
        int sortOption = bookView.getSortOptionBook();

        if (sortOption == 0) {
            return currentList;
        }

        List<Book> sortedList = new ArrayList<>(currentList);
        Comparator<Book> comparator; // Declarado fora do switch para ser acessível depois

        switch (sortOption) {
            case 1: // Melhor Avaliados
                comparator = Comparator.comparingDouble(
                        (Book b) -> b.isReadStatus() && b.getReviewInfo() != null ? b.getAverageRating() : -1.0
                ).reversed();
                break;
            case 2: // Pior Avaliados
                comparator = Comparator.comparingDouble(
                        (Book b) -> b.isReadStatus() && b.getReviewInfo() != null ? b.getAverageRating() : Double.MAX_VALUE
                );
                break;
            default:
                bookView.displayMessage("Opção de ordenação não aplicada ou inválida.");
                return currentList;
        }
        // Removido 'if (comparator != null)' pois o default já retorna. Se chegar aqui, comparator foi setado.
        sortedList.sort(comparator);
        return sortedList;
    }

    public List<Book> searchBooks(String criteria) {
        // (Seu método searchBooks, como já definido antes)
        // ...
        List<Book> results;
        if (criteria == null || criteria.isBlank()) {
            bookView.displayMessage("Critério de busca vazio. Listando todos os livros.");
            results = new ArrayList<>(this.books);
        } else {
            String lowerCriteria = criteria.toLowerCase().trim();
            results = books.stream()
                    .filter(book ->
                            book.getTitle().toLowerCase().contains(lowerCriteria) ||
                                    book.getAuthor().toLowerCase().contains(lowerCriteria) ||
                                    book.getISBN().toLowerCase().contains(lowerCriteria) ||
                                    (book.getOriginalTitle() != null && book.getOriginalTitle().toLowerCase().contains(lowerCriteria)) ||
                                    book.getGenre().stream().anyMatch(genre -> genre.toLowerCase().contains(lowerCriteria)) ||
                                    String.valueOf(book.getReleaseYear()).contains(lowerCriteria)
                    )
                    .collect(Collectors.toList());
        }

        if (results.isEmpty() && !(criteria == null || criteria.isBlank())) {
            bookView.displayMessage("Nenhum livro encontrado para o critério: '" + criteria + "'");
        } else if (!results.isEmpty()) {
            String messageHeader = (criteria == null || criteria.isBlank()) ?
                    "\n=== LISTA DE TODOS OS LIVROS (" + results.size() + ") ===" :
                    "\n=== RESULTADOS DA BUSCA POR '" + criteria + "' (" + results.size() + ") ===";
            bookView.displayMessage(messageHeader);
            for (Book book : results) { // Loop para exibir individualmente
                bookView.displayBook(book);
                bookView.displayMessage("-----");
            }
        }
        return results;
    }

    private void saveData() {
        // (Seu método saveData, como já definido antes)
        // ...
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            File dataDir = new File(DATA_DIRECTORY);
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    System.err.println("❌ Falha ao criar diretório: " + dataDir.getAbsolutePath());
                    return;
                }
            }
            objectMapper.writeValue(new File(FILE_PATH), books);
        } catch (IOException e) {
            System.err.println("Erro crítico ao salvar dados de livros em " + FILE_PATH + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        // (Seu método loadData, como já definido antes)
        // ...
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file = new File(FILE_PATH);

        if (file.exists() && file.isFile() && file.length() > 0) {
            try {
                this.books = objectMapper.readValue(file, new TypeReference<List<Book>>() {});
                System.out.println("Dados de livros carregados com sucesso de " + FILE_PATH);
            } catch (IOException e) {
                System.err.println("Erro ao ler ou desserializar o arquivo " + FILE_PATH + ": " + e.getMessage());
                e.printStackTrace();
                this.books = new ArrayList<>();
            }
        } else {
            if (!file.exists()) {
                System.out.println("Arquivo " + FILE_PATH + " não encontrado. Será criado ao salvar.");
            } else {
                System.out.println("Arquivo " + FILE_PATH + " vazio ou inválido. Iniciando com catálogo vazio.");
            }
            this.books = new ArrayList<>();
        }
    }
}