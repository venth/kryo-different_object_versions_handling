kryo different object versions handling
=======================================

The goal is to check, how kryo and java handles deserialization to a different version of class of an object.
I've checked java serialization to see how much java serialization is tolerant.

## Kryo Scenarios

1. object deserialized to new version of class sets new fields to null
2. deserialization to new class version sets old fields
3. deserialization object of new class to the object of old class sets only old fields

The scenarios are supported by: KryoSerializationObjectWithDifferentVersionsTest
 
## Java Scenarios

1. object deserialized to new version of class sets new fields to null
2. deserialization to new class version sets old fields
3. deserialization to class version with missing fields sets only existing fields
4. cannot deserialize to class version with different field type
5. deserialization object of new class to the object of old class sets only old fields
6. deserialization object of class with additional field to the object of the old class with different field sequence sets only old fields

The scenarios are supported by: JavaSerializationTest 

## Model classes

* Base model (first version)

```java
    
    public class VersionedModel implements Serializable {
        private static final long serialVersionUID = 8492539216375372328L;
        public String firstStringField;
        public int firstPrimitiveField;
    }
    
```

* Model with additional fields (there are kryo annotations there, not relevant in java scenarios)

```java

    public class VersionedModel implements Serializable {
    
        private static final long serialVersionUID = 8492539216375372328L;
    
        public String firstStringField;
        public int firstPrimitiveField;
    
        @FieldSerializer.Optional("secondStringField")
        public String secondStringField;
        @FieldSerializer.Optional("secondPrimitiveField")
        public int secondPrimitiveField;
    }

```

* Model with a missing field firstStringField

```java

    public class VersionedModel implements Serializable {
        private static final long serialVersionUID = 8492539216375372328L;
        public int firstPrimitiveField;
    }

```

* The model has firstPrimitiveField type changed to float (this shall have a different serail version uuid, but I kept the same by purpose)

```java

    public class VersionedModel implements Serializable {
        private static final long serialVersionUID = 8492539216375372328L;
    
        public String firstStringField;
        public float firstPrimitiveField;
    }

```

* The model with switched field sequence int field is at the begging now

```java

    public class VersionedModel implements Serializable {
        private static final long serialVersionUID = 8492539216375372328L;
        public int firstPrimitiveField;
        public String firstStringField;
    }
    
```

## Tests launch

Tests can be launched by: mvn clean package

The libraries containing model class must be created. They are copied by
maven dependency plugin to the test classpath. Thanks to their location, they're
loaded by tests and used to instantiate multiple different model classes at the same time.

## Conclusions
* Kryo serialization is tolerant and needs fields to be additionally annotated, not metioning kryo class registry
* Java serialization is tolerant, if the serial version uuid number is managed manually
    * the number has to be changed only, if any of serialized fields is changed its type or
     the serial version id may be left untouched, if there will be a special implementation
     of deserialization. Either way, this situation must be somehow solved
    * other cases:
        * missing fields,
        * additional fields,
        * different fields sequence isn't relevant from serialization / deserialization point of view, but
        it's important during service handling
        