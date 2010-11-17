package com.imjasonh.partychapp;

/**
 * Abstract base class for {@link Datastore}s that wrap other datastores.
 * 
 * @author mihai.parparita@gmail.com (Mihai Parparita)
 */
public abstract class WrappingDatastore extends Datastore {
  protected final Datastore wrapped;

  protected WrappingDatastore(Datastore wrapped) {
    this.wrapped = wrapped;
  }

  /**
   * Given a {@link Datastore} that may be wrapping others, unwraps them until
   * it finds one of type T, or returns null.
   */
  public static <T extends Datastore> T findWrappedInstance(
      Datastore datastore, Class<T> cls) {
    while (true) {
      if (cls.isInstance(datastore)) {
        return cls.cast(datastore);
      }

      if (!(datastore instanceof WrappingDatastore)) {
        return null;
      }

      datastore = ((WrappingDatastore) datastore).wrapped;
    }
  }
}
