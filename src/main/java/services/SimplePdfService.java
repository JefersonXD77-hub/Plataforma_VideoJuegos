
package services;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


public final class SimplePdfService {
    private SimplePdfService() {}

    public static byte[] onePageText(List<String> lines) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

           
            StringBuilder content = new StringBuilder();
            content.append("BT\n/F1 12 Tf\n72 760 Td\n");
            for (int i = 0; i < lines.size(); i++) {
                String line = escapePdf(lines.get(i));
                content.append("(").append(line).append(") Tj\n");
                if (i < lines.size() - 1) {
                    content.append("0 -16 Td\n");
                }
            }
            content.append("ET\n");
            byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

           
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            StringBuilder xref = new StringBuilder();
            int obj = 1;

            
            java.util.List<Integer> offsets = new java.util.ArrayList<>();
            offsets.add(0); 

            java.util.function.BiConsumer<Integer, byte[]> writeObj = (id, bytes) -> {
                try {
                    offsets.add(body.size());
                    body.write((id + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
                    body.write(bytes);
                    body.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

        
            writeObj.accept(obj++, "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
           
            writeObj.accept(obj++, "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.US_ASCII));
            
            writeObj.accept(obj++, ("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                    "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>").getBytes(StandardCharsets.US_ASCII));
            
            writeObj.accept(obj++, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII));
            
            String stream = "<< /Length " + contentBytes.length + " >>\nstream\n" +
                    new String(contentBytes, StandardCharsets.US_ASCII) + "endstream";
            writeObj.accept(obj++, stream.getBytes(StandardCharsets.US_ASCII));

            out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            int xrefStart = out.size() + body.size();

            out.write(body.toByteArray());

           
            xref.append("xref\n0 ").append(offsets.size()).append("\n");
            xref.append(String.format("%010d 65535 f \n", 0));
            for (int i = 1; i < offsets.size(); i++) {
                xref.append(String.format("%010d 00000 n \n", offsets.get(i) + 9)); 
            }

            out.write(xref.toString().getBytes(StandardCharsets.US_ASCII));

           
            String trailer = "trailer\n<< /Size " + offsets.size() + " /Root 1 0 R >>\n" +
                    "startxref\n" + (body.size() + 9) + "\n%%EOF";
            out.write(trailer.getBytes(StandardCharsets.US_ASCII));

            return out.toByteArray();
        } catch (Exception e) {
            
            return "%PDF-1.4\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        }
    }

    private static String escapePdf(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
