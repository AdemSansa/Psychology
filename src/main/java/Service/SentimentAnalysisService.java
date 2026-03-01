package Service;

import java.util.HashMap;
import java.util.Map;

public class SentimentAnalysisService {

    // Static sentiment analysis with comprehensive word lists
    private static final Map<String, Integer> SENTIMENT_WORDS = new HashMap<>();
    
    static {
        // Initialize sentiment word weights
        // Positive words with weights
        SENTIMENT_WORDS.put("excellent", 3);
        SENTIMENT_WORDS.put("génial", 3);
        SENTIMENT_WORDS.put("super", 2);
        SENTIMENT_WORDS.put("fantastique", 3);
        SENTIMENT_WORDS.put("merveilleux", 3);
        SENTIMENT_WORDS.put("splendide", 3);
        SENTIMENT_WORDS.put("formidable", 3);
        SENTIMENT_WORDS.put("époustouflant", 3);
        SENTIMENT_WORDS.put("incroyable", 2);
        SENTIMENT_WORDS.put("admirable", 2);
        SENTIMENT_WORDS.put("bravo", 2);
        SENTIMENT_WORDS.put("félicitations", 2);
        SENTIMENT_WORDS.put("succès", 2);
        SENTIMENT_WORDS.put("réussi", 2);
        SENTIMENT_WORDS.put("heureux", 2);
        SENTIMENT_WORDS.put("content", 2);
        SENTIMENT_WORDS.put("satisfait", 2);
        SENTIMENT_WORDS.put("ravi", 2);
        SENTIMENT_WORDS.put("enthousiaste", 2);
        SENTIMENT_WORDS.put("joyeux", 2);
        SENTIMENT_WORDS.put("agréable", 1);
        SENTIMENT_WORDS.put("beau", 1);
        SENTIMENT_WORDS.put("bon", 1);
        SENTIMENT_WORDS.put("bien", 1);
        SENTIMENT_WORDS.put("merci", 1);
        SENTIMENT_WORDS.put("amour", 2);
        SENTIMENT_WORDS.put("adore", 2);
        SENTIMENT_WORDS.put("parfait", 2);
        SENTIMENT_WORDS.put("magnifique", 2);
        SENTIMENT_WORDS.put("adorable", 1);
        SENTIMENT_WORDS.put("charmant", 1);
        SENTIMENT_WORDS.put("positif", 1);
        SENTIMENT_WORDS.put("excellent", 3);
        
        // Negative words with weights
        SENTIMENT_WORDS.put("terrible", -3);
        SENTIMENT_WORDS.put("horrible", -3);
        SENTIMENT_WORDS.put("catastrophe", -3);
        SENTIMENT_WORDS.put("désastreux", -3);
        SENTIMENT_WORDS.put("abominable", -3);
        SENTIMENT_WORDS.put("exécrable", -3);
        SENTIMENT_WORDS.put("affreux", -3);
        SENTIMENT_WORDS.put("épouvantable", -3);
        SENTIMENT_WORDS.put("calamiteux", -3);
        SENTIMENT_WORDS.put("nul", -2);
        SENTIMENT_WORDS.put("mauvais", -2);
        SENTIMENT_WORDS.put("pourri", -2);
        SENTIMENT_WORDS.put("minable", -2);
        SENTIMENT_WORDS.put("inutile", -2);
        SENTIMENT_WORDS.put("déçu", -2);
        SENTIMENT_WORDS.put("frustré", -2);
        SENTIMENT_WORDS.put("dégouté", -2);
        SENTIMENT_WORDS.put("colère", -2);
        SENTIMENT_WORDS.put("fâché", -2);
        SENTIMENT_WORDS.put("énervé", -2);
        SENTIMENT_WORDS.put("agacé", -1);
        SENTIMENT_WORDS.put("triste", -1);
        SENTIMENT_WORDS.put("inquiet", -1);
        SENTIMENT_WORDS.put("préoccupé", -1);
        SENTIMENT_WORDS.put("mécontent", -1);
        SENTIMENT_WORDS.put("problème", -1);
        SENTIMENT_WORDS.put("erreur", -1);
        SENTIMENT_WORDS.put("échec", -1);
        SENTIMENT_WORDS.put("haine", -2);
        SENTIMENT_WORDS.put("déteste", -2);
        SENTIMENT_WORDS.put("échoué", -2);
        SENTIMENT_WORDS.put("négatif", -1);
        SENTIMENT_WORDS.put("difficile", -1);
        SENTIMENT_WORDS.put("compliqué", -1);
        
        // Neutral words (weight 0)
        SENTIMENT_WORDS.put("normal", 0);
        SENTIMENT_WORDS.put("correct", 0);
        SENTIMENT_WORDS.put("acceptable", 0);
        SENTIMENT_WORDS.put("passable", 0);
        SENTIMENT_WORDS.put("moyen", 0);
        SENTIMENT_WORDS.put("régulier", 0);
    }

    public String analyzeSentiment(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return "😐 Neutre";
            }
            
            String lowerText = text.toLowerCase().trim();
            int sentimentScore = 0;
            int wordCount = 0;
            
            System.out.println("Analyzing text: " + lowerText);
            
            // Analyze each word in the text
            String[] words = lowerText.split("\\s+");
            for (String word : words) {
                // Remove punctuation
                String cleanWord = word.replaceAll("[^a-zA-Zàâäéèêëïîôöùûüÿç]", "");
                
                if (cleanWord.isEmpty()) continue;
                
                System.out.println("Checking word: '" + cleanWord + "'");
                
                // Check for exact matches first
                if (SENTIMENT_WORDS.containsKey(cleanWord)) {
                    int weight = SENTIMENT_WORDS.get(cleanWord);
                    sentimentScore += weight;
                    wordCount++;
                    System.out.println("Found exact match: '" + cleanWord + "' with weight " + weight);
                    continue;
                }
                
                // Check for partial matches (substrings)
                boolean foundPartial = false;
                for (Map.Entry<String, Integer> entry : SENTIMENT_WORDS.entrySet()) {
                    String sentimentWord = entry.getKey();
                    if (cleanWord.contains(sentimentWord) && cleanWord.length() > 3) {
                        sentimentScore += entry.getValue();
                        wordCount++;
                        System.out.println("Found partial match: '" + cleanWord + "' contains '" + sentimentWord + "' with weight " + entry.getValue());
                        foundPartial = true;
                        break; // Only count one match per word
                    }
                }
                
                if (!foundPartial) {
                    System.out.println("No match found for: '" + cleanWord + "'");
                }
            }
            
            System.out.println("Final score: " + sentimentScore + ", words found: " + wordCount);
            
            // Calculate sentiment based on score
            if (wordCount == 0) {
                System.out.println("No sentiment words found, returning neutral");
                return "😐 Neutre"; // No sentiment words found
            }
            
            double averageScore = (double) sentimentScore / wordCount;
            
            System.out.println("Text: " + text.substring(0, Math.min(50, text.length())) + "...");
            System.out.println("Sentiment Score: " + sentimentScore + " (words: " + wordCount + ", avg: " + String.format("%.2f", averageScore) + ")");
            
            // Determine sentiment based on average score - lowered threshold
            if (averageScore >= 0.5) {
                System.out.println("Returning POSITIVE (avg >= 0.5)");
                return "😊 Positif";
            } else if (averageScore <= -0.5) {
                System.out.println("Returning NEGATIVE (avg <= -0.5)");
                return "😡 Négatif";
            } else {
                System.out.println("Returning NEUTRAL (between -0.5 and 0.5)");
                return "😐 Neutre";
            }
            
        } catch (Exception e) {
            System.err.println("Sentiment analysis error: " + e.getMessage());
            e.printStackTrace();
            return "😐 Neutre";
        }
    }
    
    // Additional method for more detailed analysis
    public SentimentResult getDetailedSentiment(String text) {
        String sentiment = analyzeSentiment(text);
        return new SentimentResult(sentiment, text);
    }
    
    // Helper class for detailed results
    public static class SentimentResult {
        private final String sentiment;
        private final String originalText;
        
        public SentimentResult(String sentiment, String originalText) {
            this.sentiment = sentiment;
            this.originalText = originalText;
        }
        
        public String getSentiment() {
            return sentiment;
        }
        
        public String getOriginalText() {
            return originalText;
        }
        
        public boolean isPositive() {
            return sentiment.equals("😊 Positif");
        }
        
        public boolean isNegative() {
            return sentiment.equals("😡 Négatif");
        }
        
        public boolean isNeutral() {
            return sentiment.equals("😐 Neutre");
        }
    }
}
