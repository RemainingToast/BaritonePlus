package baritone.plus.api.util.serialization;

public interface IListConfigFile {
    void onLoadStart();

    void addLine(String line);
}
