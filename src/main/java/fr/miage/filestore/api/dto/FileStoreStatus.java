package fr.miage.filestore.api.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class FileStoreStatus {

    private int nbCpus;
    private long totalMemory;
    private long availableMemory;
    private long maxMemory;
    private Map<String, Long> latestMetrics;
    private Map<String, Long> metrics;

    public FileStoreStatus() {
    }

    public int getNbCpus() {
        return nbCpus;
    }

    public void setNbCpus(int nbCpus) {
        this.nbCpus = nbCpus;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(long availableMemory) {
        this.availableMemory = availableMemory;
    }

    public long isMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public Map<String, Long> getLatestMetrics() {
        return latestMetrics;
    }

    public void setLatestMetrics(Map<String, Long> latestMetrics) {
        this.latestMetrics = latestMetrics;
    }

    public Map<String, Long> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Long> metrics) {
        this.metrics = metrics;
    }
}

