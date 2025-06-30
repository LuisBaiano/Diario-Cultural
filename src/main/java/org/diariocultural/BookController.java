package org.diariocultural;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*; // Para Comparator, Collections, Date, Optional, etc.
import java.util.stream.Collectors;

/**
 * Controlador responsável por gerenciar as operações CRUD (Criar, Ler, Atualizar, Deletar),
 * busca e listagem avançada para a entidade {@link Book}.
 * para mensagens genéricas.
 *
 * @see Book
 */
public class BookController {

    private List<Book> books;
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "books.json";
    private static final String FILE_PATH = DATA_DIRECTORY + File.separator + FILE_NAME;

    public BookController() {
        loadData();
        if (this.books == null) {
            this.books = new ArrayList<>();
        }
        Book.updateBookCounterBasedOnLoadedData(this.books);
    }


    public void addBookViaObject(Book book) {
        if (book != null) {
            this.books.add(book);
            saveData(); // Persiste a adição
        } else {
            System.out.println(" Tentativa de adicionar um objeto Book nulo.");
        }
    }

    public void updateBook(Book updatedBook) {
        // A lógica de encontrar e substituir pode variar, mas esta é uma abordagem simples
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId() == updatedBook.getBookId()) {
                books.set(i, updatedBook); // Substitui o livro antigo pelo novo
                saveData();
                System.out.println("Livro '" + updatedBook.getTitle() + "' atualizado.");
                return;
            }
        }
    }

    public void removeBook(Book bookToRemove) {
        if (bookToRemove != null && books.contains(bookToRemove)) {
            books.remove(bookToRemove);
            System.out.println("Livro '" + bookToRemove.getTitle() + "' removido com sucesso!");
            saveData();
        } else {
            System.err.println("Tentativa de remover um livro nulo ou que não existe na lista.");
        }
    }

    // Adicione este método ao seu BookController/BookService
    public List<Book> getAllBooks() {
        return new ArrayList<>(this.books); // Retorna uma cópia da lista
    }

    public List<Book> searchBooks(String criteria) {
        if (criteria == null || criteria.isBlank()) {
            return new ArrayList<>(this.books); // Retorna todos os livros
        }

        String lowerCriteria = criteria.toLowerCase().trim();
        List<Book> results = books.stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(lowerCriteria) ||
                                book.getAuthor().toLowerCase().contains(lowerCriteria) ||
                                book.getISBN().toLowerCase().contains(lowerCriteria) ||
                                (book.getOriginalTitle() != null && book.getOriginalTitle().toLowerCase().contains(lowerCriteria)) ||
                                book.getGenre().stream().anyMatch(genre -> genre.toLowerCase().contains(lowerCriteria)) ||
                                String.valueOf(book.getReleaseYear()).contains(lowerCriteria)
                )
                .collect(Collectors.toList());

        // A lógica de imprimir no console foi removida, pois agora é responsabilidade da GUI
        return results;
    }

    public void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            File dataDir = new File(DATA_DIRECTORY);
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    System.err.println(" Falha ao criar diretório: " + dataDir.getAbsolutePath());
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file = new File(FILE_PATH);

        if (file.exists() && file.isFile() && file.length() > 0) {
            try {
                this.books = objectMapper.readValue(file, new TypeReference<List<Book>>() {});
                System.out.println(" Dados de livros carregados com sucesso de " + FILE_PATH);
            } catch (IOException e) {
                System.err.println(" Erro ao ler ou desserializar o arquivo " + FILE_PATH + ": " + e.getMessage());
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