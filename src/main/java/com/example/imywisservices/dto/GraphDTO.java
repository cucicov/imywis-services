package com.example.imywisservices.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDTO {
    private List<NodeDTO> nodes;

    @JsonCreator
    public static GraphDTO fromArray(List<NodeDTO> nodes) {
        return new GraphDTO(nodes);
    }

    @JsonValue
    public List<NodeDTO> toJson() {
        return nodes;
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
