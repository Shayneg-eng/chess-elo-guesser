# Chess Elo Guesser

A Java/Maven tool that estimates a chess player's Elo rating from their games.

## Build & run
```bash
mvn package
java -jar target/*.jar <pgn-file>
```

See `ChessGameExtractor.java` for how games are parsed. Pairs well with neural
chess-strength estimators (e.g. a PyTorch model trained on the extracted features).
