package com.tlopez.utils.timemarker.exporters;

import javafx.stage.FileChooser;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public enum ExportType {
    EditDecisionList(
            new FileChooser.ExtensionFilter("Edit Decision List", "*.edl", "*.EDL"),
            new EdlExporter()),
    TextFile(
            new FileChooser.ExtensionFilter("Text File", "*.txt", "*.TXT"),
            new TextExporter());

    private static final Logger LOGGER = Logger.getLogger(ExportType.class.getSimpleName());
    private FileChooser.ExtensionFilter extensionFilter;
    private AbstractExporter exporter;

    ExportType(FileChooser.ExtensionFilter extensionFilter, AbstractExporter exporter) {
        this.extensionFilter = extensionFilter;
        this.exporter = exporter;
    }

    public FileChooser.ExtensionFilter getExtensionFilter() {
        return extensionFilter;
    }

    public AbstractExporter getExporter() {
        return exporter;
    }

    public static List<FileChooser.ExtensionFilter> getExtensionFilterList() {
        return Arrays.stream(ExportType.values())
                .map(ExportType::getExtensionFilter)
                .collect(Collectors.toList());
    }

    public static ExportType getExportTypeFromExtensionFilter(FileChooser.ExtensionFilter filter) {
        for (ExportType type : values()) {
            if (filter.equals(type.getExtensionFilter())) {
                return type;
            }
        }
        LOGGER.warning("No ExportType found possessing the given filter. Defaulting to TextExporter");
        return ExportType.TextFile;
    }

    public static AbstractExporter getExporterFromExtensionFilter(FileChooser.ExtensionFilter filter) {
        return getExportTypeFromExtensionFilter(filter).getExporter();
    }
}
