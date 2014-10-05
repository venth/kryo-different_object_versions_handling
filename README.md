kryo different object versions handling
=======================================

The goal is to check, how kryo handles deserialization to a different version of class of an object.

The test proves that Kryo handles the deserialization to a new version of the same class, if new fields are 
marked as Optional
