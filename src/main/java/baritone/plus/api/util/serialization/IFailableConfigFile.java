package baritone.plus.api.util.serialization;

public interface IFailableConfigFile {
    void onFailLoad();

    boolean failedToLoad();
}
