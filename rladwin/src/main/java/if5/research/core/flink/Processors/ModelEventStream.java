package if5.research.core.flink.Processors;


import java.util.Stack;

public class ModelEventStream {
  private Stack<ElectricalEvent> events;

  public ModelEventStream(){
    this.events = new Stack<>();
  }

  public void add(ElectricalEvent event) {
    events.push(event);          // add newest
  }

  public void conserve_first(int W_new_size){ // n = Size(W_new)
    Stack<ElectricalEvent> temp = new Stack<>(); //ElectricalEvent

    for (int i = 0; i < W_new_size && !events.isEmpty(); i++) {
        temp.push(events.pop());
    }

    events.clear();

    while (!temp.isEmpty()) {
        events.push(temp.pop());
    }
  }

  public Stack<ElectricalEvent> getEvents() {
        return events;
  }
  
}