package com.agrodirecto.common.dto;

import java.util.List;

public record PageResponse<T>(List<T> contenido, int pagina, int tamano, long total, int totalPaginas) {

    public static <T> PageResponse<T> de(List<T> contenido, int pagina, int tamano, long total) {
        int paginas = tamano <= 0 ? 0 : (int) Math.ceil(total / (double) tamano);
        return new PageResponse<>(contenido, pagina, tamano, total, paginas);
    }
}
