package util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Validates a diploma or certificate file by scanning its text content
 * for psychology-related keywords. Supports PDF and plain-text files.
 */
public class DiplomaValidator {

    /** Keywords that indicate a psychology / therapy domain document. */
    private static final List<String> PSYCHOLOGY_KEYWORDS = List.of(
            "therapist",
            "thérapeute",
            "therapiste",
            "psychologue",
            "psychologie",
            "psychology",
            "psychiatre",
            "psychiatry",
            "psychothérapie",
            "psychotherapy",
            "counselor",
            "counsellor",
            "clinique",
            "clinical",
            "mental health",
            "santé mentale",
            "psychanalyste",
            "psychoanalyste",
            "neuropsychologie",
            "neuropsychology");

    /**
     * Validates a diploma/certificate file.
     *
     * @param file the uploaded file (PDF or TXT)
     * @return {@code true} if at least one psychology keyword is found in the
     *         document
     * @throws IOException                   if the file cannot be read
     * @throws UnsupportedOperationException if the file type is not supported
     */
    public static boolean validateDiploma(File file) throws IOException {
        if (file == null || !file.exists()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        String content;

        if (name.endsWith(".pdf")) {
            content = extractTextFromPdf(file);
        } else if (name.endsWith(".txt")) {
            content = Files.readString(file.toPath());
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported file type. Please upload a PDF or TXT file.");
        }

        if (content == null || content.isBlank()) {
            return false;
        }

        String lowerContent = content.toLowerCase();
        for (String keyword : PSYCHOLOGY_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /** Extracts all text from a PDF file using Apache PDFBox. */
    private static String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
