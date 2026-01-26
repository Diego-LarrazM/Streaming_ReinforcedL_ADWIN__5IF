package if5.research.core.flink.Processors;

import org.apache.flink.api.common.functions.MapFunction;

public class EventMapper<O> implements MapFunction<String, O> {

    private final Class<O> eventClass;

    public EventMapper(Class<O> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public O map(String value) throws Exception {
        return eventClass
                .getConstructor(String.class)
                .newInstance(value);
    }

    public Class<O> getEventClass() {
        return this.eventClass;
    }
}