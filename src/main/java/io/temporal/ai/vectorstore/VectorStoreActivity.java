package io.temporal.ai.vectorstore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;

@ActivityInterface
public interface VectorStoreActivity {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record SearchRequestInput(
            @JsonProperty("query") String query,
            @JsonProperty("top_k") Integer topK,
            @JsonProperty("similarity_threshold") Double similarityThreshold,
            @JsonProperty("filter") String filter
    ){}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record SearchRequestOutput(
            @JsonProperty("documents") List<ResultDocument> documents
    ){}

    record ResultDocument(
            @JsonProperty("id") String id,
            @JsonProperty("text") String text,
            @JsonProperty("media") Media media,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("score") Double score
    ){}

    @ActivityMethod
    SearchRequestOutput similaritySearch(SearchRequestInput request);
}
