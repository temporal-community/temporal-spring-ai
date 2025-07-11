package io.temporal.ai.vectorstore;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.stream.Collectors;

public class ActivityVectorStore implements VectorStore {
    private final VectorStoreActivity activity;

    public ActivityVectorStore(VectorStoreActivity activity) {
        this.activity = activity;
    }

    @Override
    public void add(List<Document> documents) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void delete(List<String> idList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        var result = activity.similaritySearch(new VectorStoreActivity.SearchRequestInput(
                request.getQuery(),
                request.getTopK(),
                request.getSimilarityThreshold(),
                request.getFilterExpression() != null ? String.valueOf(request.getFilterExpression()) : ""
        ));
        return result.documents().stream().map(
                doc -> Document.builder().
                        id(doc.id())
                        .text(doc.text())
                        .metadata(doc.metadata())
                        .build()).collect(Collectors.toList());
    }
}
