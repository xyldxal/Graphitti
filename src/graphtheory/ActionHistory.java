package graphtheory;

import java.util.Stack;

public class ActionHistory {
    private static class Action {
        Runnable undo;
        Runnable redo;
        Action(Runnable undo, Runnable redo) {
            this.undo = undo;
            this.redo = redo;
        }
    }

    private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();

    // Record an action with its undo and redo logic
    public void record(Runnable undoAction, Runnable redoAction) {
        undoStack.push(new Action(undoAction, redoAction));
        redoStack.clear(); // clear redo history after new action
        redoAction.run();  // perform the action immediately
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undo.run();
            redoStack.push(action);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo.run();
            undoStack.push(action);
        }
    }
}
