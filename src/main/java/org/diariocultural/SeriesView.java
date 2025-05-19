package org.diariocultural;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * View para interação do usuário na gestão de séries e temporadas.
 */
public class SeriesView extends MediaView { // Assume MediaView provides basic IO or scanner
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final Scanner scanner; // Use a local scanner for reliable input handling

    static {
        dateFormat.setLenient(false); // Strict date parsing
    }

    public SeriesView() {
        super(); // Call super constructor if needed
        this.scanner = new Scanner(System.in); // Initialize local scanner
    }

    public Series getSeriesDetails() {
        displayMessage("\n=== CADASTRO DE SÉRIE ===");

        String title = getInput("Título da série:");
        String originalTitle = getInput("Título original (deixe em branco se igual):");
        if (originalTitle.isBlank()) originalTitle = title;

        List<String> genre = getListInput("Gêneros (separados por vírgula):");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int releaseYear = getValidatedInt("Ano de lançamento:", 1888, currentYear + 10);
        int endYearInput = getValidatedInt("Ano de término (ou 0 se em andamento):", 0, currentYear + 20);
        int endYear = (endYearInput == 0 || endYearInput < releaseYear) ? 0 : endYearInput;

        List<String> whereToWatch = getListInput("Plataformas de exibição (separadas por vírgula):");
        List<String> cast = getListInput("Elenco principal (separado por vírgula):");
        boolean watchedStatus = getYesNoInput("Você já assistiu esta série completa? (s/n)");

        return new Series(
                title, originalTitle, genre, releaseYear, endYear,
                whereToWatch, cast, watchedStatus
        );
    }

    public void displaySeriesDetails(Series series) {
        // (Lógica completa conforme você forneceu)
        if (series == null) {
            displayMessage("❌ Série inválida ou não encontrada.");
            return;
        }
        displayMessage("\n=== DETALHES DA SÉRIE [ID: " + series.getSeriesId() + "] ===");
        displayMessage("Título: " + series.getTitle());
        if (series.getOriginalTitle() != null && !series.getTitle().equalsIgnoreCase(series.getOriginalTitle())) {
            displayMessage("Título Original: " + series.getOriginalTitle());
        }
        List<String> genres = series.getGenre();
        displayMessage("Gêneros: " + ((genres != null && !genres.isEmpty()) ? String.join(", ", genres) : "N/A"));
        String yearRange = (series.getEndYear() == 0) ? series.getReleaseYear() + "-Presente" : series.getReleaseYear() + "-" + series.getEndYear();
        displayMessage("Período: " + yearRange);
        List<String> platforms = series.getWhereToWatch();
        displayMessage("Onde Assistir: " + ((platforms!=null && !platforms.isEmpty())?String.join(", ", platforms):"N/A"));
        List<String> mainCast = series.getCast();
        displayMessage("Elenco Principal: " + ((mainCast!=null && !mainCast.isEmpty())?String.join(", ", mainCast):"N/A"));
        displayMessage("Status: " + (series.isWatchedStatus() ? "✅ Assistida" : "⬜ Não assistida"));
        displayAverageSeasonRating(series);
        displaySeasons(series.getSeasons());
    }

    private void displayAverageSeasonRating(Series series) {
        displayMessage("\n--- Avaliação da Série (Média das Temporadas) ---");
        double avgRating = series.getAverageRating();
        long ratedCount = series.getRatedSeasonsCount();

        if (ratedCount == 0) {
            displayMessage("Nenhuma temporada avaliada ainda.");
        } else {
            displayMessage(String.format("Média das Temporadas Avaliadas: %.1f/5 (%d temporada(s) avaliada(s))",
                    avgRating, ratedCount));
        }
    }

    public Season getSeasonDetails(int seasonNumber, int seriesReleaseYear) {
        // (Lógica completa conforme você forneceu)
        displayMessage("\n=== DETALHES TEMPORADA " + seasonNumber + " ===");
        int episodes = getValidatedInt("Número de episódios:", 1, 100);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int seasonYear = getValidatedInt("Ano de lançamento da temporada:", seriesReleaseYear, currentYear + 5);
        List<String> seasonCast = getListInput("Elenco adicional da temporada (separado por vírgula):");

        Season newSeason = new Season(seasonNumber, episodes, seasonYear, seasonCast);

        if (getYesNoInput("Deseja avaliar a Temporada " + seasonNumber + " agora? (s/n)")) {
            int rating = getValidatedInt("Nota da Temporada (0-5):", 0, 5);
            String comment = getInput("Comentário da Temporada:");
            newSeason.addReview(rating, comment);
        }
        return newSeason;
    }

    private void displaySeasons(List<Season> seasons) {
        // (Lógica completa conforme você forneceu)
        displayMessage("\n=== TEMPORADAS (" + (seasons == null ? 0 : seasons.size()) + ") ===");
        if (seasons == null || seasons.isEmpty()) {
            displayMessage("Nenhuma temporada cadastrada.");
            return;
        }
        List<Season> sortedSeasons = new ArrayList<>(seasons);
        sortedSeasons.sort(Comparator.comparingInt(Season::getSeasonNumber));

        for (Season season : sortedSeasons) {
            displayMessage(String.format("--- Temporada %d (%d) ---", season.getSeasonNumber(), season.getReleaseYear()));
            displayMessage("  Episódios: " + season.getEpisodes());
            List<String> cast = season.getCast();
            if (cast != null && !cast.isEmpty()) {
                displayMessage("  Elenco Adicional: " + String.join(", ", cast));
            }
            ReviewInfo seasonReviewInfo = season.getReviewInfo();
            int seasonReviewCount = (seasonReviewInfo != null) ? seasonReviewInfo.getReviewCount() : 0;
            if (seasonReviewCount > 0) {
                displayMessage(String.format("  Avaliação da Temporada: %.1f/5 (%d avaliação(ões))",
                        seasonReviewInfo.getAverageRating(), seasonReviewCount));
            } else {
                displayMessage("  Avaliação da Temporada: Nenhuma");
            }
        }
    }

    // --- Métodos de Entrada (seus métodos existentes) ---
    public String getInput(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine().trim();
    }

    private int getValidatedInt(String prompt, int min, int max) {
        while (true) { try { String i=getInput(prompt); if(i.isBlank()){displayMessage("❌ Entrada vazia."); continue;} int v=Integer.parseInt(i.trim()); if(v>=min && v<=max) return v; else displayMessage(String.format("❌ Valor inválido (%d-%d).", min, max)); } catch (NumberFormatException e) { displayMessage("❌ Número inválido."); } }
    }

    public int getOptionalValidatedInt(String prompt, int min, int max) {
        while (true) { try { String inputStr = getInput(prompt); if (inputStr.isBlank()) {return -1;} int value = Integer.parseInt(inputStr.trim()); if (value >= min && value <= max) {return value;} displayMessage(String.format("❌ Valor inválido (%d-%d).", min, max));} catch (NumberFormatException e) {displayMessage("❌ Número inválido.");}}}

    public List<String> getListInput(String prompt) {
        String input = getInput(prompt); if(input.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(input.split("\\s*,\\s*"))).stream().map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toList());
    }

    public boolean getYesNoInput(String prompt) {
        while (true) { String r=getInput(prompt); if(r!=null){ r=r.toLowerCase().trim(); if(r.equals("s")) return true; if(r.equals("n")) return false; } displayMessage("❌ Resposta ('s'/'n')."); }
    }

    public Boolean getOptionalYesNoInput(String prompt) {
        while (true) { String response = getInput(prompt); if (response.isBlank()) {return null;} response = response.toLowerCase().trim(); if (response.equals("s")) return true; if (response.equals("n")) return false; displayMessage("❌ Resposta inválida ('s'/'n' ou deixe em branco)."); }
    }

    // --- Métodos para Interação com Controller (seus métodos existentes) ---
    public int getNumberOfSeasons() { return getValidatedInt("Quantas temporadas deseja cadastrar inicialmente?", 0, 100);}
    public boolean shouldAddSeason() { return getYesNoInput("Deseja adicionar/avaliar uma temporada para esta série agora? (s/n)");}

    // --- Métodos de Feedback (seus métodos existentes) ---
    public void displayCreationSuccess(String title) { displayMessage("Série '" + title + "' criada com sucesso!");}
    public void displayUpdateSuccess(String title) { displayMessage("Série '" + title + "' atualizada com sucesso!");}
    public void displayRemovalSuccess(String title) { displayMessage("Série '" + title + "' removida com sucesso!");}
    public void displayNotFoundError(String title) { displayMessage("Série '" + title + "' não encontrada!");}
    public void displaySeasonAdded(int seasonNumber) { displayMessage("Temporada " + seasonNumber + " adicionada com sucesso!");}
    public void displayEmptyList() { displayMessage("📺 Nenhuma série cadastrada."); }

    // --- Listagem e Busca ---
    public boolean askToFilterByGenre() {return getYesNoInput("Deseja filtrar por gênero? (s/n)");}
    public String getGenreFilterInput() {return getInput("Digite o gênero para filtrar:");}
    public boolean askToFilterByYear() {return getYesNoInput("Deseja filtrar por ano de lançamento? (s/n)");}
    public int getYearFilterInput() {return getValidatedInt("Digite o ano de lançamento para filtrar (ou 0 para pular):", 0, Calendar.getInstance().get(Calendar.YEAR) + 10);}
    public int getSortOptionSeries() {
        displayMessage("\nOpções de Ordenação:");
        displayMessage("1 - Melhor Avaliadas (pela média das temporadas)");
        displayMessage("2 - Pior Avaliadas (pela média das temporadas)");
        displayMessage("0 - Sem ordenação específica");
        return getValidatedInt("Escolha a opção de ordenação:", 0, 2);
    }

    public void displayAllSeries(List<Series> list) {
        // (Lógica completa conforme você forneceu)
        if (list == null || list.isEmpty()) { return; }
        displayMessage("\n=== LISTA DE SÉRIES (" + list.size() + ") ===");
        list.forEach(this::displaySeriesDetails);
        displayMessage("--- Fim da Lista ---");
    }

    public void displaySeriesListBrief(List<Series> list) {
        // (Lógica completa conforme você forneceu)
        if (list == null || list.isEmpty()) return;
        list.forEach(s -> displayMessage(String.format("- [%d] %s (%d) - Média Temp.: %.1f",
                s.getSeriesId(), s.getTitle(), s.getReleaseYear(), s.getAverageRating())));
    }

    @Override // Sobrescrevendo de MediaView se displayMessage estiver lá
    public void displayMessage(String message) {
        System.out.println(message);
    }
}