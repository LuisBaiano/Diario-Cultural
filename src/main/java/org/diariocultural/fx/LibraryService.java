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

    // Atualize o searchAllMedia
    public List<Media> searchAllMedia(String criteria) {
        if (criteria == null || criteria.isBlank()) {
            return getAllMedia();
        }
        List<Book> booksFound = bookController.searchBooks(criteria);
        List<Movie> moviesFound = movieController.searchMovies(criteria);
        List<Series> seriesFound = seriesController.searchSeries(criteria);

        return Stream.of(booksFound.stream(), moviesFound.stream(), seriesFound.stream())
                .flatMap(s -> s).collect(Collectors.toList());
    }

    /**
     * Retorna uma lista de mídias filtrada e ordenada de acordo com os critérios.
     * @param genreFilter O gênero para filtrar (pode ser vazio).
     * @param yearFilter O ano para filtrar (0 se não houver filtro).
     * @param sortOrder A ordem de classificação ("Melhor Avaliados", "Pior Avaliados").
     * @return Uma lista de Media processada.
     */
    public List<Media> getFilteredAndSortedMedia(String genreFilter, int yearFilter, String sortOrder) {
        Stream<Media> mediaStream = getAllMedia().stream();

        // 1. Aplica filtro de Gênero
        if (genreFilter != null && !genreFilter.isBlank()) {
            String lowerGenreFilter = genreFilter.toLowerCase().trim();
            mediaStream = mediaStream.filter(media -> media.getGenre().stream()
                    .anyMatch(g -> g.toLowerCase().contains(lowerGenreFilter)));
        }

        // 2. Aplica filtro de Ano
        if (yearFilter > 0) {
            mediaStream = mediaStream.filter(media -> media.getReleaseYear() == yearFilter);
        }

        List<Media> filteredList = mediaStream.collect(Collectors.toList());

        // 3. Aplica a Ordenação
        if (sortOrder != null) {
            switch(sortOrder){
                case "Melhor Avaliados":
                    filteredList.sort(Comparator.comparingDouble(Media::getAverageRating).reversed());
                    break;
                case "Pior Avaliados":
                    Comparator<Media> worstRatedComparator = (m1, m2) -> {
                        double r1 = m1.getAverageRating();
                        double r2 = m2.getAverageRating();
                        if(r1 > 0 && r2 == 0) return -1;
                        if(r1 == 0 && r2 > 0) return 1;
                        return Double.compare(r1, r2);
                    };
                    filteredList.sort(worstRatedComparator);
                    break;
                case "Padrão":
                    default:
                break;

            }
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