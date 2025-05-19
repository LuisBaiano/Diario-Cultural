package org.diariocultural;

import com.fasterxml.jackson.annotation.JsonCreator; // Para construtor explícito se necessário
import com.fasterxml.jackson.annotation.JsonProperty; // Para mapear nomes JSON se diferentes dos parâmetros
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Representa uma avaliação individual feita por um usuário.
 * Contém a nota (avaliação), um comentário opcional e a data da avaliação.
 * Esta é uma classe do modelo (Model) que pode ser utilizada em sistemas
 * de gerenciamento de mídias como filmes ou séries.
 */
public record Review(
        // Jackson usará estes nomes para os campos JSON por padrão: "rating", "comment", "reviewDate"
        int rating,
        String comment,
        Date reviewDate
) {

    /**
     * Construtor canônico do record.
     * O Jackson pode usar este construtor se os nomes dos parâmetros
     * corresponderem aos campos JSON (o que acontece por padrão para records).
     * Adicionar @JsonCreator e @JsonProperty explicitamente pode ser feito para clareza ou
     * se os nomes dos campos JSON forem diferentes.
     *
     * @param rating     Nota atribuída pelo usuário (ex: 0 a 5).
     * @param comment    Comentário opcional sobre a mídia. Se null, será convertido em string vazia.
     * @param reviewDate Data da avaliação. Pode ser null se desconhecida.
     */
    @JsonCreator // Não estritamente necessário para records se os nomes batem, mas bom para clareza
    public Review(
            @JsonProperty("rating") int rating,
            @JsonProperty("comment") String comment,
            @JsonProperty("reviewDate") Date reviewDate) {
        this.rating = rating;
        this.comment = (comment != null) ? comment : ""; // Garante que comentário não seja null
        this.reviewDate = reviewDate;
    }

    // Os getters (rating(), comment(), reviewDate()) são gerados automaticamente pelo record.

    /**
     * Gera uma representação textual da avaliação, incluindo nota, data e comentário.
     * Útil para listagens simples ou depuração.
     *
     * @return String formatada representando a avaliação.
     */
    @Override
    public String toString() {
        // Cuidado: SimpleDateFormat não é thread-safe. Para aplicações concorrentes,
        // seria melhor criar uma nova instância ou usar DateTimeFormatter do Java 8+.
        // Para uma aplicação de console simples, está OK.
        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDate = (reviewDate != null) ? simpleFormat.format(reviewDate) : "Sem data";
        return "Nota: " + rating + " (" + formattedDate + ") - Comentário: \"" + comment + "\"";
    }
}