package util;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VenueCode {
    private static final Pattern PATTERN = Pattern.compile("([CM])([A-Z])([A-Z])([A-Z])(\\d)(\\d{3})");
    private static final Set<String> VALID_BUILDINGS = Set.of("A", "B", "C", "E", "L", "M", "N", "Q", "S", "U", "X");
    private static final Map<String, String> BUILDING_MAP = Map.ofEntries(
            Map.entry("A", "FOB"),
            Map.entry("B", "CDP"),
            Map.entry("C", "Exam Hall"),
            Map.entry("E", "SEC"),
            Map.entry("L", "FET"),
            Map.entry("M", "Admin"),
            Map.entry("N", "FIST"),
            Map.entry("Q", "Library"),
            Map.entry("S", "CLC"),
            Map.entry("U", "ACR"),
            Map.entry("X", "Plaza Siswa")
    );

    private VenueCode() {
    }

    public static boolean isValid(String code) {
        if (code == null || code.length() < 7) {
            return false;
        }
        Matcher matcher = PATTERN.matcher(code.toUpperCase());
        if (!matcher.matches()) {
            return false;
        }
        String building = matcher.group(2);
        if (!VALID_BUILDINGS.contains(building)) {
            return false;
        }
        return true;
    }

    public static String describe(String code) {
        Matcher matcher = PATTERN.matcher(code.toUpperCase());
        if (!matcher.matches()) {
            return "Invalid code";
        }
        String campus = matcher.group(1).equals("C") ? "Cyberjaya" : "Melaka";
        String building = matcher.group(2);
        String wing = matcher.group(3);
        String type = matcher.group(4);
        String floor = matcher.group(5);
        String room = matcher.group(6);
        String buildingName = BUILDING_MAP.getOrDefault(building, "Unknown");
        return String.format("Campus: %s, Building: %s (%s), Wing: %s, Type: %s, Floor: %s, Room: %s",
                campus, building, buildingName, wing, decodeType(type), decodeFloor(floor), room);
    }

    private static String decodeType(String type) {
        return switch (type) {
            case "R" -> "Room";
            case "U" -> "Utility";
            case "T" -> "Toilet";
            case "E" -> "External";
            case "X" -> "Theatre";
            default -> "Unknown";
        };
    }

    private static String decodeFloor(String floor) {
        return switch (floor) {
            case "0" -> "Ground";
            case "1" -> "Level 1";
            case "2" -> "Level 2";
            case "3" -> "Level 3";
            default -> "Level " + floor;
        };
    }
}
