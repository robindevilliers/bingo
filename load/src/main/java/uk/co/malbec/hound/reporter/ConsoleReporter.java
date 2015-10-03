package uk.co.malbec.hound.reporter;


import uk.co.malbec.hound.Sample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.reporter.ConsoleReporter.Alignment.CENTER;
import static uk.co.malbec.hound.reporter.ConsoleReporter.Alignment.LEFT;
import static uk.co.malbec.hound.reporter.ConsoleReporter.Alignment.RIGHT;

public class ConsoleReporter {

    public static enum Alignment {
        CENTER, RIGHT, LEFT
    }

    public void print(List<Sample> allSamples) {

        List<String> columnNames = allSamples.stream().map(s -> s.getOperationName()).distinct().collect(toList());

        Table timeProfileTable = new Table();
        timeProfileTable.addRow("Total Number of Operations");
        timeProfileTable.addRow("Number of Operations < 800ms");
        timeProfileTable.addRow("Number of Operations 800ms < t < 1200ms");
        timeProfileTable.addRow("Number of Operations > 1200ms");
        timeProfileTable.addRow("Number of Operations that Failed");

        timeProfileTable.addColumn("All Samples");
        generateTimeProfilesFor(timeProfileTable, 0, allSamples);

        for (int columnIndex = 1; columnIndex <= columnNames.size(); columnIndex++) {
            String columnName = columnNames.get(columnIndex - 1);
            timeProfileTable.addColumn(columnName + " ");
            generateTimeProfilesFor(timeProfileTable, columnIndex, allSamples.stream().filter(s -> s.getOperationName().equals(columnName)).collect(toList()));
        }

        printTable(timeProfileTable, "Time Profile");


        Table distributionsTable = new Table();
        distributionsTable.addColumn("All Samples");

        List<Long> allTimes = allSamples.stream().map(s -> s.getEnd() - s.getStart()).collect(toList());
        Long min = allTimes.stream().min(Comparator.naturalOrder()).orElse(0L);
        Long max = allTimes.stream().max(Comparator.naturalOrder()).orElse(0L) + 10;

        range(0, (max.intValue() - min.intValue()) / 10).forEach(i -> {
            distributionsTable.addRow("" + (i * 10 + min) + "ms - " + ((i * 10) + 10 + min) + "ms :");
        });
        generateTimeDistributions(distributionsTable, 0, allTimes, min.intValue(), max.intValue());

        for (int columnIndex = 1; columnIndex <= columnNames.size(); columnIndex++) {
            String columnName = columnNames.get(columnIndex - 1);
            distributionsTable.addColumn(columnName + " ");
            generateTimeDistributions(distributionsTable, columnIndex, allSamples.stream().filter(s -> s.getOperationName().equals(columnName)).map(s -> s.getEnd() - s.getStart()).collect(toList()), min.intValue(), max.intValue());
        }

        List<Integer> rowsToRemove = new ArrayList<>();
        for (int rowIndex = distributionsTable.getHeight()-1; rowIndex >= 0 ; rowIndex--){
            if (distributionsTable.getValue(rowIndex,0) == 0) {
                rowsToRemove.add(rowIndex);
            }
        }
        rowsToRemove.forEach(i -> distributionsTable.removeRow(i));


        printTable(distributionsTable, "Time Distributions");
    }

    public void generateTimeProfilesFor(Table table, int columnIndex, List<Sample> samples) {
        List<Long> allTimes = samples.stream().map(s -> s.getEnd() - s.getStart()).collect(toList());
        table.setValue(0, columnIndex, (long) samples.size());
        table.setValue(1, columnIndex, allTimes.stream().filter(i -> i < 800).count());
        table.setValue(2, columnIndex, allTimes.stream().filter(i -> i >= 800 && i < 1200).count());
        table.setValue(3, columnIndex, allTimes.stream().filter(i -> i >= 1200).count());
    }


    public static void generateTimeDistributions(Table table, int columnIndex, List<Long> samples, int min, int max) {


        range(0, (max - min) / 10).forEach(i -> {
            long count = samples.stream().filter(j -> (i * 10 + min) <= j && ((i * 10) + 10 + min) > j).count();
            table.setValue(i, columnIndex, count);
        });
    }

    private void printTable(Table table, String title) {
        int titleColumnWidth = table.getRowNames().stream().map(String::length).max(Comparator.naturalOrder()).orElse(0);
        int sampleColumnWidth = table.getColumnNames().stream().map(String::length).max(Comparator.naturalOrder()).orElse(0);

        System.out.println(s(title, '-', titleColumnWidth + (table.getColumnNames().size() * sampleColumnWidth), CENTER));

        StringBuilder header = new StringBuilder();
        header.append(s("", ' ', titleColumnWidth, LEFT));
        table.getColumnNames().forEach(name -> {
            header.append(s(name, ' ', sampleColumnWidth, CENTER));
        });
        System.out.println(header.toString());

        for (int row = 0; row < table.getHeight(); row++) {
            StringBuilder line = new StringBuilder();
            line.append(s(table.getRowName(row), ' ', titleColumnWidth, LEFT));
            for (int column = 0; column < table.getWidth(); column++) {
                line.append(s(table.getValue(row, column).toString(), ' ', sampleColumnWidth, RIGHT));
            }
            System.out.println(line.toString());
        }
    }

    public static String s(String title, char fillChar, int width, Alignment alignment) {
        String template = "--------------------------------------------------------------------------------------------------------------------------------------------";

        if (alignment == CENTER) {
            int fillWidth = (width - title.length()) / 2;
            return template.replace('-', fillChar).substring(0, fillWidth) + title + template.replace('-', fillChar).substring(0, fillWidth) + (title.length() % 2 == 1 ? fillChar : "");
        } else if (alignment == LEFT) {
            return title + template.replace('-', fillChar).substring(0, width - title.length());
        } else if (alignment == Alignment.RIGHT) {
            return template.replace('-', fillChar).substring(0, width - title.length()) + title;
        }
        return "";
    }

    public static class Table {

        private List<List<Long>> rows = new ArrayList<List<Long>>();

        private List<String> rowNames = new ArrayList<>();
        private List<String> columnNames = new ArrayList<>();

        public void addRow(String name) {
            rowNames.add(name);
            List<Long> row = new ArrayList<>();
            columnNames.forEach(s -> row.add(0L));
            rows.add(row);
        }

        public void addColumn(String name) {
            columnNames.add(name);
            rows.stream().forEach(l -> l.add(0L));
        }

        public void setValue(int rowIndex, int columnIndex, Long value) {
            rows.get(rowIndex).set(columnIndex, value);
        }

        public Long getValue(int rowIndex, int columnIndex) {
            return rows.get(rowIndex).get(columnIndex);
        }

        public List<String> getRowNames() {
            return rowNames;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }

        public int getHeight() {
            return rowNames.size();
        }

        public int getWidth() {
            return columnNames.size();
        }

        public String getRowName(int row) {
            return rowNames.get(row);
        }

        public void removeRow(Integer rowIndex) {
            rowNames.remove((int) rowIndex);
            rows.remove((int) rowIndex);
        }
    }
}
