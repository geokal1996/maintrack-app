package com.codingfactory.maintrack.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Voithitikes gia to diavasma keliwn apo Excel.
//
// To provlima pou lynei: to idio "noima" (p.x. mia imerominia) mporei na erthei
// me 4 diaforetikous tropous - san pragmatiki imerominia tou Excel, san arithmos,
// san keimeno "05/08/2026", i san keimeno "20260805". Anti na to elegxoume auto
// se kathe stili xexorista, to kanoume mia fora edo.
final class ExcelCells {

    private ExcelCells() {
    }

    // Oi morfes imerominias pou synantame sta exports
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd")
    );

    private static final List<DateTimeFormatter> TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm")
    );

    // Diavazei ena keli san keimeno. Epistrefei null an einai adeio,
    // etsi elegxoume pantou omoiomorfa me "== null".
    static String str(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        String value;
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                value = cell.getLocalDateTimeCellValue().toString();
            } else {
                double n = cell.getNumericCellValue();
                // Akeraios -> "10184947" kai oxi "1.0184947E7"
                value = n == Math.floor(n) ? String.valueOf((long) n) : String.valueOf(n);
            }
        } else {
            value = cell.toString();
        }

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    static Integer integer(Row row, int col) {
        String text = str(row, col);
        if (text == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static Double number(Row row, int col) {
        String text = str(row, col);
        if (text == null) {
            return null;
        }
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static LocalDate date(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        // Periptosi 1: to Excel to kratae san pragmatiki imerominia
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        // Periptosi 2: erxetai san keimeno
        String text = str(row, col);
        if (text == null) {
            return null;
        }
        if (text.length() > 10 && text.contains("T")) {
            text = text.substring(0, text.indexOf('T'));
        }
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(text, fmt);
            } catch (Exception ignored) {
                // dokimazoume tin epomeni morfi
            }
        }
        return null;
    }

    static LocalTime time(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        // To Excel apothikevei tin ora san klasma tis imeras (0.5 = 12:00)
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalTime();
            }
            double fraction = cell.getNumericCellValue();
            if (fraction >= 0 && fraction < 1) {
                return LocalTime.ofSecondOfDay(Math.round(fraction * 86400) % 86400);
            }
        }

        String text = str(row, col);
        if (text == null) {
            return null;
        }
        if (text.contains("T")) {
            text = text.substring(text.indexOf('T') + 1);
        }
        for (DateTimeFormatter fmt : TIME_FORMATS) {
            try {
                return LocalTime.parse(text, fmt);
            } catch (Exception ignored) {
                // dokimazoume tin epomeni morfi
            }
        }
        return null;
    }

    // Enonei imerominia + ora se ena LocalDateTime. An leipei i ora, pairnoume 00:00.
    static LocalDateTime dateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        return date.atTime(time != null ? time : LocalTime.MIDNIGHT);
    }
}
