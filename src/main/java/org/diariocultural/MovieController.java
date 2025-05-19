package org.diariocultural;

// Imports Jackson
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature; // Para ignorar propriedades desconhecidas

// Imports Java IO
import java.io.File;
import java.io.IOException;

// Imports Java Util
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Date; // Mantido pois é usado em updateMovie
import java.util.Optional; // Boa prática para find...
import java.util.stream.Collectors;

/**
 * Controlador responsável por gerenciar as operações CRUD (Criar, Ler, Atualizar, Deletar),
 * busca e listagem avançada para a entidade {@link Movie}.
 * Utiliza {@link MovieView} para interações específicas de filmes e {@link MediaView}
 * para mensagens genéricas.
 *
 * @see Movie
 * @see MovieView
 * @see MediaView
 */
public class MovieController {

    /** Armazenamento dos dados dos filmes e Views para interação com o usuário. */
    private List<Movie> movies; // Removido 'final' para ser populado por loadData
    private final MovieView movieView;
    private final MediaView mediaView; // Para mensagens/inputs genéricos

    // Constantes para persistência JSON
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "movies.json";
    private static final String FILE_PATH = DATA_DIRECTORY + File.separator + FILE_NAME;


    /**
     * Inicializa o controller, carrega dados de filmes do arquivo JSON
     * e ajusta o contador de IDs da classe Movie.
     */
    public MovieController() {
        this.movieView = new MovieView();
        this.mediaView = new MediaView();
        loadData(); // Carrega dados na inicialização
        // Garante que a lista nunca seja nula
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        // Ajusta o contador estático da classe Movie
        Movie.updateNextIdBasedOnLoadedData(this.movies);
    }

    /**
     * Solicita os detalhes de um novo filme via {@link MovieView},
     * o adiciona à lista e salva os dados.
     */
    public void addMovie() {
        Movie movie = movieView.getMovieDetails();
        if (movie != null) {
            movies.add(movie);
            mediaView.displayMessage(" Filme '" + movie.getTitle() + "' adicionado com sucesso!");
            saveData(); // Salva após adicionar
        } else {
            mediaView.displayMessage("❌ Cadastro de filme cancelado ou falhou.");
        }
    }

    /**
     * Atualiza os detalhes de um filme existente e salva os dados.
     *
     * @param title Título do filme a ser atualizado.
     */
    public void updateMovie(String title) {
        Optional<Movie> movieOptional = findMovieByTitleInternal(title);
        if (movieOptional.isEmpty()) {
            mediaView.displayMessage("❌ Filme com título '" + title + "' não encontrado!");
            return;
        }
        Movie movieToUpdate = movieOptional.get();

        movieView.displayMessage("\nAtualizando Filme: " + movieToUpdate.getTitle() + " [ID: " + movieToUpdate.getMovieId() + "]");
        movieView.displayMessage("Deixe o campo em branco para manter o valor atual.");

        // --- Atualização dos Campos (lógica existente) ---
        String newTitle = movieView.getInput("Novo Título (Atual: '" + movieToUpdate.getTitle() + "'):");
        if (!newTitle.isBlank()) movieToUpdate.setTitle(newTitle);

        String newOriginalTitle = movieView.getInput("Novo Título Original (Atual: '" + movieToUpdate.getOriginalTitle() + "'):");
        if (!newOriginalTitle.isBlank()) movieToUpdate.setOriginalTitle(newOriginalTitle);

        List<String> currentGenres = movieToUpdate.getGenre();
        String currentGenresStr = (currentGenres != null && !currentGenres.isEmpty()) ? String.join(", ", currentGenres) : "N/A";
        List<String> newGenres = movieView.getListInput("Novos Gêneros (separados por vírgula) (Atual: [" + currentGenresStr + "]):");
        if (newGenres != null && !newGenres.isEmpty()) movieToUpdate.setGenre(newGenres);

        int currentDuration = movieToUpdate.getDuration();
        int newDuration = movieView.getOptionalValidatedInt("Nova Duração (minutos) (Atual: " + currentDuration + "):", 1, 900);
        if (newDuration != -1) movieToUpdate.setDuration(newDuration);

        int currentYear = movieToUpdate.getReleaseYear();
        int newYear = movieView.getOptionalValidatedInt("Novo Ano de Lançamento (Atual: " + currentYear + "):", 1888, 9999);
        if (newYear != -1) movieToUpdate.setReleaseYear(newYear);

        String newDirector = movieView.getInput("Novo Diretor (Atual: '" + movieToUpdate.getDirector() + "'):");
        if (!newDirector.isBlank()) movieToUpdate.setDirector(newDirector);

        String newSynopsis = movieView.getInput("Nova Sinopse (Atual: '" + movieToUpdate.getSynopsis() + "'):");
        if (!newSynopsis.isBlank()) movieToUpdate.setSynopsis(newSynopsis);

        List<String> currentCast = movieToUpdate.getCast();
        String currentCastStr = (currentCast != null && !currentCast.isEmpty()) ? String.join(", ", currentCast) : "N/A";
        List<String> newCast = movieView.getListInput("Novo Elenco (separado por vírgula) (Atual: [" + currentCastStr + "]):");
        if (newCast != null && !newCast.isEmpty()) movieToUpdate.setCast(newCast);

        List<String> currentPlatforms = movieToUpdate.getWhereToWatch();
        String currentPlatformsStr = (currentPlatforms != null && !currentPlatforms.isEmpty()) ? String.join(", ", currentPlatforms) : "N/A";
        List<String> newPlatforms = movieView.getListInput("Novas Plataformas (separadas por vírgula) (Atual: [" + currentPlatformsStr + "]):");
        if (newPlatforms != null && !newPlatforms.isEmpty()) movieToUpdate.setWhereToWatch(newPlatforms);

        // --- Lógica de Status de Visualização, Data e Avaliação (lógica existente) ---
        boolean currentWatchedStatus = movieToUpdate.isWatchedStatus();
        Boolean newWatchedStatus = movieView.getOptionalYesNoInput("Já assistiu este filme? (Atual: " + (currentWatchedStatus ? "Sim" : "Não") + ") (s/n ou deixe em branco):");
        if (newWatchedStatus != null) {
            movieToUpdate.setWatchedStatus(newWatchedStatus);
            if (!newWatchedStatus) {
                movieToUpdate.setWatchDate(null);
                movieView.displayMessage("Status atualizado para Não Assistido. Data de visualização removida.");
            } else {
                movieView.displayMessage("Status atualizado para Assistido.");
                if (!currentWatchedStatus) { // Só pede se mudou de Não para Sim
                    Date newWatchDate = movieView.getWatchDateInput("Data de visualização (dd/MM/yyyy, deixe em branco):");
                    movieToUpdate.setWatchDate(newWatchDate);
                    // Pergunta sobre avaliação apenas se acabou de marcar como assistido
                    if (movieView.getYesNoInput("Deseja adicionar/atualizar a avaliação agora? (s/n)")) {
                        int rating = movieView.getRatingInput("Nota (0-5):");
                        String comment = movieView.getInput("Comentário:");
                        movieToUpdate.addReview(rating, comment);
                    }
                }
            }
        }

        // Atualiza data/avaliação separadamente se já estiver marcado como assistido
        if (movieToUpdate.isWatchedStatus()) {
            Date currentWatchDate = movieToUpdate.getWatchDate();
            String currentDateStr = (currentWatchDate != null) ? movieView.formatDate(currentWatchDate) : "N/A";
            Date newWatchDate = movieView.getOptionalWatchDateInput("Nova data de visualização (dd/MM/yyyy) (Atual: " + currentDateStr + ", deixe em branco para manter):");
            if (newWatchDate != null) {
                movieToUpdate.setWatchDate(newWatchDate);
            }
            if (movieView.getYesNoInput("Deseja adicionar/atualizar a avaliação deste filme assistido? (s/n)")) {
                int rating = movieView.getRatingInput("Nota (0-5):");
                String comment = movieView.getInput("Comentário:");
                movieToUpdate.addReview(rating, comment);
            }
        }
        // --- Fim da lógica de atualização ---

        mediaView.displayMessage(" Filme '" + movieToUpdate.getTitle() + "' atualizado com sucesso!");
        saveData(); // Salva após atualizar
    }

    /**
     * Remove um filme da lista e salva os dados.
     *
     * @param title Título do filme a ser removido.
     */
    public void removeMovie(String title) {
        Optional<Movie> movieOptional = findMovieByTitleInternal(title);
        if (movieOptional.isPresent()) {
            Movie movieToRemove = movieOptional.get();
            if (movieView.getYesNoInput("Tem certeza que deseja remover o filme '" + movieToRemove.getTitle() + "'? (s/n)")) {
                movies.remove(movieToRemove);
                mediaView.displayMessage(" Filme '" + movieToRemove.getTitle() + "' removido com sucesso!");
                saveData(); // Salva após remover
            } else {
                mediaView.displayMessage("Remoção cancelada.");
            }
        } else {
            mediaView.displayMessage(" Filme com título '" + title + "' não encontrado!");
        }
    }

    /**
     * Apresenta opções de busca de filmes ao usuário e exibe os resultados.
     * (Lógica de busca existente mantida)
     */
    public void searchMovie() {
        int option = movieView.getIntInput(
                """
                Buscar por:
                1-Título
                2-Diretor
                3-Ator
                4-Gênero
                5-Ano
                Opção:"""
        );

        List<Movie> results = switch (option) {
            case 1 -> searchByTitle();
            case 2 -> searchByDirector();
            case 3 -> searchByActor();
            case 4 -> searchByGenre();
            case 5 -> searchByYear();
            default -> {
                mediaView.displayMessage("Opção inválida.");
                yield List.of();
            }
        };

        if (results.isEmpty() && option >=1 && option <=5) { // Só mostra se uma opção válida foi escolhida
            mediaView.displayMessage(" Nenhum filme encontrado para a opção de busca.");
        } else if (!results.isEmpty()) {
            movieView.displayMoviesBrief(results);
        }
    }

    // --- Métodos Auxiliares de Busca (Privados) ---
    // (searchByTitle, searchByDirector, searchByActor, searchByGenre, searchByYear)
    // Seu código existente aqui...
    // Mudei findMovieByTitle para retornar Optional para consistência e segurança
    private List<Movie> searchByTitle() {
        String title = movieView.getInput("Digite o título:");
        Optional<Movie> movieOpt = findMovieByTitleInternal(title);
        return movieOpt.map(List::of).orElse(List.of());
    }

    private List<Movie> searchByDirector() {
        String director = movieView.getInput("Digite o diretor:");
        if (director.isBlank()) return List.of();
        String lowerDirector = director.toLowerCase().trim();
        return movies.stream()
                .filter(m -> m.getDirector() != null && m.getDirector().toLowerCase().contains(lowerDirector))
                .toList();
    }

    private List<Movie> searchByActor() {
        String actor = movieView.getInput("Digite o ator:");
        if (actor.isBlank()) return List.of();
        String lowerActor = actor.toLowerCase().trim();
        return movies.stream()
                .filter(m -> m.getCast() != null && m.getCast().stream()
                        .anyMatch(a -> a != null && a.toLowerCase().contains(lowerActor)))
                .toList();
    }

    private List<Movie> searchByGenre() {
        String genre = movieView.getInput("Digite o gênero:");
        if (genre.isBlank()) return List.of();
        String lowerGenre = genre.toLowerCase().trim();
        return movies.stream()
                .filter(m -> m.getGenre() != null && m.getGenre().stream()
                        .anyMatch(g -> g != null && g.toLowerCase().contains(lowerGenre)))
                .toList();
    }

    private List<Movie> searchByYear() {
        int year = movieView.getIntInput("Digite o ano:");
        if (year <= 0) {
            mediaView.displayMessage("Ano inválido.");
            return List.of();
        }
        return movies.stream()
                .filter(m -> m.getReleaseYear() == year)
                .toList();
    }

    /**
     * Encontra um filme pelo título (principal ou original).
     * @param title Título a buscar.
     * @return Optional<Movie>.
     */
    private Optional<Movie> findMovieByTitleInternal(String title) {
        if (title == null || title.isBlank()) return Optional.empty();
        String lowerTitle = title.toLowerCase().trim();
        return movies.stream()
                .filter(m -> (m.getTitle() != null && m.getTitle().equalsIgnoreCase(lowerTitle)) ||
                        (m.getOriginalTitle() != null && m.getOriginalTitle().equalsIgnoreCase(lowerTitle)))
                .findFirst();
    }


    // --- Listagem Avançada (lógica existente mantida) ---
    public void listAllMovies() {
        if (movies.isEmpty()) {
            mediaView.displayMessage(" Nenhum filme cadastrado.");
            return;
        }
        List<Movie> displayList = new ArrayList<>(movies);
        displayList = applyMovieFilters(displayList);
        displayList = applyMovieSorting(displayList);

        if (displayList.isEmpty()){
            mediaView.displayMessage("Nenhum filme encontrado após aplicar os filtros/ordenação.");
        } else {
            movieView.displayMovies(displayList);
        }
    }

    private List<Movie> applyMovieFilters(List<Movie> currentList) {
        // (Sua lógica de filtro existente)
        List<Movie> filteredList = new ArrayList<>(currentList);
        if (movieView.askToFilterByGenre()) {
            String genreFilter = movieView.getGenreFilterInput();
            if (!genreFilter.isBlank()) {
                String lowerGenreFilter = genreFilter.toLowerCase();
                filteredList = filteredList.stream()
                        .filter(m -> m.getGenre() != null && m.getGenre().stream()
                                .anyMatch(g -> g != null && g.toLowerCase().contains(lowerGenreFilter)))
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) mediaView.displayMessage("Nenhum filme encontrado para o gênero: " + genreFilter);
            }
        }
        if (!filteredList.isEmpty() && movieView.askToFilterByYear()) {
            int yearFilter = movieView.getYearFilterInput();
            if (yearFilter > 0) {
                filteredList = filteredList.stream()
                        .filter(m -> m.getReleaseYear() == yearFilter)
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) mediaView.displayMessage("Nenhum filme encontrado para o ano: " + yearFilter);
            }
        }
        return filteredList;
    }

    private List<Movie> applyMovieSorting(List<Movie> currentList) {
        // (Sua lógica de ordenação existente)
        if (currentList.isEmpty()) return currentList;
        int sortOption = movieView.getSortOptionMovie();
        if (sortOption == 1 || sortOption == 2) {
            Comparator<Movie> comparator = Comparator.comparingDouble(m ->
                    m.isWatchedStatus() && m.getReviewInfo() != null ? m.getAverageRating() : 0.0
            );
            if (sortOption == 1) {
                comparator = comparator.reversed();
            }
            List<Movie> sortedList = new ArrayList<>(currentList);
            sortedList.sort(comparator);
            return sortedList;
        }
        return currentList;
    }


    // --- Métodos de Persistência JSON ---

    /**
     * Salva a lista atual de filmes (this.movies) no arquivo JSON.
     */
    private void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Para JSON legível
        // Para Datas (java.util.Date), Jackson pode usar timestamp.
        // Para formato ISO 8601 (recomendado):
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        try {
            File dataDir = new File(DATA_DIRECTORY);
            if (!dataDir.exists()) {
                if (dataDir.mkdirs()) {
                    System.out.println("Diretório de dados criado: " + dataDir.getAbsolutePath());
                } else {
                    System.err.println("❌ Falha ao criar diretório de dados: " + dataDir.getAbsolutePath());
                    return; // Não prosseguir se não puder criar o diretório
                }
            }
            objectMapper.writeValue(new File(FILE_PATH), movies);
            // System.out.println("Dados de filmes salvos em " + FILE_PATH); // Log opcional
        } catch (IOException e) {
            System.err.println("❌ Erro crítico ao salvar dados de filmes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carrega a lista de filmes do arquivo JSON.
     */
    private void loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Importante: Configurar para não falhar em propriedades desconhecidas
        // Isso ajuda com arquivos JSON de versões antigas do seu modelo de dados
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Se precisar configurar formato de data ao ler:
        // objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        File file = new File(FILE_PATH);
        if (file.exists() && file.isFile() && file.length() > 0) {
            try {
                this.movies = objectMapper.readValue(file, new TypeReference<List<Movie>>() {});
                System.out.println("✅ Dados de filmes carregados de " + FILE_PATH);
            } catch (IOException e) {
                System.err.println("❌ Erro ao ler ou desserializar o arquivo " + FILE_PATH + ": " + e.getMessage());
                e.printStackTrace();
                this.movies = new ArrayList<>(); // Inicia com lista vazia em caso de erro
            }
        } else {
            if (!file.exists()) {
                System.out.println("ℹ️ Arquivo " + FILE_PATH + " não encontrado. Um novo será criado ao salvar.");
            } else if (file.isFile() && file.length() == 0) {
                System.out.println("ℹ️ Arquivo " + FILE_PATH + " está vazio. Iniciando com catálogo de filmes vazio.");
            } else if (!file.isFile()){
                System.out.println("❌ O caminho " + FILE_PATH + " existe, mas não é um arquivo. Verifique.");
            }
            this.movies = new ArrayList<>();
        }
    }
}