package org.diariocultural.fx;

import org.diariocultural.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serviço responsável por unificar as operações de busca e listagem
 * entre diferentes tipos de mídia (Livros, Filmes, etc.).
 */
public class LibraryService {

    private final BookController bookController;
    private final MovieController movieController;
    private final SeriesController seriesController;

    // Atualize o construtor
    public LibraryService(BookController bookController, MovieController movieController, SeriesController seriesController) {
        this.bookController = bookController;
        this.movieController = movieController;
        this.seriesController = seriesController; // <-- Adicione esta linha
    }

    // Atualize o getAllMedia
    public List<Media> getAllMedia() {
        return Stream.of(
                bookController.getAllBooks().stream(),
                movieController.getAllMovies().stream(),
                seriesController.getAllSeries().stream() // <-- Adicione esta linha
        ).flatMap(s -> s).collect(Collectors.toList());
    }

    /**
     * Retorna uma lista de mídias filtrada e ordenada de acordo com os critérios.
     * @param genreFilter O gênero para filtrar (pode ser vazio).
     * @param yearFilter O ano para filtrar (0 se não houver filtro).
     * @param sortOrder A ordem de classificação ("Melhor Avaliados", "Pior Avaliados").
     * @return Uma lista de Media processada.
     */
    public List<Media> getFilteredAndSortedMedia(String textCriteria, String genreFilter, int yearFilter, String sortOrder) {
        // Começa com a lista completa de todas as mídias.
        Stream<Media> mediaStream = getAllMedia().stream();

        // 1. Aplica o filtro de busca por texto, se houver
        if (textCriteria != null && !textCriteria.isBlank()) {
            String lowerCriteria = textCriteria.toLowerCase().trim();
            mediaStream = mediaStream.filter(media -> {
                // Lógica de busca unificada
                if (media instanceof Book book) {
                    return book.getTitle().toLowerCase().contains(lowerCriteria) || book.getAuthor().toLowerCase().contains(lowerCriteria);
                } else if (media instanceof Movie movie) {
                    return movie.getTitle().toLowerCase().contains(lowerCriteria) || movie.getDirector().toLowerCase().contains(lowerCriteria);
                } else if (media instanceof Series series) {
                    return series.getTitle().toLowerCase().contains(lowerCriteria) || series.getCreator().toLowerCase().contains(lowerCriteria);
                }
                return false;
            });
        }

        // 2. Aplica filtro de Gênero
        if (genreFilter != null && !genreFilter.isBlank()) {
            String lowerGenreFilter = genreFilter.toLowerCase().trim();
            mediaStream = mediaStream.filter(media -> media.getGenre().stream()
                    .anyMatch(g -> g.toLowerCase().contains(lowerGenreFilter)));
        }

        // 3. Aplica filtro de Ano
        if (yearFilter > 0) {
            mediaStream = mediaStream.filter(media -> media.getReleaseYear() == yearFilter);
        }

        List<Media> filteredList = mediaStream.collect(Collectors.toList());

        // 4. Aplica a Ordenação
        if (sortOrder != null && !sortOrder.equals("Padrão")) {
            Comparator<Media> comparator = Comparator.comparingDouble(Media::getAverageRating);
            if (sortOrder.equals("Melhor Avaliados")) {
                comparator = comparator.reversed();
            } else { // Pior Avaliados
                comparator = (m1, m2) -> {
                    double r1 = m1.getAverageRating();
                    double r2 = m2.getAverageRating();
                    if (r1 > 0 && r2 == 0) return -1;
                    if (r1 == 0 && r2 > 0) return 1;
                    return Double.compare(r1, r2);
                };
            }
            filteredList.sort(comparator);
        }

        return filteredList;
    }

    public SeriesController getSeriesController(){ return seriesController;}

    public BookController getBookController() {
        return bookController;
    }

    public MovieController getMovieController() {
        return movieController;
    }
}