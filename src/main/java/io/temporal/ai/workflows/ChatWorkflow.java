package io.temporal.ai.workflows;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.UpdateMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
/**
 * Workflow that represents a durable AI chat agent
 */
@WorkflowInterface
public interface ChatWorkflow {

    /**
     * Start a chat workflow with the given prompt.
     * */
    @WorkflowMethod
    String startChat(String prompt);

    /**
     * Ask a question in the chat workflow.
     * */
    @UpdateMethod
    String ask(String message);

    @SignalMethod
    void endChat();
}
