package org.diariocultural;

// Imports Jackson
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;

// Imports Java IO
import java.io.File;
import java.io.IOException;

// Imports Java Util
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador responsável por gerenciar as operações CRUD (Criar, Ler, Atualizar, Deletar),
 * busca e listagem avançada para a entidade {@link Series}.
 * Utiliza {@link MovieView} para interações específicas de séries e {@link MediaView}
 * para mensagens genéricas.
 *
 * @see Series
 * @see SeriesView
 * @see MediaView
 */
public class SeriesController {

    private List<Series> seriesList; // Não é final para ser populado por loadData
    private final SeriesView seriesView;

    // Constantes para persistência JSON
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_NAME = "series.json";
    private static final String FILE_PATH = DATA_DIRECTORY + File.separator + FILE_NAME;

    /**
     * Inicializa o controller, carrega dados do arquivo JSON e ajusta o contador de ID.
     */
    public SeriesController() {
        this.seriesView = new SeriesView();
        loadData(); // Carrega dados na inicialização
        if (this.seriesList == null) {
            this.seriesList = new ArrayList<>();
        }
        Series.updateNextIdBasedOnLoadedData(this.seriesList); // Ajusta o ID estático
    }

    /**
     * Adiciona uma nova série, permite adicionar temporadas iniciais e salva.
     */
    public void addSeries() {
        Series series = seriesView.getSeriesDetails(); // View coleta dados básicos da série
        if (series != null) {
            addInitialSeasons(series); // Permite ao usuário adicionar temporadas iniciais
            seriesList.add(series);
            seriesView.displayCreationSuccess(series.getTitle());
            saveData(); // Salva após adicionar a série completa
        } else {
            seriesView.displayMessage("Cadastro de série cancelado.");
        }
    }

    /**
     * Adiciona temporadas iniciais a uma série recém-criada.
     */
    private void addInitialSeasons(Series series) {
        int numSeasons = seriesView.getNumberOfSeasons();
        if (numSeasons <= 0) {
            seriesView.displayMessage("Nenhuma temporada inicial adicionada.");
            return;
        }
        seriesView.displayMessage("Cadastrando " + numSeasons + " temporada(s)...");
        for (int i = 1; i <= numSeasons; i++) {
            Season season = seriesView.getSeasonDetails(i, series.getReleaseYear());
            if (season != null) {
                series.addSeason(season); // Adiciona temporada ao objeto Series
                seriesView.displayMessage("Temporada " + i + " adicionada.");
            } else {
                seriesView.displayMessage("Cadastro da temporada " + i + " cancelado/falhou.");
            }
        }
        // Não precisa chamar saveData() aqui, pois addSeries() fará isso
    }

    /**
     * Atualiza os detalhes de uma série existente e permite adicionar novas temporadas. Salva no final.
     */
    public void updateSeries(String title) {
        Optional<Series> seriesOpt = findSeriesByTitleInternal(title);
        if (seriesOpt.isEmpty()) {
            seriesView.displayNotFoundError(title);
            return;
        }
        Series seriesToUpdate = seriesOpt.get();

        seriesView.displayMessage("\nAtualizando Série: " + seriesToUpdate.getTitle() + " [ID: " + seriesToUpdate.getSeriesId() + "]");
        seriesView.displayMessage("Deixe o campo em branco para manter o valor atual.");

        // (A lógica detalhada de atualização de campos de SeriesView vai aqui)
        // Exemplo:
        String newTitle = seriesView.getInput("Novo Título (Atual: '" + seriesToUpdate.getTitle() + "'):");
        if (!newTitle.isBlank()) seriesToUpdate.setTitle(newTitle); // Assume setter em Media

        String newOriginalTitle = seriesView.getInput("Novo Título Original (Atual: '" + seriesToUpdate.getOriginalTitle() + "'):");
        if (!newOriginalTitle.isBlank()) seriesToUpdate.setOriginalTitle(newOriginalTitle);

        List<String> currentGenres = seriesToUpdate.getGenre();
        String currentGenresStr = (currentGenres != null && !currentGenres.isEmpty()) ? String.join(", ", currentGenres) : "N/A";
        List<String> newGenres = seriesView.getListInput("Novos Gêneros (Atual: [" + currentGenresStr + "]):");
        if (newGenres != null && !newGenres.isEmpty()) seriesToUpdate.setGenre(newGenres); // Tratar lista vazia se precisar limpar

        int currentReleaseYear = seriesToUpdate.getReleaseYear();
        int newReleaseYear = seriesView.getOptionalValidatedInt("Novo Ano de Lançamento (Atual: " + currentReleaseYear + "):", 1888, 9999);
        if (newReleaseYear != -1) seriesToUpdate.setReleaseYear(newReleaseYear);

        int currentEndYear = seriesToUpdate.getEndYear();
        String endYearStr = (currentEndYear == 0) ? "Em andamento" : String.valueOf(currentEndYear);
        int newEndYear = seriesView.getOptionalValidatedInt("Novo Ano de Término (0 para 'Em andamento') (Atual: " + endYearStr + "):", 0, 9999);
        if (newEndYear != -1) {
            if (newEndYear == 0 || newEndYear >= seriesToUpdate.getReleaseYear()) {
                seriesToUpdate.setEndYear(newEndYear);
            } else {
                seriesView.displayMessage("Ano de término inválido. Mantido: " + endYearStr);
            }
        }

        // ... (Atualizar whereToWatch, cast, watchedStatus)
        List<String> currentPlatforms = seriesToUpdate.getWhereToWatch();
        String currentPlatformsStr = (currentPlatforms != null && !currentPlatforms.isEmpty()) ? String.join(", ", currentPlatforms) : "N/A";
        List<String> newPlatforms = seriesView.getListInput("Novas Plataformas (Atual: [" + currentPlatformsStr + "]):");
        if (newPlatforms != null && !newPlatforms.isEmpty()) seriesToUpdate.setWhereToWatch(newPlatforms);

        List<String> currentCast = seriesToUpdate.getCast();
        String currentCastStr = (currentCast != null && !currentCast.isEmpty()) ? String.join(", ", currentCast) : "N/A";
        List<String> newCast = seriesView.getListInput("Novo Elenco Principal (Atual: [" + currentCastStr + "]):");
        if (newCast != null && !newCast.isEmpty()) seriesToUpdate.setCast(newCast);

        boolean currentStatus = seriesToUpdate.isWatchedStatus();
        Boolean newStatusOpt = seriesView.getOptionalYesNoInput("Série já assistida completamente? (Atual: " + (currentStatus ? "Sim" : "Não") + ") (s/n ou deixe em branco):");
        if (newStatusOpt != null) {
            seriesToUpdate.setWatchedStatus(newStatusOpt);
        }


        if (seriesView.shouldAddSeason()) { // Pergunta se quer adicionar/avaliar temporada
            addSeasonToExistingSeries(seriesToUpdate); // Este método já chama saveData()
        } else {
            saveData(); // Salva se nenhuma temporada foi adicionada, mas outros campos podem ter mudado
        }
        seriesView.displayUpdateSuccess(seriesToUpdate.getTitle());
    }

    /**
     * Remove uma série e salva as alterações.
     */
    public void removeSeries(String title) {
        Optional<Series> seriesOpt = findSeriesByTitleInternal(title);
        if (seriesOpt.isPresent()) {
            Series seriesToRemove = seriesOpt.get();
            if (seriesView.getYesNoInput("Tem certeza que deseja remover a série '" + seriesToRemove.getTitle() + "'? (s/n)")) {
                seriesList.remove(seriesToRemove);
                seriesView.displayRemovalSuccess(seriesToRemove.getTitle());
                saveData(); // Salva após remover
            } else {
                seriesView.displayMessage("Remoção cancelada.");
            }
        } else {
            seriesView.displayNotFoundError(title);
        }
    }

    /**
     * Adiciona uma nova temporada a uma série existente e salva.
     */
    private void addSeasonToExistingSeries(Series series) {
        if (series == null) {
            seriesView.displayMessage("Erro: Série não fornecida para adicionar temporada.");
            return;
        }
        int nextSeasonNumber = series.getSeasons().size() + 1;
        seriesView.displayMessage("\nAdicionando Temporada " + nextSeasonNumber + " para '" + series.getTitle() + "'...");
        Season newSeason = seriesView.getSeasonDetails(nextSeasonNumber, series.getReleaseYear());
        if (newSeason != null) {
            series.addSeason(newSeason);
            seriesView.displaySeasonAdded(nextSeasonNumber);
            saveData(); // Salva após adicionar a temporada
        } else {
            seriesView.displayMessage("Adição da temporada cancelada/falhou.");
        }
    }

    /**
     * Ponto de entrada do menu para adicionar uma temporada a uma série existente.
     */
    public void addSeasonFromMenu() {
        String title = seriesView.getInput("Digite o título da série para adicionar temporada:");
        Optional<Series> seriesOpt = findSeriesByTitleInternal(title);
        if (seriesOpt.isPresent()) {
            addSeasonToExistingSeries(seriesOpt.get());
        } else {
            seriesView.displayNotFoundError(title);
        }
    }

    // --- Métodos de Busca ---
    public void searchSeries() {
        int option = seriesView.getIntInput(
                """
                Buscar Séries por:
                1 - Título
                2 - Gênero
                3 - Ano de Lançamento
                4 - Ator/Atriz do Elenco
                Opção:"""
        );
        List<Series> results = switch (option) {
            case 1 -> searchSeriesByTitle();
            case 2 -> searchSeriesByGenre();
            case 3 -> searchSeriesByYear();
            case 4 -> searchSeriesByActor();
            default -> {
                seriesView.displayMessage("❌ Opção de busca inválida.");
                yield List.of(); // Retorna lista vazia
            }
        };
        if (results.isEmpty() && option >=1 && option <=4) {
            seriesView.displayMessage("Nenhuma série encontrada para o critério informado.");
        } else if (!results.isEmpty()) {
            seriesView.displayMessage("\nResultados da Busca (" + results.size() + " encontradas):");
            seriesView.displaySeriesListBrief(results); // Exibe de forma breve
        }
    }

    private List<Series> searchSeriesByTitle() {
        String title = seriesView.getInput("Digite o título da série:");
        return findSeriesByTitleInternal(title).map(List::of).orElse(List.of());
    }

    private List<Series> searchSeriesByGenre() {
        String genreSearch = seriesView.getInput("Digite o gênero:");
        if (genreSearch.isBlank()) { seriesView.displayMessage("Gênero não pode ser vazio."); return List.of(); }
        final String lowerGenre = genreSearch.toLowerCase().trim();
        return seriesList.stream()
                .filter(s -> s.getGenre() != null && s.getGenre().stream()
                        .anyMatch(g -> g != null && g.toLowerCase().contains(lowerGenre)))
                .toList();
    }

    private List<Series> searchSeriesByYear() {
        int year = seriesView.getIntInput("Digite o ano de lançamento:");
        if (year < 1800 || year > java.time.Year.now().getValue() + 20) { // Validação de range
            seriesView.displayMessage("Ano inválido."); return List.of();
        }
        return seriesList.stream().filter(s -> s.getReleaseYear() == year).toList();
    }

    private List<Series> searchSeriesByActor() {
        String actorName = seriesView.getInput("Digite o nome do ator/atriz:");
        if (actorName.isBlank()) { seriesView.displayMessage("Nome não pode ser vazio."); return List.of(); }
        final String lowerActorName = actorName.toLowerCase().trim();
        return seriesList.stream()
                .filter(s -> s.getCast() != null && s.getCast().stream()
                        .anyMatch(a -> a != null && a.toLowerCase().contains(lowerActorName)))
                .toList();
    }

    private Optional<Series> findSeriesByTitleInternal(String title) {
        if (title == null || title.isBlank()) return Optional.empty();
        String lowerTitle = title.toLowerCase().trim();
        return seriesList.stream()
                .filter(s -> (s.getTitle() != null && s.getTitle().equalsIgnoreCase(lowerTitle)) ||
                        (s.getOriginalTitle() != null && s.getOriginalTitle().equalsIgnoreCase(lowerTitle)))
                .findFirst();
    }

    // --- Listagem Avançada ---
    public void listAllSeries() {
        if (seriesList.isEmpty()) {
            seriesView.displayEmptyList();
            return;
        }
        List<Series> displayList = new ArrayList<>(seriesList);
        displayList = applySeriesFilters(displayList);
        displayList = applySeriesSorting(displayList);

        if (displayList.isEmpty()) {
            seriesView.displayMessage("Nenhuma série encontrada após aplicar os filtros/ordenação.");
        } else {
            seriesView.displayAllSeries(displayList); // Pede para a view exibir
        }
    }

    private List<Series> applySeriesFilters(List<Series> currentList) {
        List<Series> filteredList = new ArrayList<>(currentList);
        if (seriesView.askToFilterByGenre()) {
            String genreFilter = seriesView.getGenreFilterInput();
            if (!genreFilter.isBlank()) {
                String lowerGenreFilter = genreFilter.toLowerCase();
                filteredList = filteredList.stream()
                        .filter(s -> s.getGenre().stream().anyMatch(g -> g.toLowerCase().contains(lowerGenreFilter)))
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) seriesView.displayMessage("Nenhuma série para o gênero: " + genreFilter);
            }
        }
        if (!filteredList.isEmpty() && seriesView.askToFilterByYear()) {
            int yearFilter = seriesView.getYearFilterInput();
            if (yearFilter > 0) { // 0 para pular
                filteredList = filteredList.stream()
                        .filter(s -> s.getReleaseYear() == yearFilter)
                        .collect(Collectors.toList());
                if (filteredList.isEmpty()) seriesView.displayMessage("Nenhuma série para o ano: " + yearFilter);
            }
        }
        return filteredList;
    }

    private List<Series> applySeriesSorting(List<Series> currentList) {
        if (currentList.isEmpty()) return currentList;
        int sortOption = seriesView.getSortOptionSeries();
        if (sortOption == 1 || sortOption == 2) {
            Comparator<Series> comparator = Comparator.comparingDouble(Series::getAverageRating);
            if (sortOption == 1) { comparator = comparator.reversed(); } // Melhor avaliados primeiro
            List<Series> sortedList = new ArrayList<>(currentList);
            sortedList.sort(comparator);
            return sortedList;
        }
        return currentList;
    }

    // --- Métodos de Persistência JSON ---

    private void saveData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON formatado
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Se tiver Datas

        try {
            File dataDir = new File(DATA_DIRECTORY);
            if (!dataDir.exists()) {
                if (!dataDir.mkdirs()) {
                    System.err.println(" Falha ao criar diretório de dados: " + dataDir.getAbsolutePath());
                    return;
                }
            }
            objectMapper.writeValue(new File(FILE_PATH), seriesList);
            // System.out.println("Dados de séries salvos em " + FILE_PATH);
        } catch (IOException e) {
            System.err.println(" Erro ao salvar dados de séries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignora campos extras
        // objectMapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); // Se usar datas

        File file = new File(FILE_PATH);
        if (file.exists() && file.isFile() && file.length() > 0) {
            try {
                this.seriesList = objectMapper.readValue(file, new TypeReference<List<Series>>() {});
                System.out.println(" Dados de séries carregados de " + FILE_PATH);
            } catch (IOException e) {
                System.err.println(" Erro ao carregar dados de séries: " + e.getMessage());
                e.printStackTrace();
                this.seriesList = new ArrayList<>(); // Começa vazio se houver erro
            }
        } else {
            if (!file.exists()) System.out.println("Arquivo " + FILE_PATH + " não encontrado. Será criado ao salvar.");
            else System.out.println("Arquivo " + FILE_PATH + " vazio ou é um diretório. Iniciando com lista vazia.");
            this.seriesList = new ArrayList<>(); // Lista vazia se arquivo não existe/vazio
        }
    }
}