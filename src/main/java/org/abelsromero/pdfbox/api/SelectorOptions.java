package org.abelsromero.pdfbox.api;

public class SelectorOptions {

    private boolean copyMetadata = false;
    private boolean copyIntents = false;

    /**
     * Preset options to be used with PDF/A documents.
     */
    public static final SelectorOptions COPY_PDFA_PROPERTIES = SelectorOptions.with().copyMetadata().copyIntents();

    /**
     * Create a new PDF without importing any information from the original.
     */
    public static final SelectorOptions CREATE_NEW = SelectorOptions.with();

    private SelectorOptions() {
    }

    public static SelectorOptions with() {
        return new SelectorOptions();
    }

    public SelectorOptions copyMetadata() {
        this.copyMetadata = true;
        return this;
    }

    public SelectorOptions copyIntents() {
        this.copyIntents = true;
        return this;
    }

    public boolean isCopyMetadataEnabled() {
        return copyMetadata;
    }

    public boolean isCopyIntentsEnabled() {
        return copyIntents;
    }
}
