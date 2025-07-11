package io.temporal.ai.workflows;

import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ChatWorkflow {

    @WorkflowMethod
    String startChat(String input);

    @UpdateMethod
    String ask(String input);
}
