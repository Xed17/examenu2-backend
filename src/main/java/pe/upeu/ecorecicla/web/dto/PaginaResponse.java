package pe.upeu.ecorecicla.web.dto;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record PaginaResponse<T>(
    List<T> contenido,
    int pagina,
    int tamanio,
    long totalElementos,
    int totalPaginas,
    boolean ultima
) {
    public static <E, D> PaginaResponse<D> from(Page<E> page, Function<E, D> mapper) {
        List<D> dtos = page.getContent().stream().map(mapper).collect(Collectors.toList());
        return new PaginaResponse<>(
            dtos,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}

