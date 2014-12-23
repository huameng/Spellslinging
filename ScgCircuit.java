import java.util.*;
import java.io.*;

public class ScgCircuit {
  static final String BYE = "* bye *";
  static final String BYE2 = "* awarded bye *";
  static Map<String, Player> players;
  
  static Tournament curTournament;
  static File cheaters = new File("cheaters.txt");
  static File pcnames = new File("pcnames.txt");
  
  public static void startTournament(String tournamentName, int standardRounds, int legacyRounds) throws IOException {
    curTournament = new Tournament('S');
    for(int i=1;i<=standardRounds;++i) {
      // Standard
      File thisRound = new File("tournaments/" + tournamentName + "/s" + i + ".txt");
      parseResults(new Scanner(thisRound));
    }
    curTournament.endTournament();
    curTournament = new Tournament('L');
    for(int i=1;i<=legacyRounds;++i) {
      // Legacy
      File thisRound = new File("tournaments/" + tournamentName + "/l" + i + ".txt");
      parseResults(new Scanner(thisRound));
    }
    curTournament.endTournament();
  }
  
  // TODO: put these in a file
  public static String fixName(String name) {
    name = name.trim();
    name = name.substring(0,name.indexOf("<"));
    name = name.toLowerCase();
    if (name.equals("ross, thomas r") || name.equals("ross, tom")) name = "ross, tom r";
    if (name.equals("braunduin, brian s")) name = "braun-duin, brian s";
    if (name.equals("merriam, ross")) name = "merriam, ross c";
    if (name.equals("vanmeter, chris")) name = "vanmeter, chris e";
    if (name.equals("mize, logan")) name = "mize, logan g";
    if (name.equals("nelson, brad")) name = "nelson, brad j";
    if (name.equals("mann, stephen")) name = "mann, stephen g";
    if (name.equals("davis, jim")) name = "davis, jim i";
    return name;
  }
  
  public static void parseResults(Scanner results) throws IOException {
    while(results.hasNext()) {
      String match = results.nextLine();
      if (!match.startsWith("<tr>")) {
        continue;
      }
      String[] parts = match.split("<td>");
      /*
       * part 0: worthless
       * part 1: worthless
       * part 2: p1 name
       * part 3: result word, then games if needed
       * part 4: worthless
       * part 5: p2 name
      */
      String[] names = new String[]{fixName(parts[2]), fixName(parts[5])};
      // String name1 = fixName(parts[2]);
      // String name2 = fixName(parts[5]);
      // TODO: replace this with a group
      if (names[1].equals(BYE) || names[1].equals(BYE2)) {
        continue;
      }
      if (isGroupMember(names[0], cheaters) || isGroupMember(names[1], cheaters)) {
        continue;
      }
      char result = parts[3].charAt(0);
      if (result == 'D') {
        if (isIntentionalDraw(parts[3].trim())) {
          continue;
        }
      }
      Player[] thesePlayers = new Player[]{players.get(names[0]), players.get(names[1])};
      for(int i=0;i<thesePlayers.length;++i) {
        if (thesePlayers[i] == null) {
          thesePlayers[i] = new Player(names[i]);
          players.put(names[i], thesePlayers[i]);
        }
      }
      curTournament.addResult(thesePlayers[0], thesePlayers[1], result);
    }
  }
  
  public static boolean isIntentionalDraw(String matchResult) {
    matchResult = matchResult.substring(0,matchResult.length()-5); // removes </td>
    if (matchResult.charAt(matchResult.length()-1) == '3') return true;
    // if (matchResult.substring(5).equals("0-0-0")) return true;
    return false;
  }
  
  private static void runEvents(File f) throws IOException {
    Scanner sc = new Scanner(f);
    while(sc.hasNext()) {
      String foo = sc.nextLine();
      String[] foos = foo.split(",");
      startTournament(foos[0], Integer.parseInt(foos[1]), Integer.parseInt(foos[2]));
    }
  }
  
  public static void main(String[] args) throws IOException {
    players = new HashMap<String, Player>();
    runEvents(new File("tournaments.txt"));
    List<Player> foo = new ArrayList<Player>();
    for(String s : players.keySet()) foo.add(players.get(s));
    Collections.sort(foo, new CompositeSort());
    // for(Player p : foo) {
    for(int i=0;i<foo.size();++i) {
      if (i >= 10) break;
      Player p = foo.get(i);
      makeProfile(p);
      // p.rank = i+1;
      // System.out.printf("%30s\t%.0f\t%2d\t%4.1f\t%.0f\t%2d\t%4.1f\n", p.name, 
      // p.rating, p.wins+p.draws+p.losses, p.calcWinrate('S'), p.legacyRating, 
      // p.legacyWins+p.legacyDraws+p.legacyLosses, p.calcWinrate('L'));
    }
    // printPcPlayers(foo);
    // printStandardRatings(foo);
  }
  
  static String a = "<!doctype html>\n" +
"<html lang=\"en\">\n"+
"<head>\n"+
"  <meta charset=\"utf-8\">\n"+
"  <title>SCG Test Profile</title>\n"+
"  <script src=\"http://d3js.org/d3.v3.min.js\" charset=\"utf-8\"></script>\n"+
"  <script src=\"../elo.js\" charset=\"utf-8\"></script>\n"+
"  <script src=\"../profile.js\" charset=\"utf-8\"></script>\n"+
"  <script src=\"../d3.legend.js\" charset=\"utf-8\"></script>\n"+
"</head>\n"+
"<body>\n"+
"<style>\n"+
".axis path,\n"+
".axis line {\n"+
"    fill: none;\n"+
"    stroke: black;\n"+
"    shape-rendering: crispEdges;\n"+
"}\n"+
".legend rect {\n"+
"  fill:white;\n"+
"  stroke:black;\n"+
"  opacity:0.8;}\n"+
"</style>\n"+
"<div id=\"graph\">\n"+
"<script>";

static String b= "</script>\n"+
"</body>\n"+
"</html>";
  
  public static void makeProfile(Player p) throws IOException {
    PrintWriter writer = new PrintWriter("histories/" + p.fileName() + "standard.txt", "UTF-8");
    writer.print(p.getRatingHistory('S'));
    writer.close();
    writer = new PrintWriter("histories/" + p.fileName() + "legacy.txt", "UTF-8");
    writer.print(p.getRatingHistory('L'));
    writer.close();
    writer = new PrintWriter("profiles/" + p.fileName() + ".html");
    writer.println(a);
    writer.println("doIt(\"" + p.fileName() + "\", \"" + p.properName() + "\");");
    writer.println(b);
    writer.close();
  }
  
  private static void printStandardRatings(List<Player> players) {
    for(Player p : players) if (p.wins+p.draws+p.losses>=15) System.out.printf("%.0f\n", p.rating);
  }
  
  private static void printPcPlayers(List<Player> sortedPlayers) throws IOException {
    for(int i=0;i<sortedPlayers.size();++i) {
      if (isGroupMember(sortedPlayers.get(i).name, pcnames)) {
        sortedPlayers.get(i).rank=i+1;
        Player p = sortedPlayers.get(i);
        System.out.println(p.name);
        System.out.println("Standard");
        p.printRatingHistory('S');
        System.out.println("Legacy");
        p.printRatingHistory('L');
        // System.out.println(p);
        // System.out.printf("%4d\t%30s\t%.0f\t%3d\t%5.1f\t%.0f\t%3d\t%5.1f\n", i+1, p.name, // \t%5.1f\t%d
        // p.rating, p.wins+p.draws+p.losses, p.calcWinrate('S'), p.legacyRating, 
        // p.legacyWins+p.legacyDraws+p.legacyLosses, p.calcWinrate('L')); // , p.getPcWinrate(), p.getPcGames()
      }
    }
  }
  
  private static boolean isGroupMember(String name, File group) throws IOException {
    Scanner sc = new Scanner(group);
    while(sc.hasNext()) {
      if (name.equals(sc.nextLine())) return true;
    }
    return false;
  }
  
  private static class DiffSort implements Comparator<Player> {
    public int compare(Player a, Player b) {
      return Math.abs(a.rating-a.legacyRating) >= Math.abs(b.rating-b.legacyRating) ? -1 : 1;
    }
  }
  
  private static class StandardSort implements Comparator<Player> {
    public int compare(Player a, Player b) {
      return a.rating >= b.rating ? -1 : 1;
    }
  }
  
  private static class LegacySort implements Comparator<Player> {
    public int compare(Player a, Player b) {
      return a.legacyRating >= b.legacyRating ? -1 : 1;
    }
  }
  
  private static class CompositeSort implements Comparator<Player> {
    public int compare(Player a, Player b) {
      return a.legacyRating+a.rating >= b.legacyRating+b.rating ? -1 : 1;
    }
  }
  
  private static class GamesSort implements Comparator<Player> {
    public int compare(Player a, Player b) {
      return a.getGamesPlayed() >= b.getGamesPlayed() ? -1 : 1;
    }
  }
}

class Tournament {
  private Set<Player> players;
  private char format;
  public String name;
  
  public Tournament(char format) {
    players = new HashSet<Player>();
    this.format = format;
  }
  
  private void addPlayer(Player p) {
    players.add(p);
  }
  
  public void endTournament() {
    for(Player p : players) {
      p.updateRating(format);
    }
    players.clear();
  }
  
  // TODO: remove this method
  public boolean isPcPlayer(String name) {
    if (name.equals("donegan, dylan")) return true;
    if (name.equals("mann, stephen g")) return true;
    if (name.equals("braun-duin, brian s")) return true;
    if (name.equals("nelson, brad j")) return true;
    if (name.equals("ross, tom r")) return true;
    if (name.equals("ketter, kent")) return true;
    if (name.equals("duke, reid")) return true;
    if (name.equals("lossett, joe")) return true;
    if (name.equals("jones, kevin")) return true;
    if (name.equals("davis, jim i")) return true;
    if (name.equals("merriam, ross c")) return true;
    if (name.equals("fabiano, gerard")) return true;
    if (name.equals("mize, logan g")) return true;
    if (name.equals("hoogland, jeff")) return true;
    if (name.equals("sheets, derrick w")) return true;
    if (name.equals("vanmeter, chris e")) return true;
    return false;
  }
  
  public void addResult(Player p1, Player p2, char result) {
    if (!players.contains(p1)) players.add(p1);
    if (!players.contains(p2)) players.add(p2);
    if (isPcPlayer(p2.name)) p1.addPcResult(result);
    p1.addResult(result, p2.getRating(format), format);
    char oppositeResult = (result == 'W' ? 'L' : (result == 'L' ? 'W' : 'D'));
    if (isPcPlayer(p1.name)) p2.addPcResult(oppositeResult);

    p2.addResult(oppositeResult, p1.getRating(format), format);
  }
}

class Player {

  private class RatingSnapshot {
    public double rating;
    public int date; // TODO: use this somehow
  }
  
  String name;
  double rating;
  double matchPoints;
  double expectedMatchPoints;
  int wins;
  int losses;
  int draws;
  double legacyRating;
  int legacyWins;
  int legacyLosses;
  int legacyDraws;
  int pcWins;
  int pcDraws;
  int pcLosses;
  int rank;
  List<RatingSnapshot> ratingHistory;
  List<RatingSnapshot> legacyRatingHistory;
  
  public String fileName() {
    return name.replaceAll("[^a-zA-Z]", "");
  }
  
  public String properName() {
    // format: lastname, firstname [middle initial]
    String newName = name.replaceAll(",", "");
    String[] foo = newName.split("\\s+");
    newName = uppercaseFirstLetter(foo[1]) + " " + uppercaseFirstLetter(foo[0]);
    return newName;
  }
  
  public String uppercaseFirstLetter(String s) {
    return s.substring(0,1).toUpperCase()+s.substring(1);
  }
  
  public int getRank() {
    return rank;
  }
  
  public int getPcGames() {
    return pcWins+pcDraws+pcLosses;
  }
  
  public double getPcWinrate() {
    return 100.0*(pcWins+.5*pcDraws)/getPcGames();
  }
  
  public int getGamesPlayed() {
    return wins+legacyWins + draws+legacyDraws + losses+legacyLosses;
  }
  
  public double getRating(char format) {
    if (format == 'S') return rating;
    else return legacyRating;
  }
  
  public double calcWinrate(char format) {
    if (format == 'S') return 100*(1.0*wins+.5*draws)/(wins+draws+losses);
    else return 100*(1.0*legacyWins+.5*legacyDraws)/(legacyWins+legacyDraws+legacyLosses);
  }
  
  public Player(String name) {
    this.name = name;
    this.rating = 1600;
    this.legacyRating = 1600;
    this.matchPoints = 0;
    this.expectedMatchPoints = 0;
    this.ratingHistory = new ArrayList<RatingSnapshot>();
    this.legacyRatingHistory = new ArrayList<RatingSnapshot>();
  }
  
  public void addPcResult(char result) {
    switch (result) {
      case 'W':
        this.pcWins += 1;
        break;
      case 'D':
        this.pcDraws += 1;
        break;
      case 'L':
        this.pcLosses += 1;
        break;
    }
  }
  
  public void addResult(char result, double opponentRating, char format) {
    this.expectedMatchPoints += 1/(1+Math.pow(10, (opponentRating - this.getRating(format))/800));
    switch (result) {
      case 'W':
        this.matchPoints += 1;
        if (format == 'S') this.wins++;
        else this.legacyWins++;
        break;
      case 'D':
        this.matchPoints +=.5;
        if (format == 'S') this.draws++;
        else this.legacyDraws++;
        break;
      case 'L':
       if (format == 'S') this.losses++;
       else this.legacyLosses++;
    }
  }
  
  public String getRatingHistory(char format) {
    StringBuilder sb = new StringBuilder();
    if (format == 'S') {
      for(RatingSnapshot rs : this.ratingHistory) {
        sb.append(rs.rating).append('\n');
      }
    } else {
      for(RatingSnapshot rs : this.legacyRatingHistory) {
        sb.append(rs.rating).append('\n');
      }
    }
    return sb.toString().trim();
  }
  
  public void printRatingHistory(char format) {
    if (format == 'S') {
      for(RatingSnapshot rs : this.ratingHistory) {
        System.out.println(rs.rating);
      }
    } else {
      for(RatingSnapshot rs : this.legacyRatingHistory) {
        System.out.println(rs.rating);
      }
    }
  }
  
  public void updateRating(char format) {
    if (format == 'S') {
      this.rating = this.rating + 32*(this.matchPoints - this.expectedMatchPoints);
      RatingSnapshot rs = new RatingSnapshot();
      rs.rating = this.rating;
      ratingHistory.add(rs);
    }
    else {
      this.legacyRating = this.legacyRating + 32*(this.matchPoints - this.expectedMatchPoints);
      RatingSnapshot rs = new RatingSnapshot();
      rs.rating = this.legacyRating;
      legacyRatingHistory.add(rs);
    }
    this.matchPoints = 0;
    this.expectedMatchPoints = 0;
  }
  
  public boolean equals(Player p) {
    return this.name.equals(p.name);
  }
  
  public String getStandardRating() {
    return String.format("%.0f", rating);
  }
  
  public String getStandardWinrate() {
    return String.format("%.1f", calcWinrate('S'));
  }
  
  public String getLegacyRating() {
    return String.format("%.0f", legacyRating);
  }
  
  public String getLegacyWinrate() {
    return String.format("%.1f", calcWinrate('L'));
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.properName()).append('\n');
    sb.append("Elo Rank: ").append(this.getRank()).append('\n');
    sb.append("Standard: ").append(this.getStandardRating()).append(" Elo, ").append(this.getStandardWinrate()).append(" Win Percentage\n");
    sb.append("Legacy: ").append(this.getLegacyRating()).append(" Elo, ").append(this.getLegacyWinrate()).append(" Win Percentage\n");
    sb.append("Vs Players Championship Competitors: ").append(this.pcWins).append("-").append(this.pcLosses).append("-").append(this.pcDraws).append("\n");
    return sb.toString();
  }
}