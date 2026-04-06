package com.example.imywisservices.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphDTO {
    @JsonAlias("userid")
    private String userId;

    private String userHandle;

    @JsonAlias("pagesData")
    private List<NodeDTO> nodes;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GraphDTO fromJson(Object root) {
        ObjectMapper mapper = new ObjectMapper();
        if (root == null) {
            return new GraphDTO(null, null, Collections.emptyList());
        }

        if (root instanceof List<?>) {
            List<NodeDTO> nodes = mapper.convertValue(root, new TypeReference<List<NodeDTO>>() {});
            return new GraphDTO(null, null, nodes);
        }

        if (root instanceof Map<?, ?> rawMap) {
            String userId = null;
            String userHandle = null;
            if (rawMap.containsKey("userid")) {
                userId = mapper.convertValue(rawMap.get("userid"), String.class);
            } else if (rawMap.containsKey("userId")) {
                userId = mapper.convertValue(rawMap.get("userId"), String.class);
            }
            if (rawMap.containsKey("userHandle")) {
                userHandle = mapper.convertValue(rawMap.get("userHandle"), String.class);
            }

            Object nodesRaw = rawMap.containsKey("pagesData") ? rawMap.get("pagesData") : rawMap.get("nodes");
            List<NodeDTO> nodes = nodesRaw == null
                    ? Collections.emptyList()
                    : mapper.convertValue(nodesRaw, new TypeReference<List<NodeDTO>>() {});

            return new GraphDTO(userId, userHandle, nodes);
        }

        return new GraphDTO(null, null, Collections.emptyList());
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Error generating string representation: " + e.getMessage();
        }
    }
}
