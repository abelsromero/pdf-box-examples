package org.abelsromero.pdfbox.api.streams;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.naming.OperationNotSupportedException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class PagesCollector implements Collector<PDPage, PDDocument, PDDocument> {

    @Override
    public Supplier<PDDocument> supplier() {
        return PDDocument::new;
    }

    @Override
    public BiConsumer<PDDocument, PDPage> accumulator() {
        return (doc, page) -> doc.addPage(page);
    }

    @Override
    public BinaryOperator<PDDocument> combiner() {
        // parallel streams not supported yet
        return (d1, d2) -> {
            throw new RuntimeException(new OperationNotSupportedException());
        };
    }

    @Override
    public Function<PDDocument, PDDocument> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Collector.Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
    }

}
