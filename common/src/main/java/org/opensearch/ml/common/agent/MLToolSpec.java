/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.ml.common.agent;

import static org.opensearch.core.xcontent.XContentParserUtils.ensureExpectedToken;
import static org.opensearch.ml.common.utils.StringUtils.getParameterMap;

import java.io.IOException;
import java.util.Map;

import org.opensearch.Version;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.ToXContentObject;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.ml.common.CommonValue;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class MLToolSpec implements ToXContentObject {
    public static final Version MINIMAL_SUPPORTED_VERSION_FOR_TOOL_CONFIG = CommonValue.VERSION_2_18_0;

    public static final String TOOL_TYPE_FIELD = "type";
    public static final String TOOL_NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String PARAMETERS_FIELD = "parameters";
    public static final String INCLUDE_OUTPUT_IN_AGENT_RESPONSE = "include_output_in_agent_response";
    public static final String CONFIG_FIELD = "config";

    private String type;
    private String name;
    private String description;
    private Map<String, String> parameters;
    private boolean includeOutputInAgentResponse;
    private Map<String, String> configMap;

    @Builder(toBuilder = true)
    public MLToolSpec(
        String type,
        String name,
        String description,
        Map<String, String> parameters,
        boolean includeOutputInAgentResponse,
        Map<String, String> configMap
    ) {
        if (type == null) {
            throw new IllegalArgumentException("tool type is null");
        }
        this.type = type;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.includeOutputInAgentResponse = includeOutputInAgentResponse;
        this.configMap = configMap;
    }

    public MLToolSpec(StreamInput input) throws IOException {
        type = input.readString();
        name = input.readOptionalString();
        description = input.readOptionalString();
        if (input.readBoolean()) {
            parameters = input.readMap(StreamInput::readString, StreamInput::readOptionalString);
        }
        includeOutputInAgentResponse = input.readBoolean();
        if (input.getVersion().onOrAfter(MINIMAL_SUPPORTED_VERSION_FOR_TOOL_CONFIG) && input.readBoolean()) {
            configMap = input.readMap(StreamInput::readString, StreamInput::readOptionalString);
        }
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(type);
        out.writeOptionalString(name);
        out.writeOptionalString(description);
        if (parameters != null && parameters.size() > 0) {
            out.writeBoolean(true);
            out.writeMap(parameters, StreamOutput::writeString, StreamOutput::writeOptionalString);
        } else {
            out.writeBoolean(false);
        }
        out.writeBoolean(includeOutputInAgentResponse);
        if (out.getVersion().onOrAfter(MINIMAL_SUPPORTED_VERSION_FOR_TOOL_CONFIG)) {
            if (configMap != null) {
                out.writeBoolean(true);
                out.writeMap(configMap, StreamOutput::writeString, StreamOutput::writeOptionalString);
            } else {
                out.writeBoolean(false);
            }
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (type != null) {
            builder.field(TOOL_TYPE_FIELD, type);
        }
        if (name != null) {
            builder.field(TOOL_NAME_FIELD, name);
        }
        if (description != null) {
            builder.field(DESCRIPTION_FIELD, description);
        }
        if (parameters != null && parameters.size() > 0) {
            builder.field(PARAMETERS_FIELD, parameters);
        }
        builder.field(INCLUDE_OUTPUT_IN_AGENT_RESPONSE, includeOutputInAgentResponse);
        if (configMap != null && !configMap.isEmpty()) {
            builder.field(CONFIG_FIELD, configMap);
        }
        builder.endObject();
        return builder;
    }

    public static MLToolSpec parse(XContentParser parser) throws IOException {
        String type = null;
        String name = null;
        String description = null;
        Map<String, String> parameters = null;
        boolean includeOutputInAgentResponse = false;
        Map<String, String> configMap = null;

        ensureExpectedToken(XContentParser.Token.START_OBJECT, parser.currentToken(), parser);
        while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();

            switch (fieldName) {
                case TOOL_TYPE_FIELD:
                    type = parser.text();
                    break;
                case TOOL_NAME_FIELD:
                    name = parser.text();
                    break;
                case DESCRIPTION_FIELD:
                    description = parser.text();
                    break;
                case PARAMETERS_FIELD:
                    parameters = getParameterMap(parser.map());
                    break;
                case INCLUDE_OUTPUT_IN_AGENT_RESPONSE:
                    includeOutputInAgentResponse = parser.booleanValue();
                    break;
                case CONFIG_FIELD:
                    configMap = getParameterMap(parser.map());
                    break;
                default:
                    parser.skipChildren();
                    break;
            }
        }
        return MLToolSpec
            .builder()
            .type(type)
            .name(name)
            .description(description)
            .parameters(parameters)
            .includeOutputInAgentResponse(includeOutputInAgentResponse)
            .configMap(configMap)
            .build();
    }

    public static MLToolSpec fromStream(StreamInput in) throws IOException {
        MLToolSpec toolSpec = new MLToolSpec(in);
        return toolSpec;
    }
}
