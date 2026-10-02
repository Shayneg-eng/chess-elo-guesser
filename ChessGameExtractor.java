import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChessGameExtractor {

    // Main method expects the PNG file path as an argument.
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ChessGameExtractor <image_file.png>");
            return;
        }
        
        String filePath = args[0];
        ITesseract tesseract = new Tesseract();
        // (Optional) Set the tessdata directory if it's not on your PATH:
        // tesseract.setDatapath("path_to_tessdata");
        
        try {
            // Perform OCR on the input PNG file
            String ocrText = tesseract.doOCR(new File(filePath));
            // For debugging, you might want to print the complete OCR output.
            // System.out.println("OCR Result:\n" + ocrText);
            
            // Split the text into separate game blocks.
            // We assume that each game starts with "[Event " so we split on that (using a positive lookahead)
            String[] gameBlocks = ocrText.split("(?=\\[Event\\s\")");
            
            for (String block : gameBlocks) {
                if (block.trim().isEmpty()) continue; // skip empty blocks
                
                // Extract header fields using regular expressions.
                String whitePlayer = extractField(block, "White");
                String blackPlayer = extractField(block, "Black");
                String result = extractField(block, "Result");
                String whiteElo = extractField(block, "WhiteElo");
                String blackElo = extractField(block, "BlackElo");
                
                // Extract the moves: we search for the first move number (like "1. ") and take all text following it.
                String moves = extractMoves(block);
                
                // Determine the winner based on the result field.
                String winner;
                if ("1-0".equals(result.trim())) {
                    winner = whitePlayer;
                } else if ("0-1".equals(result.trim())) {
                    winner = blackPlayer;
                } else {
                    winner = "Draw";
                }
                
                // Print out the extracted details.
                System.out.println("Game:");
                System.out.println("White: " + whitePlayer + " (Rating: " + whiteElo + ")");
                System.out.println("Black: " + blackPlayer + " (Rating: " + blackElo + ")");
                System.out.println("Result: " + result + " | Winner: " + winner);
                System.out.println("Moves: " + moves);
                System.out.println("---------------------------------------------------------");
            }
            
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Extracts the value of a given header field (for example, White, Black, Result, etc.)
     * from the PGN-like text block.
     */
    private static String extractField(String text, String fieldName) {
        // Matches patterns like: [FieldName "value"]
        Pattern pattern = Pattern.compile("\\[" + fieldName + "\\s+\"([^\"]+)\"\\]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    /**
     * Extracts the moves string from a game block.
     * Assumes that the move section starts at the first occurrence of a move number (e.g., "1.").
     */
    private static String extractMoves(String block) {
        Pattern movePattern = Pattern.compile("\\d+\\.\\s");
        Matcher matcher = movePattern.matcher(block);
        if (matcher.find()) {
            int startIndex = matcher.start();
            // Everything from the first move number to the end is taken as the moves string.
            return block.substring(startIndex).replaceAll("\\s+", " ").trim();
        }
        return "";
    }
}