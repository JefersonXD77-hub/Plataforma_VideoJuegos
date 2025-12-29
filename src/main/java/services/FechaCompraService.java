package services;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FechaCompraService {
    private FechaCompraService() {}

    
    public static Timestamp parseNullable(String s) {
        if (s == null) return null;
        String x = s.trim();
        if (x.isEmpty()) return null;

      
        try {
            LocalDateTime ldt = LocalDateTime.parse(x, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Timestamp.valueOf(ldt);
        } catch (Exception ignored) {}

        
        LocalDate ld = LocalDate.parse(x, DateTimeFormatter.ISO_LOCAL_DATE);
        return Timestamp.valueOf(ld.atStartOfDay());
    }
}
