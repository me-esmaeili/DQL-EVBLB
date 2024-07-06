package ir.mesmaeili.lba.util;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

@Slf4j
public class CSVUtils {
    public static <K, V> void writeMapOfListToCsv(String fileName, Map<K, List<V>> map) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            for (Map.Entry<K, List<V>> entry : map.entrySet()) {
                // Convert the key and values to a string array
                String[] line = new String[entry.getValue().size() + 1];
                line[0] = entry.getKey().toString();
                for (int i = 0; i < entry.getValue().size(); i++) {
                    line[i + 1] = entry.getValue().get(i).toString();
                }
                writer.writeNext(line);
            }
        } catch (Exception e) {
            log.error("Could not convert to CSV", e);
        }
    }

    public static <K, V> void writeMapToCsv(String fileName, Map<K, V> map) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                String[] line = new String[2];
                line[0] = entry.getKey().toString();
                line[1] = entry.getValue().toString();
                writer.writeNext(line);
            }
        } catch (Exception e) {
            log.error("Could not convert to CSV", e);
        }
    }
}
