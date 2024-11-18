package us.byeol.voya.api;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A class which is able to be serialized into a MongoDB format [JSON].
 * <br>
 * These classes should also contain a static deserialize method with a Map parameter.
 * See {@link User#deserialize(Map)} for an example.
 */
public abstract class MongoMappable {

    private final Function<MongoMappable, Boolean> fetchFunction;
    private final Consumer<MongoMappable> pushConsumer;

    /**
     * Creates a new Mappable class with the given fetch and push methods.<br>
     * You can cast the MongoMappable to your child class.
     *
     * @param fetchFunction the fetch function.
     * @param pushConsumer the push consumer.
     */
    protected MongoMappable(Function<MongoMappable, Boolean> fetchFunction,
                            Consumer<MongoMappable> pushConsumer) {
        this.fetchFunction = fetchFunction;
        this.pushConsumer = pushConsumer;
    }

    protected boolean valid;

    /**
     * Whether or not this object exists/can be loaded again [may have malformed data].
     *
     * @return true if valid.
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * Updates this mappable with changes that are in the database.
     * <br>
     * Should be called before any change is made to a mappable.
     */
    public void fetchUpdates() {
        this.valid = this.fetchFunction.apply(this);
    }

    /**
     * Pushes changes made to the database.
     * <br>
     * Should be called every time a change is made to a mappable.
     */
    public void pushChanges() {
        this.pushConsumer.accept(this);
    }

    /**
     * Used to update the superclass.
     * Please be careful using this method, it can cause breaking behaviours if keys are incorrectly placed.
     *
     * @param map the map.
     */
    protected abstract boolean mapResponse(Map<String, Object> map);

    /**
     * Serializes the object for database insertion.
     * <br>
     * Please remember to add the password field when the request is being executed if
     * the mappable is a {@link User}, to prevent any security issues.
     *
     * @return the serialized object.
     */
    public abstract Map<String, Object> serialize();

}
