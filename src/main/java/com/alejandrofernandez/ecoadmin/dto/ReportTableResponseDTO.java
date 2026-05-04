package com.alejandrofernandez.ecoadmin.dto;

import java.util.List;

public record ReportTableResponseDTO(
        List<String> columns,
        List<List<Object>> rows,
        int total) {
}