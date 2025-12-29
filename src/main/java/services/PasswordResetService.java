package services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordResetService {

    private static class Entry {
        final String codeHash;
        final long expiresAtMs;
        int attempts;
        boolean used;

        Entry(String codeHash, long expiresAtMs) {
            this.codeHash = codeHash;
            this.expiresAtMs = expiresAtMs;
            this.attempts = 0;
            this.used = false;
        }
    }

    private static final Map<String, Entry> store = new ConcurrentHashMap<>();
    private static final Random rnd = new Random();

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static class IssueResult {
        public final boolean ok;
        public final String mensaje;
        public final String codigoDemo; 

        IssueResult(boolean ok, String mensaje, String codigoDemo) {
            this.ok = ok;
            this.mensaje = mensaje;
            this.codigoDemo = codigoDemo;
        }
    }

    public static class VerifyResult {
        public final boolean ok;
        public final String mensaje;

        VerifyResult(boolean ok, String mensaje) {
            this.ok = ok;
            this.mensaje = mensaje;
        }
    }

    public IssueResult emitirCodigo(String correo, int minutosValidez) {
        if (correo == null || correo.trim().isEmpty()) {
            return new IssueResult(false, "Debe enviar correo.", null);
        }

        String c = correo.trim().toLowerCase();

        String code = String.format("%06d", rnd.nextInt(1000000));
        String h = sha256(code);
        long exp = System.currentTimeMillis() + (long) minutosValidez * 60L * 1000L;

        store.put(c, new Entry(h, exp));

       
        return new IssueResult(true, "Si el correo existe, se envió un código de recuperación.", code);
    }

    public VerifyResult validarCodigo(String correo, String codigo) {
        if (correo == null || codigo == null) return new VerifyResult(false, "Datos incompletos.");
        String c = correo.trim().toLowerCase();
        Entry e = store.get(c);
        if (e == null) return new VerifyResult(false, "Código inválido o expirado.");
        if (e.used) return new VerifyResult(false, "Código ya usado.");
        if (System.currentTimeMillis() > e.expiresAtMs) {
            store.remove(c);
            return new VerifyResult(false, "Código expirado.");
        }
        if (e.attempts >= 5) return new VerifyResult(false, "Demasiados intentos.");
        e.attempts++;

        String h = sha256(codigo.trim());
        if (h == null || !h.equals(e.codeHash)) return new VerifyResult(false, "Código inválido.");
        return new VerifyResult(true, "OK");
    }

    public void invalidar(String correo) {
        if (correo == null) return;
        String c = correo.trim().toLowerCase();
        Entry e = store.get(c);
        if (e != null) e.used = true;
    }
}

