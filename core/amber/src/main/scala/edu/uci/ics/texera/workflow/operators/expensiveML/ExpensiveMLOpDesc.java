package edu.uci.ics.texera.workflow.operators.expensiveML;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Preconditions;
import edu.uci.ics.texera.workflow.common.metadata.InputPort;
import edu.uci.ics.texera.workflow.common.metadata.OperatorGroupConstants;
import edu.uci.ics.texera.workflow.common.metadata.OperatorInfo;
import edu.uci.ics.texera.workflow.common.metadata.OutputPort;
import edu.uci.ics.texera.workflow.common.metadata.annotations.AutofillAttributeName;
import edu.uci.ics.texera.workflow.common.operators.OneToOneOpExecConfig;
import edu.uci.ics.texera.workflow.common.operators.map.MapOpDesc;
import edu.uci.ics.texera.workflow.common.tuple.schema.AttributeType;
import edu.uci.ics.texera.workflow.common.tuple.schema.OperatorSchemaInfo;
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema;

import static java.util.Collections.singletonList;
import static scala.collection.JavaConverters.asScalaBuffer;

public class ExpensiveMLOpDesc extends MapOpDesc {

    @JsonProperty(value = "attribute", required = true)
    @JsonPropertyDescription("column to perform analysis on")
    @AutofillAttributeName
    public String attribute;

    @JsonProperty(value = "result attribute", required = true, defaultValue = "mlOutput")
    @JsonPropertyDescription("column name of the analysis result")
    public String resultAttribute;

    @Override
    public OneToOneOpExecConfig operatorExecutor(OperatorSchemaInfo operatorSchemaInfo) {
        if (attribute == null) {
            throw new RuntimeException("sentiment analysis: attribute is null");
        }
        return new OneToOneOpExecConfig(operatorIdentifier(), worker -> new ExpensiveMLOpExec(this, operatorSchemaInfo));
    }

    @Override
    public OperatorInfo operatorInfo() {
        return new OperatorInfo(
                "Expensive ML Analysis",
                "analysis using machine learning",
                OperatorGroupConstants.ANALYTICS_GROUP(),
                asScalaBuffer(singletonList(new InputPort("", false))).toList(),
                asScalaBuffer(singletonList(new OutputPort(""))).toList());
    }

    @Override
    public Schema getOutputSchema(Schema[] schemas) {
        Preconditions.checkArgument(schemas.length == 1);
        if (resultAttribute == null || resultAttribute.trim().isEmpty()) {
            return null;
        }
        return Schema.newBuilder().add(schemas[0]).add(resultAttribute, AttributeType.DOUBLE).build();
    }
}
