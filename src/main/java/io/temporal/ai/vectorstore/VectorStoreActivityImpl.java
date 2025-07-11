package io.temporal.ai.vectorstore;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@ConditionalOnBean(VectorStore.class)
public class VectorStoreActivityImpl implements VectorStoreActivity {
    VectorStore vectorStore;

    VectorStoreActivityImpl(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    @Override
    public VectorStoreActivity.SearchRequestOutput similaritySearch(VectorStoreActivity.SearchRequestInput request) {
        var documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(request.query())
                        .topK(request.topK())
                .similarityThreshold(request.similarityThreshold())
                        .query(request.query())
                .build()).stream().map(
                        document -> new ResultDocument(
                                document.getId(),
                                document.getText(),
                                document.getMedia(),
                                document.getMetadata(),
                                null // Assuming score is not available in Document, set to null
                        )
        ).collect(Collectors.toList());
        return new VectorStoreActivity.SearchRequestOutput(documents);
    }
}
